package com.onair.videoplayer.settingsOverlays.TvFocusIndicators

import androidx.compose.foundation.Indication
import androidx.compose.foundation.IndicationInstance
import androidx.compose.foundation.interaction.InteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.ContentDrawScope
import androidx.compose.ui.unit.dp

 class MyHighlightIndicationInstance(isEnabledState: State<Boolean>) :
    IndicationInstance {
    private val isEnabled by isEnabledState
    override fun ContentDrawScope.drawIndication() {
        if (isEnabled) {
            drawRoundRect(
                size = size,
                color = Color.White,
                cornerRadius = CornerRadius(x = 24.dp.toPx(), y = 24.dp.toPx()),
                alpha = 1f
            )
        }
        drawContent()

    }
}

class MyHighlightIndication : Indication {
    @Composable
    override fun rememberUpdatedInstance(interactionSource: InteractionSource):
            IndicationInstance {
        val isFocusedState = interactionSource.collectIsFocusedAsState()
        return remember(interactionSource) {
            MyHighlightIndicationInstance(isEnabledState = isFocusedState)
        }
    }
}