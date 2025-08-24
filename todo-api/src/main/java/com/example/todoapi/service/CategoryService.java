package com.example.todoapi.service;

import lombok.RequiredArgsConstructor;
import com.example.todoapi.repository.CategoryRepository;
import com.example.todoapi.dto.category.CategoryResponse;
import com.example.todoapi.dto.category.CreateCategoryRequest;
import com.example.todoapi.dto.category.UpdateCategoryRequest;
import com.example.todoapi.entity.Category;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor // final フィールドを引数にとるコンストラクタを自動生成
public class CategoryService {
  private final CategoryRepository repo;

  /** 全件取得 */
  @Transactional
  public List<CategoryResponse> findAll() { 
    return repo.findAll().stream()
      .map(c -> new CategoryResponse(c.getId(), c.getName()))
      .toList(); 
  }

  /** カテゴリID で単一取得（存在しなければ 404） */
  @Transactional
  public CategoryResponse findById(Long id) {
    Category c = repo.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("カテゴリが見つかりません: " + id));
    return new CategoryResponse(c.getId(), c.getName());
  }
  
  /** 新規作成 */
  @Transactional
  public CategoryResponse create(CreateCategoryRequest req) { 
    Category saved = repo.save(new Category(null, req.getName()));
    return new CategoryResponse(saved.getId(), saved.getName()); 
  }

  /** 更新 */
  @Transactional
  public CategoryResponse update(Long id, UpdateCategoryRequest req) {
    Category existing = repo.findById(id)
      .orElseThrow(() -> new IllegalArgumentException("タグが見つかりません: " + id));;
    existing.setName(req.getName());
    Category updated = repo.save(existing);
    return new CategoryResponse(updated.getId(), updated.getName());
  }

  /** 削除 */
  @Transactional
  public void delete(Long id) { 
    if (!repo.existsById(id)) {
      throw new IllegalArgumentException("カテゴリが見つかりません: " + id);
    }
    repo.deleteById(id); 
  }
}