package com.androidfung.drminfo

import android.view.View
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class UtilUnitTest {

    @Test
    fun testConvertCryptoSchemeToStringRes() {
        assertEquals(R.string.text_clearkey_name, Util.convertCryptoSchemeToStringRes(Constants.CLEARKEY_UUID))
        assertEquals(R.string.text_widevine_name, Util.convertCryptoSchemeToStringRes(Constants.WIDEVINE_UUID))
        assertEquals(R.string.text_playready_name, Util.convertCryptoSchemeToStringRes(Constants.PLAYREADY_UUID))
        assertEquals(R.string.text_wiseplay_name, Util.convertCryptoSchemeToStringRes(Constants.WISEPLAY_UUID))

        val randomUuid = UUID.randomUUID()
        assertEquals(R.string.text_undefined_name, Util.convertCryptoSchemeToStringRes(randomUuid))
    }

    @Test
    fun testGoneIfNullOrEmpty() {
        assertEquals(View.GONE, Util.goneIfNullOrEmpty(null))
        assertEquals(View.GONE, Util.goneIfNullOrEmpty(""))
        assertEquals(View.GONE, Util.goneIfNullOrEmpty("   "))
        assertEquals(View.GONE, Util.goneIfNullOrEmpty(emptyList<String>()))
        assertEquals(View.GONE, Util.goneIfNullOrEmpty(emptyMap<String, String>()))
        assertEquals(View.GONE, Util.goneIfNullOrEmpty(emptyArray<String>()))

        assertEquals(View.VISIBLE, Util.goneIfNullOrEmpty("Valid String"))
        assertEquals(View.VISIBLE, Util.goneIfNullOrEmpty(listOf("item")))
        assertEquals(View.VISIBLE, Util.goneIfNullOrEmpty(mapOf("key" to "value")))
    }
}
