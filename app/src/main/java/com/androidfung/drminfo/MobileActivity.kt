package com.androidfung.drminfo

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import androidx.activity.viewModels
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityManagerCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import com.androidfung.drminfo.databinding.ActivityMobileBinding
import com.androidfung.drminfo.ui.mobile.*
import com.google.android.gms.ads.*
import com.google.firebase.analytics.FirebaseAnalytics

@Keep
class MobileActivity : AppCompatActivity() {
    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var binding: ActivityMobileBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_mobile)
        binding.lifecycleOwner = this
        binding.handler = MyHandlers()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        val infoViewModel: InfoViewModel by viewModels()
        infoViewModel.getProperties().observe(this, Observer (binding::setInfoProperties))

        val clearKeyViewModel: ClearKeyViewModel by viewModels()
        clearKeyViewModel.getProperties().observe(this, Observer(binding::setClearkeyProperties))

        val widevineViewModel: WidevineViewModel by viewModels()
        widevineViewModel.getProperties().observe(this, Observer(binding::setWidevineProperties))

        val playreadyViewModel: PlayreadyViewModel by viewModels()
        playreadyViewModel.getProperties().observe(this, Observer(binding::setPlayreadyProperties))

        val wiseplayViewModel: WiseplayViewModel by viewModels()
        wiseplayViewModel.getProperties().observe(this, Observer(binding::setWiseplayProperties))

        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager;
        val lowRam = ActivityManagerCompat.isLowRamDevice(activityManager);
        val runningTestHarness = (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ActivityManager.isRunningInUserTestHarness() else ActivityManager.isRunningInTestHarness())
        val monkey = ActivityManager.isUserAMonkey()

        if (!lowRam && !runningTestHarness && !monkey) {
            val viewStub = findViewById<ViewStub>(R.id.viewstub_ad_container)

            if (viewStub != null) {
                val bannerAdHolder : ViewGroup = viewStub.inflate() as ViewGroup
                val adView = findViewById<AdView>(R.id.adView)
                MobileAds.initialize(this) {}
                loadAdMobBannerAd(adView, bannerAdHolder)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun loadAdMobBannerAd(adView: AdView, bannerAdHolder : ViewGroup) {
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                // Code to be executed when an ad finishes loading.
                Log.i("Ads", "onAdLoaded")
                bannerAdHolder.visibility = View.VISIBLE
                adView.visibility = View.VISIBLE
            }

            override fun onAdFailedToLoad(errorCode: Int) {
                // Code to be executed when an ad request fails.
                Log.i("Ads", "onAdFailedToLoad")
                bannerAdHolder.visibility = View.GONE
                adView.visibility = View.GONE
            }

            override fun onAdOpened() {
                // Code to be executed when an ad opens an overlay that
                // covers the screen.
                Log.i("Ads", "onAdOpened")
            }

            override fun onAdLeftApplication() {
                // Code to be executed when the user has left the app.
                Log.i("Ads", "onAdLeftApplication")
            }

            override fun onAdClosed() {
                // Code to be executed when when the user is about to return
                // to the app after tapping on an ad.
                Log.i("Ads", "onAdClosed")
            }
        }
        adView.loadAd(AdRequest.Builder().build())
    }

    companion object {
        private val TAG = MobileActivity::class.java.simpleName
    }
}