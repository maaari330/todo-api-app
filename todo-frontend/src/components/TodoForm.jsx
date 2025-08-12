import React, { useState, useEffect } from 'react';
import { normalizeLocalDateTime } from '../utils/dateUtils';

/**
 * Props:
 * - onColse: () => void
 * - initialValues: { id?, title?, dueDate?, done?, category?, tags? }
 * - onSubmit: async payload => void
 * - categories: []
 * - tags: []
 */
export default function TodoForm({ onClose, initialValues = {}, onSubmit, categories= [], tags = [] }) {
  const isNew = !initialValues.id // タスク が存在するときは true → 新規作成モード　タスク が存在しないときは false → 編集モード

  const [title, setTitle]           = useState(initialValues.title || '')
  const [done, setDone]             = useState(initialValues.done ?? false);
  const [dueDate, setDueDate]       = useState(initialValues.dueDate || '')
  const [repeatType, setRepeatType] = useState(initialValues.repeatType != null ? initialValues.repeatType : 'NONE');
  const [categoryId, setCategoryId] = useState(initialValues.category?.id || null)
  const [tagIds, setTagIds]         = useState(initialValues.tags?.map(t=>t.id) || [])
  const [submitting, setSubmitting] = useState(false);

  // initialValues が変わったら内部 state に反映
  useEffect(() => {
    setTitle(initialValues.title || '')
    setDone(initialValues.done ?? false)
    setDueDate(initialValues.dueDate || '')
    setRepeatType(initialValues.repeatType != null ? initialValues.repeatType : 'NONE');
    setCategoryId(initialValues.category?.id || null)
    setTagIds(initialValues.tags?.map(t=>t.id) || [])
  }, [initialValues])

  // yyyy-MM-ddTHH:mm の形式に揃える（input用）
  const formatForDatetimeLocal = value => {
    if (!value) return '';
    const normalized = normalizeLocalDateTime(value);
    return normalized.substring(0, 16); // yyyy-MM-ddTHH:mm まで切り出す
  };

  const handleSubmit = async e => {
    e.preventDefault();
    if (!title.trim()) return;
    setSubmitting(true);
    try {
      const payload = {
        title,
        done,
        dueDate: dueDate ? normalizeLocalDateTime(dueDate) : undefined,
        repeatType,
        categoryId: categoryId ?? undefined,
        tagIds: tagIds.length ? tagIds : undefined,
      }
      await onSubmit(payload)
      onClose?.()
    } catch (err) {
      alert(isNew ? '追加に失敗しました' : '更新に失敗しました')
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-4">
      {/* タイトル */}
      <input
        type="text"
        value={title}
        onChange={e => setTitle(e.target.value)}
        placeholder="タスクを入力"
        className="w-full border rounded px-3 py-2"
        required
      />
      {/* 期日 */}
      <input
        type="datetime-local"
        value={formatForDatetimeLocal(dueDate)}
        onChange={e => setDueDate(e.target.value)}
        className="w-full border rounded px-3 py-2"
      />
      {/* 繰り返し設定 */}
      <select
        value={repeatType}
        onChange={e => setRepeatType(e.target.value)}
        className="w-full border rounded px-3 py-2"
      >
        <option value="NONE">繰り返しなし</option>
        <option value="DAILY">毎日</option>
        <option value="WEEKLY">毎週</option>
        <option value="MONTHLY">毎月</option>
      </select>
      {/* カテゴリ選択 */}
      <select
        value={categoryId ?? ''}
        onChange={e => setCategoryId(e.target.value ? +e.target.value : null)} // ＋ で数値にパース
        className="w-full border rounded px-3 py-2"
      >
        <option value="">--カテゴリ--</option>  {/* categoryId が空文字のときに「未選択」状態 */}
        {categories.map(c => (
          <option key={c.id} value={c.id}>{c.name}</option>
        ))}
      </select>
      {/* タグ選択（チップスタイル） */}
      <div>
        <p className="font-semibold mb-2">タグ</p>
        <div className="flex flex-wrap gap-2">
          {tags.map(t => (
            <label
              key={t.id}
              className={`px-3 py-1 rounded-full cursor-pointer border text-sm
                ${tagIds.includes(t.id)
                  ? 'bg-blue-500 text-white border-blue-500'
                  : 'bg-gray-100 text-gray-700 border-gray-300 hover:bg-gray-200'
                }`}
            >
              <input
                type="checkbox"
                value={t.id}
                checked={tagIds.includes(t.id)}
                onChange={e => {
                  const id = +e.target.value;
                  setTagIds(prev =>
                    e.target.checked
                      ? [...prev, id]
                      : prev.filter(x => x !== id)
                  );
                }}
                className="hidden"
              />
              {t.name}
            </label>
          ))}
        </div>
      </div>
      {/* 編集時のみ完了フラグを切り替えられる */}
      {!isNew && (
        <div className="mt-4">
          <p className="font-semibold mb-2">ステータス</p>
          <label className="relative inline-flex items-center space-x-2 cursor-pointer">
            <input
              type="checkbox"
              checked={done}
              onChange={e => setDone(e.target.checked)}
              className="sr-only peer"
            />
            {/* スイッチの土台 */}
            <div className="w-11 h-6 bg-gray-200 rounded-full peer peer-checked:bg-green-500 transition-colors"></div>
              {/* スライダー（丸い部分） */}
              <div className="absolute left-1 top-1 bg-white w-4 h-4 rounded-full transition-transform peer-checked:translate-x-5"></div>
            <span className="ml-3 text-sm font-medium text-gray-900">完了にする</span>
          </label>
        </div>
      )}
      {/* 追加ボタン */}
      <div className="flex justify-end space-x-2">
        {onClose && (
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2 border rounded hover:bg-gray-100"
          >
            キャンセル
          </button>
        )}
        <button
          type="submit" // フォーム全体（formタブ）を送信する動作がデフォルトで行われる。Reactは onSubmit={handleSubmit} ハンドラが呼ばれる
          disabled={submitting} // submitting が true ならクリックできない
          className="px-4 py-2 bg-green-600 text-white rounded hover:bg-green-700 disabled:opacity-50"
        >
          {submitting
            ? (isNew ? '追加中…' : '更新中…')
            : (isNew ? '追加する' : '更新する')}
        </button>
      </div>
    </form>
  );
}

