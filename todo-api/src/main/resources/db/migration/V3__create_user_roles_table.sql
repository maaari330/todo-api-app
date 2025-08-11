CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  roles   VARCHAR(50) NOT NULL,
  CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
);

-- 必要なら複合プライマリキーやインデックスを追加
ALTER TABLE user_roles
  ADD PRIMARY KEY (user_id, roles);

CREATE INDEX idx_user_roles_user_id
  ON user_roles (user_id);