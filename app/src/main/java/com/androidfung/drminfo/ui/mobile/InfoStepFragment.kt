package com.androidfung.drminfo.ui.mobile

import android.annotation.SuppressLint
import android.media.MediaCodecList
import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Log
import android.view.Display
import androidx.core.content.res.ResourcesCompat
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import com.androidfung.drminfo.Constants.CLEARKEY_UUID
import com.androidfung.drminfo.Constants.MARLIN_UUID
import com.androidfung.drminfo.Constants.PLAYREADY_UUID
import com.androidfung.drminfo.Constants.PRIMETIME_UUID
import com.androidfung.drminfo.Constants.VERIMATRIX_UUID
import com.androidfung.drminfo.Constants.WIDEVINE_UUID
import com.androidfung.drminfo.Constants.WISEPLAY_UUID
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util

class InfoStepFragment : GuidedStepSupportFragment() {
    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        val title = getString(R.string.app_name)
        val description = getString(R.string.text_on_android_tv)
        val icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher, null)
        return Guidance(title, description, null, icon)
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        //info section here
//        val infoTitles = intArrayOf(R.string.text_brand, R.string.text_product, R.string.text_model, R.string.text_display,
//                R.string.text_type, R.string.text_fingerprint, R.string.text_sdk_level)
//        val descriptions = arrayOf(Build.BRAND, Build.PRODUCT, Build.MODEL,
//                Build.DISPLAY, Build.TYPE, Build.FINGERPRINT, Build.VERSION.SDK_INT.toString())
        val basicInfo = arrayOf(R.string.text_brand to Build.BRAND,
                R.string.text_product to Build.PRODUCT,
                R.string.text_model to Build.MODEL,
                R.string.text_display to Build.DISPLAY,
                R.string.text_type to Build.TYPE,
                R.string.text_fingerprint to Build.FINGERPRINT,
                R.string.text_sdk_level to SDK_INT.toString())

        actions += basicInfo.filter { it.second.isNotBlank() }
                .map {
                    GuidedAction.Builder(activity)
                            .infoOnly(true)
                            .title(it.first)
                            .description(it.second)
                            .build()
                }

        if (SDK_INT >= VERSION_CODES.LOLLIPOP) {
            actions += GuidedAction.Builder(activity)
                    .infoOnly(true)
                    .title(getString(R.string.text_abi))
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
                    .map { it.supportedTypes.toList() }
                    .flatten()
                    .toSortedSet()

            if (supportedHardwareAcceleratedDecodeType.isNotEmpty()) {
                actions += GuidedAction.Builder(activity)
                        .infoOnly(false)
                        .title(getString(R.string.text_hw_decode_type))
                        .subActions(supportedHardwareAcceleratedDecodeType.map {
                            GuidedAction.Builder(activity)
                                    .infoOnly(true)
                                    .title(it)
                                    .build()
                        })
                        .build()
            }
        }


//        val supportedCryptoScheme = if (SDK_INT >= Build.VERSION_CODES.R) MediaDrm.getSupportedCryptoSchemes()
//        else listOf(Constants.CLEARKEY_UUID, Constants.WIDEVINE_UUID, Constants.PLAYREADY_UUID, Constants.VERIMATRIX_UUID, Constants.MARLIN_UUID, Constants.PRIMETIME_UUID, Constants.WISEPLAY_UUID)
//
//        actions.addAll(supportedCryptoScheme.map {
//                    it to try {
//                        MediaDrm(it)
//                    } catch (e: UnsupportedSchemeException) {
//                        null
//                    }}
//                .filter { it.second != null }
//                .filter { try{
//                    it.second!!.getPropertyString(MediaDrm.PROPERTY_DESCRIPTION).isNotEmpty()
//                }catch(e: IllegalArgumentException){
//                    false
//                } }
//                .distinctBy { it.second!!.getPropertyString(MediaDrm.PROPERTY_DESCRIPTION) }
//                .map {
//                    val subActions = ArrayList<GuidedAction>()
//                    subActions.addAll(getMediaDrmSubActions(it.second!!))
//
//                    if (Constants.WIDEVINE_UUID === it.first) {
//                        subActions.addAll(getWidevineSubActions(it.second!!))
//                    }
//
//                    val infoOnly = subActions.isEmpty()
//                    GuidedAction.Builder(activity)
//                            .infoOnly(infoOnly)
//                            .subActions(if (infoOnly) null else subActions)
//                            .title(it.second!!.getPropertyString(MediaDrm.PROPERTY_DESCRIPTION))
//                            .description(getString(R.string.text_supported))
//                            .build()
//                })
//        val cryptoSchemes = listOf(CLEARKEY_UUID to getString(R.string.text_clearkey_name),
//                WIDEVINE_UUID to getString(R.string.text_widevine_name),
//                PLAYREADY_UUID to getString(R.string.text_playready_name),
//                VERIMATRIX_UUID to getString(R.string.text_verimatrix_name),
//                MARLIN_UUID to getString(R.string.text_marlin_name),
//                PRIMETIME_UUID to getString(R.string.text_primetime_name),
//                WISEPLAY_UUID to getString(R.string.text_wiseplay_name)
//        )
        val cryptoSchemes = arrayOf(CLEARKEY_UUID, WIDEVINE_UUID, PLAYREADY_UUID, VERIMATRIX_UUID, MARLIN_UUID, PRIMETIME_UUID, WISEPLAY_UUID)

        actions += cryptoSchemes.map {
            val supported = MediaDrm.isCryptoSchemeSupported(it)
            val subActions = ArrayList<GuidedAction>()

            if (supported) {
                try {
                    val mediaDrm = MediaDrm(it)
                    subActions += getMediaDrmSubActions(mediaDrm)

                    if (WIDEVINE_UUID === it) {
                        subActions += getWidevineSubActions(mediaDrm)
                    }

                } catch (e: UnsupportedSchemeException) {
                    e.printStackTrace()
                }
            }

            val infoOnly = subActions.isEmpty()
            GuidedAction.Builder(activity)
                    .infoOnly(infoOnly)
                    .subActions(if (infoOnly) null else subActions)
                    .title(Util.convertCryptoSchemeToStringRes(it))
                    .description(if (supported) getString(R.string.text_supported) else getString(R.string.text_not_supported))
                    .build()
        }
    }

    private fun convertHdrTypeToString(hdrType: Int) =
            getString(Util.convertHdrTypeToStringRes(hdrType))

    private fun getPropertyString(mediaDrm: MediaDrm, key: String): String? =
            try {
                mediaDrm.getPropertyString(key)
            } catch (ex: IllegalStateException) {
                Log.d(TAG, ex.toString())
                null
            } catch (ex: IllegalArgumentException) {
                Log.d(TAG, ex.toString())
                null
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

    private fun getMediaDrmSubActions(mediaDrm: MediaDrm): List<GuidedAction> {
        val drmStringKeys = resources.getStringArray(R.array.mediadrm_string_property_keys)
        val drmStringNames = resources.getStringArray(R.array.mediadrm_string_property_names)

        return getSubActions(mediaDrm, drmStringKeys, drmStringNames)
    }

    private fun getWidevineSubActions(widevineDrm: MediaDrm): List<GuidedAction> {
        val wvStringKeys = resources.getStringArray(R.array.widevine_string_property_keys)
        val wvStringNames = resources.getStringArray(R.array.widevine_string_property_names)

        return getSubActions(widevineDrm, wvStringKeys, wvStringNames)
    }

    companion object {
        private val TAG = InfoStepFragment::class.java.simpleName
    }
}