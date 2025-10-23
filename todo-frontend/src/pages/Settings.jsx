// src/pages/Settings.jsx
import PushToggle from '../components/PushToggle';

export default function Settings() {
    return (
        <div classname="text-lg font-semibold">
            <h2>通知設定</h2>
            <p classname="text-sm text-gray-600">
                ブラウザの通知権限と Push 購読をここで切り替えできます。
            </p>
            <PushToggle />
        </div >
    );
}
