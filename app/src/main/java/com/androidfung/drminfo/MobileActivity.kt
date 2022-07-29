package com.androidfung.drminfo

import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.NestedScrollView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.androidfung.drminfo.databinding.ActivityMobileBinding
import com.androidfung.drminfo.ui.mobile.*
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics

//import com.google.firebase.appcheck.FirebaseAppCheck
//import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory

//import com.google.firebase.ktx.Firebase
//import com.google.firebase.remoteconfig.ktx.remoteConfig
//import com.google.firebase.remoteconfig.ktx.remoteConfigSettings

@Keep
class MobileActivity : AppCompatActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var binding: ActivityMobileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_mobile)
        binding.lifecycleOwner = this
        binding.handler = MyHandlers()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)?.apply {
            setSupportActionBar(this)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            @Suppress("UNUSED_VARIABLE") val nestedScrollView =
                findViewById<NestedScrollView>(R.id.view_nestedscrollview)?.apply {
                    val bound = Rect().also { rect ->
                        this.getHitRect(rect)
                    }

                    this.setOnScrollChangeListener { _, _, _, _, _ ->
                        val infoHeadline = findViewById<TextView>(R.id.tv_info_headline)
                        val infoHeadlineVisible = infoHeadline.getLocalVisibleRect(bound)
                        toolbar?.subtitle =
                            if (infoHeadlineVisible) null
                            else "${Build.MANUFACTURER} ${Build.MODEL}"

                    }
                }
        }

        val infoViewModel: InfoViewModel by viewModels()
        infoViewModel.getProperties().observe(this, Observer(binding::setInfoProperties))

        val clearKeyViewModel: ClearKeyViewModel by viewModels()
        clearKeyViewModel.getProperties().observe(this, Observer(binding::setClearkeyProperties))

        val widevineViewModel: WidevineViewModel by viewModels()
        widevineViewModel.getProperties().observe(this, Observer(binding::setWidevineProperties))

        val playreadyViewModel: PlayreadyViewModel by viewModels()
        playreadyViewModel.getProperties().observe(this, Observer(binding::setPlayreadyProperties))

        val wiseplayViewModel: WiseplayViewModel by viewModels()
        wiseplayViewModel.getProperties().observe(this, Observer(binding::setWiseplayProperties))

        // Firebase Analytics
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

//        // Firebase Remote Config
//        val remoteConfig = Firebase.remoteConfig.apply {
//            val configSettings = remoteConfigSettings {
//                minimumFetchIntervalInSeconds = 3600
//            }
//            setConfigSettingsAsync(configSettings)
//            setDefaultsAsync(R.xml.remote_config_defaults)
//            fetchAndActivate()
//        }

//        FirebaseApp.initializeApp(this)
//        val firebaseAppCheck = FirebaseAppCheck.getInstance()
//        firebaseAppCheck.installAppCheckProviderFactory(
//            SafetyNetAppCheckProviderFactory.getInstance()
//        )

        val bannerAdHolder = findViewById<ViewGroup>(R.id.container_ad)
        val adView = findViewById<AdView>(R.id.adView)

//        val adView =  AdView(this)
//        val adUnitId = getString(R.string.ad_unit_id)
//        adView.adUnitId= adUnitId
//        bannerAdHolder.addView(adView)

        adView?.let {
            MobileAds.initialize(this) {}
            loadAdMobBannerAd(it, bannerAdHolder)
//            loadAdMobBannerAd(adView, bannerAdHolder)
        }

        // Create a new coroutine since repeatOnLifecycle is a suspend function
//        lifecycleScope.launch(Dispatchers.Main) {
//            lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                WindowInfoTracker.getOrCreate(this@MobileActivity)
//                    .windowLayoutInfo(this@MobileActivity)
//                    .collect {
//                        //todo
//                        Log.d("***", it.displayFeatures.joinToString())
//                    }
//            }
//        }
    }

    private fun loadAdMobBannerAd(adView: AdView, bannerAdHolder: ViewGroup? = null) {
//        bannerAdHolder?.let {
//
//            val screenDensity = resources.displayMetrics.density
////            val adWidth = (bannerAdHolder.measuredWidth / screenDensity).toInt()
//            val adWidth = resources.displayMetrics.run { widthPixels / density }.toInt() - 32
//
//            val adSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(this, adWidth )
////            val adSize = AdSize.getCurrentOrientationInlineAdaptiveBannerAdSize(this,  320)
//            Log.d(TAG, "adSize screenDensity: $screenDensity, adWidth: $adWidth")
//            adView.adSize = adSize
//        }

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i("Ads", "onAdLoaded")
                bannerAdHolder?.visibility = View.VISIBLE
                adView.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(error: LoadAdError) {
//                super.onAdFailedToLoad(error)
                // Code to be executed when an ad request fails.
                Log.i("Ads", "onAdFailedToLoad, error: $error")
                bannerAdHolder?.visibility = View.GONE
                adView.visibility = View.GONE
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.i("Ads", "onAdOpened")
            }

            override fun onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
                Log.i("Ads", "onAdClosed")
            }

            override fun onAdClicked() {
                // Code to be executed when the user clicks on an ad.
                Log.i("Ads", "onAdClicked")
            }
        }

//        val requestConfiguration = RequestConfiguration.Builder().setTestDeviceIds()
        val adRequest = AdRequest.Builder().build()
//        MobileAds.setRequestConfiguration(requestConfiguration.build())
        adView.loadAd(adRequest)
    }

    companion object {
        @JvmStatic
        private val TAG = MobileActivity::class.java.simpleName
    }
}