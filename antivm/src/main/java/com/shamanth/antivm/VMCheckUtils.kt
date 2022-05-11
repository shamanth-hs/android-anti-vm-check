package com.shamanth.antivm

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Debug
import android.telephony.TelephonyManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.*


object VMCheckUtils {

    fun isRunningInEmulator(context:Context,activity:AppCompatActivity):Boolean{
        return hasKnownDeviceId(activity,context) || isBeingDebugged()|| hasGenyFiles() || hasEmulatorBuild(context)
                || hasKnownImsi(activity,context) ||
                hasPipes() || hasQEmuFiles() || hasQEmuDrivers()
    }

    private val known_device_ids = arrayOf(
        "000000000000000",  // Default emulator id
        "e21833235b6eef10",  // VirusTotal id
        "012345678912345"
    )

    /**
     * this checks against known device ids of emulators
     */
    private fun hasKnownDeviceId(act: AppCompatActivity?, context: Context): Boolean {
        return if (PermissionUtils.isLacksOfPermission(
                context,
                PermissionUtils.PERMISSION[0]
            )
        ) {
            ActivityCompat.requestPermissions(act!!, PermissionUtils.PERMISSION, 0x12)
            false
        } else {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val deviceId = telephonyManager.deviceId
            for (known_deviceId in known_device_ids) {
                if (known_deviceId.equals(deviceId, ignoreCase = true)) {
                    return true
                }
            }
            false
        }
    }


    private val known_pipes = arrayOf("/dev/socket/qemud", "/dev/qemu_pipe")

    /**
     *    qemu pipes testing if its an emulator it will be having qemu pipes

     */
    private fun hasPipes(): Boolean {
        for (pipe in known_pipes) {
            val qemu_socket = File(pipe)
            if (qemu_socket.exists()) {
                return true
            }
        }
        return false
    }

    private val known_imsi_ids = arrayOf(
        "310260000000000" // default IMSI
    )
    /**
     * checks for IMEI and IMSI numbers in emulator
     */
    private fun hasKnownImsi(act: AppCompatActivity?, context: Context): Boolean {
        return if (PermissionUtils.isLacksOfPermission(
                context,
                PermissionUtils.PERMISSION.get(0)
            )
        ) {
            ActivityCompat.requestPermissions(act!!, PermissionUtils.PERMISSION, 0x12)
            false
        } else {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
            val imsi = telephonyManager.subscriberId
            for (known_imsi in known_imsi_ids) {
                if (known_imsi.equals(imsi, ignoreCase = true)) {

                    return true
                }
            }
            false
        }
    }

    /**
     * checks for emulator with common names like SDK generic etc
     */
    private fun hasEmulatorBuild(context: Context?): Boolean {
        val BOARD = Build.BOARD // The name of the underlying board, like "unknown".
        // This appears to occur often on real hardware... that's sad
        // String BOOTLOADER = android.os.Build.BOOTLOADER; // The system bootloader version number.
        val BRAND = Build.BRAND // The brand (e.g., carrier) the software is customized for, if any.
        // "generic"
        val DEVICE = Build.DEVICE // The name of the industrial design. "generic"
        val HARDWARE = Build.HARDWARE // The name of the hardware (from the kernel command line or
        // /proc). "goldfish"
        val MODEL = Build.MODEL // The end-user-visible name for the end product. "sdk"
        val PRODUCT = Build.PRODUCT // The name of the overall product.
        if (BOARD.compareTo("unknown") == 0 /* || (BOOTLOADER.compareTo("unknown") == 0) */
            || BRAND.compareTo("generic") == 0 || DEVICE.compareTo("generic") == 0
            || MODEL.compareTo("sdk") == 0 || PRODUCT.compareTo("sdk") == 0
            || HARDWARE.compareTo("goldfish") == 0
        ) {
            return true
        }
        return false
    }

    /**
     * checks for operator name
     */
    private fun isOperatorNameAndroid( paramContext: Context): Boolean {
        val szOperatorName =
            (paramContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager).networkOperatorName
        val isAndroid = szOperatorName.equals("android", ignoreCase = true)
        return isAndroid
    }

    //qemu drivers
    private val known_qemu_drivers = arrayOf("goldfish")
    /**
     * checks for emulator with QEMU drivers
     */
    private fun hasQEmuDrivers(): Boolean {
        for (drivers_file in arrayOf<File>(File("/proc/tty/drivers"), File("/proc/cpuinfo"))) {
            if (drivers_file.exists() && drivers_file.canRead()) {
                // We don't care to read much past things since info we care about should be inside here
                val data = ByteArray(1024)
                try {
                    val `is`: InputStream = FileInputStream(drivers_file)
                    `is`.read(data)
                    `is`.close()
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
                val driver_data = String(data)
                for (known_qemu_driver in known_qemu_drivers) {
                    if (driver_data.indexOf(known_qemu_driver!!) != -1) {

                        return true
                    }
                }
            }
        }
        return false
    }

    //qemu files
    private val known_files = arrayOf(
        "/system/lib/libc_malloc_debug_qemu.so", "/sys/qemu_trace",
        "/system/bin/qemu-props"
    )

    /**
     * checks for emulator with known QEMU files
     */
    private fun hasQEmuFiles(): Boolean {
        for (pipe in known_files) {
            val qemu_file = File(pipe)
            if (qemu_file.exists()) {
                return true
            }
        }
        return false
    }

    //genymotion file
    private val known_geny_files = arrayOf("/dev/socket/genyd", "/dev/socket/baseband_genyd")
    /**
     * checks for genymotion emulator with genymotion files
     *
     * NOTE: genymotion is popular emulator
     */
    private fun hasGenyFiles(): Boolean {
        for (file in known_geny_files) {
            val geny_file = File(file)
            if (geny_file.exists()) {
                return true
            }
        }
        return false
    }


    //Monkey
    /**
     * checks for script being running on app
     */
    fun isRunningAutomationScript():Boolean {
       return ActivityManager.isUserAMonkey()
    }

    //debugger
    /**
     * checks for debugger being connected
     */
    private fun isBeingDebugged(): Boolean {
        return Debug.isDebuggerConnected()
    }


}