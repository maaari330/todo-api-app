import React, { useState } from 'react';
import { useCategories } from '../hooks/useCategories';
import { Trash2, Pencil } from "lucide-react";

/** カテゴリの新規作成・更新・削除を行うページ（コンポーネント層） */
export default function CategoryPage() {
  const [newName, setNewName] = useState('');     // 新規作成用入力
  // useCategories から取得 ＆ useCategories が最初の fetch を行い、カテゴリ一覧を取得
  const {
    categories,  // カテゴリ一覧データ
    loading,     // 取得中フラグ
    error,       // 取得エラー
    create,       // (name)    => 新規作成＋一覧再取得
    update,       // (id,name) => 更新＋一覧再取得
    remove        // (id)      => 削除＋一覧再取得
  } = useCategories();

  // ─── 1) 新規作成 ─────────────────────────────
  const handleAdd = async () => {
    if (!newName.trim()) return;
    await create(newName);
    setNewName('');
  };

  // ─── 2) 更新 （prompt で名前を取得）────────────
  const handleUpdate = async (id, oldName) => {
    const name = window.prompt('新しいカテゴリ名を入力', oldName); // ブラウザ上で簡易的な入力ダイアログを表示
    if (name && name.trim() !== oldName) { // 空文字ではなく、入力値がoldNameでないときにupdate
      await update(id, name.trim());
    }
  };

  // ─── 3) 削除 ─────────────────────────────────
  const handleDelete = async (id) => {
    await remove(id);
  };

  if (loading) return <p>読み込み中…</p>;
  if (error) return <p className="text-red-600">エラー: {error.message}</p>;

  return (
    <div className="max-w-lg mx-auto mt-8 p-4 bg-white shadow rounded">
      <h2 className="text-xl font-bold mb-4">カテゴリ管理</h2>
      <ul className="mb-4">
        {categories.map(c => (
          <li key={c.id} className="flex justify-between py-1">
            <span>{c.name}</span>
            <div className="space-x-2">
              <button
                onClick={() => handleUpdate(c.id, c.name)}
                className="inline-flex items-center justify-center rounded p-2 bg-yellow-300 text-white hover:bg-yellow-500 focus:outline-none focus:ring-2 focus:bg-yellow-200 focus:ring-offset-2"
              >
                <Pencil className="h-5 w-5" />
              </button>
              <button
                onClick={() => { handleDelete(c.id); }}
                className="inline-flex items-center justify-center rounded p-2 bg-red-500 text-white hover:bg-red-600 focus:outline-none focus:ring-2 focus:ring-red-400 focus:ring-offset-2"
              >
                <Trash2 className="h-5 w-5" />
              </button>
            </div>
          </li>
        ))}
      </ul>

      {/* 新規追加フォーム */}
      <div className="flex">
        <input
          type="text"
          value={newName}
          onChange={e => setNewName(e.target.value)}
          placeholder="新しいカテゴリ名"
          className="flex-1 p-2 border rounded-l"
        />
        <button
          onClick={handleAdd}
          className="px-4 py-2 bg-blue-600 text-white rounded-r"
          disabled={!newName.trim()}
        >
          追加
        </button>
      </div>
    </div>
  );
}