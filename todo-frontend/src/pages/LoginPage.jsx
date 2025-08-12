import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth'; 

export default function LoginPage() {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const { login } = useAuth();  // ← useAuth から login を取得

  const handleSubmit = async e => {
    e.preventDefault();
    try {
      await login(username, password);
      navigate('/todos', { replace: true });
    } catch (err) {
      setError(
        err.response?.data?.message ||  // サーバー返却メッセージがあればそれを使い
        err.message ||                  // それ以外は標準の Error.message
        'ログインに失敗しました'      // どちらも無ければデフォルト
      );
    }
  };

  return (
    <div className="max-w-sm mx-auto mt-20 p-4 shadow">
      <h1 className="text-2xl mb-4">ログイン</h1>
      {error && <p className="text-red-600 mb-2">{error}</p>}
      <form onSubmit={handleSubmit}>
        <label className="block mb-2">
          ユーザー名
          <input
            className="w-full p-2 border rounded"
            value={username}
            onChange={e => setUsername(e.target.value)}
            required
          />
        </label>
        <label className="block mb-4">
          パスワード
          <input
            type="password"
            className="w-full p-2 border rounded"
            value={password}
            onChange={e => setPassword(e.target.value)}
            required
          />
        </label>
        <button
          type="submit"
          className="w-full py-2 rounded bg-blue-600 text-white"
        >
          ログイン
        </button>
      </form>
      <p className="mt-4 text-center">
       アカウントをお持ちでない方は{' '}
       <Link to="/signup" className="text-blue-600 underline">
         新規登録
       </Link>
     </p>
    </div>
  );
}