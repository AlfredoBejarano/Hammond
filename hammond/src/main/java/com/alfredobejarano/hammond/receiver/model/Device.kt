package com.alfredobejarano.hammond.receiver.model

import android.hardware.usb.UsbDevice

/**
 * Helper class that checks if a device is supported or not.
 * It also contains the list of supported devices.
 *
 * @author Alfredo Bejarano
 * @version 1.0
 * @since 24/08/2018 - 07:17 PM
 */
class Device {
    companion object {
        /**
         * Builds a device model class from a [UsbDevice].
         * @param device The usb device that has been plugged in / is plugged in.
         * @return A [Device] class.
         */
        fun isSupported(device: UsbDevice?): Boolean {
            // Return a device model if the device matches one of the supported devices.
            return supportedProductIds.contains(device?.productId) &&
                    supportedVersionIds.contains(device?.vendorId)
        }

        /**
         * List that defines all the product ids for supported devices .
         */
        private val supportedProductIds =
                listOf(32, 775, 37, 136, 144, 80, 96, 152, 32920, 39008)
        /**
         * List that defines all the possible vendor ids for supported vendors.
         */
        private val supportedVersionIds = listOf(2100, 8122, 2392, 5265)
    }
}