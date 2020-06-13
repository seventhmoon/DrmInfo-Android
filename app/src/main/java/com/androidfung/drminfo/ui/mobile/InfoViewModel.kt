package com.androidfung.drminfo.ui.mobile

import android.annotation.SuppressLint
import android.app.Application
import android.media.MediaCodecList
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.view.Display.DEFAULT_DISPLAY
import android.view.Display.HdrCapabilities.*
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.androidfung.drminfo.R

class InfoViewModel(application: Application) : AndroidViewModel(application) {
    private val properties: MutableLiveData<ObservableArrayMap<String, Any>> by lazy {
        MutableLiveData<ObservableArrayMap<String, Any>>().also(::prepareProperties)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun prepareProperties(it: MutableLiveData<ObservableArrayMap<String, Any>>) {
        val map = ObservableArrayMap<String, Any>()

        map["sdkLevel"] = SDK_INT
        if (SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            map["abis"] = Build.SUPPORTED_ABIS
        }

        if (SDK_INT >= Build.VERSION_CODES.N){
            val displayManager = DisplayManagerCompat.getInstance(getApplication())
            val display = displayManager.getDisplay(DEFAULT_DISPLAY)

            display?.let{
//            if (display != null) {
                val supportedHdrType = it.hdrCapabilities.supportedHdrTypes
                if (supportedHdrType.isNotEmpty()) {
                    map["supportedHdrType"] = supportedHdrType.map(this::convertHdrTypeToString).filterNotNull()
                }

//                if (SDK_INT >= Build.VERSION_CODES.O) {
//                        map["hdr"] = display.isHdr
//                        map["wideColorGamut"] = display.isWideColorGamut
//                }
            }
        }

//        if (SDK_INT >= Build.VERSION_CODES.O){
//            val validConfig = Configuration().apply { setToDefaults() }
//
//            map["wideColorGamut"] = validConfig.isScreenWideColorGamut
//            map["hdr"] = validConfig.isScreenHdr
//
//        }

        if (SDK_INT >= Build.VERSION_CODES.Q) {
            map["hardwareAcceleratedType"] = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                    .filter { !it.isEncoder && it.isHardwareAccelerated}
                    .map { it.supportedTypes.toList() }
                    .flatten()
                    .toSortedSet()
        }

        it.value = map
    }

    private fun convertHdrTypeToString(hdrType: Int) : String?{
        with(getApplication<Application>()) {
            return when (hdrType) {
                HDR_TYPE_DOLBY_VISION -> getString(R.string.hdr_type_dolby_vision)
                HDR_TYPE_HDR10 -> getString(R.string.hdr_type_hdr10)
                HDR_TYPE_HDR10_PLUS -> getString(R.string.hdr_type_hdr10_plus)
                HDR_TYPE_HLG -> getString(R.string.hdr_type_hlg)
                else -> null
            }
        }
    }

    fun getProperties(): LiveData<ObservableArrayMap<String, Any>> {
        return properties
    }

}



