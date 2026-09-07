package team.inreok.getiserver.domain.file.storage

import org.springframework.boot.health.contributor.Health
import org.springframework.boot.health.contributor.HealthIndicator
import org.springframework.stereotype.Component
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.HeadBucketRequest

@Component("fileStorage")
class FileStorageHealthIndicator(
    private val s3Client: S3Client,
    private val properties: FileStorageProperties,
) : HealthIndicator {
    override fun health(): Health =
        runCatching {
            s3Client.headBucket(HeadBucketRequest.builder().bucket(properties.bucket).build())
            Health.up().build()
        }.getOrElse { Health.down().build() }
}
