package com.sagrd.mentorly.data.remote.dto.enrollment

import com.sagrd.mentorly.domain.model.enrollment.Certificate

data class CertificateDto(
    val certificateUrl: String,
    val issuedAt: String
) {
    fun toDomain() = Certificate(
        certificateUrl = certificateUrl,
        issuedAt = issuedAt
    )
}
