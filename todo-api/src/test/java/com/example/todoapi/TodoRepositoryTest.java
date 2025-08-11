package com.example.todoapi;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;

import com.example.todoapi.repository.TodoRepository;
import com.example.todoapi.entity.Todo; 

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class TodoRepositoryTest {

    @Autowired
    TodoRepository repo;   // H2 組み込み DB が自動設定されます

    @Test
    void findByTitleContaining_andPagealeSortByTitle_shouldReturnMatching() {
        // Arrange: テスト用データを保存
        repo.save(new Todo(null, "あーい", false, null));
        repo.save(new Todo(null, "あいあい", false, null));
        repo.save(new Todo(null, "あいうえお", false, null));
        // ページ番号やページサイズ、ソート順などをまとめて表すインターフェース
        Pageable pageable = PageRequest.of(0, 1, Sort.by("title").ascending());
        // Act：検索キーワード「あい」を含むものを title 昇順ソート、1 件目だけ取得
        Page<Todo> page = repo.findByTitleContaining("あい", pageable);
        // Assert: 正しく 1 件返ってくるか
        assertThat(page.getTotalElements()).isEqualTo(2);  // マッチは2件
        assertThat(page.getNumber()).isZero();                      // 0ページ目
        assertThat(page.getSize()).isEqualTo(1);           // ページサイズ1
        assertThat(page.getContent())
          .hasSize(1)
          .first()
          .extracting(Todo::getTitle)
          .isEqualTo("あいあい");
    }

    @Test
    void findByDoneContaining_andPagealeSortByDueDate_shouldReturnMatching() {
        // done=true、dueDate DESC の組み合わせ
        // Arrange: テスト用データを保存
        repo.save(new Todo(null, "A", true, LocalDateTime.of(2025, 1, 1, 0, 0)));
        repo.save(new Todo(null, "B", true, LocalDateTime.of(2024, 1, 1, 0, 0)));
        repo.save(new Todo(null, "C", false, null));
        // 1ページ目（先頭から2件）を、dueDate降順・nullsLastで取得する
        Pageable pageable = PageRequest.of(0, 2, Sort.by(Sort.Order.desc("dueDate")));
        // Act: 「true」を含むものを検索
        Page<Todo> page = repo.findByDone(true, pageable);
        // Assert
        assertThat(page.getTotalElements()).isEqualTo(2);  // マッチは2件
        assertThat(page.getContent())
          .extracting(Todo::getDueDate)
          .containsExactly(
            LocalDateTime.of(2025, 1, 1, 0, 0),
            LocalDateTime.of(2024, 1, 1, 0, 0)
          );
    }

    @Test
    void findByTitleAndDoneContaining_andPagealeSortByDueDate_shouldReturnMatching() {
        // title=foo ＆ done=false、dueDate ASC の組み合わせ
        // Arrange: テスト用データを保存
        repo.save(new Todo(null, "foo", false, LocalDateTime.of(2025, 5, 5, 5, 5)));
        repo.save(new Todo(null, "foo", false, null));
        repo.save(new Todo(null, "foo", true,  LocalDateTime.of(2024, 4, 4, 4, 4)));
        // 1ページ目（先頭から2件）を、dueDate昇順で取得する
        Pageable pageable = PageRequest.of(0, 2, Sort.by("dueDate").ascending());
        // Act
        Page<Todo> page = repo.findByTitleContainingAndDone("foo", false, pageable);
        // Assert
        assertThat(page.getContent())
          .hasSize(2)
          .extracting(Todo::getDueDate)
          .containsExactly(
            null,  // デフォルトではnullが最小値扱い
            LocalDateTime.of(2025, 5, 5, 5, 5)
          );
    }

    @Test
    void pagination_boundary_shouldReturnEmptyPage() {
        // Arrange: データは全部で3件
        for (int i = 1; i <= 3; i++) {
            repo.save(new Todo(null, "t" + i, false, null));
        }
        // 1ページあたり2件、2ページ目(0起算)を取得すると、中身は1件
        Pageable p0 = PageRequest.of(0, 2);
        Pageable p1 = PageRequest.of(1, 2);
        Pageable p2 = PageRequest.of(2, 2);
        // Act & Assert
        assertThat(repo.findAll(p0).getContent()).hasSize(2);
        assertThat(repo.findAll(p1).getContent()).hasSize(1);
        assertThat(repo.findAll(p2).getContent()).isEmpty();
    }
}