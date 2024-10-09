package com.fabricdeclarative

import android.view.View
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager

abstract class FabricDeclarativeViewManagerSpec<T : View> : SimpleViewManager<T>() {
  abstract fun setColor(view: T?, value: String?)
}
