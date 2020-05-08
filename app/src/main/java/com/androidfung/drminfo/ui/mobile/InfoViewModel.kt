package com.androidfung.drminfo.ui.mobile

import android.annotation.SuppressLint
import android.media.MediaCodecList
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidfung.drminfo.Util.formatStringArray
import java.util.*
import kotlin.collections.ArrayList

class InfoViewModel : ViewModel() {
    private val properties: MutableLiveData<ObservableArrayMap<String, Any>> by lazy {
        MutableLiveData<ObservableArrayMap<String, Any>>().also(::prepareProperties)
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun prepareProperties(it: MutableLiveData<ObservableArrayMap<String, Any>>) {
        val map = ObservableArrayMap<String, Any>()

        map["sdkLevel"] = SDK_INT
        if (SDK_INT >= 21) {
            map["abis"] = Build.SUPPORTED_ABIS
        }

        if (SDK_INT >= Build.VERSION_CODES.Q) {
            val mediaCodecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            val mediaCodecInfo = mediaCodecList.codecInfos

            val supportedHardwareAcceleratedDecodeType = TreeSet<String>()

            mediaCodecInfo.forEach {
                if (!it.isEncoder && it.isHardwareAccelerated) {
                    supportedHardwareAcceleratedDecodeType.addAll(it.supportedTypes)
                }
            }

            map["hardwareAcceleratedType"] = supportedHardwareAcceleratedDecodeType
        }

        it.value = map
    }

    fun getProperties(): LiveData<ObservableArrayMap<String, Any>> {
        return properties
    }

}



