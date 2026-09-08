package team.inreok.getiserver.domain.notification.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "관리자 Discord 채널 선택지 목록")
data class DiscordChannelListResponse(
    @param:Schema(description = "설정된 Discord 채널 목록")
    val channels: List<DiscordChannelResponse>,
)

@Schema(description = "관리자 Discord 채널 선택지")
data class DiscordChannelResponse(
    @param:Schema(description = "서버 내부 채널 Key", example = "job-notice")
    val channelKey: String,
    @param:Schema(description = "Discord 채널 ID", example = "1234567890123456789")
    val channelId: String,
    @param:Schema(description = "사람이 읽을 수 있는 채널 이름", example = "공고 공지")
    val channelName: String,
)
