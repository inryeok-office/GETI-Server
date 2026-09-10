package team.inreok.getiserver.domain.portfolio.event

import org.springframework.modulith.NamedInterface
import java.time.LocalDateTime

/**
 * 포트폴리오 수합 요청이 공개(`DRAFT -> PUBLISHED`)됐음을 알리는 최소 계약이다(Issue #331).
 * `domain.notification`이 이 Event를 구독해 대상 학생들에게 `PORTFOLIO_REQUEST_PUBLISHED` 알림을
 * 만든다.
 *
 * 공개 시점을 알림 시점으로 삼는 이유는 `DRAFT`가 학생에게 노출되지 않기 때문이다
 * ([team.inreok.getiserver.domain.portfolio.entity.type.PortfolioRequestStatus] KDoc) -- 학생이
 * 요청의 존재를 처음 알 수 있게 되는 순간이 곧 공개 시점이다.
 *
 * 알림 문구에 필요한 [title], [dueAt]을 함께 담아 구독 측이 다시 조회하지 않게 한다
 * ([team.inreok.getiserver.domain.program.event.ProgramDeletedEvent]가 `title`을 담는 것과 같은
 * 이유다). 수신자(대상 학생) 목록은 인원 수가 정해져 있지 않아 Event에 담지 않고 구독 측이
 * [team.inreok.getiserver.domain.portfolio.query.PortfolioRequestTargetQueryPort]로 조회한다.
 */
@NamedInterface
data class PortfolioRequestPublishedEvent(
    val requestId: Long,
    val title: String,
    val dueAt: LocalDateTime,
)
