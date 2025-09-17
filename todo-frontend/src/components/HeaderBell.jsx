import { useEffect, useRef, useState } from 'react';
import InAppList from './InAppList';

export default function HeaderBell({ keepMounted = false }) {
    const [open, setOpen] = useState(false);
    const btnRef = useRef(null);
    const panelRef = useRef(null);

    useEffect(() => {
        function onKey(e) { if (e.key === 'Escape') setOpen(false); }
        function onDown(e) {
            if (!open) return;
            const t = e.target;
            if (panelRef.current && !panelRef.current.contains(t) &&
                btnRef.current && !btnRef.current.contains(t)) {
                setOpen(false);
            }
        }
        document.addEventListener('keydown', onKey);
        document.addEventListener('mousedown', onDown);
        return () => {
            document.removeEventListener('keydown', onKey);
            document.removeEventListener('mousedown', onDown);
        };
    }, [open]);

    return (
        <div className="relative">
            <button
                ref={btnRef}
                type="button"
                aria-haspopup="dialog"
                aria-expanded={open}
                onClick={() => setOpen(o => !o)}
                className="relative inline-flex h-9 w-9 items-center justify-center rounded-full hover:bg-gray-100 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
                {/* シンプルなベルSVG（lucide等が無くてもOK） */}
                <svg viewBox="0 0 24 24" className="h-5 w-5" aria-hidden="true" fill="none" stroke="currentColor" strokeWidth="2">
                    <path d="M12 22a2 2 0 0 0 2-2H10a2 2 0 0 0 2 2ZM18 8a6 6 0 1 0-12 0c0 7-3 7-3 8h18c0-1-3-1-3-8Z" />
                </svg>
                <span className="sr-only">通知</span>
            </button>

            {(open || keepMounted) && (
                <div
                    ref={panelRef}
                    role="dialog"
                    aria-label="最近の通知"
                    className={`absolute right-0 mt-2 w-[360px] max-w-[calc(100vw-1rem)] rounded-2xl border border-gray-200 bg-white shadow-xl transition z-50
            ${open ? 'opacity-100 translate-y-0' : 'pointer-events-none opacity-0 -translate-y-1'}`}
                >
                    <div className="p-3">
                        <h3 className="mb-2 text-sm font-medium text-gray-700">最近の通知</h3>
                        <div className="max-h-80 overflow-auto">
                            {open || keepMounted ? <InAppList /> : null}
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
