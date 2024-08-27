package com.onair.videoplayer

import android.app.Application
import android.util.Log
import androidx.annotation.Keep
import androidx.lifecycle.AndroidViewModel
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class VideoPlayerViewModel(private val application: Application) : AndroidViewModel(application) {

    private val _videoProps = MutableStateFlow(VideoProps())
    val videoProps = _videoProps.asStateFlow()

    fun makeVideoProps(
        videoUrl: String? = null,
        title: String? = null,
        description: String? = null,
        artistName: String? = null,
        artworkUrl: String? = null,
    ) {
        try {
            _videoProps.value = _videoProps.value.copy(
                videoUrl = videoUrl ?: _videoProps.value.videoUrl,
                title = title ?: _videoProps.value.title,
                description = description ?: _videoProps.value.description,
                artistName = artistName ?: _videoProps.value.artistName,
                artworkUrl = artworkUrl ?: _videoProps.value.artworkUrl,
                exoPlayer = initializePlayer()
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initializePlayer(): ExoPlayer {

        val trackSelector = DefaultTrackSelector(application).apply {
            setParameters(buildUponParameters().setMaxVideoSizeSd())
        }

      return  ExoPlayer.Builder(application)
            .setSeekBackIncrementMs(5 * 1000L)
            .setSeekForwardIncrementMs(10 * 1000L)
            .setTrackSelector(trackSelector)
            .build().apply {
                this.setMediaItem(videoProps.value.toMediaItem())
                this.prepare()
                this.playWhenReady = playWhenReady
                this.addListener(initExoplayerListener())
            }
    }

    private fun initExoplayerListener(): Player.Listener {
        return object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                onIsPlayingChanged(isPlaying)
                Log.d(LogTag, "onIsPlayingChanged: $isPlaying")
            }

            override fun onPlayerError(error: PlaybackException) {
                //onPlayerError(error.errorCode)
                val cause = error.cause
                Log.e(LogTag, "PlaybackException Occurred: ${error.message} caused by $cause")
            }

            override fun onMetadata(metadata: Metadata) {
                super.onMetadata(metadata)
                Log.d(LogTag, "onMetadata: ${metadata}")
            }

            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                super.onMediaMetadataChanged(mediaMetadata)
                Log.d(LogTag, "onMediaMetadataChanged: ${mediaMetadata}")
            }

            override fun onTracksChanged(tracks: Tracks) {
                super.onTracksChanged(tracks)
                Log.d(LogTag, "onTracksChanged: ${tracks.groups}")
            }

        }

    }

    fun releasePlayer(){
        videoProps.value.exoPlayer?.removeListener(initExoplayerListener())
        videoProps.value.exoPlayer?.release()
        _videoProps.value = VideoProps()
        Log.i(LogTag, "Player released")
    }
}

@Keep
data class VideoProps(
    val videoUrl: String? = null,
    val title: String = "",
    val description: String = "",
    val artistName: String = "",
    val artworkUrl: String = "",
    val exoPlayer: ExoPlayer? = null,
)