package com.example.todoapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.*;

import com.example.todoapi.dto.category.CategoryResponse;
import com.example.todoapi.dto.category.CreateCategoryRequest;
import com.example.todoapi.dto.category.UpdateCategoryRequest;
import com.example.todoapi.service.CategoryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "CATEGORY API", description = "タスクのカテゴリを一覧取得・詳細取得・作成・更新・削除するエンドポイント")
@CrossOrigin(origins = "http://localhost:3000")
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    @Operation(summary = "カテゴリ一覧の取得", description = "すべてのカテゴリを取得します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "カテゴリ一覧取得成功", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Tag.class))))
    })
    public ResponseEntity<List<CategoryResponse>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "カテゴリの取得", description = "ID を指定してカテゴリを取得します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "カテゴリ取得成功", content = @Content(schema = @Schema(implementation = Tag.class))),
            @ApiResponse(responseCode = "404", description = "カテゴリが見つかりません", content = @Content)
    })
    public ResponseEntity<CategoryResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "カテゴリの作成", description = "新しいカテゴリを作成します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "カテゴリ作成成功"),
            @ApiResponse(responseCode = "400", description = "入力エラー")
    })
    public ResponseEntity<CategoryResponse> create(@Valid @RequestBody CreateCategoryRequest req) {
        CategoryResponse res = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res); // ステータス 201 Created を設定する
    }

    @PutMapping("/{id}")
    @Operation(summary = "カテゴリの更新", description = "指定した ID のカテゴリを更新します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "カテゴリ更新成功", content = @Content(schema = @Schema(implementation = Tag.class))),
            @ApiResponse(responseCode = "404", description = "カテゴリが見つかりません", content = @Content),
            @ApiResponse(responseCode = "404", description = "指定 ID のカテゴリが存在しない")
    })
    public ResponseEntity<CategoryResponse> update(@PathVariable Long id,
            @Valid @RequestBody UpdateCategoryRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "カテゴリの削除", description = "指定した ID のカテゴリを削除します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "カテゴリ削除成功", content = @Content),
            @ApiResponse(responseCode = "404", description = "カテゴリが見つかりません", content = @Content),
            @ApiResponse(responseCode = "409", description = "カテゴリに紐づくタスクが残っており削除不可", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}