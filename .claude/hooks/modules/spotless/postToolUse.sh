#!/bin/bash
INPUT=$(cat)
source "$(cd "$(dirname "${BASH_SOURCE[0]}")/../../lib" && pwd)/json.sh"

TOOL_NAME=$(json_get tool_name)
if [[ "$TOOL_NAME" == "Edit" ]] || [[ "$TOOL_NAME" == "Write" ]]; then
    FILE_PATH=$(json_get tool_input.file_path)
    CWD=$(json_get cwd)
    case "$FILE_PATH" in
        *.java|*.kt|*.kts|*.groovy)
            echo "[Hook] Running spotlessApply for $(basename "$FILE_PATH")" >&2
            cd "$CWD"
            if ./gradlew spotlessApply -q 2>&1; then
                echo "[Hook] spotless OK" >&2
            else
                echo "[Hook] spotless failed" >&2
            fi
            ;;
    esac
fi
exit 0
