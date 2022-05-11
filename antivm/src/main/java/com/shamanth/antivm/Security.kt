package com.shamanth.antivm

import android.content.Context
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import kotlin.system.exitProcess

class Security(private val context: Context,private val activity: AppCompatActivity) {

    fun exitIfRootedOrVM(){
        if(isRootedDevice() || isVmDevice())
        exitProcess(0)
    }

    fun isRootedDevice():Boolean = RootChecker.isDeviceRooted
    fun isVmDevice():Boolean = VMCheckUtils.isRunningInEmulator(context,activity)

}