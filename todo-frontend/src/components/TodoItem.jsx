import React, { useState, useEffect, useRef } from "react";
import axios from "axios";

/**
 * TodoItem component handles display and inline editing of a single todo.
 * Props:
 * - todo: { id, title, done }
 * - onUpdated: callback to re-fetch list after update
 */
export default function TodoItem({ todo, onUpdated }) {
  const [isEditing, setIsEditing] = useState(false);
  const [editTitle, setEditTitle] = useState(todo.title);
  const inputRef = useRef(null);

  // Focus input when entering edit mode
  useEffect(() => {
    if (isEditing) inputRef.current?.focus();
  }, [isEditing]);

  // Save changes
  const handleSave = async () => {
    try {
      await axios.put(`/todos/${todo.id}`, { title: editTitle });
      setIsEditing(false);
      onUpdated();
    } catch (err) {
      alert("更新に失敗しました");
    }
  };

  // Cancel editing
  const handleCancel = () => {
    setEditTitle(todo.title);
    setIsEditing(false);
  };

  return (
    <li className="flex items-center space-x-2 py-1">
      {isEditing ? (
        <>
          <input
            ref={inputRef}
            value={editTitle}
            onChange={e => setEditTitle(e.target.value)}
            className="flex-1 border rounded px-2 py-1"
          />
          <button onClick={handleSave} className="px-2 py-1 rounded bg-blue-500 text-white">保存</button>
          <button onClick={handleCancel} className="px-2 py-1 rounded bg-gray-200">取消</button>
        </>
      ) : (
        <>
          <span
            className="flex-1 cursor-pointer"
            onDoubleClick={() => setIsEditing(true)}
          >
            {todo.title}
          </span>
          <button onClick={() => setIsEditing(true)} className="px-2 py-1 rounded bg-green-200">編集</button>
        </>
      )}
    </li>
  );
}
