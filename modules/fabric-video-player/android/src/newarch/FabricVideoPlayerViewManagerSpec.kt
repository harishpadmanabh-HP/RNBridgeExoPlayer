package com.fabricvideoplayer

import android.view.View

import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.SimpleViewManager
import com.facebook.react.uimanager.ViewManagerDelegate
import com.facebook.react.viewmanagers.FabricVideoPlayerViewManagerDelegate
import com.facebook.react.viewmanagers.FabricVideoPlayerViewManagerInterface

abstract class FabricVideoPlayerViewManagerSpec<T : View> : SimpleViewManager<T>(), FabricVideoPlayerViewManagerInterface<T> {
  private val mDelegate: ViewManagerDelegate<T>

  init {
    mDelegate = FabricVideoPlayerViewManagerDelegate(this)
  }

  override fun getDelegate(): ViewManagerDelegate<T>? {
    return mDelegate
  }
}
