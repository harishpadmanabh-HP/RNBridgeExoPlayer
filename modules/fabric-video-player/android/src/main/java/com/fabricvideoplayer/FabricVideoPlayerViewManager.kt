package com.fabricvideoplayer

import android.graphics.Color
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp

@ReactModule(name = FabricVideoPlayerViewManager.NAME)
class FabricVideoPlayerViewManager :
  FabricVideoPlayerViewManagerSpec<FabricVideoPlayerView>() {
  override fun getName(): String {
    return NAME
  }

  public override fun createViewInstance(context: ThemedReactContext): FabricVideoPlayerView {
    return FabricVideoPlayerView(context)
  }

  @ReactProp(name = "color")
  override fun setColor(view: FabricVideoPlayerView?, color: String?) {
    view?.setBackgroundColor(Color.parseColor(color))
  }

  companion object {
    const val NAME = "FabricVideoPlayerView"
  }
}
