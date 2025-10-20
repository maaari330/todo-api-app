import { useLocation, NavLink } from 'react-router-dom';
import React, { useContext } from 'react';
import { AuthContext } from '../context/AuthContext';
import HeaderBell from './HeaderBell';

export default function Header() {
  const { user, logout } = useContext(AuthContext);
  const location = useLocation(); // 現在のパスを取得
  // ログイン・サインアップ画面ではボタンを隠す
  const isAuthPage = ['/login', '/signup'].includes(location.pathname);

  return (
    <header className="bg-white shadow p-4 flex items-center justify-between">
      <h1 className="text-xl font-bold">My TODO Task Manager</h1>
      {/* 右：ログイン済みかつ認証ページであれば表示　*/}
      {user && !isAuthPage && (
        <nav className="flex items-center space-x-4">
          <NavLink to="/todos" className={({ isActive }) =>
            isActive
              ? 'text-blue-600 border-b-2 border-blue-600 pb-1'
              : 'text-gray-600 hover:text-gray-800'
          }>
            TODO一覧
          </NavLink>
          <NavLink to="/calendar" className={({ isActive }) =>
            isActive
              ? 'text-blue-600 border-b-2 border-blue-600 pb-1'
              : 'text-gray-600 hover:text-gray-800'
          }>
            カレンダー
          </NavLink>
          <NavLink to="/categories" className={({ isActive }) =>
            isActive
              ? 'text-blue-600 border-b-2 border-blue-600 pb-1'
              : 'text-gray-600 hover:text-gray-800'
          }>
            カテゴリ管理
          </NavLink>
          <NavLink to="/tags" className={({ isActive }) =>
            isActive
              ? 'text-blue-600 border-b-2 border-blue-600 pb-1'
              : 'text-gray-600 hover:text-gray-800'
          }>
            タグ管理
          </NavLink>
          <NavLink to="/settings" className={({ isActive }) =>
            isActive
              ? 'text-blue-600 border-b-2 border-blue-600 pb-1'
              : 'text-gray-600 hover:text-gray-800'
          }
          >
            設定
          </NavLink>

          {/* 区切り */}
          <span className="border-l h-6 mx-2"></span>

          {/* App内通知ポップアップ表示 */}
          <HeaderBell />

          {/* ログアウトボタン */}
          <button onClick={logout} className="text-sm text-red-500 hover:underline" >
            ログアウト
          </button>
        </nav>
      )}
    </header>
  );
}