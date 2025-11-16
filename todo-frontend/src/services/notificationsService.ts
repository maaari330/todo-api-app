import api from '../utils/axiosConfig';

// バックエンドの InAppMessage に合わせた型
export type InAppMessage = {
  id: number;
  title: string;
  body: string;
  url?: string;
  createdAt: string; // Instant が ISO 文字列で返ってくる想定
};

// バックエンドの Paged<T> に対応
export type Paged<T> = {
  content: T[];
  hasNext: boolean;
};

// 「最近の通知」を取得する関数
export async function fetchRecent(
  afterIso?: string,
  page = 0,
  size = 20
): Promise<Paged<InAppMessage>> {
  const params: any = { page, size };
  if (afterIso) params.afterIso = afterIso;

  const { data } = await api.get<Paged<InAppMessage>>(
    '/notifications/in-app/recent',
    { params }
  );

  console.log('fetchRecent response:', data);

  return data; 
}