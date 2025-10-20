import React from 'react';

// フィルタリングするカテゴリを選択するためのプルダウン（<select>）をレンダー
export default function CategoryFilter({ 
  categories, // 親から渡されるカテゴリ一覧の配列
  value,  // 現在選択されているカテゴリ ID
  onChange  // カテゴリが変わったときに呼ばれるコールバック関数
}) {
  return (
    <div className="mb-4">
      <label className="mr-2">カテゴリ:</label>
      <select
        value={value||''}     // value が undefined のときは空文字 '' を使って “すべて” を表示
        onChange={e => onChange(e.target.value ? Number(e.target.value) : undefined)}
        className="border rounded px-2 py-1"
      >
        <option value="">すべて</option>
        {categories.map(c => (
          <option key={c.id} value={c.id}>{c.name}</option>
        ))}
      </select>
    </div>
  );
}