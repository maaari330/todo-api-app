import { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';

/**
 * 認証情報と操作関数を簡単に取り出せるカスタムフック
 * 
 * useContext を使用するには AuthProvider セットで定義＆ラップが必須。
 */
export function useAuth() {
  return useContext(AuthContext);
}