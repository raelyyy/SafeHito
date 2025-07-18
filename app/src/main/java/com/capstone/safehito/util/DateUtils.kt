package com.capstone.safehito.util

import android.text.format.DateUtils
import java.text.SimpleDateFormat
import java.util.*

fun Long.toRelativeTime(): String {
    return DateUtils.getRelativeTimeSpanString(
        this,
        System.currentTimeMillis(),
        DateUtils.MINUTE_IN_MILLIS
    ).toString()
}

fun Long.toDateOnly(): String {
    val format = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())
    return format.format(Date(this))
}
