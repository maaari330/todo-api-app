import api from '../utils/axiosConfig';

/**
 * タグ一覧取得／作成／更新／削除を扱うサービス層
 */
export const tagService = {
  /** 一覧取得 */
  list: async () => {
    const res = await api.get('/tags');
    return res.data;
  },
  /** 新規作成 */
  create: async name => {
    const res = await api.post('/tags', { name });
    return res.data;
  },
  /** 更新 */
  update: async (id, name) => {
    const res = await api.put(`/tags/${id}`, { name });
    return res.data;
  },
  /** 削除 */
  remove: async id => {
    await api.delete(`/tags/${id}`);
    // 削除はボディを返さない想定なので return なしでもOK
  },
};