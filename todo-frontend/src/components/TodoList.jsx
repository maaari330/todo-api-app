import React, { useEffect, useState } from 'react';
import axios from 'axios';
import TodoInput from './TodoInput';  

export default function TodoList() {
  const [todos, setTodos] = useState([]);
  const [loading, setLoading] = useState(true);          // ← 追加
  const [error, setError] = useState(null);              // ← 任意でエラー表示も

  console.log('現在の todos:', todos);

  const fetchTodos = () => {
    setLoading(true);
    axios.get('/todos')
      .then(res => {
        setTodos(res.data);
        setError(null);
      })
      .catch(err => {
        console.error(err);
        setError('データ取得に失敗しました');
      })
      .finally(() => setLoading(false));
  };

  useEffect(() => {
    fetchTodos();
  }, []);

  // 完了状態をトグルしてサーバーに PATCH し、state を更新
  const toggleComplete = (id, currentStatus) => {
    console.log('toggling id:', id);
    const newStatus = !currentStatus;
    axios.patch(`/todos/${id}`, { completed: newStatus })
      .then(() => {
        setTodos(prev =>
          prev.map(todo =>
            todo.id === id ? { ...todo, completed: newStatus } : todo
          )
        );
      })
      .catch(err => {
        console.error(err);
        alert('更新に失敗しました');
      });
  };

  // タスクの削除機能
  const deleteTodo = id => {
  axios.delete(`/todos/${id}`)
    .then(() => {
      // 削除後に一覧をリロード
      fetchTodos();
    })
    .catch(err => {
      console.error(err);
      alert('削除に失敗しました');
    });
};

  // 読み込み中画面
  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <span className="text-lg text-gray-600">Loading...</span>
      </div>
    );
  }

  // エラー画面（任意）
  if (error) {
    return (
      <div className="text-center text-red-500 py-6">
        {error}
      </div>
    );
  }

  // データ表示
  return (
    <div className="max-w-md mx-auto">
      <TodoInput onAdded={fetchTodos} />
      {todos.map(todo => (
        <div
          key={todo.id}
          className="flex items-center justify-between bg-white rounded-lg shadow p-4 mb-4"
        >
          <label className="flex items-center">
            <input
              type="checkbox"
              checked={todo.completed}
              readOnly
              className="form-checkbox h-5 w-5 text-blue-500"
            />
            <span
              className={`ml-3 text-base ${
                todo.completed ? 'line-through text-gray-400' : 'text-gray-800'
              }`}
            >
              {todo.title}
            </span>
          </label>

          {/* 完了／未完了切り替えボタン */}
          <button
            onClick={() => toggleComplete(todo.id, todo.completed)}
            className="ml-4 px-2 py-1 text-sm border rounded hover:bg-gray-100"
          >
            {todo.completed ? '未完了に戻す' : '完了にする'}
          </button>

           {/* 削除ボタン */}
          <button
            onClick={() => deleteTodo(todo.id)}
            className="px-2 py-1 text-sm text-red-600 border border-red-600 rounded hover:bg-red-50"
          >
            削除
          </button>

        </div>
      ))}
    </div>
  );
}