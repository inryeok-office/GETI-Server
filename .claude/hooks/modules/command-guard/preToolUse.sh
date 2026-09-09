#!/bin/bash
INPUT=$(cat)
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/../../lib" && pwd)/json.sh"

TOOL_NAME=$(json_get tool_name)
[[ "$TOOL_NAME" == "Bash" ]] || exit 0

COMMAND=$(json_get tool_input.command)
[[ -n "$COMMAND" ]] || exit 0

# .claude/settings.json 의 permissions.deny 와 같은 규칙을 Bash 실행 단계에서 강제한다.
# settings.json 의 Read(...) 규칙은 Read 도구만 막으므로, cat/head 등 우회 경로를 여기서 차단한다.
BLOCKED_PATTERNS=(
    # 시스템 파괴
    "rm[[:space:]]+-[a-zA-Z]*r[a-zA-Z]*f"
    "rm[[:space:]]+-[a-zA-Z]*f[a-zA-Z]*r"
    "sudo rm"
    "> /dev/"
    "dd if="
    "mkfs"
    "curl.*\|[[:space:]]*sh"
    "wget.*\|[[:space:]]*sh"

    # AGENTS.md '파괴적 명령 제한'
    "git[[:space:]]+reset[[:space:]]+--hard"
    "git[[:space:]]+clean[[:space:]]+-[a-zA-Z]*[fd]"
    "git[[:space:]]+push[[:space:]]+.*--force"
    "git[[:space:]]+push[[:space:]]+.*(^|[[:space:]])-f([[:space:]]|$)"
    "docker[[:space:]]+compose[[:space:]]+down[[:space:]]+.*-v([[:space:]]|$)"

    # Secret 파일 내용 출력 (.env.example 은 견본이므로 허용)
    "(cat|head|tail|less|more|bat|nl|od|xxd|strings)[[:space:]]+[^|;]*\.env([[:space:]]|$)"
    "(cat|head|tail|less|more|bat|nl|od|xxd|strings)[[:space:]]+[^|;]*\.env\.(local|production|prod)"
    "(cat|head|tail|less|more|bat|nl|od|xxd|strings)[[:space:]]+[^|;]*\.(pem|key|p12|jks)([[:space:]]|$)"
)

for pattern in "${BLOCKED_PATTERNS[@]}"; do
    if [[ "$COMMAND" =~ $pattern ]]; then
        echo "[Hook] Blocked by command-guard: $COMMAND" >&2
        echo "[Hook] Matched rule: $pattern" >&2
        exit 2
    fi
done

exit 0
