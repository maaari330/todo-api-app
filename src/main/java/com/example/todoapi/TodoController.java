package com.example.todoapi;

import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Parameter;

import java.util.List;

/**
 * /todos エンドポイントを通じて
 * ブラウザやクライアントからの HTTP リクエストを受け取り、
 * TodoRepository を使って DB 操作を行います。
 */
@Tag(name = "TODO API", description = "TODO リソースの操作")
@RestController
@RequestMapping("/todos")
public class TodoController {

    private final TodoRepository repo;

    // Spring が TodoRepository の実装を渡してくれるコンストラクタ
    public TodoController(TodoRepository repo) {
        this.repo = repo;
    }

    /** POST /todos : Todo を新規登録 */
    @Operation(summary = "TODO 新規登録", description = "新しいTODOアイテムを登録します")
    @ApiResponse(
    responseCode = "201",
    description = "作成成功",
    content = @Content(schema = @Schema(implementation = Todo.class))
    )
    @PostMapping
    public Todo create(@RequestBody Todo t) {
        return repo.save(t);
    }



    /** GET /todos : Todo の一覧を取得 */
    @Operation(
        summary = "TODO 一覧取得",
        description = "全ての TODO アイテムを返します。'keyword' クエリパラメータ指定でタイトル部分一致検索を行います。"
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "正常に取得成功",
            content = @Content(
                array = @ArraySchema(
                    schema = @Schema(implementation = Todo.class)
                )
            )
        )
    })
    @GetMapping
    public List<Todo> list(
        @Parameter(description = "タイトルの部分一致検索ワード", required = false)
        @RequestParam(name = "keyword", required = false) String keyword
    ) {
        if (keyword == null || keyword.isBlank()) {
            return repo.findAll();
        }
        // keyword が指定されていれば部分一致検索
        return repo.findByTitleContaining(keyword);
    }



    /** PUT /todos/{id} : 指定 ID の Todo を更新 */
    @Operation(summary = "TODO 更新", description = "指定したIDのTODOアイテムを更新します")
    @ApiResponses({
        @ApiResponse(responseCode = "200",description = "更新成功",content = @Content(schema = @Schema(implementation = Todo.class))),
        @ApiResponse(responseCode = "404", description = "対象が存在しない場合")
    })
    @PutMapping("/{id}")
    public Todo update(@PathVariable Long id, @RequestBody Todo t) {
        t.setId(id);
        return repo.save(t);
    }


    /** DELETE /todos/{id} : 指定 ID の Todo を削除 */
    @Operation(summary = "TODO 削除", description = "指定した ID の TODO アイテムを削除します")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "削除成功"),
        @ApiResponse(responseCode = "404", description = "対象が存在しない場合")
    })
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
