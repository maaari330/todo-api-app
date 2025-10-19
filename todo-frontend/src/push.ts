export const VAPID_PUBLIC_KEY = process.env.REACT_APP_VAPID_PUBLIC_KEY as string;

function urlBase64ToUint8Array(base64String: string) {
    const padding = "=".repeat((4 - (base64String.length % 4)) % 4);
    const base64 = (base64String + padding).replace(/-/g, "+").replace(/_/g, "/");
    const raw = atob(base64);
    const out = new Uint8Array(raw.length);
    for (let i = 0; i < raw.length; ++i) out[i] = raw.charCodeAt(i);
    return out;
}

function assertEnv() {
    if (!VAPID_PUBLIC_KEY) {
        // ビルド時に埋め込まれるので、ここが走るなら .env が読まれていない
        throw new Error("REACT_APP_VAPID_PUBLIC_KEY is not set");
    }
}

/** 購読を作成してサーバへ登録（endpoint/p256dh/authをそのまま送る） */
export async function ensureSubscription() {
    assertEnv();
    if (!("serviceWorker" in navigator) || !("PushManager" in window)) {
        throw new Error("Push not supported");
    }

    // sw.js は public/ に配置（/sw.jsで配信されること）
    const reg = await navigator.serviceWorker.register("/sw.js", { scope: "/" });
    await navigator.serviceWorker.ready;

    let sub = await reg.pushManager.getSubscription();
    if (!sub) {
        sub = await reg.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: urlBase64ToUint8Array(VAPID_PUBLIC_KEY),
        });
    }

    const subJson = sub.toJSON() as any;
    const body = {
        endpoint: sub.endpoint,
        keys: { p256dh: subJson.keys.p256dh, auth: subJson.keys.auth },
    };

    await fetch("/api/push/subscribe", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(body),
    });

    return sub;
}

/** 購読解除（サーバとブラウザ両方） */
export async function unsubscribePush() {
    const reg = await navigator.serviceWorker.ready;
    const sub = await reg.pushManager.getSubscription();
    if (sub) {
        try {
            await fetch(`/api/push/subscribe?endpoint=${encodeURIComponent(sub.endpoint)}`, { method: "DELETE" });
        } finally {
            await sub.unsubscribe();
        }
    }
}