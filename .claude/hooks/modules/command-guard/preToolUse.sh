#!/bin/bash
INPUT=$(cat)
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/../../lib" && pwd)/json.sh"

TOOL_NAME=$(json_get tool_name)
[[ "$TOOL_NAME" == "Bash" ]] || exit 0

COMMAND=$(json_get tool_input.command)
[[ -n "$COMMAND" ]] || exit 0

# .claude/settings.json 의 permissions.deny 와 같은 규칙을 Bash 실행 단계에서 강제한다.
# settings.json 의 Read(...) 규칙은 Read 도구만 막으므로 Bash 우회 경로를 여기서 차단한다.
#
# 한계: 이 Hook은 Defense in Depth 수단이며 보안 경계가 아니다.
# 명령 문자열 검사이므로 변수 조합, Base64 Decode, 다른 언어 Runtime을 경유한 우회는 막지 못한다.
# 실수로 인한 사고를 줄이는 것이 목적이며, 실제 신뢰 경계는 Bash 도구 권한 자체로 관리해야 한다.
BLOCKED_PATTERNS=(
    # 시스템 파괴
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

)

# rm 재귀 강제 삭제: -r/-R/--recursive 와 -f/--force 가 어떻게 나뉘어 표기되든 차단한다.
# (rm -rf / rm -fr / rm -r -f / rm --recursive --force / /bin/rm -rf 등)
if [[ "$COMMAND" =~ (^|[[:space:]\;\&\|\(])(/[^[:space:]]*/)?rm([[:space:]]|$) ]]; then
    HAS_RECURSIVE=0
    HAS_FORCE=0
    if [[ "$COMMAND" =~ (^|[[:space:]])-[a-zA-Z]*[rR][a-zA-Z]*([[:space:]]|$) || "$COMMAND" == *--recursive* ]]; then
        HAS_RECURSIVE=1
    fi
    if [[ "$COMMAND" =~ (^|[[:space:]])-[a-zA-Z]*f[a-zA-Z]*([[:space:]]|$) || "$COMMAND" == *--force* ]]; then
        HAS_FORCE=1
    fi
    if [[ $HAS_RECURSIVE -eq 1 && $HAS_FORCE -eq 1 ]]; then
        echo "[Hook] Blocked by command-guard: $COMMAND" >&2
        echo "[Hook] Matched rule: recursive forced delete (rm with -r and -f in any notation)" >&2
        exit 2
    fi
fi

# Secret 파일은 실행 명령 이름이 아니라 '경로가 명령에 등장하는지'로 판단한다.
# cat/head 만 열거하면 grep, sed, awk, cp, scp, tar 등으로 곧바로 우회되기 때문이다.
# 견본 파일(.env.example 등)은 Secret이 아니므로 검사 전에 제거한다.
SCRUBBED="$COMMAND"
for sample in ".env.example" ".env.sample" ".env.template" ".env.dist"; do
    SCRUBBED=${SCRUBBED//$sample/}
done

SECRET_PATH_PATTERNS=(
    "(^|[[:space:]=/:])[^[:space:]]*\.env([[:space:]]|;|\||&|$)"
    "\.env\.(local|production|prod|secret)"
    "(^|[[:space:]=/:])[^[:space:]]*\.(pem|key|p12|jks)([[:space:]]|;|\||&|$)"
)

for pattern in "${SECRET_PATH_PATTERNS[@]}"; do
    if [[ "$SCRUBBED" =~ $pattern ]]; then
        echo "[Hook] Blocked by command-guard: $COMMAND" >&2
        echo "[Hook] Matched rule: secret file path referenced in command ($pattern)" >&2
        exit 2
    fi
done

for pattern in "${BLOCKED_PATTERNS[@]}"; do
    if [[ "$COMMAND" =~ $pattern ]]; then
        echo "[Hook] Blocked by command-guard: $COMMAND" >&2
        echo "[Hook] Matched rule: $pattern" >&2
        exit 2
    fi
done

exit 0
