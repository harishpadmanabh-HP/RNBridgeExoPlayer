package com.onair.videoplayer.settingsOverlays

import android.util.Log
import android.view.KeyEvent.KEYCODE_BACK
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.focusGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.media3.common.Format
import com.onair.videoplayer.DeviceType
import com.onair.videoplayer.LogTag
import com.onair.videoplayer.R

@Composable
fun AudioTracksDialog(
    isFromSettings: Boolean,
    audioTracksAvailable: List<Format>,
    selectedTrack: Format?,
    onAudioTrackSelected: (Format?) -> Unit,
    dismissDialog: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }

    val trackNames = remember {
        mutableStateListOf<String>()
    }

    val scrollState = rememberScrollState()
    LaunchedEffect(audioTracksAvailable) {
        audioTracksAvailable.forEach {
            trackNames.add(it.language ?: UNKNOWN_LANGUAGE)
        }
        focusRequester.requestFocus()
    }

    BackHandler(!isFromSettings) {
        dismissDialog()
    }

    Column(
        modifier = modifier
            .then(
                if (DeviceType.isTv(context))
                    Modifier.fillMaxWidth(.2f)
                else
                    Modifier.fillMaxWidth(.4f)
            )
            .focusRequester(focusRequester)
            .focusGroup()
            .background(colorResource(id = R.color.black_dialog_bg))
            .onKeyEvent {
                Log.i(LogTag, "onKeyEvent ${it.nativeKeyEvent}")
                if (it.nativeKeyEvent.keyCode == KEYCODE_BACK) {
                    dismissDialog()
                }
                false
            }
            .verticalScroll(scrollState)
    ) {
        TrackItemOff(
            label = "Auto",
            isSelected = selectedTrack == null,
            onSubtitleTrackSelected = onAudioTrackSelected
        )
        audioTracksAvailable.forEach {
            TrackItem(
                track = it,
                isSelected = selectedTrack == it,
                onSubtitleTrackSelected = onAudioTrackSelected
            )
        }

    }

    LaunchedEffect(Unit) {
        val selectedIndex = audioTracksAvailable.indexOfFirst { it == selectedTrack }
        scrollState.animateScrollTo(selectedIndex+1)
    }

}
