import { enablePush, disablePush } from '../push'; //push.tsからインポート

/** 通知の有効化・無効化ボタン表示UI （※Push購読の登録/解除の実処理はpush.ts） */
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
        <div className="flex gap-2">
            <button onClick={onEnable}>通知を有効化</button>
            <button onClick={onDisable}>無効化</button>
        </div>
    );
}