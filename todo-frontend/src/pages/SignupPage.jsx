import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

export default function SignupPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { signup } = useAuth();  // ← useAuth から signup を取得

  const handleSubmit = async e => {
    e.preventDefault();
    try {
      await signup(username, password); // 認証処理はコンテキスト層の signup() に任せる
      navigate('/login', { replace: true }); // アカウントを作成しただけではまだ認証（トークン取得）をしていないから、新規登録→ログイン
    } catch (err) {
      setError(
        err.response?.data?.message || // サーバー返却メッセージがあればそれを使い
        err.message || // それ以外は標準の Error.message
        '新規アカウント登録に失敗しました' // どちらも無ければデフォルト
      );
    }
  };

  return (
    <div className="max-w-sm mx-auto mt-20 p-4 shadow">
      <h1 className="text-2xl mb-4">新規登録</h1>
      {error && <p className="text-red-600 mb-2">{error}</p>}
      <form onSubmit={handleSubmit}>
        <label className="block mb-2">
          ユーザー名
          <input className="w-full p-2 border rounded" 
            value={username}
            onChange={e => setUsername(e.target.value)} 
            required />
        </label>
        <label className="block mb-4">
          パスワード
          <input type="password" 
            className="w-full p-2 border rounded"
            value={password} 
            onChange={e => setPassword(e.target.value)}
            title="6文字以上・50文字以内、大文字・小文字・数字をそれぞれ1文字以上含む必要があります"
            required />
          <p className="mt-1 text-sm text-gray-500">
            6～50文字。英大文字・英小文字・数字をそれぞれ1文字以上含めてください。
          </p>
        </label>
        <button type="submit" className="w-full py-2 rounded bg-green-600 text-white">
          登録してログインへ
        </button>
      </form>
      <p className="mt-4 text-center">
        すでにアカウントをお持ちの方は{' '}
        <Link to="/login" className="text-blue-600 underline">
          ログイン
        </Link>
      </p>
    </div>
  );
}