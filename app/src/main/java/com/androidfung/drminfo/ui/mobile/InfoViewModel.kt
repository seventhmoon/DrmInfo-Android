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
            map["abis"] = formatStringArray(Build.SUPPORTED_ABIS)
        }

        val mediaCodecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
        val mediaCodecInfo = mediaCodecList.codecInfos

        val supportedHardwareAcceleratedDecodeType = TreeSet<String>()

        mediaCodecInfo.iterator().forEach{
            if (Build.VERSION.SDK_INT >= 29){
                if (!it.isEncoder && it.isHardwareAccelerated) {
//                    Log.d("InfoViewModel", it.canonicalName + "\t" + it.isHardwareAccelerated)

                    it.supportedTypes.iterator().forEach { supportedType ->
//                        Log.d("InfoViewModel", supportedType.toString())
                        supportedHardwareAcceleratedDecodeType.add(supportedType.toString())
                    }
                }
            }
        }

        map["hardwareAcceleratedType"] = supportedHardwareAcceleratedDecodeType

        it.value = map
    }

    fun getProperties(): LiveData<ObservableArrayMap<String, Any>> {
        return properties
    }

}



