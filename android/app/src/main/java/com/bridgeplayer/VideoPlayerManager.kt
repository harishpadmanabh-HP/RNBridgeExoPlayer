package com.bridgeplayer

import android.content.Intent
import android.util.Log
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.onair.videoplayer.FullScreenVideoPlayerActivity
import com.onair.videoplayer.VideoPlayer
import com.onair.videoplayer.VideoResizeKeys


class VideoPlayerManager(private val reactContext: ReactApplicationContext) :
    SimpleViewManager<ComposeView>() {

    override fun getName(): String {
        return "VideoPlayerManager"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): ComposeView {
        return ComposeView(reactContext).apply {
            Log.d("VideoPlayerViewManager", "View created")
        }
    }


    @ReactProp(name = "playerProps")
    fun setPlayerProps(view: ComposeView, props: ReadableMap?) {
        if (props == null) return

        val videoUrl = props.getString(VideoPlayerProps.VideoUrl) ?: ""
        val title = props.getString(VideoPlayerProps.VideoTitle) ?: "Bugs Bunny"
        val startPosition = props.getDouble(VideoPlayerProps.StartPosition).toLong()
        val resizeMode = props.getInt(VideoPlayerProps.ResizeMode)?:VideoResizeKeys.RESIZE_MODE_FIT

        Log.d("FullScreenVideoActivity", "View url props set  $startPosition")

        view.setContent {
            val context = LocalContext.current
            VideoPlayer(
                videoUrl = videoUrl,
                startPosition = startPosition,
                modifier = Modifier.fillMaxSize(),
                title = title,
                onFullScreenToggle = { isFullScreen, currentPosition ->
                    Log.i("VideoPlayerViewManager", " $isFullScreen onFullScreenToggle: $currentPosition")
                    val intent = Intent(context, FullScreenVideoActivity::class.java).also {
                        it.putExtra(FullScreenVideoActivity.VIDEO_URL, videoUrl)
                        it.putExtra(
                            FullScreenVideoActivity.VIDEO_START_POSITION,
                            currentPosition
                        )
                    }
                    context.startActivity(intent)
                })
        }


    }

}

object VideoPlayerProps {
    const val VideoUrl = "videoUrl"
    const val VideoTitle = "videoTitle"
    const val StartPosition = "startPosition"
    const val IsPlaying = "isPlaying"
    const val ResizeMode = "resizeMode"
}
