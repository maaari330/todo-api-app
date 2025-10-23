import axios from 'axios';

const envBase =
  (typeof process !== 'undefined' && process.env?.REACT_APP_API_URL) ||
  '/api'; // ← Docker/Nginx 本番では /api

const api = axios.create({
  baseURL: envBase, // すべてのHTTPリクエストで使われる「ベースとなる URL」
  headers: { 'Content-Type': 'application/json', },
});

api.interceptors.request.use((config) => {
  // 1) ログイン・新規登録APIにはAuthorizationを付けない
  if (config.url?.endsWith('/auth/login') || config.url?.endsWith('/auth/signup')) return config;

  // 2) 呼び出し側で Authorization:'' を指定したら付けない（尊重）
  if (config.headers?.Authorization === '') return config;

  // 3) それ以外は localStorage のトークンを自動付与
  const t = localStorage.getItem('token');
  if (t) {
    config.headers = config.headers || {};
    // サーバ側が "Bearer <token>" 期待ならここで組み立てる
    config.headers.Authorization = `Bearer ${t}`;
  }
  return config;
});

export default api;