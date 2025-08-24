package com.example.todoapi.service;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;

import com.example.todoapi.dto.tag.CreateTagRequest;
import com.example.todoapi.dto.tag.TagResponse;
import com.example.todoapi.dto.tag.UpdateTagRequest;
import com.example.todoapi.entity.Tag;
import com.example.todoapi.repository.TagRepository;

@Service
@RequiredArgsConstructor // final フィールドを引数にとるコンストラクタを自動生成
public class TagService {
    private final TagRepository repository;

    /** 全件取得 */
    public List<TagResponse> findAll() {
        return repository.findAll().stream()
            .map(t -> new TagResponse(t.getId(), t.getName()))
            .toList();
    }

    /** タグID で取得（存在しなければ 404） */
    public TagResponse findById(Long id) {
        Tag t = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("タグが見つかりません:" + id));
        return new TagResponse(t.getId(), t.getName());
    }

    /** 新規作成 */
    public TagResponse create(CreateTagRequest req) {
        Tag saved = repository.save(new Tag(null, req.getName()));
        return new TagResponse(saved.getId(), saved.getName());
    }

    /** 更新 */
    public TagResponse update(Long id, UpdateTagRequest req) {
        Tag existing = repository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("タグが見つかりません: " + id));
        existing.setName(req.getName());
        Tag updated = repository.save(existing);
        return new TagResponse(updated.getId(), updated.getName());
    }

    /** 削除 */
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new IllegalArgumentException("タグが見つかりません: " + id);
        }
        repository.deleteById(id);
    }
}