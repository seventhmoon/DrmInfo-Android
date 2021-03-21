package com.androidfung.drminfo.ui.mobile

import android.media.MediaCodecList
import android.media.MediaDrm
import android.media.MediaDrmException
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.os.Bundle
import android.util.Base64

import android.view.Display
import androidx.core.content.res.ResourcesCompat
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import com.androidfung.drminfo.BuildConfig
import com.androidfung.drminfo.Constants.CLEARKEY_UUID
import com.androidfung.drminfo.Constants.PLAYREADY_UUID
import com.androidfung.drminfo.Constants.WIDEVINE_UUID
import com.androidfung.drminfo.Constants.WISEPLAY_UUID
import com.androidfung.drminfo.R
import com.androidfung.drminfo.TvDeviceTypes.getDefaultLauncher
import com.androidfung.drminfo.Util
import com.google.android.gms.safetynet.SafetyNet
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import java.security.SecureRandom
import java.util.*

open class InfoStepFragment : GuidedStepSupportFragment() {

//    private lateinit var remoteConfig : FirebaseRemoteConfig


    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        val title = getString(R.string.app_name)
        val description = "${Build.MANUFACTURER} ${Build.MODEL}"
        val icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_launcher, null)

        return Guidance(title, description, null, icon)
    }

    @SuppressLint("ObsoleteSdkInt")
    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {

        // Setup Firebase Remote Config
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        remoteConfig.fetchAndActivate()

        // info section here

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
                .description(VERSION.SDK_INT.toString())
                .build()

        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            actions += GuidedAction.Builder(activity)
                    .infoOnly(true)
                    .multilineDescription(true)
                    .title(R.string.text_abi)
                    .description(Build.SUPPORTED_ABIS.joinToString())
                    .build()
        }

        // Display Related
        if (VERSION.SDK_INT >= VERSION_CODES.N) {
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

        // Codec Related
        if (VERSION.SDK_INT >= VERSION_CODES.Q) {
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
//        val deviceTypeTextRes = Util.convertTvDeviceTypeToStringRes(packageManager.obtainDeviceType())
//
//        actions += GuidedAction.Builder(activity)
//                .infoOnly(true)
//                .title(R.string.text_device_type)
//                .description(deviceTypeTextRes)
//                .build()

        val packageName = packageManager.getDefaultLauncher()
        packageName?.let {
            actions += GuidedAction.Builder(activity)
                    .infoOnly(true)
                    .title(R.string.text_default_launcher)
                    .description(packageName)
                    .build()
        }

        val safetyNetEnabled = remoteConfig.getBoolean("safetynet_enabled")

        if (safetyNetEnabled) {
            val safetyNetAction = GuidedAction.Builder(requireActivity())
                    .infoOnly(true)
                    .title(R.string.text_cts_profile_match)
                    .description("N/A")
                    .build()

            actions += safetyNetAction

            val nonce = byteArrayOf(32)
            val random = SecureRandom()
            random.nextBytes(nonce)

            SafetyNet.getClient(requireActivity()).attest(nonce, API_KEY)
                    .addOnSuccessListener(requireActivity()) {

                        val result = it.jwsResult.split(".")
                        if (result.size == 3) {
                            val payload = result[1]
                            val decodedPayload = String(Base64.decode(payload, Base64.DEFAULT))
//                            val returnedPayload = Json { ignoreUnknownKeys = true }.decodeFromString<JwtResult>(decodedPayload)
//                            val returnedPayload = Gson().fromJson(decodedPayload, JwtResult::class.java)
//                        val returnedPayload = Json { ignoreUnknownKeys = true }.decodeFromString(JwtResult.serializer(), decodedPayload)

                            //"ctsProfileMatch":true
                            safetyNetAction.description = if (decodedPayload.contains("\"ctsProfileMatch\":true")) getString(R.string.text_true) else getString(R.string.text_false)
//                        safetyNetAction.description = if (returnedPayload.ctsProfileMatch) getString(R.string.text_true) else getString(R.string.text_false)
//                        Log.d("***", decodedPayload)
//                        }catch (ex : java.lang.Exception){
//                            actions.remove(safetyNetAction)
//                            Log.e(TAG, ex.toString())
//                        }
                        } else {
                            actions.remove(safetyNetAction)
                        }

                    }.addOnFailureListener(requireActivity()) {
                        safetyNetAction.description = it.message
                        actions.remove(safetyNetAction)
                    }
        }

        //MediaDrm Section
//        val cryptoSchemes = arrayOf(CLEARKEY_UUID, WIDEVINE_UUID, PLAYREADY_UUID, VERIMATRIX_UUID, MARLIN_UUID, PRIMETIME_UUID, WISEPLAY_UUID, DIVX_UUID)
        val cryptoSchemes = arrayOf(CLEARKEY_UUID, WIDEVINE_UUID, PLAYREADY_UUID, WISEPLAY_UUID)

        // MediaDrm.getSupportedCryptoSchemes() only available on R+
        if (VERSION.SDK_INT >= VERSION_CODES.R) {
            val supportedCryptoScheme = MediaDrm.getSupportedCryptoSchemes()

            actions += GuidedAction.Builder(requireActivity())
                    .infoOnly(true)
                    .title("Supported Crypto Schemes")
                    .description(supportedCryptoScheme.joinToString())
                    .multilineDescription(true)
                    .build()
        }

        cryptoSchemes.forEach { uuid ->
            val supported = isCryptoSchemeSupported(uuid)
            val subActions = if (supported) getSubActions(uuid) else null
            val infoOnly = subActions.isNullOrEmpty()

            if (supported) {
                actions += GuidedAction.Builder(activity)
                        .infoOnly(infoOnly)
                        .subActions(if (infoOnly) null else subActions)
                        .title(Util.convertCryptoSchemeToStringRes(uuid))
                        .description(if (supported) R.string.text_supported else R.string.text_not_supported)
                        .build()
            }
        }
    }

    private fun getSubActions(uuid: UUID): List<GuidedAction>? {
        // MediaDrm did not implemented Closeable in older version of Android
//        MediaDrm(uuid).use { mediaDrm ->
//            return when (uuid){
//                WIDEVINE_UUID -> getMediaDrmSubActions(mediaDrm) + getWidevineSubActions(mediaDrm)
//                else -> getMediaDrmSubActions(mediaDrm)
//            }
//        }

        return try {
            val mediaDrm = MediaDrm(uuid)
            when (uuid) {
                WIDEVINE_UUID -> getWidevineSubActions(mediaDrm)
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


    private fun getPrpoerties(mediaDrm: MediaDrm,
                              drmStringKeys: Array<String> = resources.getStringArray(R.array.mediadrm_string_property_keys),
                              drmStringNames: Array<String> = resources.getStringArray(R.array.mediadrm_string_property_names)) =
            drmStringKeys.zip(drmStringNames)
                    .associate { (key, name) -> key to (name to mediaDrm.getPropertyStringOrNull(key)) }


//    private fun getPrpoerties(mediaDrm: MediaDrm, drmStringKeys: Array<String>, drmStringNames: Array<String>): Map<String, Pair<String, String?>> {
//        val map = ArrayMap<String, Pair<String, String?>>()
//        map += drmStringKeys.zip(drmStringNames)
//                .associate { (key, name) -> key to (name to mediaDrm.getPropertyStringOrNull(key)) }
//
////        if (VERSION.SDK_INT >= VERSION_CODES.P) {
////            map[getString(R.string.mediadrm_key_maxNumberOfSessions)] = getString(R.string.widevine_string_property_name_maxNumberOfSessions) to mediaDrm.maxSessionCount.toString()
////            map[getString(R.string.mediadrm_key_numberOfOpenSessions)] = getString(R.string.widevine_string_property_name_numberOfOpenSessions) to mediaDrm.openSessionCount.toString()
////            map[getString(R.string.mediadrm_key_maxHdcpLevel)] = getString(R.string.widevine_string_property_name_maxHdcpLevel) to getString(Util.convertHdcpLevelToStringRes(mediaDrm.maxHdcpLevel))
////            map[getString(R.string.mediadrm_key_hdcpLevel)] = getString(R.string.widevine_string_property_name_hdcpLevel) to getString(Util.convertHdcpLevelToStringRes(mediaDrm.connectedHdcpLevel))
////        }
//
//        return map
//    }


    private fun getSubActions(mediaDrm: MediaDrm,
                              drmStringKeys: Array<String> = resources.getStringArray(R.array.mediadrm_string_property_keys),
                              drmStringNames: Array<String> = resources.getStringArray(R.array.mediadrm_string_property_names)): List<GuidedAction> {
        val actions = ArrayList<GuidedAction>()

        getPrpoerties(mediaDrm, drmStringKeys, drmStringNames).values
                .filterNot { (_, desc) -> desc.isNullOrEmpty() }
                .forEach { (name, desc) ->
                    actions += GuidedAction.Builder(activity)
                            .infoOnly(true)
                            .title(name)
                            .description(desc)
                            .build()
                }


        if (VERSION.SDK_INT >= VERSION_CODES.P) {
            try {
                actions += GuidedAction.Builder(requireActivity())
                        .infoOnly(true)
                        .title(R.string.widevine_string_property_name_maxHdcpLevel)
                        .description(getString(Util.convertHdcpLevelToStringRes(mediaDrm.maxHdcpLevel)))
                        .build()

                actions += GuidedAction.Builder(requireActivity())
                        .infoOnly(true)
                        .title(R.string.widevine_string_property_name_hdcpLevel)
                        .description(getString(Util.convertHdcpLevelToStringRes(mediaDrm.connectedHdcpLevel)))
                        .build()

            } catch (ignored: Exception) {
            }

        }

        return actions
    }

//    private fun getSubActions(mediaDrm: MediaDrm, keyResId: Int, namesResId: Int) =
//            getSubActions(mediaDrm, resources.getStringArray(keyResId), resources.getStringArray(namesResId))

    private fun getMediaDrmSubActions(mediaDrm: MediaDrm) = getSubActions(mediaDrm)
//    getSubActions(mediaDrm, resources.getStringArray(R.array.mediadrm_string_property_keys), resources.getStringArray(R.array.mediadrm_string_property_names))

    private fun getWidevineSubActions(widevineDrm: MediaDrm) =
            getSubActions(widevineDrm, resources.getStringArray(R.array.mediadrm_string_property_keys) + resources.getStringArray(R.array.widevine_string_property_keys),
                    resources.getStringArray(R.array.mediadrm_string_property_names) + resources.getStringArray(R.array.widevine_string_property_names))

    companion object {
        @JvmStatic
        private val TAG: String = InfoStepFragment::class.java.simpleName
        private const val API_KEY: String = BuildConfig.API_KEY
    }
}