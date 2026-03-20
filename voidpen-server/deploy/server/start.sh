#!/usr/bin/env bash
set -euo pipefail

APP_HOME="${VOIDPEN_APP_HOME:-/opt/voidpen}"
SERVER_HOME="${APP_HOME}/server"
LOG_HOME="${APP_HOME}/logs"
JAR_PATH="${VOIDPEN_JAR_PATH:-${SERVER_HOME}/voidpen-server.jar}"
PID_FILE="${VOIDPEN_PID_FILE:-${SERVER_HOME}/app.pid}"
ENV_FILE="${VOIDPEN_ENV_FILE:-${SERVER_HOME}/.env}"
LOG_FILE="${VOIDPEN_LOG_FILE:-${LOG_HOME}/app.log}"
SERVER_PORT="${VOIDPEN_SERVER_PORT:-8080}"

mkdir -p "${SERVER_HOME}" "${LOG_HOME}"

if [[ -f "${ENV_FILE}" ]]; then
  # shellcheck source=/dev/null
  set -a
  source "${ENV_FILE}"
  set +a
fi

if [[ -f "${PID_FILE}" ]]; then
  OLD_PID="$(cat "${PID_FILE}")"
  if [[ -n "${OLD_PID}" ]] && kill -0 "${OLD_PID}" 2>/dev/null; then
    echo "Voidpen server is already running (PID=${OLD_PID})"
    exit 0
  fi
  rm -f "${PID_FILE}"
fi

if [[ ! -f "${JAR_PATH}" ]]; then
  echo "JAR not found: ${JAR_PATH}"
  exit 1
fi

nohup java -jar "${JAR_PATH}" \
  --spring.profiles.active=prod \
  --server.port="${SERVER_PORT}" \
  > "${LOG_FILE}" 2>&1 &

NEW_PID="$!"
echo "${NEW_PID}" > "${PID_FILE}"
echo "Voidpen server started (PID=${NEW_PID})"
echo "Log file: ${LOG_FILE}"
