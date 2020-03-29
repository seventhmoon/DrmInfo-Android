package com.androidfung.drminfo

import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Keep
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.androidfung.drminfo.databinding.ActivityMobileBinding
import com.androidfung.drminfo.ui.mobile.*
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import com.google.firebase.analytics.FirebaseAnalytics

@Keep
class MobileActivity : AppCompatActivity() {
    private lateinit var mFirebaseAnalytics: FirebaseAnalytics
    private lateinit var mBinding: ActivityMobileBinding
    private var mBannerAdHolder: ViewGroup? = null
    private var mAdView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_mobile)
        mBinding.lifecycleOwner = this
        mBinding.handler = MyHandlers()

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this)

        MobileAds.initialize(this, getString(R.string.ad_app_id))
        mBannerAdHolder = findViewById(R.id.container_ad)
        mAdView = findViewById(R.id.adView)

        loadAdMobBannerAd()

        val viewModelProvider = ViewModelProvider(this)

        val infoViewModel = viewModelProvider.get(InfoViewModel::class.java)
        infoViewModel.properties.observe(this, Observer (mBinding::setInfoProperties))
//        {mBinding::setInfoProperties infoProperties: ObservableArrayMap<String, Any> -> mBinding.infoProperties = infoProperties })
//        infoViewModel.properties.observe(this, Observer{infoProperties -> mBinding.infoProperties(infoProperties)})

        val clearKeyViewModel: MediaDrmViewModel = viewModelProvider.get(ClearKeyViewModel::class.java)
        clearKeyViewModel.properties.observe(this, Observer(mBinding::setClearkeyProperties))
//        clearKeyViewModel.properties.observe(this, Observer { clearkeyProperties -> mBinding.setClearkeyProperties(clearkeyProperties) })


        val widevineViewModel = viewModelProvider.get(WidevineViewModel::class.java)
        widevineViewModel.properties.observe(this, Observer(mBinding::setWidevineProperties))
//        widevineViewModel.properties.observe(this, Observer { widevineProperties -> mBinding.setWidevineProperties(widevineProperties) })

        val playreadyViewModel: MediaDrmViewModel = viewModelProvider.get(PlayreadyViewModel::class.java)
        playreadyViewModel.properties.observe(this, Observer(mBinding::setPlayreadyProperties))
//        playreadyViewModel.properties.observe(this, Observer { playreadyProperties -> mBinding.setPlayreadyProperties(playreadyProperties) })

    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    private fun loadAdMobBannerAd() {
        if (mAdView != null) {
            mAdView!!.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    // Code to be executed when an ad finishes loading.
                    Log.i("Ads", "onAdLoaded")
                    mBannerAdHolder!!.visibility = View.VISIBLE
                    mAdView!!.visibility = View.VISIBLE
                }

                override fun onAdFailedToLoad(errorCode: Int) {
                    // Code to be executed when an ad request fails.
                    Log.i("Ads", "onAdFailedToLoad")
                    mBannerAdHolder!!.visibility = View.GONE
                    mAdView!!.visibility = View.GONE
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
            mAdView!!.loadAd(AdRequest.Builder().build())
        }
    }
    companion object {
        private val TAG = MobileActivity::class.java.simpleName
    }
}