import TodoForm from './TodoForm';
import React from 'react'

/**
 * 汎用 Drawer（新規 or 編集）
 * 
 * 内部で TodoFormを呼び、その周囲を「固定ポジションのオーバーレイ＋横からスライドインするパネル」で囲む
 *
 * Props:
 * - open: boolean
 * - onClose: () => void
 * - initialValues?: { id?, title?, dueDate?, category?, tags? }
 * - categories, tags
 * - onSubmit: async (payload) => void
 */
export default function TodoDrawer({ open, onClose, initialValues = {}, onSubmit, categories= [], tags= [] }) {
  if (!open) return null
  const isNew = !initialValues.id // タスク が存在するときは true → 新規作成モード　タスク が存在しないときは false → 編集モード

  return (
    <div className="fixed inset-0 flex z-50">
      {/* 背景オーバーレイ */}
      <div className="flex-1 bg-black/40" onClick={onClose} />
      {/* パネル本体 */}
      <div className="w-80 bg-white shadow-xl p-6 overflow-auto relative">
        <button
          className="absolute top-4 right-4 text-gray-500 hover:text-gray-800"
          onClick={onClose}
        >
          ✕
        </button>
        <h2 className="text-2xl mb-4">
          {isNew ? '新規タスク作成' : 'タスクを編集'}
        </h2>
        <TodoForm
          onClose={onClose}
          initialValues={initialValues}
          onSubmit={onSubmit}
          categories={categories}
          tags={tags}
        />
      </div>
    </div>
  )
}