package team.inreok.getiserver.domain.system.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "핵심 Infrastructure 구성요소의 현재 상태")
data class SystemHealthResponse(
    @param:Schema(description = "현재 정상인 구성요소 수", example = "4")
    val healthyCount: Int,
    @param:Schema(description = "전체 구성요소 수", example = "5")
    val totalCount: Int,
    @param:Schema(description = "전체 상태. 모두 UP이면 HEALTHY, 하나라도 DOWN이면 DEGRADED", example = "HEALTHY")
    val status: AggregateHealthStatus,
    @param:Schema(description = "구성요소별 현재 상태")
    val systems: List<SystemHealthItem>,
)

@Schema(description = "정상 시스템 구성요소")
data class SystemHealthItem(
    @param:Schema(description = "구성요소 종류", example = "DATABASE")
    val type: SystemHealthType,
    @param:Schema(description = "구성요소 상태", example = "UP")
    val status: ComponentHealthStatus,
)

enum class SystemHealthType { APPLICATION, DATABASE, REDIS, ELASTICSEARCH, FILE_STORAGE }

enum class ComponentHealthStatus { UP, DOWN }

enum class AggregateHealthStatus { HEALTHY, DEGRADED }
