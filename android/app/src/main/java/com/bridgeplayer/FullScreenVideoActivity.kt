package com.bridgeplayer

import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.media3.exoplayer.ExoPlayer
import com.facebook.react.ReactApplication
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import com.onair.videoplayer.VideoPlayer
import com.onair.videoplayer.VideoResizeKeys
import com.onair.videoplayer.ui.theme.VideoPlayerTheme

class FullScreenVideoActivity : AppCompatActivity() {

    private val startPosition by lazy {
        intent.getLongExtra(VIDEO_START_POSITION, 0L)
    }

    private val videoUrl by lazy {
        intent.getStringExtra(VIDEO_URL)
    }

    var currentPosition = 0L

    private lateinit var exoPlayer: ExoPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {


            VideoPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    videoUrl?.let { url ->
             /*           VideoPlayer(
                            videoUrl = url,
                            startPosition = startPosition,
                            resizeMode = VideoResizeKeys.RESIZE_MODE_FILL,
                            onProgressChange = {
                                currentPosition = it
                                Log.i("FullScreenVideoActivity", "onProgressChange: $it")
                            },
                            onPlayerAttached = {
                                exoPlayer = it
                            },
                            modifier = Modifier.fillMaxSize()
                        )*/
                    }
                }
            }


        }
    }

    override fun onBackPressed() {
        Log.i("FullScreenVideoActivity", "onBackPress start ${exoPlayer.currentPosition}")

        val reactContext =
            (application as? ReactApplication)?.reactNativeHost?.reactInstanceManager?.currentReactContext
        if (reactContext == null) {
            Log.e("FullScreenVideoActivity", "ReactContext is null")
            return
        }
        val params: WritableMap = Arguments.createMap()
        params.putDouble("startPosition", exoPlayer.currentPosition.toDouble())
        params.putBoolean("isPlaying", true)

        reactContext?.getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
            ?.emit("onFullScreenResult", params)
        Log.i("FullScreenVideoActivity", "onBackPress done")

        super.onBackPressed()
        finish()
    }

    companion object {
        const val VIDEO_START_POSITION = "videoStartPosition"
        const val VIDEO_URL = "videoUrl"
        const val IS_PLAYING = "isPlaying"

    }
}

