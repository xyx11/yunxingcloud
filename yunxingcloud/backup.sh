#!/bin/bash
set -e
# ============================================
# yunxingcloud 数据库备份脚本
# 用法: ./backup.sh [daily|hourly|manual]
# crontab: 0 2 * * * /opt/yunxingcloud/backup.sh daily
# ============================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
source "$SCRIPT_DIR/deploy.conf" 2>/dev/null || {
    DB_HOST="localhost"
    DB_PORT="3306"
    DB_USER="root"
    DB_PASS="${DB_PASSWORD:-yunxingcloud123}"
}
DB_NAME="${DB_NAME:-yunxingcloud_core}"

BACKUP_DIR="${BACKUP_DIR:-/opt/yunxingcloud/backups}"
RETENTION_DAYS=30
MODE=${1:-manual}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="$BACKUP_DIR/${DB_NAME}_${MODE}_${TIMESTAMP}.sql.gz"

mkdir -p "$BACKUP_DIR"

echo "[$(date '+%Y-%m-%d %H:%M:%S')] 开始备份: $DB_NAME ($MODE)"

mysqldump -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" \
    --single-transaction --quick --routines --triggers \
    "$DB_NAME" 2>/dev/null | gzip > "$BACKUP_FILE"

if [ $? -eq 0 ] && [ -f "$BACKUP_FILE" ]; then
    SIZE=$(du -h "$BACKUP_FILE" | cut -f1)
    echo "  备份成功: $BACKUP_FILE ($SIZE)"

    # 清理过期备份
    DELETED=$(find "$BACKUP_DIR" -name "*.sql.gz" -mtime +$RETENTION_DAYS -delete -print | wc -l)
    [ "$DELETED" -gt 0 ] && echo "  清理了 $DELETED 个过期备份 (>${RETENTION_DAYS}天)"

    # 保留最近 7 个 daily 备份
    find "$BACKUP_DIR" -name "*_daily_*.sql.gz" | sort -r | tail -n +8 | xargs rm -f 2>/dev/null

    echo "  当前备份数: $(ls "$BACKUP_DIR"/*.sql.gz 2>/dev/null | wc -l)"
else
    echo "  备份失败！" >&2
    exit 1
fi
