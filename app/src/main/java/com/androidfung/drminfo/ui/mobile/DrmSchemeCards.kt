package com.androidfung.drminfo.ui.mobile

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.androidfung.drminfo.R
import com.androidfung.drminfo.model.DrmSchemeState
import com.androidfung.drminfo.ui.components.CardSectionHeader
import com.androidfung.drminfo.ui.components.InfoCardContainer
import com.androidfung.drminfo.ui.components.LabelValueItem
import com.androidfung.drminfo.ui.components.LocalSnackbarHostState
import com.androidfung.drminfo.ui.components.SkeletonCard
import com.androidfung.drminfo.ui.components.TagChip
import com.androidfung.drminfo.util.Util.toStringRes

@Composable
fun WidevineCard(
    scheme: DrmSchemeState.Widevine,
    modifier: Modifier = Modifier
) {
    InfoCardContainer(
        title = scheme.description,
        iconRes = R.drawable.ic_security,
        modifier = modifier
    ) {
        MediaDrmCardContent(scheme = scheme)

        scheme.systemId?.let {
            LabelValueItem(
                label = stringResource(id = R.string.widevine_string_property_name_systemId),
                value = it,
                allowCopy = true
            )
        }

        scheme.securityLevel?.let {
            LabelValueItem(
                label = stringResource(id = R.string.widevine_string_property_name_securityLevel),
                value = it,
                allowCopy = true
            )
        }

        scheme.oemCryptoVersion?.let {
            LabelValueItem(
                label = stringResource(id = R.string.widevine_string_property_name_oemCryptoApiVersion),
                value = it,
                allowCopy = true
            )
        }

        scheme.extraProperties.forEach { (label, value) ->
            LabelValueItem(
                label = label,
                value = value,
                allowCopy = true
            )
        }
    }
}

@Composable
fun MediaDrmCard(
    scheme: DrmSchemeState,
    modifier: Modifier = Modifier
) {
    InfoCardContainer(
        title = scheme.description,
        iconRes = R.drawable.ic_security,
        modifier = modifier
    ) {
        MediaDrmCardContent(scheme = scheme)

        scheme.extraProperties.forEach { (label, value) ->
            LabelValueItem(
                label = label,
                value = value,
                allowCopy = true
            )
        }
    }
}

@Composable
private fun MediaDrmCardContent(scheme: DrmSchemeState) {
    scheme.vendor?.let {
        LabelValueItem(
            label = stringResource(id = R.string.mediadrm_string_property_name_vendor),
            value = it,
            allowCopy = true
        )
    }

    scheme.version?.let {
        LabelValueItem(
            label = stringResource(id = R.string.mediadrm_string_property_name_version),
            value = it,
            allowCopy = true
        )
    }

    scheme.maxHdcpLevel?.let {
        LabelValueItem(
            label = stringResource(id = R.string.widevine_string_property_name_maxHdcpLevel),
            value = stringResource(id = it.toStringRes()),
            allowCopy = true
        )
    }

    scheme.connectedHdcpLevel?.let {
        LabelValueItem(
            label = stringResource(id = R.string.widevine_string_property_name_hdcpLevel),
            value = stringResource(id = it.toStringRes()),
            allowCopy = true
        )
    }
}
