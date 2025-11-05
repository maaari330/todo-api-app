import { ensureSubscription, unsubscribePush, isSubscribed } from '../push';
import { useState, useEffect } from 'react';
import { useAuth } from '../hooks/useAuth';

/** 通知の有効化・無効化ボタン表示UI */
export default function PushToggle() {
    const { user } = useAuth();
    const [enabled, setEnabled] = useState(false);
    const [busy, setBusy] = useState(false);
    const [checking, setChecking] = useState(true); // （※）で確認済みかどうかフラグ

    // 初回マウント時（ログイン後のユーザー確定時）に、購読有無を問い合わせ（※）
    useEffect(() => {
        let cancelled = false;
        (async () => {
            if (!user) { setChecking(false); return; }
            try {
                const ok = await isSubscribed();
                if (!cancelled) setEnabled(ok);
            } finally {
                if (!cancelled) setChecking(false);
            }
        })();
        return () => { cancelled = true; };
    }, [user]);

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
            disabled={busy || checking}
            aria-pressed={enabled}
            className={`rounded-md px-3 py-2 text-sm font-medium shadow ${enabled ? 'bg-indigo-600 text-white' : 'bg-gray-200 text-gray-800'
                }`}
            title={enabled ? '通知を無効化' : '通知を有効化'}
        >
            {busy ? '処理中…' : checking ? '判定中…' : enabled ? '通知をOFF' : '通知をON'}
        </button>
    );
}