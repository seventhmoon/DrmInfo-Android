package com.androidfung.drminfo

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.View
import android.view.animation.DecelerateInterpolator
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.compose.runtime.getValue
import androidx.core.animation.doOnEnd
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.androidfung.drminfo.ui.DrmViewModel
import com.androidfung.drminfo.ui.tv.TvScreen
import com.google.firebase.analytics.FirebaseAnalytics

@Keep
class TvActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        splashScreen.setOnExitAnimationListener { splashScreenViewProvider ->
            val splashView = splashScreenViewProvider.view
            ObjectAnimator.ofFloat(splashView, View.ALPHA, 1f, 0f).apply {
                interpolator = DecelerateInterpolator()
                duration = 220L
                doOnEnd { splashScreenViewProvider.remove() }
                start()
            }
        }

        super.onCreate(savedInstanceState)

        val drmViewModel: DrmViewModel by viewModels()
        val firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)

        setContent {
            val state by drmViewModel.state.collectAsStateWithLifecycle()

            TvScreen(
                state = state,
                onRefresh = {
                    firebaseAnalytics.logEvent("refresh_drm_info", null)
                    drmViewModel.refresh(this@TvActivity)
                },
                onDisplayConfigurationChanged = { activeContext ->
                    drmViewModel.updateDisplayInfo(activeContext)
                }
            )
        }
    }
}
