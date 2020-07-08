package com.androidfung.drminfo.ui.mobile

import android.media.MediaCodecList
import android.media.MediaDrm
import android.media.MediaDrmException
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.view.Display
import androidx.core.content.res.ResourcesCompat
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import com.androidfung.drminfo.Constants.CLEARKEY_UUID
import com.androidfung.drminfo.Constants.DIVX_UUID
import com.androidfung.drminfo.Constants.MARLIN_UUID
import com.androidfung.drminfo.Constants.PLAYREADY_UUID
import com.androidfung.drminfo.Constants.PRIMETIME_UUID
import com.androidfung.drminfo.Constants.VERIMATRIX_UUID
import com.androidfung.drminfo.Constants.WIDEVINE_UUID
import com.androidfung.drminfo.Constants.WISEPLAY_UUID
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util
import java.util.*

class InfoStepFragment : GuidedStepSupportFragment() {
    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        val title = getString(R.string.app_name)
        val description = getString(R.string.text_on_android_tv)
        val icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher, null)
        return Guidance(title, description, null, icon)
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        //info section here
        actions += GuidedAction.Builder(activity)
                .infoOnly(true)
                .title(R.string.text_brand)
                .description(Build.BRAND)
                .build()

        actions += GuidedAction.Builder(activity)
                .infoOnly(true)
                .title(R.string.text_product)
                .description(Build.PRODUCT)
                .build()

        actions += GuidedAction.Builder(activity)
                .infoOnly(true)
                .title(R.string.text_model)
                .description(Build.MODEL)
                .build()

        actions += GuidedAction.Builder(activity)
                .infoOnly(true)
                .title(R.string.text_display)
                .description(Build.DISPLAY)
                .build()

        actions += GuidedAction.Builder(activity)
                .infoOnly(true)
                .title(R.string.text_type)
                .description(Build.TYPE)
                .build()

        actions += GuidedAction.Builder(activity)
                .infoOnly(true)
                .multilineDescription(true)
                .title(R.string.text_fingerprint)
                .description(Build.FINGERPRINT)
                .build()

        actions += GuidedAction.Builder(activity)
                .infoOnly(true)
                .title(R.string.text_sdk_level)
                .description(SDK_INT.toString())
                .build()


        if (SDK_INT >= VERSION_CODES.LOLLIPOP) {
            actions += GuidedAction.Builder(activity)
                    .infoOnly(true)
                    .multilineDescription(true)
                    .title(R.string.text_abi)
                    .description(Build.SUPPORTED_ABIS.joinToString())
                    .build()
        }

        if (SDK_INT >= VERSION_CODES.N) {
            val displayManager = DisplayManagerCompat.getInstance(requireActivity())
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

            display?.let {
                val supportedHdrType = it.hdrCapabilities.supportedHdrTypes

                if (supportedHdrType.isNotEmpty()) {
                    actions += GuidedAction.Builder(activity)
                            .infoOnly(true)
                            .title(R.string.text_supported_hdr_types)
                            .description(supportedHdrType.map(this@InfoStepFragment::convertHdrTypeToString).sorted().joinToString())
                            .build()
                }
            }
        }

        if (SDK_INT >= VERSION_CODES.Q) {
            val supportedHardwareAcceleratedDecodeType = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                    .filter { it.isHardwareAccelerated }
                    .filterNot { it.isEncoder }
                    .flatMap { it.supportedTypes.asIterable() }
                    .toSortedSet()

            if (supportedHardwareAcceleratedDecodeType.isNotEmpty()) {
                actions += GuidedAction.Builder(activity)
                        .infoOnly(false)
                        .title(R.string.text_hw_decode_type)
                        .multilineDescription(true)
                        .description(supportedHardwareAcceleratedDecodeType.joinToString())
                        .build()
            }
        }

        //MediaDrm Section
        val cryptoSchemes = arrayOf(CLEARKEY_UUID, WIDEVINE_UUID, PLAYREADY_UUID, VERIMATRIX_UUID, MARLIN_UUID, PRIMETIME_UUID, WISEPLAY_UUID, DIVX_UUID)

        actions += cryptoSchemes.map {
            val supported = isCryptoSchemeSupported(it)
            val subActions = if (supported) getSubActions(it) else null
            val infoOnly = subActions.isNullOrEmpty()

            GuidedAction.Builder(activity)
                    .infoOnly(infoOnly)
                    .subActions(if (infoOnly) null else subActions)
                    .title(Util.convertCryptoSchemeToStringRes(it))
                    .description(if (supported) getString(R.string.text_supported) else getString(R.string.text_not_supported))
                    .build()
        }
    }

    private fun getSubActions(uuid: UUID): List<GuidedAction>? {
        return try {
            val mediaDrm = MediaDrm(uuid)
            if (WIDEVINE_UUID === uuid) {
                getMediaDrmSubActions(mediaDrm) + getWidevineSubActions(mediaDrm)
            } else {
                getMediaDrmSubActions(mediaDrm)
            }

        } catch (e: MediaDrmException) {
            e.printStackTrace()
            null
        } catch (e: RuntimeException) {
            e.printStackTrace()
            null
        }
    }

    private fun isCryptoSchemeSupported(uuid: UUID): Boolean {
        return try {
            MediaDrm.isCryptoSchemeSupported(uuid)
        } catch (e: RuntimeException) {
            false
        } catch (e: MediaDrmException) {
            false
        }
    }

    private fun convertHdrTypeToString(hdrType: Int) =
            getString(Util.convertHdrTypeToStringRes(hdrType))

    private fun getPropertyString(mediaDrm: MediaDrm, key: String): String? {
        return try {
            mediaDrm.getPropertyString(key)
        } catch (e: RuntimeException) {
            null
        } catch (e: MediaDrmException) {
            null
        }
    }

    private fun getSubActions(mediaDrm: MediaDrm, drmStringKeys: Array<String>, drmStringNames: Array<String>) =
            drmStringNames.zip(drmStringKeys.map { getPropertyString(mediaDrm, it) })
                    .filterNot { it.second.isNullOrBlank() }
                    .map {
                        GuidedAction.Builder(activity)
                                .infoOnly(true)
                                .title(it.first)
                                .description(it.second)
                                .build()
                    }

    private fun getSubActions(mediaDrm: MediaDrm, keyResId: Int, namesResId: Int) =
            getSubActions(mediaDrm, resources.getStringArray(keyResId), resources.getStringArray(namesResId))

    private fun getMediaDrmSubActions(mediaDrm: MediaDrm) =
            getSubActions(mediaDrm, R.array.mediadrm_string_property_keys, R.array.mediadrm_string_property_names)

    private fun getWidevineSubActions(widevineDrm: MediaDrm) =
            getSubActions(widevineDrm, R.array.widevine_string_property_keys, R.array.widevine_string_property_names)

    companion object {
        @JvmStatic
        private val TAG = InfoStepFragment::class.java.simpleName
    }
}