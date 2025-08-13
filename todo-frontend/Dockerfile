# ---------- build stage (Nodeで本番ビルド) ----------
# node: 本番用に最適化された静的ファイルを生成
FROM node:20-alpine AS builder
WORKDIR /app

# 依存関係を先に入れてキャッシュを効かせる
COPY package*.json ./
# lock があれば npm ci、なければ install
RUN if [ -f package-lock.json ]; then npm ci; else npm install; fi

# アプリ本体
COPY . .

# 本番ビルド　CRA: /app/build が生成される　# CRA: 
RUN npm run build


# ---------- runtime stage (Nginxで配信) ----------
# Nginx: 本番向けの高速・軽量なWebサーバ  静的配信＋/api の振り分け（リバースプロキシ）を行う
FROM nginx:1.27-alpine AS runtime
WORKDIR /usr/share/nginx/html

# SPA ルーティング & 圧縮のための nginx 設定
COPY ./nginx.conf /etc/nginx/conf.d/default.conf

# CRAビルド成果物を Nginx のドキュメントルートにコピー
COPY --from=builder /app/build /usr/share/nginx/html/
# このコンテナは 80/tcp を待ち受ける　とドキュメント的宣言（実際の公開は docker-compose の ports: "3000:80" で行う）
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]