-- タスク：リマインド設定＆送信済みフラグ
ALTER TABLE todos
  ADD COLUMN remind_offset_minutes INT NOT NULL DEFAULT 0,
  ADD COLUMN notified_at DATETIME(6);

-- 抽出クエリ用（notified_at IS NULL → due_date の範囲＆並び）
-- TodoRepository内のfindIdsDueForNotification()用
-- notified_at 昇順(nullは先頭に来る)　→ due_date 昇順で効率よく取り出す
CREATE INDEX idx_todos_notify_scan ON todos (notified_at, due_date);

-- 履歴表示用（user一致 → notified_at の範囲＆並び）
-- TodoRepository内のfindRecentNotifiedByUser()用
CREATE INDEX idx_todos_user_notified ON todos (user_id, notified_at);

CREATE TABLE push_subscriptions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  user_id BIGINT NOT NULL,
  endpoint VARCHAR(500) NOT NULL,
  p256dh  VARCHAR(255) NOT NULL,
  auth    VARCHAR(255) NOT NULL,
  user_agent VARCHAR(255),
  -- 行を INSERT した瞬間のサーバ時刻を自動で入れる
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  -- 一意制約：同じ endpoint は1つだけ
  CONSTRAINT uq_push_endpoint UNIQUE (endpoint),
  -- 外部キー：ユーザー削除時に購読も削除
  CONSTRAINT fk_push_subscription_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
) ENGINE=InnoDB;
