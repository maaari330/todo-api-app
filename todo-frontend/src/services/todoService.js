import api from '../utils/axiosConfig';

/**
 * Todo の CRUD やフィルタ付き取得を扱うサービス層
 */
export const todoService = {
  /**
   * ページング・ソート・フィルタ付きの Todo 一覧取得
   * @param {{ page: number; size: number; sort: string; keyword?: string; status?: 'all'|'active'|'done'; category?: number; tags?: number[] }} opts // listメソッドの引数をプロパティ名: 型で指定
   */
  list: async ({ page, size, sort, keyword, status, category, tags }) => {
    const params = { page, size, sort };
    if (keyword) params.keyword = keyword;
    if (status === 'active') params.done = false;
    if (status === 'done') params.done = true;
    if (category) params.category = category;
    if (tags?.length) params.tags = tags.join(',');
    const res = await api.get('/todos', { params });
    return res.data;
  },
  /** 新規作成 */
  create: async ({ title, dueDate, remindOffsetMinutes, repeatType, categoryId, tagIds }) =>
    api.post('/todos', { title, dueDate, remindOffsetMinutes, repeatType, categoryId, tagIds }),
  /** 更新 */
  update: async (id, { title, dueDate, remindOffsetMinutes, done, repeatType, categoryId, tagIds }) =>
    api.put(`/todos/${id}`, { title, dueDate, remindOffsetMinutes, done, repeatType, categoryId, tagIds }),
  /** 完了・未完了切替 */
  toggle: async id =>
    api.patch(`/todos/${id}`),
  /** 削除 */
  remove: async id =>
    api.delete(`/todos/${id}`),
};
