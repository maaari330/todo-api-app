-- タスク：リマインド設定＆送信済みフラグ
ALTER TABLE todos
  ADD COLUMN remind_offset_minutes INT NULL,
  ADD COLUMN notified_at DATETIME NULL; 

-- WebPush購読情報（ユーザーごとに複数端末想定）
CREATE TABLE IF NOT EXISTS push_subscriptions (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  endpoint VARCHAR(1024) NOT NULL,
  p256dh VARCHAR(255) NOT NULL,
  auth VARCHAR(255) NOT NULL,
  created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_endpoint (endpoint)
);
-- （事前に）from クライアント to サーバー
------クライアントの公開鍵(p256dh) 、両者で共有している秘密材料(auth)
-- ★通信発生　
-----サーバ処理 
------　通信ごとに “共有秘密” を導出して鍵を作成し暗号化
------　サーバー公開鍵、秘密鍵 -> 共有秘密 -> authなど -> salt鍵作成　-> 暗号文作成
-----from サーバー to クライアント
------　サーバ公開鍵、salt鍵、暗号文
-----クライアント処理
------　サーバー公開鍵、クライアント秘密鍵 -> 共有秘密 -> authなど -> salt鍵作成
------  上記で暗号文作成に必要な材料が分かる -> 暗号文を作成し、サーバの暗号文との一致確認
