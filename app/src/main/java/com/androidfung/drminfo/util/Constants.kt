package com.androidfung.drminfo.util

import java.util.UUID

object Constants {
    @JvmField
    val WIDEVINE_UUID = UUID(-0x121074568629b532L, -0x5c37d8232ae2de13L)
    @JvmField
    val PLAYREADY_UUID = UUID(-0x65fb0f8667bfbd7aL, -0x546d19a41f77a06bL)
    @JvmField
    val MARLIN_UUID = UUID(0x5E629AF538DA4063L, -0x768868004266fd2cL)
    @JvmField
    val VERIMATRIX_UUID = UUID(-0x65d8227d021db8dbL, -0x7343bdcb55f913f7L)
    @JvmField
    val CLEARKEY_UUID = UUID(0x1077EFECC0B24D02L, -0x531cc3e1ad1d04b5L)
    @JvmField
    val PRIMETIME_UUID = UUID(-0xdc61896105cb7b0L, -0x63e956fc396cd105L)
    @JvmField
    val DIVX_UUID = UUID(0x35BF197B530E42D7L, -0x749ae4b40beaf8f1L)
    @JvmField
    val WISEPLAY_UUID = UUID(0X3D5E6D359B9A41E8L, -0x47bc22c3918d3bd4L)

    // Standard MediaDrm Property Keys (Device and Driver level)
    const val PROPERTY_DESCRIPTION = "description"
    const val PROPERTY_VENDOR = "vendor"
    const val PROPERTY_VERSION = "version"
    const val PROPERTY_ALGORITHMS = "algorithms"
    const val PROPERTY_DEVICE_UNIQUE_ID = "deviceUniqueId"

    // Widevine-specific Property Keys
    const val PROPERTY_SECURITY_LEVEL = "securityLevel"
    const val PROPERTY_SYSTEM_ID = "systemId"
    const val PROPERTY_OEM_CRYPTO_API_VERSION = "oemCryptoApiVersion"
}
