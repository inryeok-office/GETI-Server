package team.inreok.getiserver.domain.portfolio.query

import org.springframework.modulith.NamedInterface

/**
 * `notification` Module이 포트폴리오 수합 요청 알림의 수신자(대상 학생)를 읽는 공개 계약이다
 * (Issue #331). 의존 방향과 이유는
 * [team.inreok.getiserver.domain.program.query.ProgramApplicantQueryPort]와 같다 -- 소유 Domain인
 * `portfolio`가 공개하고 `notification`이 참조하므로 `notification -> portfolio` 단방향만 생겨
 * `ModularityTest`의 순환 검증에 걸리지 않는다. 반대로 `notification`에 두면 `portfolio`가
 * `notification`을 참조하게 되는데, `notification`이 이미
 * [team.inreok.getiserver.domain.portfolio.event.PortfolioRequestPublishedEvent]를 구독하고 있어
 * 순환이 된다.
 */
@NamedInterface
interface PortfolioRequestTargetQueryPort {
    /**
     * 수합 요청의 제출 대상 학생 회원 id다. 요청이 없거나 대상이 없으면 빈 목록을 반환한다.
     *
     * 대상 학생 집합은 `DRAFT`에서만 교체할 수 있고(`PortfolioRequestServiceImpl.update`), 공개
     * 이후에는 고정되므로 공개 Transaction이 Commit된 뒤에 조회해도 같은 집합을 얻는다.
     */
    fun findTargetStudentMemberIds(requestId: Long): List<Long>
}
