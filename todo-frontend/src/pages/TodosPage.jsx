import React, { useState, useEffect, useContext } from 'react';
import { useNavigate } from 'react-router-dom';
import { AuthContext } from '../context/AuthContext';
import { canModifyTodo } from '../utils/permissionUtils';
import { useTodos } from '../hooks/useTodos';
import { useCategories } from '../hooks/useCategories';
import { useTags } from '../hooks/useTags';
import SearchBar from '../components/SearchBar';
import StatusFilter from '../components/StatusFilter';
import CategoryFilter from '../components/CategoryFilter';
import TagFilter from '../components/TagFilter';
import SortFilter from '../components/SortFilter';
import TodoDrawer from '../components/TodoDrawer';
import TodoItem from '../components/TodoItem';
import Pagination from '../components/Pagination';
import LoadingSpinner from '../components/LoadingSpinner';

export default function TodosPage() {
  const { user, loading: authLoading } = useContext(AuthContext);
  const navigate = useNavigate();
  const { todos, loading: todosLoading, error: todosError, pagination, params,
    setParams,
    create, toggle, update, remove, } = useTodos();
  const { categories, loading: catsLoading, error: catsError } = useCategories();
  const { tags, loading: tagsLoading, error: tagsError } = useTags();
  // Drawer管理：open + currently編集中のtodoオブジェクト
  const [openDrawer, setOpenDrawer] = useState(false)
  const [editingTodo, setEditingTodo] = useState(null);

  useEffect(() => {
    if (!authLoading && !user) navigate('/login', { replace: true }); // /login が現在のページとなり、元のページに戻れなくなる
  }, [authLoading, user, navigate]);

  if (authLoading || todosLoading || catsLoading || tagsLoading) return <LoadingSpinner />;
  if (todosError || catsError || tagsError) return <div className="text-red-500">{todosError?.message || catsError?.message || tagsError?.message}</div>;

  // 新規タスク作成、編集用TodoDrawer → TodoForm を開く
  const handleNew = () => { setEditingTodo(null); setOpenDrawer(true); };
  const handleEdit = todo => {
    if (!canModifyTodo(todo, user)) {
      alert('編集権限がありません');
      return;
    }
    const editingData = {
      ...todo,
      repeatType: todo.repeatType || 'NONE',
      category: todo.categoryId ? { id: todo.categoryId } : null,
      tags: (todo.tagIds || []).map(id => ({ id })),
      remindOffsetMinutes: todo.remindOffsetMinutes ?? null,
    };
    setEditingTodo(editingData);
    setOpenDrawer(true);
  }; // 編集中のタスクデータがフォームにセットされる  

  // TodoForm からフォームをバックエンドにapi送信
  const handleSubmit = async payload => {
    if (editingTodo) {
      await update(editingTodo.id, payload);
    } else {
      await create(payload);
    }
    // useTodos フック内で fetchTodos が走るので、ここでは Drawer を閉じるだけ
    setOpenDrawer(false);
  };

  return (
    <>
      {/* ── フィルタ追加ボタン ── */}
      <div className="flex flex-wrap items-center justify-between mb-4 space-y-2">
        <div className="flex flex-wrap space-x-4">
          <SearchBar
            value={params.keyword}
            onChange={keyword => setParams(p => ({ ...p, keyword, page: 0 }))}
          />
          <StatusFilter
            value={params.status}
            onChange={status => setParams(p => ({ ...p, status, page: 0 }))}
          />
          <CategoryFilter
            categories={categories}
            value={params.category}
            onChange={category => setParams(p => ({ ...p, category, page: 0 }))}
          />
          <TagFilter
            tags={tags}
            selectedIds={params.tags}
            onChange={tags => setParams(p => ({ ...p, tags, page: 0 }))}
          />
          <div className="flex space-x-4">
            <SortFilter
              field="title"
              label="タイトル"
              sort={params.sort}
              onChange={newSort => setParams(p => ({ ...p, sort: newSort, page: 0 }))}
            />
            <SortFilter
              field="dueDate"
              label="期限"
              sort={params.sort}
              onChange={newSort => setParams(p => ({ ...p, sort: newSort, page: 0 }))}
            />
          </div>
        </div>
        {/* ── 新規タスク追加ボタン ── */}
        <button
          className="px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          onClick={handleNew}
        >
          + 新規タスク
        </button>
      </div>

      {/* ── タスク一覧 ── */}
      <ul className="divide-y">
        {todos.length > 0 ? todos.map(todo => (
          <TodoItem
            key={todo.id}
            todo={todo}
            onToggle={() => toggle(todo.id)}
            onEdit={() => handleEdit(todo)}
            onDelete={() => remove(todo.id)}
          />
        )) : (
          <li className="p-4 text-gray-500">タスクはまだありません</li>
        )}
      </ul>
      {/* ── ページネーション ── */}
      <Pagination
        page={pagination.page}
        totalPages={pagination.totalPages}
        onPageChange={page => setParams(p => ({ ...p, page: page }))}
      />
      {/* ── Drawer: 新規タスク作成、編集フォーム ── */}
      <TodoDrawer
        open={openDrawer}
        onClose={() => setOpenDrawer(false)}
        initialValues={editingTodo || {}}
        onSubmit={handleSubmit}
        categories={categories}
        tags={tags}
      />
    </>
  );
}
