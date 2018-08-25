package com.alfredobejarano.hammond.receiver

import android.arch.lifecycle.MutableLiveData
import android.content.Context
import android.hardware.usb.*
import com.alfredobejarano.hammond.receiver.model.Device

/**
 * Singleton class that defines the current status of a fingerprint device.
 *
 * @author Alfredo Bejarano
 * @version 1.0
 * @since 24/08/2018 - 07:07 PM
 */
object FingerprintReader {
    /**
     * Reports the current status of the Fingerprint Reader device.
     */
    var status = MutableLiveData<DeviceStatus>()

    /**
     * Reference to the USB device for this FingerprintReader.
     */
    var usbDevice: UsbDevice? = null

    /**
     * Reference to this sensor USB interface.
     */
    var usbInterface: UsbInterface? = null

    /**
     * Reference to this sensor USB read endpoint.
     */
    var usbReadEndpoint: UsbEndpoint? = null

    /**
     * Reference to this sensor USB write endpoint.
     */
    var usbWriteEndpoint: UsbEndpoint? = null

    /**
     * Reference to this sensor USB connection.
     */
    var usbDeviceConnection: UsbDeviceConnection? = null

    /**
     * Checks if a supported fingerprint reader device is currently connected.
     * @return true if a supported device is connected.
     */
    fun isConnected(ctx: Context): Boolean {
        // Set the connected device as null to reset the connected device.
        usbDevice = null
        // Retrieve the UsbManager from the system services.
        val manager = ctx.getSystemService(Context.USB_SERVICE) as UsbManager?
        // Get the list of connected devices.
        val devices = manager?.deviceList
        // Iterate through the connected devices.
        devices?.forEach {
            // Check if the current device is supported.
            if (Device.isSupported(it.value)) {
                usbDevice = it.value
            }
        }
        // If the connected device is not null, return true.
        return usbDevice != null
    }

    /**
     * Opens a connection to the Fingerprint sensor.
     */
    private fun open(ctx: Context) {
        // Retrieve the UsbManager from the system services.
        val manager = ctx.getSystemService(Context.USB_SERVICE) as UsbManager?
        // Retrieve the USB device interface.
        val usbDeviceInterface = usbDevice?.getInterface(0)
        // Try to perform a connection to the device.
        Device.attemptConnection(manager, usbDeviceInterface)
    }

    /**
     * Removes all references to this sensor USB properties and reports its disconnected status.
     */
    fun close() {
        usbDevice = null
        usbInterface = null
        usbReadEndpoint = null
        usbWriteEndpoint = null
        usbDeviceConnection = null
        status.postValue(DeviceStatus.STATUS_DISCONNECTED)
    }
}