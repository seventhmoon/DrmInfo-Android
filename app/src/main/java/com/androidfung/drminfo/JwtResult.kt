package com.androidfung.drminfo

import androidx.annotation.Keep

@Keep
data class JwtResult(
        val nonce: String,
        val ctsProfileMatch: Boolean,
        val basicIntegrity: Boolean
)


