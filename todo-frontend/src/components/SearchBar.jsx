import React from 'react';

export default function SearchBar({ value, onChange }) {
  return (
    <div className="flex justify-center mb-4">
      <input
        type="text"
        placeholder="検索キーワードを入力"
        value={value}
        onChange={e => onChange(e.target.value)}
        className="w-1/2 max-w-xs border rounded px-3 py-2"
      />
    </div>
  );
}