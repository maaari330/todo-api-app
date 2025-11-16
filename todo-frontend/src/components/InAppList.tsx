// src/components/InAppList.tsx
import { useEffect, useState } from 'react';
import { fetchRecent, type InAppMessage, type Paged } from '../services/notificationsService';

/** アプリ内通知表示 */
export default function InAppList() {
  const [items, setItems] = useState<InAppMessage[]>([]);
  const [hasNext, setHasNext] = useState(false);

  useEffect(() => {
    fetchRecent()
      .then((paged: Paged<InAppMessage>) => {
        console.log('InAppList got paged:', paged); // デバッグログ
        setItems(paged.content);
        setHasNext(paged.hasNext);
      })
      .catch((e) => console.error('fetchRecent failed', e));
  }, []);

  if (items.length === 0) {
    return <p className="text-gray-500 text-sm">まだ通知はありません。</p>;
  }

  return (
    <div>
      <ul aria-live="polite" className="space-y-2">
        {items.map((m: InAppMessage) => (
          <li key={m.id}>
            <b>{m.title}</b> — {m.body}{' '}
            <a href={m.url ?? '#'} className="text-blue-600 underline">
              開く
            </a>
            <small className="ml-2 text-gray-600">
              {new Date(m.createdAt).toLocaleString()}
            </small>
          </li>
        ))}
      </ul>

      {hasNext && (
        <p className="mt-2 text-xs text-gray-500">さらに過去の通知があります。</p>
      )}
    </div>
  );
}
