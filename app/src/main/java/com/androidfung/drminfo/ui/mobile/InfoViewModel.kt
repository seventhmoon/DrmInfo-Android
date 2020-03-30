package com.androidfung.drminfo.ui.mobile

import android.app.Application
import android.os.Build
import androidx.databinding.ObservableArrayMap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.androidfung.drminfo.Util.formatStringArray

class InfoViewModel (application: Application?) : AndroidViewModel(application!!) {
//    private var mProperties: MutableLiveData<ObservableArrayMap<String, Any>>? = null

//    private val sdkLevel : MutableLiveData<String> by lazy {
//        MutableLiveData<String>().also{
//            it.value = Build.VERSION.SDK_INT.toString()
//        }
//    }


     private val properties: MutableLiveData<ObservableArrayMap<String, Any>> by lazy {

        MutableLiveData<ObservableArrayMap<String, Any>>().also {
            val map = ObservableArrayMap<String, Any>()

            map["sdkLevel"] = Build.VERSION.SDK_INT
            if (Build.VERSION.SDK_INT >= 21) {
                map["abis"] = formatStringArray(Build.SUPPORTED_ABIS)
            }

            it.value = map
        }
    }


    fun getProperties(): LiveData<ObservableArrayMap<String, Any>> {
        return properties
    }

//    val properties: LiveData<ObservableArrayMap<String, Any>>
//        get() {
//            if (mProperties == null) {
//                mProperties = MutableLiveData()
//                val map = ObservableArrayMap<String, Any>()
//
//                map["sdkLevel"] = Build.VERSION.SDK_INT
//                if (Build.VERSION.SDK_INT >= 21) {
//                    map["abis"] = formatStringArray(Build.SUPPORTED_ABIS)
//                }
//
//                mProperties!!.value = map
//            }
//            return mProperties!!
//        }
}



