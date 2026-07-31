package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.content.Context
import android.graphics.ImageFormat
import android.graphics.PixelFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.params.StreamConfigurationMap
import androidx.camera.featurecombinationquery.CameraDeviceSetupCompat
import androidx.camera.featurecombinationquery.CameraDeviceSetupCompatFactory
import androidx.collection.ArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class CameraViewModel(
    application: Application,
    private val state: SavedStateHandle
) : AndroidViewModel(application) {

    private val properties: MutableLiveData<ArrayMap<String, Any>> by lazy {
        MutableLiveData<ArrayMap<String, Any>>().also { liveData ->
            viewModelScope.launch(Dispatchers.Default) {
                prepareProperties(liveData)
            }
        }
    }

    private fun prepareProperties(liveData: MutableLiveData<ArrayMap<String, Any>>) {
        val context = getApplication<Application>()
        val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager

        if (cameraManager == null) {
            liveData.postValue(ArrayMap())
            return
        }

        val map = ArrayMap<String, Any>()

        try {
            val cameraIds = cameraManager.cameraIdList
            map["cameraCount"] = cameraIds.size

            val factory = CameraDeviceSetupCompatFactory(context)
            val cameraDetails = mutableListOf<String>()
            val formatDetails = mutableListOf<String>()

            for (cameraId in cameraIds) {
                val characteristics = runCatching {
                    cameraManager.getCameraCharacteristics(cameraId)
                }.getOrNull()

                val facing = when (characteristics?.get(CameraCharacteristics.LENS_FACING)) {
                    CameraCharacteristics.LENS_FACING_FRONT -> "Front"
                    CameraCharacteristics.LENS_FACING_BACK -> "Back"
                    CameraCharacteristics.LENS_FACING_EXTERNAL -> "External"
                    else -> "Unknown"
                }

                val setupCompat: CameraDeviceSetupCompat? = runCatching {
                    factory.getCameraDeviceSetupCompat(cameraId)
                }.getOrNull()

                val supportStatusStr = if (setupCompat != null) "Supported" else "Unsupported"
                cameraDetails.add("Camera $cameraId ($facing): $supportStatusStr")

                // Extract Stream Configuration Formats & Max Resolutions
                val streamConfigMap: StreamConfigurationMap? =
                    characteristics?.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

                if (streamConfigMap != null) {
                    val formats = streamConfigMap.outputFormats ?: intArrayOf()
                    val formatResolutionStrings = formats.asIterable().mapNotNull { format ->
                        val sizes = streamConfigMap.getOutputSizes(format)
                        val maxSize = sizes?.maxByOrNull { it.width * it.height } ?: return@mapNotNull null

                        val formatName = FORMAT_NAME_MAP[format] ?: ("0x" + Integer.toHexString(format).uppercase(Locale.US))
                        val megaPixels = (maxSize.width.toDouble() * maxSize.height.toDouble()) / 1_000_000.0
                        val mpString = if (megaPixels >= 0.1) String.format(Locale.US, " (%.1fMP)", megaPixels) else ""

                        "$formatName: ${maxSize.width}x${maxSize.height}$mpString"
                    }

                    if (formatResolutionStrings.isNotEmpty()) {
                        formatDetails.add("Camera $cameraId:\n" + formatResolutionStrings.joinToString("\n"))
                    }
                }
            }

            if (cameraDetails.isNotEmpty()) {
                map["cameraDetails"] = cameraDetails
            }
            if (formatDetails.isNotEmpty()) {
                map["formatDetails"] = formatDetails
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        liveData.postValue(map)
    }

    fun getProperties(): LiveData<ArrayMap<String, Any>> = properties

    companion object {
        private val FORMAT_NAME_MAP = mapOf(
            ImageFormat.JPEG to "JPEG",
            ImageFormat.YUV_420_888 to "YUV_420_888",
            ImageFormat.PRIVATE to "PRIVATE",
            ImageFormat.RAW_SENSOR to "RAW_SENSOR",
            ImageFormat.RAW10 to "RAW10",
            ImageFormat.RAW12 to "RAW12",
            ImageFormat.DEPTH16 to "DEPTH16",
            ImageFormat.HEIC to "HEIC",
            ImageFormat.Y8 to "Y8",
            ImageFormat.NV21 to "NV21",
            PixelFormat.RGBA_8888 to "RGBA_8888",
            PixelFormat.RGB_565 to "RGB_565"
        )
    }
}
