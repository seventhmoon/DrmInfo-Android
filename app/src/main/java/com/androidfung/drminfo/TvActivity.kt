package com.androidfung.drminfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.androidfung.drminfo.ui.mobile.ClearKeyViewModel
import com.androidfung.drminfo.ui.mobile.InfoViewModel
import com.androidfung.drminfo.ui.mobile.PlayreadyViewModel
import com.androidfung.drminfo.ui.mobile.WidevineViewModel
import com.androidfung.drminfo.ui.mobile.WiseplayViewModel
import com.androidfung.drminfo.ui.tv.TvScreen
import com.google.firebase.analytics.FirebaseAnalytics

@Keep
class TvActivity : ComponentActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val infoViewModel: InfoViewModel by viewModels()
        val clearKeyViewModel: ClearKeyViewModel by viewModels()
        val widevineViewModel: WidevineViewModel by viewModels()
        val playreadyViewModel: PlayreadyViewModel by viewModels()
        val wiseplayViewModel: WiseplayViewModel by viewModels()

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        setContent {
            val infoProperties by infoViewModel.properties.observeAsState()
            val clearkeyProperties by clearKeyViewModel.properties.observeAsState()
            val widevineProperties by widevineViewModel.properties.observeAsState()
            val playreadyProperties by playreadyViewModel.properties.observeAsState()
            val wiseplayProperties by wiseplayViewModel.properties.observeAsState()

            TvScreen(
                infoProperties = infoProperties,
                clearkeyProperties = clearkeyProperties,
                widevineProperties = widevineProperties,
                playreadyProperties = playreadyProperties,
                wiseplayProperties = wiseplayProperties
            )
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= TRIM_MEMORY_UI_HIDDEN) {
            // Release heavy UI resources if needed
        }
    }

    companion object {
        private val TAG = TvActivity::class.java.simpleName
    }
}