import { useEffect, useState } from 'react';
import { fetchRecent, type InAppMessage } from '../api/notifications';

/** アプリ内通知表示 */
export default function InAppList() {
    const [items, setItems] = useState<InAppMessage[]>([]);
    useEffect(() => { fetchRecent().then(setItems); }, []); // 初回マウント時に一度だけ実行
    return (

        <ul aria-live="polite">
            {items.map(m => (
                <li key={m.id}>
                    <b>{m.title}</b> — {m.body} <a href={m.url || '#'}>開く</a>
                    <small className="ml-2 text-gray-600">{new Date(m.createdAt).toLocaleString()}</small>
                </li>
            ))}
        </ul>
    );
}