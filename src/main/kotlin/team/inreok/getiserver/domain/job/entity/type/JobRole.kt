package team.inreok.getiserver.domain.job.entity.type

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "공고 직무 분류")
enum class JobRole {
    BACKEND,
    FRONTEND,
    FULLSTACK,
    MOBILE,
    AI,
    DATA,
    EMBEDDED_IOT,
    CLOUD_DEVOPS,
    SECURITY,
    UX_UI_DESIGN,
    ETC,
}
