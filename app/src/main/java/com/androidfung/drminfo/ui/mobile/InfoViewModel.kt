package com.androidfung.drminfo.ui.mobile

import android.annotation.SuppressLint
import android.media.MediaCodecList
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class InfoViewModel : ViewModel() {
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

        if (SDK_INT >= Build.VERSION_CODES.Q) {
            map["hardwareAcceleratedType"] = MediaCodecList(MediaCodecList.REGULAR_CODECS).codecInfos
                    .filter { !it.isEncoder && it.isHardwareAccelerated}
                    .map { it.supportedTypes.toList() }
                    .flatten()
                    .toSortedSet()
        }

        it.value = map
    }

    fun getProperties(): LiveData<ObservableArrayMap<String, Any>> {
        return properties
    }

}



