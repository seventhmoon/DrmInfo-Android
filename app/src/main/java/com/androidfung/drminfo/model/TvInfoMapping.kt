package com.androidfung.drminfo.model

import android.content.Context
import com.androidfung.drminfo.ui.tv.TvInfoItem

/**
 * Maps [DisplayableProperty] directly to [TvInfoItem].
 */
fun DisplayableProperty.toTvInfoItem(context: Context): TvInfoItem {
    return TvInfoItem(
        title = context.getString(labelRes),
        description = value
    )
}
