package com.androidfung.drminfo.ui.mobile

import android.media.MediaDrm
import android.media.UnsupportedSchemeException
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import androidx.core.content.res.ResourcesCompat
import androidx.leanback.app.GuidedStepSupportFragment
import androidx.leanback.widget.GuidanceStylist.Guidance
import androidx.leanback.widget.GuidedAction
import com.androidfung.drminfo.Constants
import com.androidfung.drminfo.R

class InfoStepFragment : GuidedStepSupportFragment() {
    override fun onCreateGuidance(savedInstanceState: Bundle?): Guidance {
        val title = getString(R.string.app_name)
        val description = getString(R.string.text_on_android_tv)
        val icon = ResourcesCompat.getDrawable(resources, R.mipmap.ic_launcher_round, null)
        return Guidance(title, description, null, icon)
    }

    private fun getMediaDrmSubActions(mediaDrm: MediaDrm): MutableList<GuidedAction> {
        val subActions: MutableList<GuidedAction> = ArrayList()
        val drmStringKeys = resources.getStringArray(R.array.mediadrm_string_property_keys)
        val drmStringNames = resources.getStringArray(R.array.mediadrm_string_property_names)
        for (i in drmStringKeys.indices) {
            try {
                val value = mediaDrm.getPropertyString(drmStringKeys[i])
                val name = drmStringNames[i]
                if (!TextUtils.isEmpty(value)) {
                    subActions.add(GuidedAction.Builder(activity)
                            .infoOnly(true)
                            .title(name)
                            .description(value)
                            .build())
                }
            } catch (ex: IllegalStateException) {
                Log.d(TAG, ex.toString())
            } catch (ex: IllegalArgumentException) {
                Log.d(TAG, ex.toString())
            }
        }
        return subActions
    }

    private fun getWidevineSubActions(widevineDrm: MediaDrm): MutableList<GuidedAction> {
        val subActions = getMediaDrmSubActions(widevineDrm)
        val wvStringKeys = resources.getStringArray(R.array.widevine_string_property_keys)
        val wvStringNames = resources.getStringArray(R.array.widevine_string_property_names)
        for (i in wvStringKeys.indices) {
            try {
                val value = widevineDrm.getPropertyString(wvStringKeys[i])
                if (!TextUtils.isEmpty(value)) {
                    subActions.add(GuidedAction.Builder(activity)
                            .infoOnly(true)
                            .title(wvStringNames[i])
                            .description(value)
                            .build())
                }
            } catch (ex: IllegalStateException) {
                Log.d(TAG, ex.toString())
            } catch (ex: IllegalArgumentException) {
                Log.d(TAG, ex.toString())
            }
        }
        return subActions
    }

    override fun onCreateActions(actions: MutableList<GuidedAction>, savedInstanceState: Bundle?) {

        //info section here
        val infoTitles = intArrayOf(R.string.text_brand, R.string.text_product, R.string.text_model, R.string.text_sdk_level, R.string.text_hdr, R.string.text_wide_color)
        val descriptions = arrayOf(Build.BRAND, Build.PRODUCT, Build.MODEL, Build.VERSION.SDK_INT.toString(),
                if (this.resources.getBoolean(R.bool.is_highdr)) getString(R.string.text_supported) else getString(R.string.text_not_supported),
                if (this.resources.getBoolean(R.bool.is_widecg)) getString(R.string.text_supported) else getString(R.string.text_not_supported))
        for (i in infoTitles.indices) {
            if (descriptions[i] != null) {
                actions.add(GuidedAction.Builder(activity)
                        .infoOnly(true)
                        .title(getString(infoTitles[i]))
                        .description(descriptions[i])
                        .build())
            }
        }
        val uuids = arrayOf(Constants.CLEARKEY_UUID, Constants.WIDEVINE_UUID, Constants.PLAYREADY_UUID, Constants.VERIMATRIX_UUID, Constants.MARLIN_UUID, Constants.PRIMETIME_UUID)
        val titles = intArrayOf(R.string.text_clearkey_name, R.string.text_widevine_name, R.string.text_playready_name, R.string.text_verimatrix_name, R.string.text_marlin_name, R.string.text_primetime_name)
        for (i in uuids.indices) {
            val supported = MediaDrm.isCryptoSchemeSupported(uuids[i])
            var subActions: List<GuidedAction>? = null
            if (supported) {
                try {
                    val mediaDrm = MediaDrm(uuids[i])
                    subActions = if (Constants.WIDEVINE_UUID === uuids[i]) {
                        getWidevineSubActions(mediaDrm)
                    } else {
                        getMediaDrmSubActions(mediaDrm)
                    }
                } catch (e: UnsupportedSchemeException) {
                    e.printStackTrace()
                }
            }
            val infoOnly = subActions == null || subActions.isEmpty()
            actions.add(GuidedAction.Builder(activity)
                    .infoOnly(infoOnly)
                    .subActions(if (infoOnly) null else subActions)
                    .title(getString(titles[i]))
                    .description(if (supported) getString(R.string.text_supported) else getString(R.string.text_not_supported))
                    .build())
        }
    }

    companion object {
        private val TAG = InfoStepFragment::class.java.simpleName
    }
}