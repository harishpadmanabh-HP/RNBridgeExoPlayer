package com.onair.videoplayer

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Build
import android.view.Window
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.WindowManager
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}


fun Context.setLandscape() {
    val activity = this.findActivity()
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
}

@SuppressLint("SourceLockedOrientationActivity")
fun Context.setPortrait() {
    val activity = this.findActivity()
    activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
}

@RequiresApi(Build.VERSION_CODES.R)
fun Context.hideSystemBars() {
    val activity = this.findActivity()
    activity?.window?.setDecorFitsSystemWindows(false)
    activity?.window?.let { window ->
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        // Configure the behavior of the hidden system bars.
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { view, windowInsets ->
            windowInsetsController.hide(WindowInsetsCompat.Type.navigationBars())
            ViewCompat.onApplyWindowInsets(view, windowInsets)
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        } else {
            window.insetsController?.apply {
                hide(WindowInsets.Type.systemBars())
                systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        }
    }


}

@Composable
fun getDialogWindow(): Window? = (LocalView.current.parent as? DialogWindowProvider)?.window

@Composable
fun getActivityWindow(): Window? = LocalView.current.context.findActivity()?.window


fun VideoProps.toMediaItem(isLive: Boolean = false): MediaItem {
    if (this.videoUrl.isNullOrEmpty())
        throw IllegalArgumentException("Invalid video URL passed to exoplayer.")

    val metaData = MediaMetadata.Builder()
        .setArtworkUri(Uri.parse(this.artworkUrl))
        .setTitle(title)
        .setDisplayTitle(title)
        .setDescription(description)
        .setAlbumArtist(artistName)
        .build()

    return when {
        this.videoUrl.endsWith(".mpd") -> {
            MediaItem.Builder()
                .setUri(this.videoUrl)
                .setMimeType(MimeTypes.APPLICATION_MPD)
                .setMediaMetadata(metaData)
                .also {
                    if(isLive)
                        it .setLiveConfiguration(
                            MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.02f).build()
                        )
                }
                .build()
        }

        this.videoUrl.endsWith(".m3u8") -> {
            // HLS
            MediaItem.Builder()
                .setUri(this.videoUrl)
                .setMimeType(MimeTypes.APPLICATION_M3U8)
                .setMediaMetadata(metaData)
                .also {
                    if(isLive)
                        it .setLiveConfiguration(
                            MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.02f).build()
                        )
                }
                .build()
        }

        else -> {
            // Normal progressive
            MediaItem.Builder()
                .setUri(this.videoUrl)
                .setMediaMetadata(metaData)
                .also {
                    if(isLive)
                        it .setLiveConfiguration(
                            MediaItem.LiveConfiguration.Builder().setMaxPlaybackSpeed(1.02f).build()
                        )
                }
                .build()
        }
    }

}
