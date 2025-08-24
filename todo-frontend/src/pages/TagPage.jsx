import React, { useState } from 'react';
import { useTags } from '../hooks/useTags';

/** タグの新規作成・更新・削除を行うページ（コンポーネント層） */
export default function TagPage() {
  const [newName, setNewName] = useState('');
  const {
    tags,        // タグ一覧データ
    loading,     // 初回取得中フラグ
    error,       // 初回取得エラー
    create,      // (name)    => 新規作成 + 再取得
    update,      // (id,name) => 更新 + 再取得
    remove       // (id)      => 削除 + 再取得
  } = useTags();

  // ─── 1) 新規作成 ─────────────────────────────
  const handleAdd = async () => {
    if (!newName.trim()) return;
    await create(newName.trim());
    setNewName('');
  };

  // ─── 2) 更新 （prompt で名前を取得）────────────
  const handleUpdate = async (id, oldName) => { 
    const name = window.prompt('新しいタグ名を入力', oldName); // ブラウザ上で簡易的な入力ダイアログを表示
    if (name && name.trim() !== oldName) { // 空文字ではなく、入力値がoldNameでないときにupdate
      await update(id, name.trim());
    }
  };

  // ─── 3) 削除 ─────────────────────────────────
  const handleDelete = async id => {
    if (window.confirm('本当にこのタグを削除しますか？')) { // ブラウザ組み込みのモーダルダイアログを表示
      await remove(id);
    }
  };

  if (loading) return <p>読み込み中…</p>;
  if (error)   return <p className="text-red-600">エラー: {error.message}</p>;

  return (
    <div className="max-w-lg mx-auto mt-8 p-4 bg-white shadow rounded">
      <h2 className="text-xl font-bold mb-4">タグ管理</h2>

      {/* 一覧表示 */}
      <ul className="mb-4">
        {tags.map(t => (
          <li key={t.id} className="flex justify-between py-1">
            <span>{t.name}</span>
            <div className="space-x-2">
              <button
                onClick={() => handleUpdate(t.id, t.name)}
                className="px-2 py-1 bg-yellow-300 rounded"
              >
                編集
              </button>
              <button
                onClick={() => handleDelete(t.id)}
                className="px-2 py-1 bg-red-400 text-white rounded"
              >
                削除
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
          placeholder="新しいタグ名"
          className="flex-1 p-2 border rounded-l"
        />
        <button
          onClick={handleAdd}
          className="px-4 py-2 bg-blue-600 text-white rounded-r disabled:opacity-50"
          disabled={!newName.trim()}
        >
          追加
        </button>
      </div>
    </div>
  );
}