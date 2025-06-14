package com.example.todoapi;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.*;
import jakarta.validation.Valid;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

@Tag(name = "TODO API", description = "TODO リソースの操作")
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/todos")
public class TodoController {

    private final TodoService service;

    public TodoController(TodoService service) {
        this.service = service;
    }

    @Operation(summary = "TODO 新規登録", description = "新しいTODOアイテムを登録します")
    @ApiResponse(responseCode = "201", description = "作成成功",
        content = @Content(schema = @Schema(implementation = Todo.class)))
    @PostMapping
    public Todo create(@RequestBody Todo t) {
        return service.create(t);
    }

    @Operation(summary = "TODO 一覧取得", description = "全ての TODO アイテムを返します。'keyword' クエリパラメータ指定でタイトル部分一致検索を行います。")
    @ApiResponse(responseCode = "200", description = "正常に取得成功",
        content = @Content(array = @ArraySchema(schema = @Schema(implementation = Todo.class))))
    @GetMapping
    public List<Todo> list(@RequestParam(name = "keyword", required = false) String keyword) {
        return service.list(keyword);
    }

    @Operation(summary = "TODO 更新", description = "指定したIDのTODOアイテムを更新します")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Todo.class))),
        @ApiResponse(responseCode = "404", description = "対象が存在しない場合")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Todo> update(
        @PathVariable Long id,
        @RequestBody @Valid UpdateTodoRequest req
    ) {
        Todo updated = service.update(id, req);
        return ResponseEntity.ok(updated);
    }

    @Operation(summary = "TODO 削除", description = "指定した ID の TODO アイテムを削除します")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "削除成功"),
        @ApiResponse(responseCode = "404", description = "対象が存在しない場合")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "完了/未完了切替", description = "指定したIDの完了フラグを反転します")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "更新成功", content = @Content(schema = @Schema(implementation = Todo.class))),
        @ApiResponse(responseCode = "404", description = "対象が存在しない場合")
    })
    @PatchMapping("/{id}")
    public ResponseEntity<Todo> toggleDone(@PathVariable Long id) {
        try {
            Todo updated = service.toggleDone(id);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}