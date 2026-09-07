package team.inreok.getiserver.domain.system.controller

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint
import team.inreok.getiserver.domain.system.dto.AggregateHealthStatus
import team.inreok.getiserver.domain.system.dto.ComponentHealthStatus
import team.inreok.getiserver.domain.system.dto.SystemHealthType

@ExtendWith(MockitoExtension::class)
class SystemHealthControllerTest {
    @Mock
    lateinit var healthEndpoint: HealthEndpoint

    @Test
    fun `Actuator component 조회 실패 시 해당 component만 DOWN으로 반환한다`() {
        val response = SystemHealthController(healthEndpoint).getHealth().data

        assertThat(response.totalCount).isEqualTo(5)
        assertThat(response.healthyCount).isEqualTo(1)
        assertThat(response.status).isEqualTo(AggregateHealthStatus.DEGRADED)
        assertThat(response.systems).containsExactly(
            team.inreok.getiserver.domain.system.dto.SystemHealthItem(
                SystemHealthType.APPLICATION,
                ComponentHealthStatus.UP,
            ),
            team.inreok.getiserver.domain.system.dto.SystemHealthItem(
                SystemHealthType.DATABASE,
                ComponentHealthStatus.DOWN,
            ),
            team.inreok.getiserver.domain.system.dto.SystemHealthItem(
                SystemHealthType.REDIS,
                ComponentHealthStatus.DOWN,
            ),
            team.inreok.getiserver.domain.system.dto.SystemHealthItem(
                SystemHealthType.ELASTICSEARCH,
                ComponentHealthStatus.DOWN,
            ),
            team.inreok.getiserver.domain.system.dto.SystemHealthItem(
                SystemHealthType.FILE_STORAGE,
                ComponentHealthStatus.DOWN,
            ),
        )
    }
}
