package com.udacity.util

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class DownloadJobSummary(val id: Long, val title: String, val description: String, val outcome: Boolean): Parcelable