-- Users
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

/* 
  ENGINE: ストレージエンジン、テーブルの“内部実装”
    MySQLがテーブルを実際にどう保存・更新・ロック・復旧するかを実装
       CHARSET: バイト列 ↔ 文字の対応表
         utf8mb4: 真のUTF-8
　　   COLLATE: 文字セットの既定の照合順序（比較・ソート規則）
  　　   utf8mb4_unicode_ci: Unicode規則に基づく比較・整列、ci = case-insensitive（大文字小文字を区別しない）
*/

-- Category
CREATE TABLE category (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Tag
CREATE TABLE tag (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Todos（repeat_type は V2 で追加するのでここでは入れない）
CREATE TABLE todos (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  title VARCHAR(255) NOT NULL,
  done TINYINT(1) NOT NULL,
  due_date DATETIME(6),
  remind_offset_minutes INT NULL, 
  notified_at DATETIME(6) NULL, 
  repeat_type VARCHAR(50) NOT NULL DEFAULT 'NONE',
  category_id BIGINT,
  user_id BIGINT NOT NULL,
  CONSTRAINT fk_todos_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_todos_category FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
/* CONSTRAINT: 制約名、テーブルのルール
   FOREIGN KEY (user_id) REFERENCES users(id)
   todos.user_id は必ずusers.idに存在する値だとDBが強制
*/

-- 中間テーブル todo_tag
CREATE TABLE todo_tag (
  todo_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (todo_id, tag_id),
  CONSTRAINT fk_todotag_todo FOREIGN KEY (todo_id) REFERENCES todos(id),
  CONSTRAINT fk_todotag_tag  FOREIGN KEY (tag_id)  REFERENCES tag(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Push 購読（Web Push）
CREATE TABLE push_subscriptions (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  owner_id BIGINT NULL,
  endpoint VARCHAR(512) NOT NULL,
  p256dh  VARCHAR(255) NOT NULL,
  auth    VARCHAR(255) NOT NULL,
  user_agent VARCHAR(255),
  created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  CONSTRAINT uq_push_endpoint UNIQUE (endpoint),
  CONSTRAINT fk_push_subscription_owner
    FOREIGN KEY (owner_id) REFERENCES users(id)
    ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- インデックス（検索用）
CREATE INDEX idx_push_owner ON push_subscriptions(owner_id);