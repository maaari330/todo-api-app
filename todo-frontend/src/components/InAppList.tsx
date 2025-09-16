import { useEffect, useState } from 'react';
import { fetchRecent, InAppMessage } from '../api/notifications';

export default function InAppList() {
    const [items, setItems] = useState<InAppMessage[]>([]);
    useEffect(() => { fetchRecent().then(setItems); }, []);
    return (
        <ul>
            {items.map(m => (
                <li key={m.id}>
                    <b>{m.title}</b> — {m.body} <a href={m.url || '#'}>開く</a>
                    <small style={{ marginLeft: 8, color: '#666' }}>{new Date(m.createdAt).toLocaleString()}</small>
                </li>
            ))}
        </ul>
    );
}