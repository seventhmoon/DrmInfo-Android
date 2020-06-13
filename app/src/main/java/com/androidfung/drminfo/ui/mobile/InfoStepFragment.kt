package com.androidfung.drminfo.ui.mobile

import android.annotation.SuppressLint
import android.media.MediaCodecList
import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Display
import android.view.Display.HdrCapabilities.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import com.androidfung.drminfo.Constants
import com.androidfung.drminfo.R

class InfoStepFragment : GuidedStepSupportFragment() {
    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        val title = getString(R.string.app_name)
        val description = getString(R.string.text_on_android_tv)
        val icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher, null)
        return Guidance(title, description, null, icon)
    }

    private fun getPropertyString(mediaDrm: MediaDrm, key: String): String? {
        return try {
            mediaDrm.getPropertyString(key)
        } catch (ex: IllegalStateException) {
            Log.d(TAG, ex.toString())
            null
        } catch (ex: IllegalArgumentException) {
            Log.d(TAG, ex.toString())
            null
        }
    }

    private fun getSubActions(mediaDrm: MediaDrm, drmStringKeys: Array<String>, drmStringNames: Array<String>): List<GuidedAction> {
        val drmStringValue = drmStringKeys.map {
            getPropertyString(mediaDrm, it)
        }

        return drmStringNames.zip(drmStringValue)
                .filter {
                    !it.second.isNullOrBlank()
                }.map {
                    GuidedAction.Builder(activity)
                            .infoOnly(true)
                            .title(it.first)
                            .description(it.second)
                            .build()
                }
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

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {
        //info section here
        val infoTitles = intArrayOf(R.string.text_brand, R.string.text_product, R.string.text_model, R.string.text_display,
                R.string.text_type, R.string.text_fingerprint, R.string.text_sdk_level)
        val descriptions = arrayOf(Build.BRAND, Build.PRODUCT, Build.MODEL,
                Build.DISPLAY, Build.TYPE, Build.FINGERPRINT, Build.VERSION.SDK_INT.toString())

        actions.addAll(
                infoTitles.zip(descriptions)
                        .filter { !it.second.isNullOrBlank() }
                        .map {
                            GuidedAction.Builder(activity)
                                    .infoOnly(true)
                                    .title(it.first)
                                    .description(it.second)
                                    .build()
                        })

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            actions.add(GuidedAction.Builder(activity)
                    .infoOnly(true)
                    .title(getString(R.string.text_abi))
                    .description(Build.SUPPORTED_ABIS.joinToString { it })
                    .build())
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val displayManager = DisplayManagerCompat.getInstance(requireActivity())
            val display = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

            if (display != null) {
                val supportedHdrType = display.hdrCapabilities.supportedHdrTypes

                if (supportedHdrType.isNotEmpty()) {
                    GuidedAction.Builder(activity)
                            .infoOnly(true)
                            .title(R.string.text_supported_hdr_types)
                            .description(supportedHdrType
                                    .map(this@InfoStepFragment::convertHdrTypeToString)
                                    .filterNotNull()
                                    .joinToString { it })
                            .build()
                }
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val supportedHardwareAcceleratedDecodeType = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                    .filter { !it.isEncoder && it.isHardwareAccelerated }
                    .map { it.supportedTypes.toList() }
                    .flatten()
                    .toSortedSet()
                    .map {
                        GuidedAction.Builder(activity)
                                .infoOnly(true)
                                .title(it)
                                .build()
                    }

            if (supportedHardwareAcceleratedDecodeType.isNotEmpty()) {
                actions.add(GuidedAction.Builder(activity)
                        .infoOnly(false)
                        .title(getString(R.string.text_hw_decode_type))
                        .subActions(supportedHardwareAcceleratedDecodeType)
                        .build())
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


        val uuids = arrayOf(Constants.CLEARKEY_UUID, Constants.WIDEVINE_UUID, Constants.PLAYREADY_UUID, Constants.VERIMATRIX_UUID, Constants.MARLIN_UUID, Constants.PRIMETIME_UUID, Constants.WISEPLAY_UUID)
        val titles = intArrayOf(R.string.text_clearkey_name, R.string.text_widevine_name, R.string.text_playready_name, R.string.text_verimatrix_name, R.string.text_marlin_name, R.string.text_primetime_name, R.string.text_wiseplay_name)
        val names = titles.map { getString(it) }

        actions.addAll(uuids.zip(names).map {
            val supported = MediaDrm.isCryptoSchemeSupported(it.first)
            val subActions = ArrayList<GuidedAction>()

            if (supported) {
                try {
                    val mediaDrm = MediaDrm(it.first)
                    subActions.addAll(getMediaDrmSubActions(mediaDrm))

                    if (Constants.WIDEVINE_UUID === it.first) {
                        subActions.addAll(getWidevineSubActions(mediaDrm))
                    }

                } catch (e: UnsupportedSchemeException) {
                    e.printStackTrace()
                }
            }

            val infoOnly = subActions.isEmpty()
            GuidedAction.Builder(activity)
                    .infoOnly(infoOnly)
                    .subActions(if (infoOnly) null else subActions)
                    .title(it.second)
                    .description(if (supported) getString(R.string.text_supported) else getString(R.string.text_not_supported))
                    .build()
        })
    }

    private fun convertHdrTypeToString(hdrType: Int): String? {
        return when (hdrType) {
            HDR_TYPE_DOLBY_VISION -> getString(R.string.hdr_type_dolby_vision)
            HDR_TYPE_HDR10 -> getString(R.string.hdr_type_hdr10)
            HDR_TYPE_HDR10_PLUS -> getString(R.string.hdr_type_hdr10_plus)
            HDR_TYPE_HLG -> getString(R.string.hdr_type_hlg)
            else -> null
        }
    }

    companion object {
        private val TAG = InfoStepFragment::class.java.simpleName
    }
}