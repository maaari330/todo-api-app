import React from 'react';

// フィルタリングするタグを選択するためのプルダウン（<select>）をレンダー
export default function TagFilter({
  tags,
  selectedIds,
  onChange
}) {
  return (
    <div className="mb-4">
      <label className="mr-2">タグ:</label>
      {tags.map(t => (
        <label key={t.id} className="inline-flex items-center mr-3">
          <input
            type="checkbox"
            value={t.id}
            checked={selectedIds.includes(t.id)} // selectedIds（選択中のタグ ID 配列）の中に、t.id が含まれていたらチェックボックスをオンにする
            onChange={e => {
              const id = Number(e.target.value);
              onChange(
                e.target.checked
                  ? [...selectedIds, id]
                  : selectedIds.filter(x => x !== id) // 元のselectedIds配列から id と一致する要素だけを除外した新しい配列を生成
              );
            }}
          />
          <span className="ml-1">{t.name}</span>
        </label>
      ))}
    </div>
  );
}