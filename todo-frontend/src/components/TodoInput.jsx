import React, { useState } from 'react';
import axios from 'axios';

export default function TodoInput({ onAdded }) {
  const [title, setTitle] = useState('');
  const [submitting, setSubmitting] = useState(false);

  const handleSubmit = async e => {
    e.preventDefault();
    if (!title.trim()) return;
    setSubmitting(true);
    try {
      // バックエンドに新規タスクを追加するPOSTリクエスト
      await axios.post('/todos', {
        title,
        completed: false
      });
      setTitle('');
      onAdded();  // 呼び出し元（TodoList）に「追加完了したよ」と伝える
    } catch (err) {
      console.error(err);
      alert('追加に失敗しました');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="flex items-center space-x-2 mb-6">
      <input
        type="text"
        value={title}
        onChange={e => setTitle(e.target.value)}
        placeholder="新しいタスクを入力"
        className="flex-1 border rounded px-3 py-2 focus:outline-none"
      />
      <button
        type="submit"
        disabled={submitting}
        className="bg-blue-500 text-white rounded px-3 py-1 hover:bg-blue-600 disabled:opacity-50"
      >
        追加
      </button>
    </form>
  );
}