package team.inreok.getiserver.domain.notification.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import team.inreok.getiserver.domain.notification.dto.DiscordChannelListResponse
import team.inreok.getiserver.domain.notification.dto.DiscordChannelResponse
import team.inreok.getiserver.global.discord.DiscordChannelResolver
import team.inreok.getiserver.global.openapi.BEARER_AUTH_SCHEME
import team.inreok.getiserver.global.web.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponse as SwaggerApiResponse

@Tag(name = "Notification - Discord 채널", description = "관리자 Discord 채널 선택지")
@SecurityRequirement(name = BEARER_AUTH_SCHEME)
@RestController
class DiscordChannelController(
    private val channelResolver: DiscordChannelResolver,
) {
    @Operation(
        summary = "Discord 채널 선택지 조회",
        description = "관리자 Discord 전달 내역 Filter에 사용할, 설정된 채널의 ID와 표시 이름을 조회한다. 개발자만 사용할 수 있다.",
    )
    @ApiResponses(
        SwaggerApiResponse(responseCode = "200", description = "채널 선택지 조회 성공"),
        SwaggerApiResponse(responseCode = "401", description = "인증되지 않음"),
        SwaggerApiResponse(responseCode = "403", description = "개발자 권한 없음"),
    )
    @GetMapping("/api/v1/admin/discord-channels")
    fun listChannels(): ApiResponse<DiscordChannelListResponse> =
        ApiResponse.of(
            DiscordChannelListResponse(
                channels =
                    channelResolver.availableChannels().map {
                        DiscordChannelResponse(it.channelKey, it.channelId, it.channelName)
                    },
            ),
        )
}
