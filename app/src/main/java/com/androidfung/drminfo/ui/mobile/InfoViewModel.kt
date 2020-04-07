package com.androidfung.drminfo.ui.mobile

import android.os.Build
import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.androidfung.drminfo.Util.formatStringArray

class InfoViewModel : ViewModel() {
     private val properties: MutableLiveData<ObservableArrayMap<String, Any>> by lazy {
        MutableLiveData<ObservableArrayMap<String, Any>>().also(::prepareProperties)
    }

    private fun prepareProperties(it : MutableLiveData<ObservableArrayMap<String, Any>>){
        val map = ObservableArrayMap<String, Any>()

        map["sdkLevel"] = Build.VERSION.SDK_INT
        if (Build.VERSION.SDK_INT >= 21) {
            map["abis"] = formatStringArray(Build.SUPPORTED_ABIS)
        }

        it.value = map
    }

    fun getProperties(): LiveData<ObservableArrayMap<String, Any>> {
        return properties
    }

}



