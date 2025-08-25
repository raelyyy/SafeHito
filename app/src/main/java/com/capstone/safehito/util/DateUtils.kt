package com.capstone.safehito.util

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

fun Long.toRelativeTime(): String {
    val now = System.currentTimeMillis()
    val diff = now - this

    return when {
        diff < DateUtils.MINUTE_IN_MILLIS -> "Just now"
        else -> DateUtils.getRelativeTimeSpanString(
            this,
            now,
            DateUtils.MINUTE_IN_MILLIS
        ).toString()
    }
}

fun Long.toDateOnly(): String {
    val format = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    return format.format(Date(this))
}
