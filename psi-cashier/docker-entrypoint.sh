#!/bin/sh
set -e

# 如果终端数据目录没有 myp.db，则从模板初始化一份
if [ ! -f /app/data/myp.db ]; then
  echo "[psi-cashier] 初始化终端 SQLite 数据库 /app/data/myp.db ..."
  mkdir -p /app/data
  cp /app/myp-template.db /app/data/myp.db
fi

# 支持通过环境变量传入终端/门店标识
: "${TERMINAL_ID:=T01}"
: "${STORE_ID:=S001}"

export TERMINAL_ID
export STORE_ID

exec java \
  -Dspring.profiles.active=docker \
  -Dpsi.cashier.terminal-id="${TERMINAL_ID}" \
  -Dpsi.cashier.store-id="${STORE_ID}" \
  -jar /app/app.jar
