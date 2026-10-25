package com.androidfung.drminfo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidfung.drminfo.ui.DrmViewModel
import com.androidfung.drminfo.ui.tv.TvScreen

@Keep
class TvActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val drmViewModel: DrmViewModel by viewModels()

        setContent {
            val state by drmViewModel.state.collectAsStateWithLifecycle()

            TvScreen(
                state = state,
                onRefresh = {
                    drmViewModel.refresh(this@TvActivity)
                },
                onDisplayConfigurationChanged = { activeContext ->
                    drmViewModel.updateDisplayInfo(activeContext)
                }
            )
        }
    }
}
