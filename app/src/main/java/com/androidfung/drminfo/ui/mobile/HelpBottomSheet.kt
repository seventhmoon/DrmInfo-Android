package com.androidfung.drminfo.ui.mobile

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.androidfung.drminfo.BuildConfig
import com.androidfung.drminfo.R

private const val GITHUB_REPO_URL = "https://github.com/seventhmoon/DrmInfo-Android"
private const val PRIVACY_POLICY_URL = "https://github.com/seventhmoon/DrmInfo-Android/blob/master/PRIVACY_POLICY.md"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpBottomSheet(
    onDismissRequest: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current

    PredictiveBackHandler(enabled = sheetState.isVisible) { progress ->
        try {
            progress.collect {
                // Progressive back gesture received; sheet will animate on completion
            }
            sheetState.hide()
            onDismissRequest()
        } catch (_: Exception) {
            // Back gesture cancelled
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        shape = MaterialTheme.shapes.extraLarge,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = stringResource(id = R.string.title_about),
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${stringResource(id = R.string.app_name)} v${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = stringResource(id = R.string.about_license),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, GITHUB_REPO_URL.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_github),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "GitHub",
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                OutlinedButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, PRIVACY_POLICY_URL.toUri())
                        context.startActivity(intent)
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_security),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.action_privacy_policy),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider(
                thickness = 0.5.dp,
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = stringResource(id = R.string.title_faq),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            FaqAccordionItem(
                question = stringResource(id = R.string.faq_q_can_fix),
                answer = stringResource(id = R.string.faq_a_can_fix)
            )
            FaqAccordionItem(
                question = stringResource(id = R.string.faq_q_levels),
                answer = stringResource(id = R.string.faq_a_levels)
            )
            FaqAccordionItem(
                question = stringResource(id = R.string.faq_q_downgrade),
                answer = stringResource(id = R.string.faq_a_downgrade)
            )
            FaqAccordionItem(
                question = stringResource(id = R.string.faq_q_netflix_sd),
                answer = stringResource(id = R.string.faq_a_netflix_sd)
            )
            FaqAccordionItem(
                question = stringResource(id = R.string.faq_q_hdr),
                answer = stringResource(id = R.string.faq_a_hdr)
            )
            FaqAccordionItem(
                question = stringResource(id = R.string.faq_q_hdcp),
                answer = stringResource(id = R.string.faq_a_hdcp)
            )
            FaqAccordionItem(
                question = stringResource(id = R.string.faq_q_mpc),
                answer = stringResource(id = R.string.faq_a_mpc)
            )
            FaqAccordionItem(
                question = stringResource(id = R.string.faq_q_refresh_rate),
                answer = stringResource(id = R.string.faq_a_refresh_rate)
            )
            FaqAccordionItem(
                question = stringResource(id = R.string.faq_q_schemes),
                answer = stringResource(id = R.string.faq_a_schemes)
            )
            FaqAccordionItem(
                question = stringResource(id = R.string.faq_q_privacy),
                answer = stringResource(id = R.string.faq_a_privacy)
            )
        }
    }
}

@Composable
private fun FaqAccordionItem(
    question: String,
    answer: String
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded }
            .padding(vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = question,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            Icon(
                painter = painterResource(id = R.drawable.ic_chevron_right),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.rotate(if (isExpanded) 90f else 0f)
            )
        }

        AnimatedVisibility(visible = isExpanded) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )
            }
        }
    }
    HorizontalDivider(
        thickness = 0.5.dp,
        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    )
}
