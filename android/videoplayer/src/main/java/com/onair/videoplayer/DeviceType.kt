package com.onair.videoplayer
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration

object DeviceType {

    enum class Device{
        Phone,
        Tablet,
        Tv
    }

    fun isTv(context: Context): Boolean {
        return context.packageManager.hasSystemFeature(PackageManager.FEATURE_LEANBACK)
    }

    fun isTablet(context: Context): Boolean {
        return (context.resources.configuration.screenLayout and Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE
    }

    fun isPhone(context: Context): Boolean {
        return !isTv(context) && !isTablet(context)
    }

    fun getDeviceType(context: Context): Device {
        return when {
            isTv(context) -> Device.Tv
            isTablet(context) -> Device.Tablet
            else -> Device.Phone
        }
    }
}
