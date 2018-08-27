package com.alfredobejarano.hammond.receiver.listener

import android.graphics.Bitmap
import android.os.Message

/**
 *
 */
interface OnScanResultListener {
    /**
     * This function will receive the pertinent data from a successful sensor fingerprint scanning.
     *
     * @param scan [Bitmap] result from the scan.
     * @param resolution The resolution from the image result, it is the image height.
     * @param deviceVendorId Id of the USB device vendor.
     * @param deviceProductId Id of the USB device product.
     */
    fun onResultSuccess(scan: Bitmap, resolution: Int, deviceVendorId: Int, deviceProductId: Int)

    fun onResultFailure(msg: Message)
}