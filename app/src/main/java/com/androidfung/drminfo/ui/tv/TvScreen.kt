package com.androidfung.drminfo.ui.tv

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.PredictiveBackHandler
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Icon
import androidx.tv.material3.ListItem
import androidx.tv.material3.ListItemDefaults
import androidx.tv.material3.LocalContentColor
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import androidx.tv.material3.darkColorScheme
import com.androidfung.drminfo.R
import com.androidfung.drminfo.model.DrmInfoUiState
import com.androidfung.drminfo.model.DrmSchemeState
import com.androidfung.drminfo.model.TvDeviceType
import com.androidfung.drminfo.model.toTvInfoItem
import com.androidfung.drminfo.util.QrCodeGenerator
import com.androidfung.drminfo.util.Util
import com.androidfung.drminfo.util.Util.toStringRes
import java.util.concurrent.CancellationException

@Immutable
data class TvInfoItem(
    val title: String,
    val description: String,
    val badge: String? = null,
    val isHeader: Boolean = false,
    val subItems: List<TvInfoItem>? = null
)

enum class TvCategory(@StringRes val titleRes: Int) {
    DRM(R.string.tv_category_drm),
    DISPLAY(R.string.tv_category_display),
    AUDIO_CODECS(R.string.tv_category_audio_codecs),
    SYSTEM(R.string.tv_category_system),
    SUMMARY_QR(R.string.tv_category_summary_qr)
}

@OptIn(ExperimentalTvMaterial3Api::class)
private val TvBrandDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4DD0E1),
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    onPrimaryContainer = Color(0xFF97F0FF),
    secondary = Color(0xFF80DEEA),
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF1E4D53),
    onSecondaryContainer = Color(0xFFA6EEF7),
    tertiary = Color(0xFF90CAF9),
    onTertiary = Color(0xFF003258),
    tertiaryContainer = Color(0xFF0B4A77),
    onTertiaryContainer = Color(0xFFD1E4FF),
    background = Color(0xFF0E1416),
    onBackground = Color(0xFFDEE3E5),
    surface = Color(0xFF131B1D),
    onSurface = Color(0xFFDEE3E5),
    surfaceVariant = Color(0xFF222E31),
    onSurfaceVariant = Color(0xFFBFC8CB)
)

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TvScreen(
    state: DrmInfoUiState,
    onRefresh: (() -> Unit)? = null,
    onDisplayConfigurationChanged: ((Context) -> Unit)? = null
) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val windowSize = LocalWindowInfo.current.containerSize

    var selectedCategory by remember { mutableStateOf(TvCategory.DRM) }
    var selectedItemWithSubActions by remember { mutableStateOf<TvInfoItem?>(null) }
    var focusedItemForPreview by remember { mutableStateOf<TvInfoItem?>(null) }
    var lastSelectedIndex by remember { mutableIntStateOf(0) }

    val mainListState = rememberLazyListState()
    val subListState = rememberLazyListState()
    val initialNavFocusRequester = remember { FocusRequester() }
    val contentFocusRequester = remember { FocusRequester() }
    val subFocusRequester = remember { FocusRequester() }

    LaunchedEffect(windowSize, configuration.orientation) {
        onDisplayConfigurationChanged?.invoke(context)
    }

    LaunchedEffect(Unit) {
        runCatching { initialNavFocusRequester.requestFocus() }
    }

    val categorizedItems = remember(state) {
        buildCategorizedTvItems(context, state)
    }

    val activeCategoryItems = categorizedItems[selectedCategory].orEmpty()

    LaunchedEffect(selectedCategory, activeCategoryItems) {
        focusedItemForPreview = activeCategoryItems.firstOrNull { !it.subItems.isNullOrEmpty() }
    }

    PredictiveBackHandler(enabled = selectedItemWithSubActions != null) { progress ->
        try {
            progress.collect {}
            selectedItemWithSubActions = null
        } catch (_: CancellationException) {
            // Cancelled
        }
    }

    LaunchedEffect(selectedItemWithSubActions) {
        if (selectedItemWithSubActions != null) {
            runCatching { subFocusRequester.requestFocus() }
        } else {
            if (lastSelectedIndex in activeCategoryItems.indices) {
                runCatching { mainListState.scrollToItem(lastSelectedIndex) }
            }
        }
    }

    MaterialTheme(
        colorScheme = TvBrandDarkColorScheme
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurface) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 48.dp, vertical = 32.dp),
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // Left Navigation & Status Rail (Leanback 10-foot Sidebar)
                Column(
                    modifier = Modifier
                        .weight(0.30f)
                        .fillMaxHeight()
                        .background(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        // Brand & Device Identity Header
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(
                                        colorResource(id = R.color.ic_launcher_background),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                val deviceSubtitle = "${state.device.manufacturer} ${state.device.model}".trim()
                                if (deviceSubtitle.isNotEmpty()) {
                                    Text(
                                        text = deviceSubtitle,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick Hardware & DRM Status Badges
                        TvQuickStatusBadges(state = state)

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Navigation Items
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            TvCategory.entries.forEachIndexed { index, category ->
                                val isSelected = selectedCategory == category && selectedItemWithSubActions == null
                                val itemCount = categorizedItems[category]?.size ?: 0
                                val navModifier = if (index == 0) {
                                    Modifier
                                        .fillMaxWidth()
                                        .focusRequester(initialNavFocusRequester)
                                } else {
                                    Modifier.fillMaxWidth()
                                }

                                ListItem(
                                    selected = isSelected,
                                    onClick = {
                                        selectedCategory = category
                                        selectedItemWithSubActions = null
                                        lastSelectedIndex = 0
                                        runCatching { contentFocusRequester.requestFocus() }
                                    },
                                    colors = ListItemDefaults.colors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.65f)
                                        } else {
                                            Color.Transparent
                                        },
                                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    ),
                                    headlineContent = {
                                        Text(
                                            text = stringResource(id = category.titleRes),
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    },
                                    trailingContent = {
                                        if (category != TvCategory.SUMMARY_QR && itemCount > 0) {
                                            Text(
                                                text = itemCount.toString(),
                                                style = MaterialTheme.typography.labelMedium,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    },
                                    modifier = navModifier.onFocusChanged { focusState ->
                                        if (focusState.isFocused && selectedCategory != category) {
                                            selectedCategory = category
                                            selectedItemWithSubActions = null
                                            lastSelectedIndex = 0
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // Bottom Refresh Action & Hint
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (onRefresh != null) {
                            ListItem(
                                selected = false,
                                onClick = onRefresh,
                                colors = ListItemDefaults.colors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                headlineContent = {
                                    Text(
                                        text = stringResource(id = R.string.action_refresh),
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Text(
                            text = stringResource(id = R.string.tv_hint_dpad_inspect),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right Guided Content & Live Detail Inspector Area
                Box(
                    modifier = Modifier
                        .weight(0.70f)
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
                        if (activeSubItem != null) {
                            // Sub-Actions Drill-down List (Full Property Breakdown)
                            TvSubItemsDrillDownPane(
                                activeSubItem = activeSubItem,
                                subListState = subListState,
                                subFocusRequester = subFocusRequester,
                                onBack = { selectedItemWithSubActions = null },
                                onCopyItem = { title, desc ->
                                    copyTvItemToClipboard(context, title, desc)
                                }
                            )
                        } else if (selectedCategory == TvCategory.SUMMARY_QR) {
                            val shareReportTitle = stringResource(id = R.string.share_report_title)
                            // 10-Foot Summary & Scannable QR Export Pane
                            TvSummaryAndQrExportPane(
                                state = state,
                                contentFocusRequester = contentFocusRequester,
                                onCopyPlainReport = {
                                    val report = Util.buildReport(context, state)
                                    copyTvItemToClipboard(
                                        context,
                                        shareReportTitle,
                                        report
                                    )
                                },
                                onCopyJsonReport = {
                                    val json = Util.buildJsonReport(context, state)
                                    copyTvItemToClipboard(
                                        context,
                                        shareReportTitle,
                                        json
                                    )
                                }
                            )
                        } else {
                            // Category Master-Detail Split View
                            Row(
                                modifier = Modifier.fillMaxSize(),
                                horizontalArrangement = Arrangement.spacedBy(24.dp)
                            ) {
                                val hasAnyGroupWithSubItems = activeCategoryItems.any { !it.subItems.isNullOrEmpty() }
                                LazyColumn(
                                    state = mainListState,
                                    modifier = Modifier
                                        .weight(if (hasAnyGroupWithSubItems) 0.56f else 1f)
                                        .fillMaxHeight(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp),
                                    contentPadding = PaddingValues(vertical = 4.dp)
                                ) {
                                    itemsIndexed(
                                        items = activeCategoryItems,
                                        key = { index, item -> "${selectedCategory.name}_${index}_${item.title}" },
                                        contentType = { _, item ->
                                            if (item.subItems.isNullOrEmpty()) "tv_item" else "tv_group"
                                        }
                                    ) { index, item ->
                                        val hasSubItems = !item.subItems.isNullOrEmpty()
                                        val itemModifier = if (index == 0) {
                                            Modifier
                                                .fillMaxWidth()
                                                .focusRequester(contentFocusRequester)
                                        } else {
                                            Modifier.fillMaxWidth()
                                        }

                                        ListItem(
                                            selected = false,
                                            onClick = {
                                                if (hasSubItems) {
                                                    lastSelectedIndex = index
                                                    selectedItemWithSubActions = item
                                                } else {
                                                    copyTvItemToClipboard(context, item.title, item.description)
                                                }
                                            },
                                            colors = ListItemDefaults.colors(
                                                containerColor = MaterialTheme.colorScheme.surface,
                                                focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                                focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                                            ),
                                            headlineContent = {
                                                Text(
                                                    text = item.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            },
                                            supportingContent = {
                                                Text(
                                                    text = item.description,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            },
                                            trailingContent = {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    item.badge?.let { badgeText ->
                                                        TvBadgePill(text = badgeText)
                                                    }
                                                    if (hasSubItems) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.ic_chevron_right),
                                                            contentDescription = null,
                                                            modifier = Modifier.size(22.dp)
                                                        )
                                                    }
                                                }
                                            },
                                            modifier = itemModifier.onFocusChanged { focusState ->
                                                if (focusState.isFocused && hasSubItems) {
                                                    focusedItemForPreview = item
                                                }
                                            }
                                        )
                                    }
                                }

                                // Live Right-Hand Inspector Card for Focused Group
                                if (hasAnyGroupWithSubItems) {
                                    TvLivePreviewCard(
                                        previewItem = focusedItemForPreview,
                                        modifier = Modifier
                                            .weight(0.44f)
                                            .fillMaxHeight()
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

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TvQuickStatusBadges(state: DrmInfoUiState) {
    val widevine = state.schemes.filterIsInstance<DrmSchemeState.Widevine>().firstOrNull()
    val securityLevel = widevine?.securityLevel
    val hdcp = widevine?.connectedHdcpLevel ?: widevine?.maxHdcpLevel

    FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        state.device.tvDeviceType?.let { tvType ->
            if (tvType != TvDeviceType.ANDROID_NON_TV) {
                TvBadgePill(
                    text = stringResource(id = tvType.stringRes),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        if (!securityLevel.isNullOrEmpty()) {
            val isL1 = securityLevel.equals("L1", ignoreCase = true)
            TvBadgePill(
                text = "Widevine $securityLevel",
                containerColor = if (isL1) Color(0xFF004F58) else Color(0xFF4E342E),
                contentColor = if (isL1) Color(0xFF97F0FF) else Color(0xFFFFE082)
            )
        }
        hdcp?.let { level ->
            TvBadgePill(
                text = stringResource(id = level.toStringRes()),
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        Util.getResolutionLabel(state.device.displayWidth, state.device.displayHeight)?.let { resTag ->
            TvBadgePill(
                text = resTag,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvBadgePill(
    text: String,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Box(
        modifier = Modifier
            .background(color = containerColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = contentColor
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvLivePreviewCard(
    previewItem: TvInfoItem?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(20.dp)
    ) {
        if (previewItem != null && !previewItem.subItems.isNullOrEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = previewItem.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                previewItem.badge?.let { badge ->
                    Spacer(modifier = Modifier.width(8.dp))
                    TvBadgePill(text = badge)
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 8.dp)
            ) {
                items(
                    items = previewItem.subItems,
                    key = { "${it.title}_${it.description}" }
                ) { sub ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = sub.title,
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = sub.description,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvSubItemsDrillDownPane(
    activeSubItem: TvInfoItem,
    subListState: androidx.compose.foundation.lazy.LazyListState,
    subFocusRequester: FocusRequester,
    onBack: () -> Unit,
    onCopyItem: (String, String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = activeSubItem.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(12.dp))
        LazyColumn(
            state = subListState,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            item(
                key = "breadcrumb_back",
                contentType = "tv_breadcrumb"
            ) {
                ListItem(
                    selected = false,
                    onClick = onBack,
                    headlineContent = {
                        Text(
                            text = stringResource(id = R.string.action_back_to_overview),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    },
                    leadingContent = {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_back),
                            contentDescription = stringResource(id = R.string.action_back_to_overview),
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(subFocusRequester)
                )
            }

            items(
                items = activeSubItem.subItems ?: emptyList(),
                key = { "${it.title}_${it.description}" },
                contentType = { "tv_sub_item" }
            ) { subItem ->
                ListItem(
                    selected = false,
                    onClick = {
                        onCopyItem(subItem.title, subItem.description)
                    },
                    colors = ListItemDefaults.colors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    ),
                    headlineContent = {
                        Text(
                            text = subItem.title,
                            style = MaterialTheme.typography.titleMedium
                        )
                    },
                    supportingContent = {
                        Text(
                            text = subItem.description,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    trailingContent = {
                        subItem.badge?.let { badgeText ->
                            TvBadgePill(text = badgeText)
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
private fun TvSummaryAndQrExportPane(
    state: DrmInfoUiState,
    contentFocusRequester: FocusRequester,
    onCopyPlainReport: () -> Unit,
    onCopyJsonReport: () -> Unit
) {
    val context = LocalContext.current
    val compactSummary = remember(state) {
        Util.buildCompactSummary(context, state)
    }
    val qrMatrix = remember(compactSummary) {
        runCatching { QrCodeGenerator.encode(compactSummary) }.getOrNull()
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(20.dp)
            )
            .padding(28.dp),
        horizontalArrangement = Arrangement.spacedBy(32.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Scannable QR Code Card
        if (qrMatrix != null) {
            Box(
                modifier = Modifier
                    .size(220.dp)
                    .background(Color.White, shape = RoundedCornerShape(16.dp))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                QrCodeCanvas(
                    matrix = qrMatrix,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Summary Details & Export Actions
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = stringResource(id = R.string.tv_qr_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = stringResource(id = R.string.tv_qr_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                compactSummary.lines().drop(1).forEach { line ->
                    Text(
                        text = line,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            ListItem(
                selected = false,
                onClick = onCopyPlainReport,
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                headlineContent = {
                    Text(
                        text = stringResource(id = R.string.tv_action_copy_plain_report),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(contentFocusRequester)
            )

            ListItem(
                selected = false,
                onClick = onCopyJsonReport,
                colors = ListItemDefaults.colors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f),
                    focusedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                    focusedContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                headlineContent = {
                    Text(
                        text = stringResource(id = R.string.tv_action_copy_json_report),
                        style = MaterialTheme.typography.titleMedium
                    )
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QrCodeCanvas(
    matrix: Array<BooleanArray>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val moduleCount = matrix.size
        if (moduleCount == 0) return@Canvas
        val cellSize = minOf(size.width, size.height) / moduleCount.toFloat()
        val offsetX = (size.width - cellSize * moduleCount) / 2f
        val offsetY = (size.height - cellSize * moduleCount) / 2f

        for (y in 0 until moduleCount) {
            val row = matrix[y]
            for (x in 0 until moduleCount) {
                if (row[x]) {
                    drawRect(
                        color = Color(0xFF0E1416),
                        topLeft = Offset(offsetX + x * cellSize, offsetY + y * cellSize),
                        size = Size(cellSize + 0.5f, cellSize + 0.5f)
                    )
                }
            }
        }
    }
}

private fun copyTvItemToClipboard(context: Context, title: String, value: String) {
    runCatching {
        val clipboard = context.getSystemService(ClipboardManager::class.java)
        clipboard?.setPrimaryClip(ClipData.newPlainText(title, value))
        Toast.makeText(
            context,
            context.getString(R.string.message_copied_to_clipboard, title),
            Toast.LENGTH_SHORT
        ).show()
    }
}

internal fun buildCategorizedTvItems(
    context: Context,
    state: DrmInfoUiState
): Map<TvCategory, List<TvInfoItem>> {
    val dev = state.device

    // 1. DRM & Security Category (Prioritized first for Android TV users)
    val drmItems = buildList {
        state.schemes.forEach { scheme ->
            val subItems = mutableListOf<TvInfoItem>()
            subItems.add(
                TvInfoItem(
                    context.getString(R.string.mediadrm_string_property_name_description),
                    scheme.description
                )
            )
            scheme.vendor?.let {
                subItems.add(TvInfoItem(context.getString(R.string.mediadrm_string_property_name_vendor), it))
            }
            scheme.version?.let {
                subItems.add(TvInfoItem(context.getString(R.string.mediadrm_string_property_name_version), it))
            }
            scheme.securityLevel?.let {
                subItems.add(
                    TvInfoItem(
                        context.getString(R.string.widevine_string_property_name_securityLevel),
                        it,
                        badge = it
                    )
                )
            }
            scheme.maxHdcpLevel?.let {
                subItems.add(
                    TvInfoItem(
                        context.getString(R.string.widevine_string_property_name_maxHdcpLevel),
                        context.getString(it.toStringRes())
                    )
                )
            }
            scheme.connectedHdcpLevel?.let {
                subItems.add(
                    TvInfoItem(
                        context.getString(R.string.widevine_string_property_name_hdcpLevel),
                        context.getString(it.toStringRes())
                    )
                )
            }
            if (scheme is DrmSchemeState.Widevine) {
                scheme.systemId?.let {
                    subItems.add(TvInfoItem(context.getString(R.string.widevine_string_property_name_systemId), it))
                }
            }
            scheme.maxSessions?.let {
                subItems.add(
                    TvInfoItem(context.getString(R.string.widevine_string_property_name_maxNumberOfSessions), it)
                )
            }
            if (scheme is DrmSchemeState.Widevine) {
                scheme.oemCryptoVersion?.let {
                    subItems.add(
                        TvInfoItem(context.getString(R.string.widevine_string_property_name_oemCryptoApiVersion), it)
                    )
                }
            }
            scheme.extraProperties.forEach { (label, value) ->
                subItems.add(TvInfoItem(label, value))
            }

            val summaryParts = buildList {
                scheme.securityLevel?.let { add(it) }
                if (scheme is DrmSchemeState.Widevine) {
                    scheme.systemId?.let { add("System ID $it") }
                }
                (scheme.connectedHdcpLevel ?: scheme.maxHdcpLevel)?.let {
                    add(context.getString(it.toStringRes()))
                }
                if (isEmpty()) {
                    add(context.getString(R.string.text_supported))
                }
            }

            add(
                TvInfoItem(
                    title = scheme.name,
                    description = summaryParts.joinToString(" • "),
                    badge = scheme.securityLevel,
                    subItems = subItems.ifEmpty { null }
                )
            )
        }

        dev.playIntegrityStatus?.takeIf { it.isNotEmpty() }?.let { status ->
            add(TvInfoItem(context.getString(R.string.text_play_integrity), status))
        }
    }

    // 2. Display & HDR Category
    val displayItems = buildList {
        dev.activeDisplayMode?.let { activeMode ->
            add(TvInfoItem(context.getString(R.string.text_active_display_mode), activeMode))
        }
        Util.formatResolution(dev.displayWidth, dev.displayHeight)?.let { resolutionText ->
            add(
                TvInfoItem(
                    title = context.getString(R.string.text_display_resolution),
                    description = resolutionText,
                    badge = Util.getResolutionLabel(dev.displayWidth, dev.displayHeight)
                )
            )
        }
        if (dev.supportedHdrTypes.isNotEmpty()) {
            val hdrTypeNames = Util.formatHdrTypes(dev.supportedHdrTypes) { id ->
                context.getString(id)
            }
            if (hdrTypeNames.isNotEmpty()) {
                add(
                    TvInfoItem(
                        title = context.getString(R.string.text_supported_hdr_types),
                        description = hdrTypeNames.joinToString(),
                        badge = "${hdrTypeNames.size} formats"
                    )
                )
            }
        }
        dev.matchContentFrameRatePreference?.let { pref ->
            add(
                TvInfoItem(
                    title = context.getString(R.string.text_match_content_frame_rate),
                    description = context.getString(Util.convertMatchContentFrameRateToStringRes(pref))
                )
            )
        }
        if (dev.supportedRefreshRates.isNotEmpty()) {
            val rates = Util.formatRefreshRates(dev.supportedRefreshRates)
            add(TvInfoItem(context.getString(R.string.text_supported_refresh_rate), rates.joinToString()))
        }
        if (dev.alternativeRefreshRates.isNotEmpty()) {
            val altRates = Util.formatRefreshRates(dev.alternativeRefreshRates)
            add(TvInfoItem(context.getString(R.string.text_seamless_refresh_rate), altRates.joinToString()))
        }
        dev.isWideColorGamut?.let { supported ->
            add(
                TvInfoItem(
                    context.getString(R.string.text_wide_color),
                    if (supported) context.getString(R.string.text_supported) else context.getString(R.string.text_not_supported)
                )
            )
        }
        if (dev.supportedDisplayModes.isNotEmpty()) {
            val modeSubItems = dev.supportedDisplayModes.mapIndexed { idx, modeStr ->
                TvInfoItem(
                    title = "Mode ${idx + 1}",
                    description = modeStr
                )
            }
            add(
                TvInfoItem(
                    title = context.getString(R.string.text_supported_display_modes),
                    description = "${dev.supportedDisplayModes.size} HDMI / panel modes",
                    subItems = modeSubItems
                )
            )
        }
    }

    // 3. Audio & Codecs Category
    val audioCodecItems = buildList {
        if (dev.supportedAudioEncodings.isNotEmpty()) {
            val audioSubItems = dev.supportedAudioEncodings.map { formatName ->
                val detail = dev.audioEncodingDetails[formatName] ?: context.getString(R.string.text_supported)
                TvInfoItem(title = formatName, description = detail)
            }
            add(
                TvInfoItem(
                    title = context.getString(R.string.section_audio),
                    description = dev.supportedAudioEncodings.joinToString(),
                    badge = "${dev.supportedAudioEncodings.size}",
                    subItems = audioSubItems
                )
            )
        }
        dev.spatialAudioSupported?.let { supported ->
            add(
                TvInfoItem(
                    context.getString(R.string.text_spatial_audio),
                    if (supported) context.getString(R.string.text_supported) else context.getString(R.string.text_not_supported)
                )
            )
        }
        dev.headTrackingSupported?.let { supported ->
            add(
                TvInfoItem(
                    context.getString(R.string.text_head_tracking),
                    if (supported) context.getString(R.string.text_supported) else context.getString(R.string.text_not_supported)
                )
            )
        }
        if (dev.secureDecoders.isNotEmpty()) {
            val secureDecoderSubItems = dev.secureDecoders.map { mime ->
                val cap = dev.codecCapabilities[mime] ?: context.getString(R.string.text_supported)
                TvInfoItem(title = mime, description = cap)
            }
            add(
                TvInfoItem(
                    title = context.getString(R.string.text_hw_secure_decode_type),
                    description = dev.secureDecoders.joinToString(),
                    badge = "${dev.secureDecoders.size}",
                    subItems = secureDecoderSubItems
                )
            )
        }
        if (dev.hardwareDecoders.isNotEmpty()) {
            val decoderSubItems = dev.hardwareDecoders.map { mime ->
                val cap = dev.codecCapabilities[mime] ?: context.getString(R.string.text_supported)
                TvInfoItem(title = mime, description = cap)
            }
            add(
                TvInfoItem(
                    title = context.getString(R.string.text_hw_decode_type),
                    description = "${dev.hardwareDecoders.size} supported",
                    badge = "${dev.hardwareDecoders.size}",
                    subItems = decoderSubItems
                )
            )
        }
        if (dev.hardwareEncoders.isNotEmpty()) {
            val encoderSubItems = dev.hardwareEncoders.map { mime ->
                TvInfoItem(title = mime, description = context.getString(R.string.text_supported))
            }
            add(
                TvInfoItem(
                    title = context.getString(R.string.text_hw_encode_type),
                    description = "${dev.hardwareEncoders.size} supported",
                    badge = "${dev.hardwareEncoders.size}",
                    subItems = encoderSubItems
                )
            )
        }
    }

    // 4. System & SoC Category
    val systemItems = buildList {
        dev.tvDeviceType?.let { tvType ->
            if (tvType != TvDeviceType.ANDROID_NON_TV) {
                add(TvInfoItem(context.getString(R.string.text_tv_type), context.getString(tvType.stringRes)))
            }
        }
        dev.hardwareProperties.forEach { prop ->
            add(prop.toTvInfoItem(context))
        }
        if (dev.abis.isNotEmpty()) {
            add(TvInfoItem(context.getString(R.string.text_abi), dev.abis.joinToString()))
        }
        dev.systemProperties.forEach { prop ->
            add(prop.toTvInfoItem(context))
        }
    }

    return mapOf(
        TvCategory.DRM to drmItems,
        TvCategory.DISPLAY to displayItems,
        TvCategory.AUDIO_CODECS to audioCodecItems,
        TvCategory.SYSTEM to systemItems,
        TvCategory.SUMMARY_QR to emptyList()
    )
}
