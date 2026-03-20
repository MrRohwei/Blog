#!/usr/bin/env bash
set -euo pipefail

APP_HOME="${VOIDPEN_APP_HOME:-/opt/voidpen}"
SERVER_HOME="${APP_HOME}/server"
PID_FILE="${VOIDPEN_PID_FILE:-${SERVER_HOME}/app.pid}"

if [[ ! -f "${PID_FILE}" ]]; then
  echo "No PID file found: ${PID_FILE}"
  exit 0
fi

PID="$(cat "${PID_FILE}")"
if [[ -z "${PID}" ]]; then
  echo "PID file is empty, removing: ${PID_FILE}"
  rm -f "${PID_FILE}"
  exit 0
fi

if ! kill -0 "${PID}" 2>/dev/null; then
  echo "Process ${PID} is not running, cleaning PID file."
  rm -f "${PID_FILE}"
  exit 0
fi

kill "${PID}"
for _ in {1..20}; do
  if ! kill -0 "${PID}" 2>/dev/null; then
    rm -f "${PID_FILE}"
    echo "Voidpen server stopped (PID=${PID})"
    exit 0
  fi
  sleep 1
done

echo "Graceful stop timed out, force killing PID=${PID}"
kill -9 "${PID}" 2>/dev/null || true
rm -f "${PID_FILE}"
echo "Voidpen server stopped (force)"
