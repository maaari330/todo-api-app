import axios from 'axios';

const envBase =
  (typeof process !== 'undefined' && process.env?.REACT_APP_API_BASE) ||
  '/api'; // ← Docker/Nginx 本番では /api

const api = axios.create({
  baseURL: envBase, // すべてのHTTPリクエストで使われる「ベースとなる URL」
  headers: { 'Content-Type': 'application/json', },
});

export default api;