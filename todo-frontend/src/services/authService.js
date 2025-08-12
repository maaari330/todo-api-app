import api from '../utils/axiosConfig';

/**
 * 認証まわりの API 呼び出しをまとめるサービス層
 */
export const authService = {
  /** 現在のユーザー情報取得 */
  me:    async ()           => (await api.get('/auth/me')).data,
  /** ログイン（username, password を送ってトークン取得） */
  login: async (u, p)       => (await api.post('/auth/login', { username: u, password: p })).data,
  /** ログアウト */
  logout: async ()          => api.post('/auth/logout'),
  /** 新規登録 */
  signup: async (u, p)      => (await api.post('/auth/signup', { username: u, password: p })).data,
};