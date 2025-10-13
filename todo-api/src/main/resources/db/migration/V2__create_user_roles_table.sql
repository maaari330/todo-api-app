-- user_roles
CREATE TABLE user_roles (
  user_id BIGINT NOT NULL,
  roles   VARCHAR(50) NOT NULL,
  CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id) REFERENCES users(id)
    ON DELETE CASCADE
)ENGINE=InnoDB;


-- 必要なら複合プライマリキーを追加
ALTER TABLE user_roles
  ADD PRIMARY KEY (user_id, roles);
