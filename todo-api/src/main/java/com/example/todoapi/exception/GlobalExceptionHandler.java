package com.example.todoapi.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import com.example.todoapi.dto.ErrorResponse;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

// 全コントローラに対して例外をキャッチし、統一した JSON エラー応答を返却
@ControllerAdvice
public class GlobalExceptionHandler {

    /** バリデーションエラー (400) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        // フィールドごとのエラーメッセージを "field: message" の形で連結
        String details = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining("; "));
        // ErrorResponse の組み立て
        ErrorResponse err = new ErrorResponse();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(HttpStatus.BAD_REQUEST.value());
        err.setError(HttpStatus.BAD_REQUEST.getReasonPhrase());
        err.setMessage(details);
        err.setPath(req.getRequestURI());
        // HTTP 400 で返却
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(err);
    }

    /** アクセス権限がない (403) */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest req) {

        ErrorResponse err = new ErrorResponse();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(HttpStatus.FORBIDDEN.value());
        err.setError(HttpStatus.FORBIDDEN.getReasonPhrase());
        err.setMessage("この操作を行う権限がありません");
        err.setPath(req.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(err);
    }

    /** 該当エンティティ未検出 (404) :サービス層・コントローラ層などで「引数不正」「見つからない」を投げた場合, 削除対象が存在しないときなど */
    @ExceptionHandler({ IllegalArgumentException.class, EmptyResultDataAccessException.class,
            UsernameNotFoundException.class, EntityNotFoundException.class })
    public ResponseEntity<ErrorResponse> handleNotFound(
            RuntimeException ex,
            HttpServletRequest req) {

        ErrorResponse err = new ErrorResponse();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(HttpStatus.NOT_FOUND.value());
        err.setError(HttpStatus.NOT_FOUND.getReasonPhrase());
        err.setMessage(ex.getMessage());
        err.setPath(req.getRequestURI());

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(err);
    }

    /** 外部キー制約など「データ整合性違反」時の例外を 409(CONFLICT) に正規化 */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleFkConflict(
            DataIntegrityViolationException ex,
            HttpServletRequest req) {

        ErrorResponse err = new ErrorResponse();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(HttpStatus.CONFLICT.value());
        err.setError(HttpStatus.CONFLICT.getReasonPhrase());
        err.setMessage("参照中のため削除できません"); // 固定文言 or ex.getMostSpecificCause().getMessage()
        err.setPath(req.getRequestURI());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(err);
    }

    /** Service層などで明示的に投げた ResponseStatusException を、そのままのHTTPステータスで返すハンドラ */
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponse> handleResponseStatus(
            ResponseStatusException ex,
            HttpServletRequest req) {
        var status = ex.getStatusCode(); // HttpStatusCode
        ErrorResponse err = new ErrorResponse();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(status.value());
        err.setError(status.toString());
        err.setMessage(ex.getReason());
        err.setPath(req.getRequestURI());

        return ResponseEntity.status(status).body(err);
    }

    /** その他想定外の例外 (500) ※顧客へは汎用メッセージを返却（内部実装の詳細は隠蔽） */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleAll(
            Exception ex,
            HttpServletRequest req) {

        ErrorResponse err = new ErrorResponse();
        err.setTimestamp(LocalDateTime.now());
        err.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        err.setError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        err.setMessage("予期せぬエラーが発生しました");
        err.setPath(req.getRequestURI());

        // ログ出力などの追加処理もここで
        ex.printStackTrace();

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(err);
    }
}
