package com.alfredobejarano.hammond.receiver.model

import android.hardware.usb.*
import com.alfredobejarano.hammond.receiver.DeviceStatus
import com.alfredobejarano.hammond.receiver.FingerprintReader

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
         * Defines the value of a write direction for an endpoint.
         */
        private const val WRITE_ENDPOINT_DIRECTION = 0

        /**
         * Defines the value of a read direction for an endpoint.
         */
        private const val READ_ENDPOINT_DIRECTION = 128

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

        /**
         * @return true if a given USB interface is not null and contains at least two endpoint.
         */
        private fun isDeviceInterfaceUsable(intf: UsbInterface?) = intf != null
                && intf.endpointCount >= 1

        /**
         * @return true if a [Pair] of USB endpoints are not null.
         */
        private fun areIOEndpointsUsable(endpoints: Pair<UsbEndpoint?, UsbEndpoint?>) =
                endpoints.first != null && endpoints.second != null

        /**
         * Iterates through all the endpoints of a USB interface and builds a Pair of them.
         * @return [Pair] of [UsbEndpoint], the first being the read Endpoint and the
         * second the write Endpoint.
         */
        private fun getInterfaceIOEndpoints(intf: UsbInterface): Pair<UsbEndpoint?, UsbEndpoint?> {
            // Initialize the read and write endpoints for the sensor.
            var readEndpoint: UsbEndpoint? = null
            var writeEndpoint: UsbEndpoint? = null
            // Iterate through all the endpoints in the sensor interface.
            for (i in 0 until intf.endpointCount) {
                // Hold a reference to the current endpoint.
                val currentEndpoint = intf.getEndpoint(i)
                // Check the direction value of the endpoint.
                when (currentEndpoint.direction) {
                    // If it matches the read direction, assign it as the read endpoint.
                    READ_ENDPOINT_DIRECTION -> readEndpoint = currentEndpoint
                    // If it matches the write direction, assign it as the write endpoint.
                    WRITE_ENDPOINT_DIRECTION -> writeEndpoint = currentEndpoint
                }
            }
            // If any of both endpoints is null, the device was not opened correctly.
            return if (readEndpoint == null || writeEndpoint == null) {
                // Report that the device failed while opening a connection to it.
                FingerprintReader.status.postValue(DeviceStatus.STATUS_OPEN_FAILED)
                // Return a null pair of USB endpoints.
                Pair(null, null)
            } else {
                // Return the pair of USB endpoints.
                return Pair(readEndpoint, writeEndpoint)
            }
        }

        /**
         * Checks if a connection is null ot not, if it is, reports the status of the
         * [FingerprintReader], if not, assigns such connection to the [FingerprintReader].
         */
        private fun assignConnection(connection: UsbDeviceConnection?) = if (connection == null) {
            // If the connection is null, it means the connection could not been opened.
            FingerprintReader.status.postValue(DeviceStatus.STATUS_OPEN_FAILED)
        } else {
            // If not, assign this sensor connection.
            FingerprintReader.usbDeviceConnection = connection
        }

        /**
         * Performs a variety of checks about opening a USB connection like
         * if the interface is not null, if the endpoints inside the interface
         * are not null and if the connection made is not null.
         *
         * If a step fails, the status is reported to the [FingerprintReader].
         */
        fun attemptConnection(usbManager: UsbManager?, intf: UsbInterface?) {
            // Check if the device interface was retrieved and it contains some endpoints within it.
            if (Device.isDeviceInterfaceUsable(intf)) {
                // Assign this interface as the Fingerprint interface.
                FingerprintReader.usbInterface = intf
                // Retrieve the IO endpoints from the device interface.
                val endpoints = Device.getInterfaceIOEndpoints(intf!!)
                // Check if the IO endpoints are usable.
                if (Device.areIOEndpointsUsable(endpoints)) {
                    FingerprintReader.usbWriteEndpoint = endpoints.first
                    FingerprintReader.usbReadEndpoint = endpoints.second
                    // Now, retrieve a connection to the device.
                    val connection = usbManager?.openDevice(FingerprintReader.usbDevice)
                    // Check the status of the connection.
                    assignConnection(connection)
                } else {
                    // If those endpoints are not usable, report the status of the sensor.
                    FingerprintReader.status.postValue(DeviceStatus.STATUS_ENDPOINTS_FAILED)
                }
            } else {
                // If not, report that the interface cant be used.
                FingerprintReader.status.postValue(DeviceStatus.STATUS_INTERFACE_FAILED)
            }
        }
    }
}