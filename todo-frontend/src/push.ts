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


/** Push を有効化するメイン関数 */
export async function enablePush() {
    if (!('serviceWorker' in navigator) || !('PushManager' in window)) {
        throw new Error('Push未対応のブラウザです');
    }
    // 1) Service Worker登録
    const reg = await navigator.serviceWorker.register('/sw.js');
    // 2) 通知許可をポップアップで確認
    const perm = await Notification.requestPermission(); //　例：「このサイトからの通知を許可しますか？」
    if (perm !== 'granted') throw new Error('通知が許可されていません');

    // 3) Push 購読の作成（VAPID鍵が必要）
    if (!VAPID_PUBLIC_KEY) {
        throw new Error('REACT_APP_VAPID_PUBLIC_KEY が未設定です（todo-frontend/.env に設定）');
    }
    // ブラウザ側の購読登録
    const sub = await reg.pushManager.subscribe({
        userVisibleOnly: true,
        applicationServerKey: urlBase64ToUint8Array(VAPID_PUBLIC_KEY),
    }); // pushManager.subscribe：ブラウザに「このオリジンでプッシュを受け取る購読を作って」と頼む
    // sub：購読を表すオブジェクトが返る（endpoint, getKey('p256dh'), getKey('auth'), unsubscribe()など）

    // 4) サーバへ購読登録
    const body = {
        endpoint: sub.endpoint,
        keys: {
            p256dh: bufToBase64Url(sub.getKey('p256dh')!),
            auth: bufToBase64Url(sub.getKey('auth')!),
        },
    };
    await api.post('/push/subscribe', body);
}


/** Push を無効化（購読解除） */
export async function disablePush() {
    // 既存のService Worker登録/ Push購読を取得
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
    // サーバ側の購読解除
    await api.delete('/push/unsubscribe', { data: body });
    // ブラウザ側の購読解除
    await sub.unsubscribe();
}