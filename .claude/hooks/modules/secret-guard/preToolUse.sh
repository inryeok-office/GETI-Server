#!/bin/bash
INPUT=$(cat)
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/../../lib" && pwd)/json.sh"

TOOL_NAME=$(json_get tool_name)
if [[ "$TOOL_NAME" == "Write" ]] || [[ "$TOOL_NAME" == "Edit" ]]; then
    CONTENT=$(json_get_first tool_input.content tool_input.new_string)
    FILE_PATH=$(json_get tool_input.file_path)
    if [[ "$FILE_PATH" == *.env* ]] || [[ "$(basename "$FILE_PATH")" == ".env" ]]; then
        exit 0
    fi
    PATTERNS=(
        "AKIA[0-9A-Z]{16}"
        "ghp_[A-Za-z0-9]{36}"
        "ghs_[A-Za-z0-9]{36}"
        "github_pat_[A-Za-z0-9_]{82}"
        "sk-[A-Za-z0-9]{48}"
        "sk-proj-[A-Za-z0-9_-]{50,}"
        "-----BEGIN[[:space:]]*(RSA[[:space:]]*|EC[[:space:]]*|OPENSSH[[:space:]]*)?PRIVATE KEY-----"
        "xox[baprs]-[A-Za-z0-9-]+"
    )
    for pattern in "${PATTERNS[@]}"; do
        if echo "$CONTENT" | grep -qE "$pattern"; then
            echo "[Hook] Potential secret detected in $(basename "$FILE_PATH"). Pattern: $pattern" >&2
            echo "Possible secret or credential detected in the file content. Review before writing."
            exit 2
        fi
    done
fi
exit 0
