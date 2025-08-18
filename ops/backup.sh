## mysqlのフルダンプ作成 ##

set -Eeuo pipefail
# パス/Compose関連の基本変数
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="${PROJECT_DIR:-$(cd "$SCRIPT_DIR/.." && pwd)}"
# docker関連の変数
COMPOSE_FILE="${COMPOSE_FILE:-${PROJECT_DIR}/docker-compose.yml}"
APP_SERVICE="${APP_SERVICE:-app}"
DB_SERVICE="${DB_SERVICE:-db}"
# ローカルにおけるバックアップ先と保持日数
BACKUP_ROOT="${BACKUP_ROOT:-${PROJECT_DIR}/backups}"
RETENTION_DAYS="${RETENTION_DAYS:-1}"
# アプリログの取得範囲
ENABLE_APP_LOG_BACKUP="${ENABLE_APP_LOG_BACKUP:-1}"
APP_LOG_SINCE="${APP_LOG_SINCE:-24h}" # 直近24時間分のログを対象
APP_LOG_TAIL="${APP_LOG_TAIL:-50000}"


# 圧縮コマンドの選択（zstd優先）
if command -v zstd >/dev/null 2>&1; then
  COMPRESS_DATA='zstd -19 --threads=0'; DATA_EXT=zst
  COMPRESS_LOGS='zstd -5 --threads=0';  LOG_EXT=zst
else
  COMPRESS_DATA='gzip -9'; DATA_EXT=gz
  COMPRESS_LOGS='gzip -6'; LOG_EXT=gz
fi

# rcloneの挙動設定
RCLONE_UPLOAD="${RCLONE_UPLOAD:-1}"
RCLONE_REMOTE="${RCLONE_REMOTE:-gcrypt:todo-backups}"
DELETE_LOCAL_AFTER_UPLOAD="${DELETE_LOCAL_AFTER_UPLOAD:-1}"
REMOTE_RETENTION_DAYS="${REMOTE_RETENTION_DAYS:-}" # クラウド側の保持日数

# docker composeをdc()で呼ぶための関数作成
dc() { docker compose -f "$COMPOSE_FILE" "$@"; }
if [[ -f "${PROJECT_DIR}/.env" ]]; then set -a; source "${PROJECT_DIR}/.env"; set +a; fi

# 日付名のディレクトリを作成
DATE_TAG="$(date +%Y%m%d_%H%M%S)"
DAY_DIR="$(date +%Y-%m-%d)"
OUT_DIR="${BACKUP_ROOT}/${DAY_DIR}"
mkdir -p "${OUT_DIR}"

# アプリ用コンテナ (app) と DB コンテナ (db) が稼働しているか確認
APP_CID="$(dc ps -q "${APP_SERVICE}" || true)"
DB_CID="$(dc ps -q "${DB_SERVICE}" || true)"
if [[ -n "${DB_CID}" ]]; then
  DB_NAME="${MYSQL_DATABASE:-todo}"
  DB_USER="${SPRING_DATASOURCE_USERNAME:-root}"
  DB_PASS="${SPRING_DATASOURCE_PASSWORD:-${MYSQL_ROOT_PASSWORD:-}}"
  [[ -z "${DB_PASS}" ]] && { echo "[backup] ERROR: DBパスワード未設定"; exit 1; }
# フルダンプ（mysqldump）の作成
  dc exec -T -e MYSQL_PWD="${DB_PASS}" "${DB_SERVICE}" sh -lc \
    "mysqldump --single-transaction --quick --routines --triggers --events \
     --no-tablespaces --column-statistics=0 \
     --databases \"${DB_NAME}\" -u \"${DB_USER}\"" \
  | ${COMPRESS_DATA} > "${OUT_DIR}/mysql.${DB_NAME}.${DATE_TAG}.sql.${DATA_EXT}"
  [[ -s "${OUT_DIR}/mysql.${DB_NAME}.${DATE_TAG}.sql.${DATA_EXT}" ]] || { echo "[backup] ERROR: dump empty"; exit 1; } # 上記で作成したファイルが存在するか確認
fi

# rcloneでクラウドアップロード → ローカルの元データ削除 → クラウド上の古いログを削除
if [[ "${RCLONE_UPLOAD}" == "1" ]]; then
  rclone copy "${OUT_DIR}" "${RCLONE_REMOTE%/}/${DAY_DIR}/" # クラウドにコピー
  if [[ "${DELETE_LOCAL_AFTER_UPLOAD}" == "1" ]]; then rm -rf "${OUT_DIR}"; fi
  if [[ -n "${REMOTE_RETENTION_DAYS}" ]]; then
    rclone delete "${RCLONE_REMOTE%/}/" --min-age "${REMOTE_RETENTION_DAYS}d" \  # 最終更新時刻（modtime）が N 日以上前のファイル
      --include "*/mysql.*.sql.${DATA_EXT}" --include "*/app.log.*.${LOG_EXT}" || true
    rclone rmdirs "${RCLONE_REMOTE%/}/" --leave-root || true
  fi
fi

# 1日より古いローカルファイルを削除
find "${BACKUP_ROOT}" -type f -mtime +"${RETENTION_DAYS}" -print -delete | sed 's/^/[prune-local] /' || true
echo "[backup] done: ${DAY_DIR}"
