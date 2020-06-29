package com.androidfung.drminfo.ui.mobile

import android.annotation.SuppressLint
import android.app.Application
import android.media.MediaCodecList
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.view.Display.DEFAULT_DISPLAY
import androidx.core.hardware.display.DisplayManagerCompat
import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.androidfung.drminfo.Util
import java.text.DecimalFormat

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

        if (SDK_INT >= Build.VERSION_CODES.M) {
            val displayManager = DisplayManagerCompat.getInstance(getApplication())
            val display = displayManager.getDisplay(DEFAULT_DISPLAY)

            display?.let {
                val supportedRefreshRate = it.supportedModes.map { mode ->
                    mode.refreshRate
                }.toSortedSet()

                if (supportedRefreshRate.size > 1) {
                    map["supportedRefreshRate"] = supportedRefreshRate.map(DecimalFormat("0.##")::format)
                }

                if (SDK_INT >= Build.VERSION_CODES.N) {
                    val supportedHdrType = it.hdrCapabilities.supportedHdrTypes
                    if (supportedHdrType.isNotEmpty()) {
                        map["supportedHdrType"] = supportedHdrType.map(this::convertHdrTypeToString).sorted()
                    }
                }
            }
        }

        if (SDK_INT >= Build.VERSION_CODES.Q) {
            map["hardwareAcceleratedType"] = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                    .filterNot { it.isEncoder }
                    .filter { it.isHardwareAccelerated }
                    .map { it.supportedTypes.toList() }
                    .flatten()
                    .toSortedSet()
        }

        it.value = map
    }

    private fun convertHdrTypeToString(hdrType: Int) =
            getApplication<Application>().getString(Util.convertHdrTypeToStringRes(hdrType))

    fun getProperties(): LiveData<ObservableArrayMap<String, Any>> = properties

}



