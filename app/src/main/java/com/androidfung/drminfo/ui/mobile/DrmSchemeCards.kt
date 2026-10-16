package com.androidfung.drminfo.ui.mobile

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.androidfung.drminfo.R
import com.androidfung.drminfo.model.DrmSchemeState
import com.androidfung.drminfo.ui.components.InfoCardContainer
import com.androidfung.drminfo.ui.components.LabelValueItem
import com.androidfung.drminfo.util.Util.toStringRes

internal fun matchesQuery(query: String, vararg fields: String?): Boolean {
    if (query.isBlank()) return true
    val trimmed = query.trim()
    return fields.any { it?.contains(trimmed, ignoreCase = true) == true }
}

@Composable
fun WidevineCard(
    scheme: DrmSchemeState.Widevine,
    modifier: Modifier = Modifier,
    searchQuery: String = ""
) {
    val cardTitleMatches = searchQuery.isNotBlank() &&
            matchesQuery(searchQuery, scheme.name, scheme.description, "widevine")
    val effectiveQuery = if (cardTitleMatches) "" else searchQuery

    InfoCardContainer(
        title = scheme.description,
        iconRes = R.drawable.ic_security,
        modifier = modifier
    ) {
        MediaDrmCardContent(scheme = scheme, searchQuery = effectiveQuery)

        scheme.systemId?.let { value ->
            val label = stringResource(id = R.string.widevine_string_property_name_systemId)
            if (matchesQuery(effectiveQuery, label, value)) {
                LabelValueItem(
                    label = label,
                    value = value,
                    allowCopy = true
                )
            }
        }

        scheme.securityLevel?.let { value ->
            val label = stringResource(id = R.string.widevine_string_property_name_securityLevel)
            if (matchesQuery(effectiveQuery, label, value)) {
                LabelValueItem(
                    label = label,
                    value = value,
                    allowCopy = true
                )
            }
        }

        scheme.maxSessions?.let { value ->
            val label = stringResource(id = R.string.widevine_string_property_name_maxNumberOfSessions)
            if (matchesQuery(effectiveQuery, label, value)) {
                LabelValueItem(
                    label = label,
                    value = value,
                    allowCopy = true
                )
            }
        }

        scheme.oemCryptoVersion?.let { value ->
            val label = stringResource(id = R.string.widevine_string_property_name_oemCryptoApiVersion)
            if (matchesQuery(effectiveQuery, label, value)) {
                LabelValueItem(
                    label = label,
                    value = value,
                    allowCopy = true
                )
            }
        }

        scheme.extraProperties.forEach { (label, value) ->
            if (matchesQuery(effectiveQuery, label, value)) {
                LabelValueItem(
                    label = label,
                    value = value,
                    allowCopy = true
                )
            }
        }
    }
}

@Composable
fun MediaDrmCard(
    scheme: DrmSchemeState,
    modifier: Modifier = Modifier,
    searchQuery: String = ""
) {
    val cardTitleMatches = searchQuery.isNotBlank() &&
            matchesQuery(searchQuery, scheme.name, scheme.description, scheme.id)
    val effectiveQuery = if (cardTitleMatches) "" else searchQuery

    InfoCardContainer(
        title = scheme.description,
        iconRes = R.drawable.ic_security,
        modifier = modifier
    ) {
        MediaDrmCardContent(scheme = scheme, searchQuery = effectiveQuery)

        scheme.extraProperties.forEach { (label, value) ->
            if (matchesQuery(effectiveQuery, label, value)) {
                LabelValueItem(
                    label = label,
                    value = value,
                    allowCopy = true
                )
            }
        }
    }
}

@Composable
private fun MediaDrmCardContent(
    scheme: DrmSchemeState,
    searchQuery: String = ""
) {
    scheme.vendor?.let { value ->
        val label = stringResource(id = R.string.mediadrm_string_property_name_vendor)
        if (matchesQuery(searchQuery, label, value)) {
            LabelValueItem(
                label = label,
                value = value,
                allowCopy = true
            )
        }
    }

    scheme.version?.let { value ->
        val label = stringResource(id = R.string.mediadrm_string_property_name_version)
        if (matchesQuery(searchQuery, label, value)) {
            LabelValueItem(
                label = label,
                value = value,
                allowCopy = true
            )
        }
    }

    scheme.maxHdcpLevel?.let { level ->
        val label = stringResource(id = R.string.widevine_string_property_name_maxHdcpLevel)
        val value = stringResource(id = level.toStringRes())
        if (matchesQuery(searchQuery, label, value)) {
            LabelValueItem(
                label = label,
                value = value,
                allowCopy = true
            )
        }
    }

    scheme.connectedHdcpLevel?.let { level ->
        val label = stringResource(id = R.string.widevine_string_property_name_hdcpLevel)
        val value = stringResource(id = level.toStringRes())
        if (matchesQuery(searchQuery, label, value)) {
            LabelValueItem(
                label = label,
                value = value,
                allowCopy = true
            )
        }
    }
}
