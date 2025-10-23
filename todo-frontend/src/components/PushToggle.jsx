import { ensureSubscription, unsubscribePush } from '../push';
import { useState } from 'react';
import { useAuth } from '../hooks/useAuth';

/** 通知の有効化・無効化ボタン表示UI */
export default function PushToggle() {
    const { user } = useAuth();
    const [enabled, setEnabled] = useState(false);
    const [busy, setBusy] = useState(false);

    if (!user) return null;
    const handleClick = async () => {
        setBusy(true);
        try {
            if (!enabled) {
                await ensureSubscription();
                setEnabled(true);
                alert('通知を有効化しました');
            } else {
                await unsubscribePush();
                setEnabled(false);
                alert('通知を無効化しました');
            }
        } catch (e) {
            alert(e instanceof Error ? e.message : String(e));
        } finally {
            setBusy(false);
        }
    };

    return (
        <button
            type="button"
            onClick={handleClick}
            disabled={busy}
            aria-pressed={enabled}
            className={`rounded-md px-3 py-2 text-sm font-medium shadow ${enabled ? 'bg-indigo-600 text-white' : 'bg-gray-200 text-gray-800'
                }`}
            title={enabled ? '通知を無効化' : '通知を有効化'}
        >
            {busy ? '処理中…' : enabled ? '通知をOFF' : '通知をON'}
        </button>
    );
}