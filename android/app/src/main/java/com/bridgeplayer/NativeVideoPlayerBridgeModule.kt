package com.bridgeplayer

import android.widget.Toast
import com.facebook.react.ReactPackage
import com.facebook.react.bridge.NativeModule
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.uimanager.ViewManager


class NativeVideoPlayerBridgeModule(private val reactContext: ReactApplicationContext?) :
    ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "NativeVideoPlayerBridgeModule"
    }

    @ReactMethod
    fun testLog(param: String) {
        println("GET DATA FROM RN <--- $param")
    }

    @ReactMethod
    fun showToast(message: String) {
        Toast.makeText(reactContext, message, Toast.LENGTH_SHORT).show()
    }

}

class NativeVideoPlayerBridgeModulePackage : ReactPackage {

    override fun createNativeModules(reactContext: ReactApplicationContext): List<NativeModule> {
        val modules = mutableListOf<NativeModule>()
        modules.add(NativeVideoPlayerBridgeModule(reactContext))
        return modules
    }

    override fun createViewManagers(reactContext: ReactApplicationContext): List<ViewManager<*, *>> {
        return listOf(VideoPlayerManager(reactContext))
    }
}

