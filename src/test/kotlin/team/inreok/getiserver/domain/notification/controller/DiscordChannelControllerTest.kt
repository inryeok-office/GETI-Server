package team.inreok.getiserver.domain.notification.controller

import org.junit.jupiter.api.Test
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import team.inreok.getiserver.global.discord.DiscordChannelOption
import team.inreok.getiserver.global.discord.DiscordChannelResolver
import team.inreok.getiserver.global.security.JwtTokenProvider
import team.inreok.getiserver.global.security.NormalSecurityTestConfig

@WebMvcTest(controllers = [DiscordChannelController::class])
@Import(NormalSecurityTestConfig::class)
@EnableWebSecurity
class DiscordChannelControllerTest
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
    ) {
        @MockitoBean
        private lateinit var channelResolver: DiscordChannelResolver

        @MockitoBean
        private lateinit var jwtTokenProvider: JwtTokenProvider

        private fun auth(role: String) =
            authentication(
                UsernamePasswordAuthenticationToken(
                    1L,
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_$role")),
                ),
            )

        @Test
        fun `개발자는 설정된 채널 선택지를 조회할 수 있다`() {
            given(channelResolver.availableChannels()).willReturn(
                listOf(DiscordChannelOption("job-notice", "123", "공고 공지")),
            )

            mockMvc
                .perform(get("/api/v1/admin/discord-channels").with(auth("DEVELOPER")))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.channels[0].channelKey").value("job-notice"))
                .andExpect(jsonPath("$.data.channels[0].channelId").value("123"))
                .andExpect(jsonPath("$.data.channels[0].channelName").value("공고 공지"))
        }

        @Test
        fun `교사는 채널 선택지를 조회할 수 없다`() {
            mockMvc
                .perform(get("/api/v1/admin/discord-channels").with(auth("TEACHER")))
                .andExpect(status().isForbidden)
        }

        @Test
        fun `인증되지 않은 사용자는 채널 선택지를 조회할 수 없다`() {
            mockMvc
                .perform(get("/api/v1/admin/discord-channels"))
                .andExpect(status().isUnauthorized)
        }

        @Test
        fun `설정된 채널이 없으면 빈 목록을 반환한다`() {
            given(channelResolver.availableChannels()).willReturn(emptyList())

            mockMvc
                .perform(get("/api/v1/admin/discord-channels").with(auth("DEVELOPER")))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.data.channels").isEmpty)
        }
    }
