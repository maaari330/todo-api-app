import api from './utils/axiosConfig';

/** フロント側での Service Worker の登録
 * バックエンドの /push/subscribe に購読情報の送信 */

// React のビルド時に公開鍵を注入
const VAPID_PUBLIC_KEY = (process.env.REACT_APP_VAPID_PUBLIC_KEY || '').trim();

/** Base64URL ⇄ バイナリ変換ユーティリティ */
// 1) VAPID 公開鍵（Base64URL）を Uint8Array（バイト配列）に変換する関数
function urlBase64ToUint8Array(base64: string) {
    const padding = '='.repeat((4 - (base64.length % 4)) % 4);
    const base64Safe = (base64 + padding).replace(/-/g, '+').replace(/_/g, '/');
    const raw = atob(base64Safe);
    return Uint8Array.from([...raw].map(c => c.charCodeAt(0)));
}

// 2) ブラウザの鍵（ArrayBufferで渡される p256dh/auth）を Base64URL に戻す関数
function bufToBase64Url(buf: ArrayBuffer) {
    const s = String.fromCharCode(...new Uint8Array(buf));
    return btoa(s).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
}

/** 追加：現在の SW 登録を取得（なければ register）→ 現在の購読を返す（なければ作成） */
async function getOrCreateSubscription(): Promise<PushSubscription> {
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
        throw new Error('Push未対応のブラウザです');
    }
    if (!VAPID_PUBLIC_KEY) {
        throw new Error('REACT_APP_VAPID_PUBLIC_KEY が未設定です（.env を確認）');
    }

    // 既存の登録を使う。なければ register（←毎回 register し直さない）
    const reg =
        (await navigator.serviceWorker.getRegistration()) ??
        (await navigator.serviceWorker.register('/sw.js'));

    // 権限確認（未許可ならここで終了）
    const perm = await Notification.requestPermission();
    if (perm !== 'granted') throw new Error('通知が許可されていません');

    // 既存購読があればそのまま使う（＝endpoint が変わらない限り再購読しない）
    const existing = await reg.pushManager.getSubscription();
    if (existing) return existing;

    // なければ新規購読を作る
    return reg.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(VAPID_PUBLIC_KEY),
    });
}

/** ここを呼べば“いまの購読 endpoint”が必ずサーバに登録される（＝DBとブラウザの宛先が一致） */
export async function enablePush() {
    const sub = await getOrCreateSubscription();

    // サーバが期待する形（既存と同じ：keys 配下に p256dh/auth）
    const body = {
        endpoint: sub.endpoint,
        keys: {
            p256dh: bufToBase64Url(sub.getKey('p256dh')!),
            auth: bufToBase64Url(sub.getKey('auth')!),
        },
    };

    // ★重要：ログイン後などアプリ起動時に毎回 upsert 送る
    //  └ 端末/プロファイル変更や SW 再購読で endpoint が変わっても、DBが最新に追随できる
    await api.post('/push/subscribe', body);
    const reg = (await navigator.serviceWorker.getRegistration())
        ?? (await navigator.serviceWorker.register('/sw.js'));
    console.log('[SW] register done. scope=', reg.scope);
    console.log('[push] registered endpoint:', sub.endpoint);
}

/** 購読解除（サーバから削除 → ブラウザ側も解除） */
export async function disablePush() {
    const reg = await navigator.serviceWorker.getRegistration();
    const sub = await reg?.pushManager.getSubscription();
    if (!sub) return;

    const body = {
        endpoint: sub.endpoint,
        keys: {
            p256dh: bufToBase64Url(sub.getKey('p256dh')!),
            auth: bufToBase64Url(sub.getKey('auth')!),
        },
    };

    await api.delete('/push/unsubscribe', { data: body });
    await sub.unsubscribe();
    console.log('[push] unregistered endpoint:', sub.endpoint);
}