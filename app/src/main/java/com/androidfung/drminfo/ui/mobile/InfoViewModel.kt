package com.androidfung.drminfo.ui.mobile

import android.annotation.SuppressLint
import android.app.Application
import android.media.MediaCodecList
import android.os.Build
import android.os.Build.VERSION
import android.os.Build.VERSION_CODES
import android.view.Display.DEFAULT_DISPLAY
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import com.androidfung.drminfo.Util
import java.text.DecimalFormat

class InfoViewModel(application: Application, private val state: SavedStateHandle) : AndroidViewModel(application) {
    private val properties: MutableLiveData<ObservableArrayMap<String, Any>> by lazy {
        MutableLiveData<ObservableArrayMap<String, Any>>().also(::prepareProperties)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun prepareProperties(it: MutableLiveData<ObservableArrayMap<String, Any>>) {
        val map = ObservableArrayMap<String, Any>()

        map["sdkLevel"] = VERSION.SDK_INT

        // Supported Arch
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
            map["abis"] = Build.SUPPORTED_ABIS
        }

        if (VERSION.SDK_INT >= VERSION_CODES.M) {
            map["securityPatch"] = Build.VERSION.SECURITY_PATCH

            // Display Related
            val displayManager = DisplayManagerCompat.getInstance(getApplication())
            displayManager.getDisplay(DEFAULT_DISPLAY)?.also { display ->
                val supportedRefreshRate = display.supportedModes.map { mode -> mode.refreshRate }.toSortedSet()
                map["supportedRefreshRate"] = supportedRefreshRate.map(DecimalFormat("0.##")::format)

                if (VERSION.SDK_INT >= VERSION_CODES.N) {
                    val supportedHdrType = display.hdrCapabilities.supportedHdrTypes
                    if (supportedHdrType.isNotEmpty()) {
                        map["supportedHdrType"] = supportedHdrType.map(this::convertHdrTypeToString).sorted()
                    }
                }
            }
        }

        // Codec Related
        if (VERSION.SDK_INT >= VERSION_CODES.Q) {
            map["hardwareAcceleratedType"] =
                MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                    .filter { codec -> codec.isHardwareAccelerated && !codec.isEncoder }
                    .flatMap { codec -> codec.supportedTypes.asIterable() }
                    .toSortedSet()
        }

        if (VERSION.SDK_INT >= VERSION_CODES.R) {
            map["releaseOrCodename"] = Build.VERSION.RELEASE_OR_CODENAME
        }

//        if (BuildCompat.isAtLeastS()) {
//            map["socManufacturer"] = Build.SOC_MANUFACTURER
//            map["socModel"] = Build.SOC_MODEL
//            map["mediaPerformanceClass"] = Build.VERSION.MEDIA_PERFORMANCE_CLASS
//        }

        it.value = map
    }

    private fun convertHdrTypeToString(hdrType: Int) =
            getApplication<Application>().getString(Util.convertHdrTypeToStringRes(hdrType))

    fun getProperties(): LiveData<ObservableArrayMap<String, Any>> = properties

}



