package com.onair.videoplayer.settingsOverlays

import android.util.Log
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.Format
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.DefaultTrackNameProvider
import com.onair.videoplayer.LogTag
import com.onair.videoplayer.R
import com.onair.videoplayer.settingsOverlays.TvFocusIndicators.MyHighlightIndication


@OptIn(UnstableApi::class)
@Composable
fun TrackItem(
    track: Format,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSubtitleTrackSelected: (Format?) -> Unit
) {
    val highlightIndication = remember { MyHighlightIndication() }
    var interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()


    val context = LocalContext.current


    Box(modifier = modifier
        .fillMaxWidth()
        .height(45.dp)
        .clickable(
            interactionSource = interactionSource,
            indication = highlightIndication,
            enabled = true,
            onClick = {
                if (!isSelected)
                    onSubtitleTrackSelected(track)
            }
        )) {
        Row(
            modifier = modifier
                .wrapContentWidth()
                .height(45.dp)
                .padding(12.dp),
        ) {
            if (isSelected)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(colorResource(id = R.color.red))
                )

            Text(
                text = DefaultTrackNameProvider(context.resources).getTrackName(track),
                fontFamily = FontFamily(
                    listOf(if (isSelected) Font(R.font.dm_sans_bold) else Font(R.font.dm_sans_light))
                ),
                fontSize = 14.sp,
                color = colorResource(id = if (isFocused) R.color.red else R.color.white),
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }

}

@Composable
fun TrackItemOff(
    label:String="Off",
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onSubtitleTrackSelected: (Format?) -> Unit,
) {
    val highlightIndication = remember { MyHighlightIndication() }
    var interactionSource = remember { MutableInteractionSource() }
    val isFocused by interactionSource.collectIsFocusedAsState()
    Log.i(LogTag, "isFocused OFF $isFocused")

    Box(modifier = modifier
        .fillMaxWidth()
        .height(45.dp)
        .clickable(
            interactionSource = interactionSource,
            indication = highlightIndication,
            enabled = true,
            onClick = {
                if (!isSelected)
                    onSubtitleTrackSelected(null)
            }
        )) {
        Row(
            modifier = modifier
                .wrapContentWidth()
                .height(45.dp)
                .padding(12.dp),
        ) {
            if (isSelected)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(2.dp)
                        .background(colorResource(id = R.color.red))
                )

            Text(
                text = label,
                fontFamily = FontFamily(
                    listOf(if (isSelected) Font(R.font.dm_sans_bold) else Font(R.font.dm_sans_light))
                ),
                fontSize = 14.sp,
                color = colorResource(id = if (isFocused) R.color.red else R.color.white),
                textAlign = TextAlign.Start,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }
    }


}

