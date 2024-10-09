package com.onair.videoplayer

import android.app.Activity
import android.util.Log
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastState
import com.google.android.gms.cast.framework.CastStateListener

fun getCastContext(activity: Activity) = CastContext.getSharedInstance(activity)

fun getCastStateListener() = CastStateListener { newState ->
    if (newState != CastState.NO_DEVICES_AVAILABLE) {
        Log.i(LogTag, "Cast state changed: $newState")
    }
}

fun CastContext?.addCastStateListener(listener: CastStateListener) {
    this?.addCastStateListener(listener)
}

fun CastContext?.removeCastStateListener(listener: CastStateListener) {
    this?.removeCastStateListener(listener)
}


