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
import com.androidfung.drminfo.DeviceTypes
import com.androidfung.drminfo.DeviceTypes.getDefaultLauncher
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util
import java.util.*

open class InfoStepFragment : GuidedStepSupportFragment() {
    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        val title = getString(R.string.app_name)
        val description = "${Build.MANUFACTURER}  ${Build.MODEL}"
        val icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher, null)
//        val packageManager = requireActivity().packageManager
//
//        val deviceTypeText = when (DeviceTypes.obtainDeviceType(packageManager)) {
//            DeviceTypes.ANDROID_TV, DeviceTypes.ANDROID_TV_WITH_WATSON -> getString(R.string.text_android_tv)
//            DeviceTypes.ANDROID_TV_OPERATOR_DEVICE, DeviceTypes.ANDROID_TV_OPERATOR_DEVICE_WITH_CUSTOM_LAUNCHER -> getString(R.string.text_android_tv_operator_tier)
//            DeviceTypes.GOOGLE_TV -> getString(R.string.text_google_tv)
//            DeviceTypes.ANDROID_OTHER_TV -> getString(R.string.text_android_unknown)
//            DeviceTypes.ANDROID_NON_TV -> getString(R.string.text_android_non_tv)
//            else -> getString(R.string.text_android_unknown)
//        }

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
                .title(R.string.text_hardware)
                .description(Build.HARDWARE)
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
            val defaultDisplay = displayManager.getDisplay(Display.DEFAULT_DISPLAY)

            defaultDisplay?.let { display ->
                val supportedHdrType = display.hdrCapabilities.supportedHdrTypes

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
                    .filter { codec -> codec.isHardwareAccelerated && !codec.isEncoder }
                    .flatMap { codec -> codec.supportedTypes.asIterable() }
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

        val packageManager = requireActivity().packageManager

        val deviceTypeText = when (DeviceTypes.obtainDeviceType(packageManager)) {
            DeviceTypes.ANDROID_TV, DeviceTypes.ANDROID_TV_WITH_WATSON -> getString(R.string.text_android_tv)
            DeviceTypes.ANDROID_TV_OPERATOR_DEVICE, DeviceTypes.ANDROID_TV_OPERATOR_DEVICE_WITH_CUSTOM_LAUNCHER -> getString(R.string.text_android_tv_operator_tier)
            DeviceTypes.GOOGLE_TV -> getString(R.string.text_google_tv)
            DeviceTypes.ANDROID_OTHER_TV -> getString(R.string.text_android_unknown)
            DeviceTypes.ANDROID_NON_TV -> getString(R.string.text_android_non_tv)
            else -> getString(R.string.text_android_unknown)
        }

        actions += GuidedAction.Builder(activity)
                .infoOnly(true)
                .title(R.string.text_device_type)
                .description(deviceTypeText)
                .build()

//        val pm = requireActivity().packageManager
//        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addCategory(Intent.CATEGORY_DEFAULT)
//        val resolveInfo = pm.resolveActivity(intent, 0)
//        resolveInfo?.let {
//            val applicationLabel = pm.getApplicationLabel(it.activityInfo.applicationInfo)
        val packageName = packageManager.getDefaultLauncher()
        packageName?.let {
            actions += GuidedAction.Builder(activity)
                    .infoOnly(true)
                    .title(R.string.text_default_launcher)
                    .description(packageName)
                    .build()
        }
//
//        }

        //MediaDrm Section
        val cryptoSchemes = arrayOf(CLEARKEY_UUID, WIDEVINE_UUID, PLAYREADY_UUID, VERIMATRIX_UUID, MARLIN_UUID, PRIMETIME_UUID, WISEPLAY_UUID, DIVX_UUID)

        actions += cryptoSchemes.map { uuid ->
            val supported = isCryptoSchemeSupported(uuid)
            val subActions = if (supported) getSubActions(uuid) else null
            val infoOnly = subActions.isNullOrEmpty()

            GuidedAction.Builder(activity)
                    .infoOnly(infoOnly)
                    .subActions(if (infoOnly) null else subActions)
                    .title(Util.convertCryptoSchemeToStringRes(uuid))
                    .description(if (supported) R.string.text_supported else R.string.text_not_supported)
                    .build()
        }
    }

    private fun getSubActions(uuid: UUID): List<GuidedAction>? {
        // Closeable is not implemented in older version of Android
//        MediaDrm(uuid).use { mediaDrm ->
//            return when (uuid){
//                WIDEVINE_UUID -> getMediaDrmSubActions(mediaDrm) + getWidevineSubActions(mediaDrm)
//                else -> getMediaDrmSubActions(mediaDrm)
//            }
//        }

        return try {
            val mediaDrm = MediaDrm(uuid)
            when (uuid) {
                WIDEVINE_UUID -> getMediaDrmSubActions(mediaDrm) + getWidevineSubActions(mediaDrm)
                else -> getMediaDrmSubActions(mediaDrm)
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

    private fun MediaDrm.getPropertyStringOrNull(key: String): String? {
        return try {
            this.getPropertyString(key)
        } catch (e: RuntimeException) {
            null
        } catch (e: MediaDrmException) {
            null
        }
    }

    private fun getPrpoerties(mediaDrm: MediaDrm, drmStringKeys: Array<String>, drmStringNames: Array<String>) =
            drmStringKeys.zip(drmStringNames)
                    .map { (key, name) -> key to Pair(name, mediaDrm.getPropertyStringOrNull(key)) }
                    .toMap()

    private fun getSubActions(mediaDrm: MediaDrm, drmStringKeys: Array<String>, drmStringNames: Array<String>) =
            getPrpoerties(mediaDrm, drmStringKeys, drmStringNames).values
                    .filterNot { (_, desc) -> desc.isNullOrBlank() }
                    .map { (name, desc) ->
                        GuidedAction.Builder(activity)
                                .infoOnly(true)
                                .title(name)
                                .description(desc)
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