package com.fabricdeclarative


import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import androidx.compose.ui.platform.ComposeView
import com.facebook.react.bridge.ReactContext
import com.facebook.react.uimanager.UIManagerHelper

class FabricDeclarativeView : LinearLayout {
  internal lateinit var viewModel: JetpackComposeViewModel // 👈 add this

  constructor(context: Context) : super(context) {
    configureComponent(context)
  }
  constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
    configureComponent(context)
  }
  constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
    context,
    attrs,
    defStyleAttr
  ) {
    configureComponent(context)
  }

  private fun configureComponent(context: Context) {

    viewModel = JetpackComposeViewModel() //👈 add this

    layoutParams = LayoutParams(
      LayoutParams.WRAP_CONTENT,
      LayoutParams.WRAP_CONTENT
    )

    ComposeView(context).also {         // 👈 creating ComposeView
      it.layoutParams = LayoutParams(
        LayoutParams.WRAP_CONTENT,
        LayoutParams.WRAP_CONTENT
      )

      it.setContent {                  // 👈 which holds
        JetpackComposeView(
          viewModel = viewModel,
          onSubmit = { inputString, selectedNumber, restNumbers->

            val surfaceId = UIManagerHelper.getSurfaceId(context)
            val viewId = this.id

            UIManagerHelper
              .getEventDispatcherForReactTag(context as ReactContext, viewId)
              ?.dispatchEvent(
                SubmitEvent(
                  surfaceId,
                  viewId,
                  inputString,
                  selectedNumber,
                  restNumbers
                )
              )
          }
        ) //👈 update this
      }                                // 👈 as it's content

      addView(it)                      // 👈 and adding compose view to the layout
    }

  }
}
