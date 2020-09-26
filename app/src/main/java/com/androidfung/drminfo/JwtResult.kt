package com.androidfung.drminfo

import kotlinx.serialization.Serializable

@Serializable
data class JwtResult(
        val nonce: String,
        val ctsProfileMatch: Boolean,
        val basicIntegrity: Boolean
)


