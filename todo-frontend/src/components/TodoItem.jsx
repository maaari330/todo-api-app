import React, {} from 'react';
/**
 * Props:
 * - todo
 * - onToggle(id,newDone)
 * - onDelete(id)
 * - onEdit(id)  // ← 追加: 編集用 Drawer を開く
 */
export default function TodoItem({ todo, onToggle, onEdit, onDelete }) {

  return (
    <li className="flex items-center justify-between p-4 mb-4 rounded-lg shadow" >
      {/* 左側：チェック＋本文 */}
      <div className="flex items-center flex-1 space-x-3">
        {/* ✔ 完了トグルは onChange のみ */}
        <input
          type="checkbox"
          checked={todo.done}
          onChange={() => onToggle(todo.id)}
          className="form-checkbox h-5 w-5 text-blue-500"
        />
        <span className={todo.done ? 'line-through text-gray-400' : 'text-gray-800'}>
          {todo.title}
        </span>
        {todo.dueDate && (
          <span className="text-sm text-gray-500">{todo.dueDate}</span>
        )}
      </div>

      {/* 右側：操作ボタン */}
      <div className="flex space-x-2">
        <button
          onClick={() => onEdit(todo)} // TodosPage の handleEdit により編集用TodoFormが開く
          className="px-2 py-1 text-sm text-blue-600 border border-blue-600 rounded hover:bg-blue-50"
        >
          編集
        </button>
        <button
          onClick={() => onDelete(todo.id)}
          className="px-2 py-1 text-sm text-red-600 border border-red-600 rounded hover:bg-red-50"
        >
          削除
        </button>
      </div>
    </li>
  )
}