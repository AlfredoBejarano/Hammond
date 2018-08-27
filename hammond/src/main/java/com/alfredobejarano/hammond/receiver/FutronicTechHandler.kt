package com.alfredobejarano.hammond.receiver

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Handler
import android.os.Message
import com.alfredobejarano.hammond.receiver.listener.OnScanResultListener

class FutronicTechHandler(private val listener: OnScanResultListener) : Handler() {
    companion object {
        /**
         * Constant value from the Futronic Android SDK.
         */
        private const val SCAN_RESULT_OK = 3
    }

    override fun handleMessage(msg: Message) {
        when (msg.what) {
            SCAN_RESULT_OK -> generateImageBitmap(msg.arg1, msg.arg2, msg.obj as ByteArray)
            else -> listener.onResultFailure(msg)
        }
    }

    /**
     * Generates a [Bitmap] from a scan result.
     */
    private fun generateImageBitmap(width: Int, height: Int, scanResult: ByteArray) {
        // Create a new IntArray for the Bitmap pixels.
        val pixels = intArrayOf(scanResult.size)
        // Iterate through the bites and generate pixels for the bitmap
        scanResult.forEachIndexed { index, byte ->
            val pixel = byte.toInt()
            pixels[index] = Color.rgb(pixel, pixel, pixel)
        }
        // Define the configurations for the Bitmap.
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        // Set the pixels to the bitmap.
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
        // Retrieve the sensor UsbDevice data.
        val usbDevice = FingerprintReader.usbDevice
        // Notify that the Bitmap has been generated.
        listener.onResultSuccess(bitmap, height,
                usbDevice?.productId ?: 0, usbDevice?.vendorId ?: 0)
        // Notify that the sensor is ready.
        FingerprintReader.status.postValue(DeviceStatus.STATUS_READY)
    }
}