package com.fabricdeclarative

import android.view.View

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.viewmanagers.FabricDeclarativeViewManagerDelegate
import com.facebook.react.viewmanagers.FabricDeclarativeViewManagerInterface

abstract class FabricDeclarativeViewManagerSpec<T : View> : SimpleViewManager<T>(), FabricDeclarativeViewManagerInterface<T> {
  private val mDelegate: ViewManagerDelegate<T>

  init {
    mDelegate = FabricDeclarativeViewManagerDelegate(this)
  }

  override fun getDelegate(): ViewManagerDelegate<T>? {
    return mDelegate
  }
}
