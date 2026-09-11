package team.inreok.getiserver.domain.notification.entity.type

/**
 * 인앱 알림의 종류다. 원본 요구사항 문서("GETI Notification 도메인 개발 요구사항") 3절이 제시한
 * 15개 값을 그대로 채택했다 — 같은 문서 25절 7번이 "실제 Notification Type 전체 목록"을 아직
 * 확정되지 않은 정책으로 남겼지만, 현재 저장소와 문서를 통틀어 이 목록이 유일한 근거이고 5절의
 * 예상 Event 목록과 거의 1:1로 대응한다(DECISION_REQUIRED로 보고).
 *
 * 이번 Notification Core 범위에는 알림 생성자(Domain Event 수신)가 없어 실제로 만들어지는 값이
 * 아직 없다. Event 연결 시점에 값이 더 필요해지면 추가한다(Enum 값 추가는 응답 계약에 하위 호환).
 *
 * [PORTFOLIO_REQUEST_PUBLISHED]는 위 15개 목록에 없던 값을 Issue #331에서 추가한 것이다 --
 * 원본 문서의 목록은 Portfolio 도메인이 저장소에 들어오기 전에 작성됐고, 같은 문서 25절 7번이
 * 목록 자체를 미확정으로 남겨 두었다. `notifications.type`은 `VARCHAR(100)`에 CHECK 제약이
 * 없어(V16 Migration) 값 추가에 Schema 변경이 필요하지 않다.
 */
enum class NotificationType {
    JOB_PUBLISHED,
    JOB_UPDATED,
    JOB_CLOSED,
    JOB_DELETED,
    JOB_APPLICATION_STATUS_CHANGED,
    PROGRAM_PUBLISHED,
    PROGRAM_UPDATED,
    PROGRAM_CLOSED,
    PROGRAM_DELETED,
    PROGRAM_APPLICATION_APPLIED,
    PROGRAM_APPLICATION_CANCELED,
    PROGRAM_VACANCY_AVAILABLE,
    INQUIRY_ANSWERED,
    MEMBER_APPROVAL_RESULT,
    PORTFOLIO_REQUEST_PUBLISHED,
    SYSTEM,
}
