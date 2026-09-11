package team.inreok.getiserver.domain.notification.service

import team.inreok.getiserver.domain.notification.entity.type.NotificationType

/**
 * Push로 보낼 `NotificationType`의 확정 목록이다(Issue #190 작업 지시, #191 수신자 정책의 확정
 * 여부와 무관하게 Enum 값 자체로 판단한다). "사용자 행동/상태 변화와 직접 관련된 알림" 중심으로
 * 좁힌다는 Issue #190 원칙에 따라, 단순 정보성 변경 알림(JOB_UPDATED 등)은 제외한다.
 *
 * 포함: INQUIRY_ANSWERED, JOB_APPLICATION_STATUS_CHANGED, MEMBER_APPROVAL_RESULT,
 * PROGRAM_DELETED, JOB_PUBLISHED, PROGRAM_PUBLISHED, PROGRAM_APPLICATION_APPLIED,
 * PROGRAM_APPLICATION_CANCELED, PORTFOLIO_REQUEST_PUBLISHED.
 *
 * 제외: JOB_UPDATED, JOB_CLOSED, JOB_DELETED, PROGRAM_UPDATED, PROGRAM_CLOSED,
 * PROGRAM_VACANCY_AVAILABLE, SYSTEM.
 *
 * PORTFOLIO_REQUEST_PUBLISHED를 포함하는 이유는 마감(`dueAt`)이 있는 제출을 학생에게 직접
 * 요구하는 알림이라서다 -- 앱을 열지 않으면 마감을 놓치므로 정보성 변경 알림과 성격이 다르다
 * (Issue #331에서 확정).
 */
object PushEligibleNotificationTypes {
    val TYPES: Set<NotificationType> =
        setOf(
            NotificationType.INQUIRY_ANSWERED,
            NotificationType.JOB_APPLICATION_STATUS_CHANGED,
            NotificationType.MEMBER_APPROVAL_RESULT,
            NotificationType.PROGRAM_DELETED,
            NotificationType.JOB_PUBLISHED,
            NotificationType.PROGRAM_PUBLISHED,
            NotificationType.PROGRAM_APPLICATION_APPLIED,
            NotificationType.PROGRAM_APPLICATION_CANCELED,
            NotificationType.PORTFOLIO_REQUEST_PUBLISHED,
        )

    fun isEligible(type: NotificationType): Boolean = type in TYPES
}
