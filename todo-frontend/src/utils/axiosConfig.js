import axios from 'axios';

const envBase =
  (typeof process !== 'undefined' && process.env?.REACT_APP_API_BASE) ||
  '/api'; // ← Docker/Nginx 本番では /api（env未設定でここに落ちる）

const api = axios.create({
  baseURL: envBase, // すべてのHTTPリクエストで使われる「ベースとなる URL」
  withCredentials: true,   // フロントエンド → バックエンドに Cookie を渡す
  headers: { 'Content-Type': 'application/json', },
});

export default api;