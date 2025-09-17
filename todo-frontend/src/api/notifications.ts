export type InAppMessage = { id: number; title: string; body: string; url?: string; createdAt: string; };

export async function fetchRecent(afterIso?: string, page = 0, size = 20): Promise<InAppMessage[]> {
    const q = new URLSearchParams({ page: String(page), size: String(size) });
    if (afterIso) q.set('afterIso', afterIso);
    const res = await fetch(`/notifications/in-app/recent?${q.toString()}`); // HTTP GET＋JSONパース
    if (!res.ok) throw new Error('通知取得に失敗しました');
    return res.json();
}
