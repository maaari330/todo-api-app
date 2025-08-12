import { useState, useEffect } from 'react';
import { categoryService } from '../services/categoryService';

/** カテゴリのフック層
 * コンポーネント層（CategoryPage）で使うカテゴリ一覧データ、ロジック集約、ローディング・エラーを管理
 * 
 * サービス層（categoryService）での API 呼び出しの 結果を state に反映 */
export function useCategories() {
  const [categories, setCategories] = useState([]); // カテゴリ一覧データ
  const [loading, setLoading] = useState(true); // カテゴリデータ取得状態の管理
  const [error, setError]           = useState(null);

  // カテゴリデータ取得用ロジック
  const refetch = async () => {
    setLoading(true);
    try {
      const data = await categoryService.list();
      setCategories(data);
      setError(null);
    } catch (e) {
      setError(e);
    } finally {
      setLoading(false);
    }
  };

  // マウント時に一度だけカテゴリ一覧を取得、以降は手動でrefetch()を呼び再フェッチ
  useEffect(() => {
    refetch(); 
  }, []);

  // 作成用ロジック
  const create  = async (name)    => { await categoryService.create(name); await refetch(); };
  // 更新用ロジック
  const update  = async (id, name)=> { await categoryService.update(id,name); await refetch(); };
  // 削除用ロジック
  const remove  = async (id)      => { await categoryService.remove(id); await refetch(); };

  return { categories, loading, error, refetch, create, update, remove };
}