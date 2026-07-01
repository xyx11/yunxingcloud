#!/bin/bash
set -e
# ============================================
# yunxingcloud 全库备份脚本 (6 独立数据库)
# 用法:
#   ./backup-all.sh            # 本地备份
#   ./backup-all.sh sync       # 备份 + 异地同步
#   ./backup-all.sh restore YYYYMMDD_HHMMSS  # 恢复指定备份
#
# crontab: 0 2 * * * /opt/yunxingcloud/backup-all.sh sync
# ============================================

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
BACKUP_DIR="${BACKUP_DIR:-$SCRIPT_DIR/backups}"
REMOTE="${REMOTE_BACKUP:-}"  # 异地备份目标: user@host:/path
RETENTION_DAYS=${RETENTION_DAYS:-7}
TIMESTAMP=$(date +%Y%m%d_%H%M%S)

# 6 个数据库
DBS=(
    "yunxingcloud_core"
    "yunxingcloud_usercenter"
    "yunxingcloud_payment"
    "yunxingcloud_order"
    "yunxingcloud_inventory"
    "yunxingcloud_core"
)

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
info()  { echo -e "${GREEN}[INFO]${NC}  $1"; }
warn()  { echo -e "${YELLOW}[WARN]${NC}  $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; exit 1; }

# ---- 备份 ----
backup() {
    mkdir -p "$BACKUP_DIR/$TIMESTAMP"

    for db in "${DBS[@]}"; do
        info "备份 $db..."
        mysqldump --single-transaction --quick --routines --triggers \
            "$db" 2>/dev/null | gzip > "$BACKUP_DIR/$TIMESTAMP/${db}.sql.gz"
        info "  → ${db}.sql.gz ($(du -h "$BACKUP_DIR/$TIMESTAMP/${db}.sql.gz" | cut -f1))"
    done

    # 打包
    tar -czf "$BACKUP_DIR/yunxingcloud-backup-$TIMESTAMP.tar.gz" \
        -C "$BACKUP_DIR" "$TIMESTAMP/"
    rm -rf "$BACKUP_DIR/$TIMESTAMP"
    info "备份完成: yunxingcloud-backup-$TIMESTAMP.tar.gz"
    echo "$TIMESTAMP" > "$BACKUP_DIR/LATEST"
}

# ---- 清理过期备份 ----
cleanup() {
    info "清理 ${RETENTION_DAYS} 天前的备份..."
    find "$BACKUP_DIR" -name "yunxingcloud-backup-*.tar.gz" -mtime "+$RETENTION_DAYS" -delete
    info "清理完成"
}

# ---- 异地同步 ----
sync_remote() {
    if [ -z "$REMOTE" ]; then
        warn "未配置 REMOTE_BACKUP，跳过异地同步"
        return
    fi
    info "同步到 $REMOTE ..."
    rsync -avz --delete "$BACKUP_DIR/" "$REMOTE/"
    info "同步完成"
}

# ---- 恢复 ----
restore() {
    local ts=$1
    local archive="$BACKUP_DIR/yunxingcloud-backup-$ts.tar.gz"
    [ -f "$archive" ] || error "备份不存在: $archive"

    warn "即将恢复数据库到 $ts 状态"
    warn "当前数据将被覆盖！"
    read -p "输入 YES 确认: " confirm
    [ "$confirm" != "YES" ] && error "已取消"

    local tmp="$BACKUP_DIR/restore_tmp"
    mkdir -p "$tmp"
    tar -xzf "$archive" -C "$tmp"

    for db in "${DBS[@]}"; do
        if [ -f "$tmp/$ts/${db}.sql.gz" ]; then
            info "恢复 $db..."
            gunzip < "$tmp/$ts/${db}.sql.gz" | mysql "$db"
        else
            warn "跳过 $db (无备份)"
        fi
    done

    rm -rf "$tmp"
    info "恢复完成"
}

# ============================================
CMD=${1:-backup}
case "$CMD" in
    backup)  backup; cleanup ;;
    sync)    backup; cleanup; sync_remote ;;
    restore) restore "$2" ;;
    list)    ls -lh "$BACKUP_DIR/yunxingcloud-backup-"*.tar.gz 2>/dev/null || echo "无备份文件" ;;
    *)
        echo "用法: $0 {backup|sync|restore <ts>|list}"
        echo ""
        echo "  backup           本地备份 6 数据库"
        echo "  sync             备份 + 异地同步"
        echo "  restore <ts>     恢复指定时间点备份"
        echo "  list             列出所有备份"
        echo ""
        echo "  环境变量:"
        echo "    BACKUP_DIR       备份目录 (默认 ./backups)"
        echo "    REMOTE_BACKUP    异地目标 (user@host:/path)"
        echo "    RETENTION_DAYS   保留天数 (默认 7)"
        exit 1
        ;;
esac