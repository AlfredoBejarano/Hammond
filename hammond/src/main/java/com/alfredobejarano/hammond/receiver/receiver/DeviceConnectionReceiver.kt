package com.alfredobejarano.hammond.receiver.receiver

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.alfredobejarano.hammond.receiver.DeviceStatus
import com.alfredobejarano.hammond.receiver.FingerprintReader

/**
 * Simple [BroadcastReceiver] class that detects when a
 * USB device gets connected or disconnected and when a
 * USB device usage permission request gets prompted.
 *
 * @author Alfredo Bejarano
 * @since August 24th, 2018 - 06:00 PM
 * @version 1.0
 */
class DeviceConnectionReceiver : BroadcastReceiver() {
    companion object {
        /**
         * Constant value that defines the action of a USB device being connected.
         */
        private const val ACTION_DEVICE_ATTACHED = "android.hardware.usb.action.USB_DEVICE_ATTACHED"
        /**
         * Constant value that defines the action of a USB device being disconnected.
         */
        private const val ACTION_DEVICE_DETACHED = "android.hardware.usb.action.USB_DEVICE_DETACHED"
        /**
         * Constant value that defines the action of a USB permissions request.
         */
        const val ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION"
    }

    private lateinit var mIntent: Intent
    private lateinit var mContext: Context

    /**
     * This function gets executed when a USB device has been connected
     * or disconnected. When received, it checks if the device data matches
     * with the list of supported devices.
     */
    override fun onReceive(context: Context, intent: Intent) {
        mContext = context
        mIntent = intent
        when (mIntent.action) {
            // Check if a device has been connected.
            ACTION_DEVICE_ATTACHED -> validateConnectedUSBDevice()
            // Check if a device has been disconnected.
            ACTION_DEVICE_DETACHED -> validateDisconnectedUSBDevice()
            // Check if a permission request has been made.
            ACTION_USB_PERMISSION -> validatePermissionRequestResult()
        }
    }

    /**
     * Checks the intent data when the system reports that a
     * USB device has been connected to the device.
     */
    private fun validateConnectedUSBDevice() {
        // Retrieve the USB device from the intent.
        val usbDevice: UsbDevice? = mIntent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        // Check if the device is null or not.
        usbDevice?.let {
            // Report that the device is connected, but still not ready.
            FingerprintReader.status.postValue(DeviceStatus.STATUS_CONNECTED)
            // Request permissions for the connected device.
            requestPermissionsForDevice(usbDevice)
        }?.run {
            // Notify that the device is disconnected if it came null.
            FingerprintReader.status.postValue(DeviceStatus.STATUS_DISCONNECTED)
        }
    }

    /**
     * Performs a check for devices that can be still connected when
     * the OS reports that a USB device has been disconnected.
     * If no supported device is still connected, it reports that the
     * device has been disconnected.
     */
    private fun validateDisconnectedUSBDevice() {
        // If the device gets disconnected, re-run the search of another supported device still being connected.
        if (FingerprintReader.isConnected(mContext)) {
            // Report that a device is still connected, but its permissions are unknown.
            FingerprintReader.status.postValue(DeviceStatus.STATUS_CONNECTED)
            // Request permissions for the connected device.
            requestPermissionsForDevice(FingerprintReader.usbDevice)
        } else { // Report a status for a device being disconnected and no other supported one is connected.
            FingerprintReader.close()
            FingerprintReader.status.postValue(DeviceStatus.STATUS_DISCONNECTED)
        }
    }

    /**
     * Retrieves the permissions data from a result intent and report
     * the device status depending on the user granting permissions or not.
     */
    private fun validatePermissionRequestResult() = synchronized(this) {
        // Get the UsbDevice from the intent.
        val device: UsbDevice? = mIntent.getParcelableExtra(UsbManager.EXTRA_DEVICE)
        // Check if the USB permission has been granted.
        if (mIntent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            device?.apply {
                // Open a connection to the usb device.
                openFingerprintSensor(this)
            }
        } else {
            // Report that the permission has been denied, so the device is not usable.
            FingerprintReader.status.postValue(DeviceStatus.STATUS_PERMISSION_NEEDED)
        }
    }

    /**
     * Performs a request that will retrieve permissions to use a device.
     * @param usbDevice The device for retrieving permissions for.
     */
    private fun requestPermissionsForDevice(usbDevice: UsbDevice?) {
        // Retrieve the UsbManager from the system services.
        val manager = mContext.getSystemService(Context.USB_SERVICE) as UsbManager?
        // Check if the USB device has already a permission given.
        if (manager?.hasPermission(usbDevice) == true) {
            // Open a connection to the usb device.
            openFingerprintSensor(usbDevice)
        } else {
            // Build a PendingIntent for requesting access permissions for a given device.
            val permissionsIntent = PendingIntent.getBroadcast(mContext, 0,
                    Intent(DeviceConnectionReceiver.ACTION_USB_PERMISSION), 0)
            // Use the UsbManager to request the permissions.
            manager?.requestPermission(usbDevice, permissionsIntent)
        }
    }

    /**
     * Assigns a usb device to the [FingerprintReader] object
     * and opens a connection to said usb device.
     */
    private fun openFingerprintSensor(usbDevice: UsbDevice?) {
        // Set the USB device for the Fingerprint reader.
        FingerprintReader.usbDevice = usbDevice
        // Open a connection to the device.
        FingerprintReader.open(mContext)
        // We know the device status, so the device is ready to use.
        FingerprintReader.status.postValue(DeviceStatus.STATUS_READY)
    }
}
