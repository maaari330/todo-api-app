/* eslint-env serviceworker, es2020 */

/** 
 * Service Worker本体 
 * ブラウザの“裏側”でページとは独立して動くため、タブを閉じていても処理できる
 * WebPushSender.java（バックエンド）からjsonを受領したら実行される
 * */

self.addEventListener('install', () => self.skipWaiting());
self.addEventListener('activate', (e) => e.waitUntil(self.clients.claim()));

// 1) プッシュ通知
globalThis.addEventListener('push', (event) => {
    let raw = '';
    try { raw = event.data ? event.data.text() : ''; } catch { }
    // ページへも通知（通常のConsoleで見える）
    event.waitUntil((async () => {
        const clients = await globalThis.clients.matchAll({ type: 'window', includeUncontrolled: true });
        for (const c of clients) c.postMessage({ type: 'PUSH_DEBUG', raw });
    })());
    const data = (() => {
        try { return event.data?.json() || {}; } catch { return {}; }
    })(); // サーバが WebPushSender.java で送ったペイロード（JSON）を受け取る

    const baseTitle = data.title || '通知';
    const title = data.appName ? `${data.appName} - ${baseTitle}` : baseTitle;
    const options = {
        body: data.body || '',
        icon: '/icons/icon-192x192.png',
        data: { url: data.url || '/', todoId: data.todoId }, // notificationclick で「クリックしたら開く遷移先URL」を持たせる
        tag: data.tag || (data.todoId ? `todo-${data.todoId}` : 'todo-general'),
        renotify: data.renotify ?? true,
    };
    // showNotification(...) を呼んで通知を表示する +　上記のPromiseによりtitle, optionsが作成完了するのをwaitUntilで待つ
    event.waitUntil(globalThis.registration.showNotification(title, options));
});


// 2) 通知をクリックした時のハンドリング（showNotification から発出）
globalThis.addEventListener('notificationclick', (event) => {
    event.notification.close();
    const url = event.notification.data?.url || '/';
    event.waitUntil((async () => {
        // 現在開いているタブ全てを取得
        const all = await globalThis.clients.matchAll({ type: 'window', includeUncontrolled: true });
        // 上記urlと同じサイト配下のタブがあれば取得
        const hit = all.find(c => 'navigate' in c && c.url.includes(globalThis.registration.scope));
        if (hit) { hit.focus(); hit.navigate(url); }
        else { globalThis.clients.openWindow(url); } // 該当タブが無ければ、新しいタブ/ウィンドウで url を開く
    })());
});