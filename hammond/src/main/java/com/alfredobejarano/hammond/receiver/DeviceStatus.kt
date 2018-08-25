package com.alfredobejarano.hammond.receiver

/**
 * Enum class that defines the different statuses that a device
 * can be in the progress of using a device.
 *
 * @author Alfredo Bejarano
 * @version 1.0
 * @since 24/08/2018 - 08:24 PM
 */
enum class DeviceStatus {
    /**
     * The device is ready to use.
     */
    STATUS_READY,
    /**
     * The device has been connected but it is unsafe to be used.
     */
    STATUS_CONNECTED,
    /**
     *The device has been disconnected.
     */
    STATUS_DISCONNECTED,
    /**
     * The device is connected but permissions for access to it need to be granted.
     */
    STATUS_PERMISSION_NEEDED
}