package com.example.todoapi.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import com.example.todoapi.entity.Todo;
import com.example.todoapi.service.TodoService;
import com.example.todoapi.dto.ErrorResponse;
import com.example.todoapi.dto.todo.CreateTodoRequest;
import com.example.todoapi.dto.todo.TodoResponse;
import com.example.todoapi.dto.todo.UpdateTodoRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.net.URI;
import java.util.Set;

// todoエンティティをserviceから受け取ってResponseEntity<Todo>に変換して返却
@io.swagger.v3.oas.annotations.tags.Tag(name = "TODO API", description = "TODO リソースの操作")
@CrossOrigin(origins = "http://localhost:3000")
@RequiredArgsConstructor
@RestController
@RequestMapping("/todos")
public class TodoController {
    
    private final TodoService todoService;

    @Operation(summary = "TODO 新規登録", description = "新しいTODOアイテムを登録します")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "作成成功", content = @Content(schema = @Schema(implementation = Todo.class))),
        @ApiResponse(responseCode = "400", description  = "バリデーションエラー", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "作成用 DTO", required = true, 
        content = @Content(mediaType = "application/json", schema = @Schema(implementation = CreateTodoRequest.class)))
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<TodoResponse> create(@RequestBody @Valid CreateTodoRequest req, @AuthenticationPrincipal UserDetails user) {
        TodoResponse res = todoService.create(req, user);
        return ResponseEntity
                .created(URI.create("/todos/" + res.getId()))        // ← 201 + Location
                .body(res);
    }


    @Operation(summary = "TODO 一覧取得", description = "全ての TODO アイテムを返します。'keyword'クエリパラメータ指定でタイトル部分一致検索を行います。また'done'クエリパラメータ指定で完了状況一致検索を行います。")
    @ApiResponse(responseCode = "200", description = "正常に取得成功", content = @Content(mediaType   = "application/json", array = @ArraySchema(schema = @Schema(implementation = Todo.class))))
    @GetMapping
    public ResponseEntity<Page<TodoResponse>> list(  // 一覧を返却するためPage<todo>を使用
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "done", required = false) Boolean done,
        @RequestParam(required = false) Long category,
        @RequestParam(required = false) Set<Long> tags,
        @Parameter(description = "ページ番号 (0始まり), 件数, ソート順 (e.g. sort=dueDate,desc)") Pageable pageable,
        @AuthenticationPrincipal UserDetails user) {
            /** @AuthenticationPrincipalauthControllerを付けるとSecurityContext の principal が入る
                SecurityContextHolder
                └─ SecurityContext リクエストを処理する間持っている「認証情報」
                    └─ Authentication
                        └─principal    ← これが「誰がログインしているか」を表す */
            Page<TodoResponse> page = todoService.list(keyword, done, category, tags, pageable, user);
            return ResponseEntity.ok(page);
    }


    @Operation(summary = "TODO 更新", description = "指定したIDのTODOアイテムを更新します")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Todo.class))),
        @ApiResponse(responseCode = "404", description = "対象が存在しない場合", content = @Content(mediaType = "application/json", schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<TodoResponse> update(@PathVariable Long id, @RequestBody @Valid UpdateTodoRequest req, @AuthenticationPrincipal UserDetails user) {
        TodoResponse res = todoService.update(id, req, user);
        return ResponseEntity.ok(res);
    }


    @Operation(summary = "TODO 削除", description = "指定した ID の TODO アイテムを削除します")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "削除成功"),
        @ApiResponse(responseCode = "404", description = "対象が存在しない場合")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        todoService.delete(id, user);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "完了/未完了切替", description = "指定したIDの完了フラグを反転します")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Todo.class))),
        @ApiResponse(responseCode = "404", description = "対象が存在しない場合")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<TodoResponse> toggleDone(@PathVariable Long id, @AuthenticationPrincipal UserDetails user) {
        TodoResponse res = todoService.toggleDone(id, user);
        return ResponseEntity.ok(res);
    }



}