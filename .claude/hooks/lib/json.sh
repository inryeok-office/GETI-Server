#!/bin/bash
# Hook 공통 JSON 파싱 헬퍼.
# jq를 우선 사용하고, 없으면 Python으로 대체한다.
# 둘 다 없으면 경고를 남기고 빈 값을 반환해 Hook이 조용히 무동작하지 않도록 한다.

json_get() {
    local path="$1"
    if command -v jq >/dev/null 2>&1; then
        printf '%s' "$INPUT" | jq -r --arg p "$path" 'getpath($p | split("."))? // empty'
    elif command -v python >/dev/null 2>&1; then
        printf '%s' "$INPUT" | python -c '
import sys, json
try:
    data = json.load(sys.stdin)
except Exception:
    sys.exit(0)
for key in sys.argv[1].split("."):
    if not isinstance(data, dict):
        data = None
        break
    data = data.get(key)
print(data if isinstance(data, str) else "")
' "$path"
    else
        echo "[Hook] Neither jq nor python found. Hook is inactive." >&2
        return 1
    fi
}

json_get_first() {
    local value
    for path in "$@"; do
        value=$(json_get "$path")
        if [[ -n "$value" ]]; then
            printf '%s' "$value"
            return 0
        fi
    done
    return 0
}
