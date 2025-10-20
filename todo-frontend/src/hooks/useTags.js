import { useState, useEffect } from 'react';
import { tagService } from '../services/tagService';

/** タグのフック層 
 * コンポーネント層（TagPage）で使うロジック集約、ローディング・エラー管理
 * サービス層（TagService）での API 呼び出しの 結果を state に反映 */
export function useTags() {
  const [tags, setTags] = useState([]);
  const [loading, setLoading] = useState(true); // タグデータ取得状態の管理
  const [error, setError] = useState(null);

  // カテゴリデータ取得用ロジック
  const refetch = async () => {
    setLoading(true);
    try {
      const data = await tagService.list();
      setTags(data);
      setError(null);
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
  };

  // マウント時に一度だけタグ一覧を取得、以降は手動でrefetch()を呼び再フェッチ
  useEffect(() => {
    refetch();
  }, []);

  // 作成用ロジック
  const create = async (name) => { await tagService.create(name); await refetch(); };
  // 更新用ロジック
  const update = async (id, name) => { await tagService.update(id, name); await refetch(); };
  // 削除用ロジック
  const remove = async (id) => {
    try { await tagService.remove(id); await refetch(); } catch (e) {
      if (e?.response?.status === 409) {
        alert(e.response?.data?.message || 'このタグが付いたタスクが残っているため削除できません');
        return;
      }
      throw e;
    }
  };

  return { tags, loading, error, refetch, create, update, remove };
}