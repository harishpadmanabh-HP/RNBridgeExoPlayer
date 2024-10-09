package com.fabricdeclarative

import android.graphics.Color
import com.facebook.react.module.annotations.ReactModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.uimanager.ThemedReactContext
import com.facebook.react.uimanager.annotations.ReactProp
import com.facebook.react.common.MapBuilder // 👈 add this


@ReactModule(name = FabricDeclarativeViewManager.NAME)
class FabricDeclarativeViewManager :
  FabricDeclarativeViewManagerSpec<FabricDeclarativeView>() {
  override fun getName(): String {
    return NAME
  }

  override fun setTitle(view: FabricDeclarativeView?, value: String?) {
    if (value != null) {
      view?.viewModel?.updateTitle(value)
    }
  }

  public override fun createViewInstance(context: ThemedReactContext): FabricDeclarativeView {
    return FabricDeclarativeView(context)
  }

  @ReactProp(name = "color")
  fun setColor(view: FabricDeclarativeView?, color: String?) {
    view?.setBackgroundColor(Color.parseColor(color))
  }






  @ReactProp(name = "options")
  override // 👈 add this
   fun setOptions(view: FabricDeclarativeView?, value: ReadableArray?) {
    if(value == null) { // 👈 add this
      return            // 👈 add this
    }                   // 👈 add this

    view?.viewModel?.updateOptions(value.toArrayList() as ArrayList<Double>) // <-- add this
  }

  override fun getExportedCustomDirectEventTypeConstants(): MutableMap<String, Any> = mutableMapOf(
    SubmitEvent.EVENT_NAME to MapBuilder.of("registrationName", "onSubmit")
  )


  companion object {
    const val NAME = "FabricDeclarativeView"
  }
}
