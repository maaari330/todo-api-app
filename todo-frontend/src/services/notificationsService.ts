import api from '../utils/axiosConfig';

export type InAppMessage = { id: number; title: string; body: string; url?: string; createdAt: string; };

export async function fetchRecent(afterIso?: string, page = 0, size = 20): Promise<InAppMessage[]> {
    const params: any = { page, size };
    if (afterIso) params.afterIso = afterIso;
    const { data } = await api.get('/notifications/in-app/recent', { params });
    return data.content.map((m: any) => ({
        id: m.id,
        title: m.title,
        body: m.body,
        url: m.url,
        createdAt: m.createdAt,
    }));
}
