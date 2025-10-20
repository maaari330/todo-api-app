package com.example.todoapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.util.*;
import com.example.todoapi.service.TagService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;

import com.example.todoapi.dto.tag.CreateTagRequest;
import com.example.todoapi.dto.tag.TagResponse;
import com.example.todoapi.dto.tag.UpdateTagRequest;
import com.example.todoapi.entity.Tag;

@RestController
@RequestMapping("/tags")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
@io.swagger.v3.oas.annotations.tags.Tag(name = "TAG API", description = "タスクのタグを一覧取得・詳細取得・作成・更新・削除するエンドポイント")
public class TagController {
    private final TagService service;

    @GetMapping
    @Operation(summary = "タグ一覧の取得", description = "すべてのタグを取得します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "タグ一覧取得成功", content = @Content(array = @ArraySchema(schema = @Schema(implementation = Tag.class))))
    })
    public ResponseEntity<List<TagResponse>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    @Operation(summary = "タグの取得", description = "ID を指定してタグを取得します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "タグ取得成功", content = @Content(schema = @Schema(implementation = Tag.class))),
            @ApiResponse(responseCode = "404", description = "タグが見つかりません", content = @Content)
    })
    public ResponseEntity<TagResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "タグの作成", description = "新しいタグを作成します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "タグ作成成功"),
            @ApiResponse(responseCode = "400", description = "入力エラー")
    })
    public ResponseEntity<TagResponse> create(@RequestBody @Valid CreateTagRequest req) {
        TagResponse res = service.create(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(res); // ステータス201を返却
    }

    @PutMapping("/{id}")
    @Operation(summary = "タグの更新", description = "指定した ID のタグを更新します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "タグ更新成功"),
            @ApiResponse(responseCode = "400", description = "入力エラー"),
            @ApiResponse(responseCode = "404", description = "タグが見つかりません")
    })
    public ResponseEntity<TagResponse> update(@PathVariable Long id, @RequestBody @Valid UpdateTagRequest req) {
        return ResponseEntity.ok(service.update(id, req));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "タグの削除", description = "指定した ID のタグを削除します")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "タグ削除成功", content = @Content),
            @ApiResponse(responseCode = "404", description = "タグが見つかりません", content = @Content),
            @ApiResponse(responseCode = "409", description = "タグが付いたタスクが残っており削除不可", content = @Content)
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}