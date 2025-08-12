import { createContext, useState, useEffect } from 'react';
import api from '../utils/axiosConfig';
import { authService } from '../services/authService';

/** APIモジュール */

/** グローバルに共有できるReact の「コンテキスト」
 * user: 認証済みユーザー情報
 * loading: 認証チェック中フラグ
 * login: ログイン実行関数
 * logout: ログアウト実行関数
 * signup: 新規ユーザー登録実行関数
*/
export const AuthContext = createContext({ user: null, loading: true, login: async () => {}, logout: () => {}, signup: async () => {} });

// AuthContextの中身を実際に提供
export function AuthProvider({ children }) {
  const [user, setUser] = useState(null); // null または { id, username, roles } のオブジェクト
  const [loading, setLoading] = useState(true); // 最初は true → 認証チェック後に false

  // 1) マウント時にトークンがあればヘッダーにセット → /auth/me を叩いて認証状態を確認
  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) {
      setLoading(false);
      return;
    }
    api.defaults.headers.common.Authorization = `Bearer ${token}`;
    authService.me()
      .then(res => setUser(res.data))
      .catch(() => setUser(null))
      .finally(() => setLoading(false));
  }, []);

  // 2) ログイン関数
  const login = async (username, password) => {
    // レスポンス全体を取得
    const response = await api.post('/auth/login', { username, password });
    // 正しいキー名を使って分解
    const { accessToken , tokenType } = response.data;
    if (!accessToken) {
      throw new Error("ログインレスポンスにaccessTokenが含まれていません");
    }
    // localStorage に保存
    localStorage.setItem('token', accessToken);
    // axios デフォルトヘッダーにセット
    api.defaults.headers.common.Authorization = `${tokenType} ${accessToken}`;
    // /auth/me でユーザー情報を取得
    const me = await await authService.me()
    setUser(me);
    return me;
  };

  // ログアウト関数
  const logout = async () => {
    try {
      // ① サーバ側の /auth/logout を叩く
      await api.post('/auth/logout');
    } catch (e) {
      console.warn('Logout endpoint failed:', e);
    } finally {
      // ② 失敗してもクライアント側のトークン＆ユーザー情報をクリア
      localStorage.removeItem('token');
      delete api.defaults.headers.common.Authorization;
      setUser(null);
      // ③ ログイン画面にリダイレクト
      window.location.href = '/login';
    }
  };

  // 新規登録関数
  const signup = async (username, password) => {
    await authService.signup(username, password);
  };

  // 子コンポーネントがAuthContextでvalueを呼び出せる。propsで渡さない
  return (
    <AuthContext.Provider value={{ user, loading, login, logout, signup }}>
      {children}
    </AuthContext.Provider>
  );
}