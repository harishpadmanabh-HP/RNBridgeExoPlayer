package com.bridgeplayer

import android.widget.FrameLayout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.onair.videoplayer.VideoPlayer
import com.onair.videoplayer.VideoResizeKeys


class VideoPlayerManager(private val reactContext: ReactApplicationContext) :
    SimpleViewManager<FrameLayout>() {

    override fun getName(): String {
        return "VideoPlayerManager"
    }

    override fun createViewInstance(reactContext: ThemedReactContext): FrameLayout {
        return FrameLayout(reactContext).apply {
            val composeView = ComposeView(reactContext)
            this.addView(composeView)
        }
    }


    @ReactProp(name = "playerProps")
    fun setPlayerProps(view: FrameLayout, props: ReadableMap?) {
        val activity = reactContext.currentActivity
        if (props != null && activity != null) {
            val videoUrl = props.getString(VideoPlayerProps.VideoUrl) ?: ""
            val title = props.getString(VideoPlayerProps.VideoTitle) ?: "Bugs Bunny"
            val startPosition = props.getDouble(VideoPlayerProps.StartPosition).toLong()
            val resizeMode =
                props.getInt(VideoPlayerProps.ResizeMode) ?: VideoResizeKeys.RESIZE_MODE_FILL

            (view.getChildAt(0) as? ComposeView)?.setContent {
                VideoPlayer(
                    videoUrl = videoUrl,
                    reactActivity = activity,
                    parentFrame = view,
                    modifier = Modifier.wrapContentSize(),
                    title = title,
                    resizeMode = resizeMode,
                )
            }
        } else
            return


    }

}

object VideoPlayerProps {
    const val VideoUrl = "videoUrl"
    const val VideoTitle = "videoTitle"
    const val StartPosition = "startPosition"
    const val IsPlaying = "isPlaying"
    const val ResizeMode = "resizeMode"
    const val VideoDescription = "videoDescription"
}
