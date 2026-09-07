package team.inreok.getiserver.domain.system.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import team.inreok.getiserver.domain.system.dto.AggregateHealthStatus
import team.inreok.getiserver.domain.system.dto.ComponentHealthStatus
import team.inreok.getiserver.domain.system.dto.SystemHealthItem
import team.inreok.getiserver.domain.system.dto.SystemHealthResponse
import team.inreok.getiserver.domain.system.dto.SystemHealthType
import team.inreok.getiserver.global.openapi.BEARER_AUTH_SCHEME
import team.inreok.getiserver.global.web.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@Tag(name = "System - Health", description = "개발자용 핵심 Infrastructure 현재 상태")
@SecurityRequirement(name = BEARER_AUTH_SCHEME)
@RestController
class SystemHealthController(
    private val healthEndpoint: HealthEndpoint,
) {
    @Operation(
        summary = "핵심 시스템 상태 조회",
        description =
            "Application, PostgreSQL, Redis, Elasticsearch, File Storage의 현재 UP/DOWN 상태와 전체 상태를 조회한다. " +
                "DEVELOPER만 사용할 수 있다.",
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "시스템 상태 조회 성공"),
        SwaggerApiResponse(responseCode = "401", description = "인증되지 않음"),
        SwaggerApiResponse(responseCode = "403", description = "개발자 권한 없음"),
    )
    @GetMapping("/api/v1/admin/system/health")
    fun getHealth(): ApiResponse<SystemHealthResponse> {
        val systems =
            listOf(
                SystemHealthType.APPLICATION to ComponentHealthStatus.UP,
                SystemHealthType.DATABASE to componentStatus("db"),
                SystemHealthType.REDIS to componentStatus("redis"),
                SystemHealthType.ELASTICSEARCH to componentStatus("elasticsearch"),
                SystemHealthType.FILE_STORAGE to componentStatus("fileStorage"),
            ).map { (type, status) -> SystemHealthItem(type, status) }
        val healthyCount = systems.count { it.status == ComponentHealthStatus.UP }
        return ApiResponse.of(
            SystemHealthResponse(
                healthyCount = healthyCount,
                totalCount = systems.size,
                status =
                    if (healthyCount ==
                        systems.size
                    ) {
                        AggregateHealthStatus.HEALTHY
                    } else {
                        AggregateHealthStatus.DEGRADED
                    },
                systems = systems,
            ),
        )
    }

    private fun componentStatus(path: String): ComponentHealthStatus =
        runCatching { healthEndpoint.healthForPath(path)?.status }
            .getOrNull()
            ?.let { if (it.code == "UP") ComponentHealthStatus.UP else ComponentHealthStatus.DOWN }
            ?: ComponentHealthStatus.DOWN
}
