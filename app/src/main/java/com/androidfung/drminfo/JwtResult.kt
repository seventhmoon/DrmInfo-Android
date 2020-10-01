package com.androidfung.drminfo

import androidx.annotation.Keep
import kotlinx.serialization.Serializable

@Keep
@Serializable
data class JwtResult(
        val nonce: String,
        val ctsProfileMatch: Boolean,
        val basicIntegrity: Boolean
)


