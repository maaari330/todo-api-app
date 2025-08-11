-- Users
CREATE TABLE users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

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
  title VARCHAR(255),
  done BIT(1) NOT NULL,
  due_date DATETIME(6),
  category_id BIGINT,
  user_id BIGINT NOT NULL,
  CONSTRAINT fk_todos_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_todos_category FOREIGN KEY (category_id) REFERENCES category(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 中間テーブル todo_tag
CREATE TABLE todo_tag (
  todo_id BIGINT NOT NULL,
  tag_id BIGINT NOT NULL,
  PRIMARY KEY (todo_id, tag_id),
  CONSTRAINT fk_todotag_todo FOREIGN KEY (todo_id) REFERENCES todos(id),
  CONSTRAINT fk_todotag_tag  FOREIGN KEY (tag_id)  REFERENCES tag(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;