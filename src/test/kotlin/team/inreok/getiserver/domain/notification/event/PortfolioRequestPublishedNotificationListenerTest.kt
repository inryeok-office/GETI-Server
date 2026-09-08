package team.inreok.getiserver.domain.notification.event

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoInteractions
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.quality.Strictness
import org.springframework.core.task.SyncTaskExecutor
import team.inreok.getiserver.domain.notification.dto.NotificationCreateCommand
import team.inreok.getiserver.domain.notification.entity.type.NotificationTargetType
import team.inreok.getiserver.domain.notification.entity.type.NotificationType
import team.inreok.getiserver.domain.notification.service.NotificationService
import team.inreok.getiserver.domain.portfolio.event.PortfolioRequestPublishedEvent
import team.inreok.getiserver.domain.portfolio.query.PortfolioRequestTargetQueryPort
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PortfolioRequestPublishedNotificationListenerTest {
    @Mock
    private lateinit var portfolioRequestTargetQueryPort: PortfolioRequestTargetQueryPort

    @Mock
    private lateinit var notificationService: NotificationService

    // 실행 Thread 분리 자체는 이 Test의 관심사가 아니라 동기 Executor를 넣어 호출 결과를 그대로
    // 검증한다(ProgramDeletedNotificationListenerTest와 같은 이유).
    private val listener by lazy {
        PortfolioRequestPublishedNotificationListener(
            portfolioRequestTargetQueryPort,
            notificationService,
            SyncTaskExecutor(),
        )
    }

    private val event =
        PortfolioRequestPublishedEvent(
            requestId = 1L,
            title = "3학년 포트폴리오",
            dueAt = LocalDateTime.of(2026, 9, 30, 23, 59),
        )

    @Test
    fun `대상 학생 모두에게 PORTFOLIO_REQUEST_PUBLISHED 알림을 생성한다`() {
        given(portfolioRequestTargetQueryPort.findTargetStudentMemberIds(1L)).willReturn(listOf(10L, 11L))

        listener.onPortfolioRequestPublished(event)

        val commands = captureCommands(2)
        assertThat(commands.map { it.recipientMemberId }).containsExactly(10L, 11L)
        assertThat(commands).allSatisfy { command ->
            assertThat(command.type).isEqualTo(NotificationType.PORTFOLIO_REQUEST_PUBLISHED)
            assertThat(command.targetType).isEqualTo(NotificationTargetType.PORTFOLIO_REQUEST)
            assertThat(command.targetId).isEqualTo(1L)
            assertThat(command.content).contains("3학년 포트폴리오")
            assertThat(command.content).contains("2026-09-30 23:59")
            assertThat(command.sourceEventType).isEqualTo("PortfolioRequestPublishedEvent")
            assertThat(command.sourceEventId).isEqualTo(1L)
        }
    }

    @Test
    fun `같은 학생이 중복으로 조회돼도 알림은 한 번만 생성한다`() {
        given(portfolioRequestTargetQueryPort.findTargetStudentMemberIds(1L)).willReturn(listOf(10L, 10L))

        listener.onPortfolioRequestPublished(event)

        verify(notificationService, times(1)).create(commandFor(10L))
    }

    @Test
    fun `대상 학생이 없으면 알림을 생성하지 않는다`() {
        given(portfolioRequestTargetQueryPort.findTargetStudentMemberIds(1L)).willReturn(emptyList())

        listener.onPortfolioRequestPublished(event)

        verifyNoInteractions(notificationService)
    }

    @Test
    fun `한 수신자의 알림 생성이 실패해도 나머지 수신자에게는 알림을 생성한다`() {
        given(portfolioRequestTargetQueryPort.findTargetStudentMemberIds(1L)).willReturn(listOf(10L, 11L))
        given(notificationService.create(commandFor(10L))).willThrow(RuntimeException("db down"))

        listener.onPortfolioRequestPublished(event)

        verify(notificationService).create(commandFor(11L))
    }

    @Test
    fun `대상 학생 조회가 실패해도 예외를 다시 던지지 않고 알림을 생성하지 않는다`() {
        given(portfolioRequestTargetQueryPort.findTargetStudentMemberIds(1L)).willThrow(RuntimeException("db down"))

        listener.onPortfolioRequestPublished(event)

        verifyNoInteractions(notificationService)
    }

    private fun commandFor(recipientMemberId: Long): NotificationCreateCommand =
        NotificationCreateCommand(
            recipientMemberId = recipientMemberId,
            type = NotificationType.PORTFOLIO_REQUEST_PUBLISHED,
            title = "새로운 포트폴리오 수합 요청이 등록되었습니다",
            content = "\"3학년 포트폴리오\" 요청이 등록되었습니다. 2026-09-30 23:59까지 제출해 주세요.",
            sourceEventType = "PortfolioRequestPublishedEvent",
            sourceEventId = 1L,
            targetType = NotificationTargetType.PORTFOLIO_REQUEST,
            targetId = 1L,
        )

    private fun captureCommands(expectedCount: Int): List<NotificationCreateCommand> {
        val captor = ArgumentCaptor.forClass(NotificationCreateCommand::class.java)
        // capture()가 반환하는 Java platform type을 non-null 파라미터에 그대로 넘기면 Kotlin이
        // 끼워 넣은 호출부 null 검사 때문에 NPE가 난다. Elvis 기본값으로 우회한다
        // (ProgramDeletedNotificationListenerTest와 동일한 이유).
        verify(notificationService, times(expectedCount)).create(captor.capture() ?: commandFor(0L))
        return captor.allValues
    }
}
