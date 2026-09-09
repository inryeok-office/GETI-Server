#!/bin/bash
# Hook 공통 JSON 파싱 헬퍼.
#
# jq를 우선 사용하고, 없으면 Python으로 대체한다.
# 둘 다 없으면 Fail Closed로 동작한다. 즉 통과시키지 않고 차단한다.
# 보안 Hook이 입력을 해석하지 못하는 상태에서 조용히 통과시키면,
# 보호되고 있다고 착각하게 만들 뿐 실제로는 아무 보호도 제공하지 않기 때문이다.

if command -v jq >/dev/null 2>&1; then
    JSON_PARSER="jq"
elif command -v python >/dev/null 2>&1; then
    JSON_PARSER="python"
elif command -v python3 >/dev/null 2>&1; then
    JSON_PARSER="python3"
else
    echo "[Hook] Neither jq nor python is available on PATH, so command-guard cannot inspect this call." >&2
    echo "[Hook] Blocking by default. Install jq (winget install jqlang.jq) or make python available, then retry." >&2
    exit 2
fi

json_get() {
    local path="$1"
    if [[ "$JSON_PARSER" == "jq" ]]; then
        printf '%s' "$INPUT" | jq -r --arg p "$path" 'getpath($p | split("."))? // empty'
    else
        printf '%s' "$INPUT" | "$JSON_PARSER" -c '
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
