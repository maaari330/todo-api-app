import { enablePush, disablePush } from '../push'; // push.tsからインポート

/** 通知の有効化・無効化ボタン表示UI（軽い装飾） */
export default function PushToggle() {
    const onEnable = async () => {
        try {
            await enablePush();
            alert('通知を有効化しました');
        } catch (e) {
            alert(e instanceof Error ? e.message : String(e));
        }
    };

    const onDisable = async () => {
        try {
            await disablePush();
            alert('通知を無効化しました');
        } catch (e) {
            alert(e instanceof Error ? e.message : String(e));
        }
    };

    return (
        <div className="inline-flex items-center gap-2">
            <button
                type="button"
                onClick={onEnable}
                className="rounded-md bg-indigo-600 px-3.5 py-2 text-sm font-medium text-white shadow-sm hover:bg-indigo-500 focus:outline-none focus-visible:ring-2 focus-visible:ring-indigo-500"
                title="ブラウザ通知を許可"
            >
                通知を有効化
            </button>
            <button
                type="button"
                onClick={onDisable}
                className="rounded-md border border-gray-300 bg-white px-3.5 py-2 text-sm font-medium text-gray-900 shadow-sm hover:bg-gray-50 focus:outline-none focus-visible:ring-2 focus-visible:ring-gray-400"
                title="ブラウザ通知を無効化"
            >
                無効化
            </button>
        </div>
    );
}