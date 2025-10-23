import api from './utils/axiosConfig';

/** VAPID公開鍵をAPIから取得する */
async function getVapidPublicKey(): Promise<string> {
    const { data } = await api.get('/push/public-key');
    return data;
}

function urlBase64ToUint8Array(base64String: string) {
    const padding = "=".repeat((4 - (base64String.length % 4)) % 4);
    const base64 = (base64String + padding).replace(/-/g, "+").replace(/_/g, "/");
    const raw = atob(base64);
    const out = new Uint8Array(raw.length);
    for (let i = 0; i < raw.length; ++i) out[i] = raw.charCodeAt(i);
    return out;
}

/** 購読を作成してサーバへ登録（endpoint/p256dh/authをそのまま送る） */
export async function ensureSubscription() {
    if (!("serviceWorker" in navigator) || !("PushManager" in window)) {
        throw new Error("Push not supported");
    }
    const publicKey = await getVapidPublicKey();

    // Service Worker登録
    const reg = await navigator.serviceWorker.register('/sw.js', { scope: '/' });
    await navigator.serviceWorker.ready;
    let sub = await reg.pushManager.getSubscription();
    if (!sub) {
        sub = await reg.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: urlBase64ToUint8Array(publicKey),
        });
    }

    // 登録情報をバックエンドへ送る
    const subJson = sub.toJSON() as any;
    const body = {
        endpoint: sub.endpoint,
        keys: { p256dh: subJson.keys.p256dh, auth: subJson.keys.auth },
    };
    await api.post('/push/subscribe', {
        endpoint: sub.endpoint,
        p256dh: subJson.keys.p256dh,
        auth: subJson.keys.auth,
        userAgent: navigator.userAgent,
    });
    return sub;
}

/** 購読解除（サーバとブラウザ両方） */
export async function unsubscribePush() {
    const reg = await navigator.serviceWorker.ready;
    const sub = await reg.pushManager.getSubscription();
    if (sub) {
        try {
            await api.delete('/push/subscribe', { params: { endpoint: sub.endpoint } });
        } finally {
            await sub.unsubscribe();
        }
    }
}