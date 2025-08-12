import React from 'react';

/**
 * ソートフィルタボタン
 * @param {{ field: string; label: string; sort: string; onChange: (newSort: string) => void }} props
 * 
 * field: このボタンが担当するソート対象（例: 'title', 'dueDate'）
 * sort: 現在のソート状態（例: 'title,asc'）
 */
export default function SortFilter({ field, label, sort, onChange }) {
  const [currentField, currentDir] = sort.split(',');
  const isActive = currentField === field;
  const dir = isActive ? currentDir : 'asc'; // field が非アクティブ → ascをデフォルトとして扱う
  const toggle = () => {
    const nextDir = isActive && currentDir === 'asc' ? 'desc' : 'asc'; // ボタンが押下されたら 昇順 ↔ 降順
    onChange(`${field},${nextDir}`);
  };
  return (
    <button
      onClick={toggle}
      className={`px-2 py-1 rounded ${isActive ? 'bg-blue-500 text-white' : 'bg-gray-200 text-gray-800'}`}
    >
      {label} {isActive ? (dir === 'asc' ? '▲ 昇順' : '▼ 降順') : ''}
    </button>
  );
}