import axios from 'axios';

// 環境に応じて切り替えられるように REACT_APP_API_URL を使う例
const api = axios.create({
  baseURL: process.env.REACT_APP_API_URL || 'http://localhost:8080', // すべてのHTTPリクエストで使われる「ベースとなる URL」
  withCredentials: true,   // フロントエンド → バックエンドに Cookie を渡す
  headers: {
    'Content-Type': 'application/json',
  },
});

export default api;