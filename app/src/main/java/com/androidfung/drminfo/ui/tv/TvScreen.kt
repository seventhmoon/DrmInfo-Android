package com.androidfung.drminfo.ui.tv

import android.content.Context
import android.media.AudioManager
import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import com.androidfung.drminfo.Constants
import com.androidfung.drminfo.R
import com.androidfung.drminfo.Util
import java.util.UUID

data class TvInfoItem(
    val title: String,
    val description: String,
    val isHeader: Boolean = false,
    val subItems: List<TvInfoItem>? = null
)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun TvScreen(
    infoProperties: Map<String, Any>?,
    clearkeyProperties: Map<String, Any>?,
    widevineProperties: Map<String, Any>?,
    playreadyProperties: Map<String, Any>?,
    wiseplayProperties: Map<String, Any>?
) {
    val context = LocalContext.current
    var selectedItemWithSubActions by remember { mutableStateOf<TvInfoItem?>(null) }

    BackHandler(enabled = selectedItemWithSubActions != null) {
        selectedItemWithSubActions = null
    }

    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(start = 48.dp, end = 48.dp, top = 36.dp, bottom = 36.dp),
                horizontalArrangement = Arrangement.spacedBy(48.dp)
            ) {
                // Left Guidance Panel (Leanback Keyline Layout)
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.Top
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .background(colorResource(id = R.color.ic_launcher_background), shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                            contentDescription = null,
                            modifier = Modifier.size(64.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        text = selectedItemWithSubActions?.title ?: stringResource(id = R.string.app_name),
                        style = MaterialTheme.typography.headlineLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (selectedItemWithSubActions != null) {
                            stringResource(id = R.string.text_supported)
                        } else {
                            "${Build.MANUFACTURER} ${Build.MODEL}"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Right Guided Actions Panel with Slide Transition
                val mainItems = remember(infoProperties, clearkeyProperties, widevineProperties, playreadyProperties, wiseplayProperties) {
                    buildTvInfoItems(
                        context = context,
                        clearkeyProperties = clearkeyProperties,
                        widevineProperties = widevineProperties,
                        playreadyProperties = playreadyProperties,
                        wiseplayProperties = wiseplayProperties
                    )
                }

                Box(
                    modifier = Modifier
                        .weight(0.65f)
                        .fillMaxHeight()
                ) {
                    AnimatedContent(
                        targetState = selectedItemWithSubActions,
                        transitionSpec = {
                            if (targetState != null) {
                                slideInHorizontally { width -> width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> -width } + fadeOut()
                            } else {
                                slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                        slideOutHorizontally { width -> width } + fadeOut()
                            }
                        },
                        label = "LeanbackActionsSlideTransition"
                    ) { activeSubItem ->
                        if (activeSubItem == null) {
                            // Main Actions List
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                items(mainItems) { item ->
                                    val hasSubItems = !item.subItems.isNullOrEmpty()

                                    ListItem(
                                        selected = false,
                                        onClick = {
                                            if (hasSubItems) {
                                                selectedItemWithSubActions = item
                                            }
                                        },
                                        headlineContent = {
                                            Text(
                                                text = item.title,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        },
                                        supportingContent = {
                                            Text(
                                                text = item.description,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        trailingContent = {
                                            if (hasSubItems) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_chevron_right),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        } else {
                            // Sub-Actions List (DRM Properties)
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding = PaddingValues(vertical = 12.dp)
                            ) {
                                items(activeSubItem.subItems ?: emptyList()) { subItem ->
                                    ListItem(
                                        selected = false,
                                        onClick = {},
                                        headlineContent = {
                                            Text(
                                                text = subItem.title,
                                                style = MaterialTheme.typography.titleLarge
                                            )
                                        },
                                        supportingContent = {
                                            Text(
                                                text = subItem.description,
                                                style = MaterialTheme.typography.bodyLarge
                                            )
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun buildTvInfoItems(
    context: Context,
    clearkeyProperties: Map<String, Any>?,
    widevineProperties: Map<String, Any>?,
    playreadyProperties: Map<String, Any>?,
    wiseplayProperties: Map<String, Any>?
): List<TvInfoItem> = buildList {
    add(TvInfoItem(context.getString(R.string.text_brand), Build.BRAND))
    add(TvInfoItem(context.getString(R.string.text_product), Build.PRODUCT))
    add(TvInfoItem(context.getString(R.string.text_hardware), Build.HARDWARE))
    add(TvInfoItem(context.getString(R.string.text_display), Build.DISPLAY))

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        add(TvInfoItem(context.getString(R.string.text_soc_manufacturer), Build.SOC_MANUFACTURER))
        add(TvInfoItem(context.getString(R.string.text_soc_model), Build.SOC_MODEL))
    }

    add(TvInfoItem(context.getString(R.string.text_type), Build.TYPE))
    add(TvInfoItem(context.getString(R.string.text_fingerprint), Build.FINGERPRINT))
    add(TvInfoItem(context.getString(R.string.text_sdk_level), Build.VERSION.SDK_INT.toString()))

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA) {
        add(TvInfoItem(context.getString(R.string.text_sdk_level_full), Build.VERSION.SDK_INT_FULL.toString()))
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val audioManager = context.getSystemService(AudioManager::class.java)
        audioManager?.spatializer?.let { spatializer ->
            add(TvInfoItem(context.getString(R.string.text_spatial_audio), if (spatializer.isAvailable) context.getString(R.string.text_supported) else context.getString(R.string.text_not_supported)))
            add(TvInfoItem(context.getString(R.string.text_head_tracking), if (spatializer.isHeadTrackerAvailable) context.getString(R.string.text_supported) else context.getString(R.string.text_not_supported)))
        }
    }

    add(TvInfoItem(context.getString(R.string.text_abi), Build.SUPPORTED_ABIS.joinToString()))

    // DRM Schemes
    addDrmSchemeItem(context, Constants.CLEARKEY_UUID, clearkeyProperties)
    addDrmSchemeItem(context, Constants.WIDEVINE_UUID, widevineProperties)
    addDrmSchemeItem(context, Constants.PLAYREADY_UUID, playreadyProperties)
    addDrmSchemeItem(context, Constants.WISEPLAY_UUID, wiseplayProperties)
}

private fun MutableList<TvInfoItem>.addDrmSchemeItem(
    context: Context,
    uuid: UUID,
    properties: Map<String, Any>?
) {
    if (!properties.isNullOrEmpty()) {
        val title = context.getString(Util.convertCryptoSchemeToStringRes(uuid))
        val subItems = properties.mapNotNull { (key, value) ->
            val valueStr = (value as Any?)?.toString()
            if (!valueStr.isNullOrEmpty()) {
                TvInfoItem(title = key, description = valueStr)
            } else null
        }

        add(
            TvInfoItem(
                title = title,
                description = context.getString(R.string.text_supported),
                subItems = subItems.ifEmpty { null }
            )
        )
    }
}
