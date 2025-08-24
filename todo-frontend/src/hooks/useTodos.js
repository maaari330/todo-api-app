import { useState, useEffect, useCallback } from 'react';
import { todoService } from '../services/todoService';

/**
 * TodosPage コンポーネントで使うロジックを集約したフック層
 */
export function useTodos(initial = {}) {
  const { page: initPage = 0, size: initSize = 10, sort: initSort = 'dueDate,asc', status: initStatus = 'all' } = initial;
  const [todos, setTodos] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [params, setParams] = useState({page: initPage, size: initSize, sort: initSort, keyword: '', status: initStatus, category: undefined, tags: [],});
  const [pagination, setPagination] = useState({ page: initPage, size: initSize, totalPages: 0, totalElements: 0 });


  const fetchTodos = useCallback(async () => {
    setLoading(true);
    try {
      const data = await todoService.list(params);
      setTodos(Array.isArray(data.content) ? data.content : []);
      setPagination({page: data.number, size: data.size,
        totalPages: data.totalPages,
        totalElements: data.totalElements,
      });
      setError(null);
    } catch (e) {
      setError(e);
      setTodos([]);
    } finally {
      setLoading(false);
    }

  }, [params]);

  // 初回レンダー後に一度実行。そのあとは fetchTodos の params が変わったら再実行
  useEffect(() => { fetchTodos(); }, [fetchTodos]);

  // 各アクション
  const create = useCallback(async payload => { await todoService.create(payload); fetchTodos(); }, [fetchTodos]);
  const update = useCallback(async (id, payload) => { await todoService.update(id, payload); fetchTodos(); }, [fetchTodos]);
  const toggle = useCallback(async id => { await todoService.toggle(id); fetchTodos(); }, [fetchTodos]);
  const remove = useCallback(async id => { await todoService.remove(id); fetchTodos(); }, [fetchTodos]);

  return {
    todos, loading, error, pagination, params, 
    setParams, // コンポーネント側（TodosPage）から「パラメータ（ページ番号・フィルタ・ソートなど）を変更したい」場合に、フック内の状態を更新
    fetchTodos, create, update, toggle, remove,
  };
}