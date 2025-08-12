/**
 * 受け取った文字列（yyyy-MM-dd, yyyy-MM-ddTHH:mm, yyyy-MM-ddTHH:mm:ss）
 * を必ず "yyyy-MM-ddTHH:mm:ss" 形式に揃えて返す
 */
export function normalizeLocalDateTime(s) {
  if (!s) return undefined;
  // 日付だけ
  if (/^\d{4}-\d{2}-\d{2}$/.test(s)) {
    return `${s}T00:00:00`;
  }
  // 日付＋時分
  if (/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}$/.test(s)) {
    return `${s}:00`;
  }
  // すでに秒まであるならそのまま
  return s;
}