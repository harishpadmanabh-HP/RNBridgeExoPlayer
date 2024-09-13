package com.bridgeplayer

import android.os.Build
import android.view.View
import android.widget.FrameLayout
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
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
            this.id = View.generateViewId()
            this.tag = "VideoPlayerManager"
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
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
                    modifier = Modifier.fillMaxSize(),
                    title = title,
                    resizeMode = resizeMode,
                    onFullScreenChanged = {
                        val params = Arguments.createMap().apply {
                            putBoolean("isFullScreen", it)
                        }
                        sendEvent("onFullScreenChanged", params)
                    },
                    onPlayerError = { error ->
                        error?.let {
                            val params = Arguments.createMap().apply {
                                putString("playerError", it)
                            }
                            sendEvent("onPlayerError", params)
                        }

                    }
                )
            }
        } else
            return


    }

    private fun sendEvent(eventName: String, params: WritableMap?) {
        reactContext.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            .emit(eventName, params)
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
