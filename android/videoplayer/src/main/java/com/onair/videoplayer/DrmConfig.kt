package com.onair.videoplayer

import androidx.annotation.Keep

@Keep
data class DrmConfig(
    val type: String,
    val licenseUrl:String
)