package com.onair.videoplayer

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.onair.videoplayer.ui.theme.VideoPlayerTheme

class FullScreenVideoPlayerActivity : ComponentActivity() {

    private val startPosition by lazy {
        intent.getLongExtra(VIDEO_START_POSITION, 0L)
    }

    private val videoUrl by lazy {
        intent.getStringExtra(VIDEO_URL)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            VideoPlayerTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    videoUrl?.let { url ->
                        VideoPlayer(
                            videoUrl = url,
                            startPosition=startPosition,
                            resizeMode = VideoResizeKeys.RESIZE_MODE_FILL,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent().apply {
            putExtra(VIDEO_START_POSITION,13)
            putExtra(IS_PLAYING, true)
        }
        setResult(Activity.RESULT_OK, intent)
        super.onBackPressed()
    }

    companion object {
        const val VIDEO_START_POSITION = "videoStartPosition"
        const val VIDEO_URL = "videoUrl"
        const val IS_PLAYING = "isPlaying"


    }
}

