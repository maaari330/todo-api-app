set -Eeuo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="${PROJECT_DIR:-$(cd "$SCRIPT_DIR/.." && pwd)}"
# docker関連の変数
COMPOSE_FILE="${COMPOSE_FILE:-${PROJECT_DIR}/docker-compose.yml}"
DB_SERVICE="${DB_SERVICE:-db}"
dc() { docker compose -f "$COMPOSE_FILE" "$@"; }
# rclone接続先クラウド
RCLONE_REMOTE="${RCLONE_REMOTE:-gcrypt:todo-backups}"
# ローカルの一時作業用ディレクトリ
TMP_DIR="${TMP_DIR:-${PROJECT_DIR}/.tmp/binlog}"
# STATE_FILE は「送信済み binlog のファイル名を記録」する台帳
STATE_FILE="${STATE_FILE:-${PROJECT_DIR}/ops/.binlog_uploaded.list}"  
mkdir -p "${TMP_DIR}" "$(dirname "${STATE_FILE}")"; touch "${STATE_FILE}"

# .env の読み込みと DB 接続情報の決定
if [[ -f "${PROJECT_DIR}/.env" ]]; then set -a; source "${PROJECT_DIR}/.env"; set +a; fi
DB_PASS="${SPRING_DATASOURCE_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}"
DB_USER="${SPRING_DATASOURCE_USERNAME:-root}"
# 現在の binlog を切り上げて新しいファイルへ切替 → 直前までの binlog が “確定” し
dc exec -T -e MYSQL_PWD="${DB_PASS}" "${DB_SERVICE}" mysql -u"${DB_USER}" -e "FLUSH BINARY LOGS;"
# binlog 一覧取得 → “最新以外” を候補に抽出
MAP=$(dc exec -T -e MYSQL_PWD="${DB_PASS}" "${DB_SERVICE}" mysql -N -u"${DB_USER}" -e "SHOW BINARY LOGS;" | awk '{print $1}')
readarray -t LOGS <<<"$MAP"
(( ${#LOGS[@]} <= 1 )) && { echo "[binlog] nothing to ship"; exit 0; }
PENDING=("${LOGS[@]:0:${#LOGS[@]}-1}") # 配列の 最後の要素が“現在の活きてる binlog” なので除外
NEW=()
# STATE_FILE台帳とPENDINGを突合 → 台帳になければ未送信としてNEWに追加
for L in "${PENDING[@]}"; do grep -qx "$L" "${STATE_FILE}" || NEW+=("$L"); done
(( ${#NEW[@]} == 0 )) && { echo "[binlog] no new logs"; exit 0; }

# 一時出力ディレクトリ作成
DATE_TAG="$(date +%Y%m%d_%H%M%S)"; DAY_DIR="$(date +%Y-%m-%d)"; OUT_DIR="${TMP_DIR}/${DAY_DIR}"
mkdir -p "${OUT_DIR}"
# 取り出してtar.gzに圧縮　ローカル（OUT_DIR）に一時保存
for L in "${NEW[@]}"; do
  echo "[binlog] ship $L"
  dc exec -T "${DB_SERVICE}" sh -lc "tar -C /var/lib/mysql -czf - ${L}" > "${OUT_DIR}/${L}.tar.gz"
done

# クラウドへのアップロード
rclone copy "${OUT_DIR}" "${RCLONE_REMOTE%/}/binlog/${DAY_DIR}/"
# クラウド側の古い binlog を削除（72h 既定、環境変数で調整可）
BINLOG_REMOTE_RETENTION_HOURS="${BINLOG_REMOTE_RETENTION_HOURS:-72}"
rclone delete "${RCLONE_REMOTE%/}/binlog/" \
  --min-age "${BINLOG_REMOTE_RETENTION_HOURS}h" \  #その時間より“古い”ファイルのみ削除対象 
  --include "*/binlog.*.tar.gz" \
  --include "*/mysql-bin.*.tar.gz" || true
rclone rmdirs "${RCLONE_REMOTE%/}/binlog/" --leave-root || true
# STATE_FILE台帳の更新、一時保存用ディレクトリ（OUT_DIR）の削除
for L in "${NEW[@]}"; do echo "$L" >> "${STATE_FILE}"; done
rm -rf "${OUT_DIR}"
echo "[binlog] done"
