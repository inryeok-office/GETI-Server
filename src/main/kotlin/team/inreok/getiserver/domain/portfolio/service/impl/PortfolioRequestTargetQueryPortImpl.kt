package team.inreok.getiserver.domain.portfolio.service.impl

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import team.inreok.getiserver.domain.portfolio.query.PortfolioRequestTargetQueryPort
import team.inreok.getiserver.domain.portfolio.repository.PortfolioRequestTargetRepository

/**
 * 다른 Domain Module(notification)에 공개된 조회 계약([PortfolioRequestTargetQueryPort])의 구현이다.
 * [team.inreok.getiserver.domain.program.service.impl.ProgramApplicantQueryPortImpl]과 같은 이유로
 * `PortfolioRequestServiceImpl`에 합치지 않고 분리한다 -- 이 Class의 사용자는 REST 요청이 아니라
 * 다른 Module이고, 공개 계약의 변경 이유가 Portfolio 자체 Use Case와 다르다.
 */
@Service
class PortfolioRequestTargetQueryPortImpl(
    private val targetRepository: PortfolioRequestTargetRepository,
) : PortfolioRequestTargetQueryPort {
    @Transactional(readOnly = true)
    override fun findTargetStudentMemberIds(requestId: Long): List<Long> =
        targetRepository.findStudentMemberIdsByRequestId(requestId)
}
