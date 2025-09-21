import { useEffect, useState } from 'react';
import { fetchRecent, type InAppMessage } from '../services/notificationsService';

/** アプリ内通知表示 */
export default function InAppList() {
    const [items, setItems] = useState<InAppMessage[]>([]);
    useEffect(() => {
        fetchRecent()
            .then(setItems)
            .catch((e) => console.error('fetchRecent failed', e));
    }, []);

    return (
        <ul aria-live="polite">
            {items.map((m: InAppMessage) => (
                <li key={m.id}>
                    <b>{m.title}</b> — {m.body} <a href={m.url ?? '#'}>開く</a>
                    <small className="ml-2 text-gray-600">
                        {new Date(m.createdAt).toLocaleString()}
                    </small>
                </li>
            ))}
        </ul>
    );
}