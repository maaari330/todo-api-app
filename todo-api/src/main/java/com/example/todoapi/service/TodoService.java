package com.example.todoapi.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import com.example.todoapi.repository.CategoryRepository;
import com.example.todoapi.repository.TagRepository;
import com.example.todoapi.repository.TodoRepository;
import com.example.todoapi.repository.UserRepository;
import com.example.todoapi.Specification.TodoSpecifications;
import com.example.todoapi.dto.todo.CreateTodoRequest;
import com.example.todoapi.dto.todo.TodoResponse;
import com.example.todoapi.dto.todo.UpdateTodoRequest;
import com.example.todoapi.entity.Category;
import com.example.todoapi.entity.RepeatType;
import com.example.todoapi.entity.Tag;
import com.example.todoapi.entity.Todo;
import com.example.todoapi.entity.User;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

// todoエンティティをTodoControllerに返却
// DTOでjsonデータ検証・変換
@Service
@RequiredArgsConstructor // final フィールドを引数にとるコンストラクタを自動生成
@Transactional(readOnly = true)
public class TodoService {
    private final TodoRepository repo;
    private final UserRepository userRepo;
    private final CategoryRepository categoryRepo;
    private final TagRepository tagRepo;

    /** 新規作成 */
    @Transactional
    public TodoResponse create(CreateTodoRequest req, UserDetails user) {
        User createdUser = userRepo.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));
        Todo todo = new Todo(); // リクエストDTO → Entity マッピング
        todo.setOwner(createdUser);
        todo.setTitle(req.getTitle());
        todo.setDone(req.getDone());
        todo.setDueDate(req.getDueDate());
        if (req.getDueDate() != null && req.getRemindOffsetMinutes() != null && req.getRemindOffsetMinutes() > 0) {
            todo.setRemindOffsetMinutes(req.getRemindOffsetMinutes());
        } else {
            todo.setRemindOffsetMinutes(null);
        }
        todo.setRepeatType(req.getRepeatType());
        if (req.getCategoryId() != null) {
            Category cat = categoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("カテゴリが見つかりません: " + req.getCategoryId()));
            ;
            todo.setCategory(cat);
        }
        if (req.getTagIds() != null) {
            Set<Tag> tags = req.getTagIds().stream()
                    .map(id -> tagRepo.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("タグが見つかりません: " + id)))
                    .collect(Collectors.toSet());
            todo.setTags(tags);
        }
        Todo created = repo.save(todo);
        return TodoResponse.from(created); // Entity → レスポンスDTO
    }

    /** 一覧取得 */
    @Transactional(readOnly = true)
    public Page<TodoResponse> list(String keyword, Boolean done, Long categoryId, Set<Long> tagIds, Pageable pageable,
            UserDetails principal) {
        User current = userRepo.findByUsername(principal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));
        // ベースの SpecificationをTodoSpecificationsから作成
        Specification<Todo> spec = Specification.where(TodoSpecifications.titleContains(keyword))
                .and(TodoSpecifications.doneIs(done))
                .and(TodoSpecifications.categoryIs(categoryId))
                .and(TodoSpecifications.hasTags(tagIds));
        // 一般ユーザーなら owner 制御を追加
        boolean isAdmin = principal.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            spec = spec.and(TodoSpecifications.ownerIs(current.getId()));
        }
        // Page<Todo> を取得
        Page<Todo> page = repo.findAll(spec, pageable);
        // Entity→DTO はここで一括マッピング
        return page.map(TodoResponse::from);
    }

    /** 更新 */
    @Transactional
    public TodoResponse update(Long id, UpdateTodoRequest req, UserDetails user) {
        Todo existing = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Todo が見つかりません: " + id));
        User current = userRepo.findByUsername(user.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));
        if (!existing.getOwner().getId().equals(current.getId())) {
            throw new AccessDeniedException("自分のタスクのみ更新できます");
        }
        existing.setTitle(req.getTitle());
        existing.setDone(req.getDone());
        LocalDateTime beforeDue = existing.getDueDate();
        existing.setDueDate(req.getDueDate());

        Integer beforeOffset = existing.getRemindOffsetMinutes();
        Integer nextOffset = null;
        if (req.getDueDate() != null && req.getRemindOffsetMinutes() != null && req.getRemindOffsetMinutes() > 0) {
            nextOffset = req.getRemindOffsetMinutes();
        }
        existing.setRemindOffsetMinutes(nextOffset); // null にもなり得る

        // 期日またはオフセットが変わったら、再通知できるように打刻をクリア
        boolean dueChanged = (beforeDue == null && existing.getDueDate() != null)
                || (beforeDue != null && !beforeDue.equals(existing.getDueDate()));
        boolean offsetChanged = (beforeOffset == null && nextOffset != null)
                || (beforeOffset != null && !beforeOffset.equals(nextOffset));
        if (dueChanged || offsetChanged) {
            existing.setNotifiedAt(null);
        }

        existing.setRepeatType(req.getRepeatType());

        if (req.getCategoryId() != null) {
            Category cat = categoryRepo.findById(req.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("カテゴリが見つかりません: " + req.getCategoryId()));
            existing.setCategory(cat);
        } else {
            existing.setCategory(null);
        }
        existing.getTags().clear();
        if (req.getTagIds() != null) {
            Set<Tag> tags = req.getTagIds().stream()
                    .map(tagId -> tagRepo.findById(tagId)
                            .orElseThrow(() -> new EntityNotFoundException("タグが見つかりません: " + tagId)))
                    .collect(Collectors.toSet());
            existing.getTags().addAll(tags);
        }
        Todo updated = repo.save(existing);
        return TodoResponse.from(updated); // Entity→DTO
    }

    /** 削除 */
    @Transactional
    public void delete(Long id, UserDetails principal) {
        Todo t = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Todo が見つかりません"));
        User user = userRepo.findByUsername(principal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));
        if (!t.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("自分のタスクのみ削除できます");
        }
        repo.deleteById(id);
    }

    /** 完了フラグの切り替え */
    @Transactional
    public TodoResponse toggleDone(Long id, UserDetails principal) {
        Todo t = repo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Todo が見つかりません"));
        User user = userRepo.findByUsername(principal.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("ユーザーが見つかりません"));
        if (!t.getOwner().getId().equals(user.getId())) {
            throw new AccessDeniedException("自分のタスクのみ操作できます");
        }
        boolean wasDone = t.isDone(); // 切り替え前の状態を保持
        t.setDone(!t.isDone());
        Todo saved = repo.save(t);

        // もともと切り替え前が未完了で、今回の操作で「完了」になり、かつ繰り返し種別が設定されている場合に次回タスクを生成
        if (!wasDone && t.getRepeatType() != null && t.getRepeatType() != RepeatType.NONE) {
            LocalDateTime nextDue = calculateNextDueDate(t.getDueDate(), t.getRepeatType());
            Todo next = new Todo();
            next.setTitle(t.getTitle());
            next.setOwner(t.getOwner());
            next.setRepeatType(t.getRepeatType());
            next.setCategory(t.getCategory());
            next.setTags(new HashSet<>(t.getTags()));
            next.setDone(false);
            next.setDueDate(nextDue);
            next.setRemindOffsetMinutes(t.getRemindOffsetMinutes());
            next.setNotifiedAt(null);
            repo.save(next);
        }
        return TodoResponse.from(saved);
    }

    /** 繰り返しタスクの期日設定（完了フラグの切り替えメソッドで使用） */
    private LocalDateTime calculateNextDueDate(LocalDateTime current, RepeatType type) {
        LocalDateTime base = (current != null) ? current : LocalDateTime.now();
        return switch (type) {
            case DAILY -> base.plusDays(1);
            case WEEKLY -> base.plusWeeks(1);
            case MONTHLY -> base.plusMonths(1);
            default -> base;
        };
    }
}
