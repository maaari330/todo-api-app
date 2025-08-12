import api from '../utils/axiosConfig';

/**
 * タグ一覧取得／作成／更新／削除を扱うサービス層
 */
export const categoryService = {
   /** 一覧取得 */
  list: async () => {
    const res = await api.get('/categories');
    return res.data;
  },
  /** 新規作成 */
  create: async name => {
    const res = await api.post('/categories', { name });
    return res.data;
  },
  /** 更新 */
  update: async (id, name) => {
    const res = await api.put(`/categories/${id}`, { name });
    return res.data;
  },
  /** 削除 */
  remove: async id => {
    await api.delete(`/categories/${id}`);
    // 削除はボディを返さない想定なので、そのまま完了を返す
  },
};