package com.futronictech;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;

import java.nio.ByteBuffer;

public class UsbDeviceDataExchangeImpl {
    static final String ACTION_USB_PERMISSION = "com.futronictech.FtrScanDemoActivity.USB_PERMISSION";
    public static final int MESSAGE_ALLOW_DEVICE = 255;
    public static final int MESSAGE_DENY_DEVICE = 256;
    static final String log_tag = "FUTRONICFTR_J";
    static final int transfer_buffer_size = 4096;
    private Context context = null;
    private Handler handler = null;
    private UsbManager mDevManager;
    private PendingIntent mPermissionIntent = null;
    private final BroadcastReceiver mUsbReceiver = new C02851();
    private byte[] max_transfer_buffer = new byte[4096];
    private boolean pending_open = false;
    private FTR_USB_DEVICE_INTERNAL usb_ctx = null;

    /* renamed from: com.futronictech.UsbDeviceDataExchangeImpl$1 */
    class C02851 extends BroadcastReceiver {
        C02851() {
        }

        public void onReceive(Context context, Intent intent) {
            if (UsbDeviceDataExchangeImpl.ACTION_USB_PERMISSION.equals(intent.getAction())) {
                synchronized (UsbDeviceDataExchangeImpl.this.mPermissionIntent) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra("device");
                    if (intent.getBooleanExtra("permission", false)) {
                        if (device != null) {
                            UsbDeviceDataExchangeImpl.this.usb_ctx = UsbDeviceDataExchangeImpl.this.OpenDevice(device);
                        }
                        UsbDeviceDataExchangeImpl.this.handler.obtainMessage(255).sendToTarget();
                    } else {
                        UsbDeviceDataExchangeImpl.this.handler.obtainMessage(256).sendToTarget();
                    }
                }
            }
        }
    }

    public class FTR_USB_DEVICE_INTERNAL {
        public UsbDeviceConnection mDevConnetion_;
        public UsbDevice mDev_;
        public boolean mHandleClaimed_ = false;
        public UsbInterface mIntf_;
        public UsbEndpoint mReadPoint_;
        public UsbEndpoint mWritePoint_;

        public FTR_USB_DEVICE_INTERNAL(UsbDevice mDev, UsbInterface mIntf, UsbEndpoint mReadPoint, UsbEndpoint mWritePoint, UsbDeviceConnection mDevConnetion) {
            this.mDev_ = mDev;
            this.mIntf_ = mIntf;
            this.mReadPoint_ = mReadPoint;
            this.mWritePoint_ = mWritePoint;
            this.mDevConnetion_ = mDevConnetion;
        }
    }

    public UsbDeviceDataExchangeImpl(Context ctx, Handler trg_handler) {
        this.context = ctx;
        this.handler = trg_handler;
        this.mDevManager = (UsbManager) ctx.getSystemService(Context.USB_SERVICE);
        this.mPermissionIntent = PendingIntent.getBroadcast(ctx, 0, new Intent(ACTION_USB_PERMISSION), 0);
        this.context.registerReceiver(this.mUsbReceiver, new IntentFilter(ACTION_USB_PERMISSION));
    }

    public void Destroy() {
        this.context.unregisterReceiver(this.mUsbReceiver);
    }

    public boolean OpenDevice(int instance, boolean is_activity_thread) {
        boolean z = false;
        synchronized (this) {
            if (this.usb_ctx == null) {
                this.pending_open = false;
                int index = 0;
                for (UsbDevice device : this.mDevManager.getDeviceList().values()) {
                    if (IsFutronicDevice(device.getVendorId(), device.getProductId())) {
                        if (index < instance) {
                            index++;
                        } else {
                            if (!this.mDevManager.hasPermission(device)) {
                                this.mDevManager.requestPermission(device, this.mPermissionIntent);
                                if (is_activity_thread) {
                                    this.pending_open = true;
                                    break;
                                }
                                this.pending_open = false;
                                synchronized (this.mPermissionIntent) {
                                    try {
                                        this.mPermissionIntent.wait();
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                            if (this.mDevManager.hasPermission(device)) {
                                this.usb_ctx = OpenDevice(device);
                            } else {
                                Log.e(log_tag, "device not allow");
                            }
                        }
                    }
                }
            }
            if (this.usb_ctx != null) {
                z = true;
            }
        }
        return z;
    }

    public void CloseDevice() {
        synchronized (this) {
            if (this.usb_ctx != null) {
                if (this.usb_ctx.mHandleClaimed_) {
                    this.usb_ctx.mDevConnetion_.releaseInterface(this.usb_ctx.mIntf_);
                    this.usb_ctx.mHandleClaimed_ = false;
                }
                if (this.usb_ctx.mDevConnetion_ != null) {
                    this.usb_ctx.mDevConnetion_.close();
                }
            }
            this.usb_ctx = null;
        }
    }

    public boolean IsPendingOpen() {
        return this.pending_open;
    }

    public boolean DataExchange(byte[] out_data, byte[] in_data, int in_time_out, int out_time_out, boolean keep_open, boolean use_max_end_point_size, int trace_level) {
        synchronized (this) {
            boolean res = false;
            boolean check_res = false;
            if (this.usb_ctx != null) {
                check_res = (this.usb_ctx.mIntf_ == null || this.usb_ctx.mDevConnetion_ == null || this.usb_ctx.mReadPoint_ == null || this.usb_ctx.mWritePoint_ == null) ? false : true;
            }
            if (check_res) {
                if (!this.usb_ctx.mHandleClaimed_) {
                    this.usb_ctx.mDevConnetion_.claimInterface(this.usb_ctx.mIntf_, false);
                    this.usb_ctx.mHandleClaimed_ = true;
                }
                if (out_data.length > 0) {
                    if (this.usb_ctx.mDevConnetion_.bulkTransfer(this.usb_ctx.mWritePoint_, out_data, out_data.length, out_time_out) == -1) {
                        Log.e(log_tag, String.format("Send %d bytes failed", new Object[]{Integer.valueOf(out_data.length)}));
                        return false;
                    }
                }
                try {
                    int transfer_bytes;
                    int to_read_size = in_data.length;
                    int copy_pos = 0;
                    while (to_read_size >= getTransferBuffer().length) {
                        transfer_bytes = this.usb_ctx.mDevConnetion_.bulkTransfer(this.usb_ctx.mReadPoint_, getTransferBuffer(), getTransferBuffer().length, in_time_out);
                        if (transfer_bytes == -1) {
                            Log.e(log_tag, String.format("Receive(1) %d bytes failed", new Object[]{Integer.valueOf(getTransferBuffer().length)}));
                            return false;
                        } else if (copy_pos + transfer_bytes > in_data.length) {
                            Log.e(log_tag, String.format("Small receive buffer. Need %d bytes", new Object[]{Integer.valueOf((copy_pos + transfer_bytes) - in_data.length)}));
                            return false;
                        } else {
                            System.arraycopy(getTransferBuffer(), 0, in_data, copy_pos, transfer_bytes);
                            to_read_size -= transfer_bytes;
                            copy_pos += transfer_bytes;
                        }
                    }
                    if (to_read_size > this.usb_ctx.mReadPoint_.getMaxPacketSize()) {
                        int data_left = to_read_size - (to_read_size % this.usb_ctx.mReadPoint_.getMaxPacketSize());
                        if (data_left > 0) {
                            transfer_bytes = this.usb_ctx.mDevConnetion_.bulkTransfer(this.usb_ctx.mReadPoint_, getTransferBuffer(), data_left, in_time_out);
                            if (transfer_bytes == -1) {
                                Log.e(log_tag, String.format("Receive(2) %d bytes failed", new Object[]{Integer.valueOf(data_left)}));
                                return false;
                            } else if (copy_pos + transfer_bytes > in_data.length) {
                                Log.e(log_tag, String.format("Small receive buffer. Need %d bytes", new Object[]{Integer.valueOf((copy_pos + transfer_bytes) - in_data.length)}));
                                return false;
                            } else {
                                System.arraycopy(getTransferBuffer(), 0, in_data, copy_pos, transfer_bytes);
                                to_read_size -= transfer_bytes;
                                copy_pos += transfer_bytes;
                            }
                        }
                    }
                    while (to_read_size > 0) {
                        int maxPacketSize;
                        UsbDeviceConnection usbDeviceConnection = this.usb_ctx.mDevConnetion_;
                        UsbEndpoint usbEndpoint = this.usb_ctx.mReadPoint_;
                        byte[] transferBuffer = getTransferBuffer();
                        if (use_max_end_point_size) {
                            maxPacketSize = this.usb_ctx.mReadPoint_.getMaxPacketSize();
                        } else {
                            maxPacketSize = to_read_size;
                        }
                        if (usbDeviceConnection.bulkTransfer(usbEndpoint, transferBuffer, maxPacketSize, in_time_out) == -1) {
                            Log.e(log_tag, String.format("Receive(3) %d bytes failed", new Object[]{Integer.valueOf(to_read_size)}));
                            return false;
                        }
                        int real_read;
                        if (to_read_size > this.usb_ctx.mReadPoint_.getMaxPacketSize()) {
                            real_read = this.usb_ctx.mReadPoint_.getMaxPacketSize();
                        } else {
                            real_read = to_read_size;
                        }
                        if (copy_pos + real_read > in_data.length) {
                            Log.e(log_tag, String.format("Small receive buffer. Need %d bytes", new Object[]{Integer.valueOf((copy_pos + real_read) - in_data.length)}));
                            return false;
                        }
                        System.arraycopy(getTransferBuffer(), 0, in_data, copy_pos, real_read);
                        to_read_size -= real_read;
                        copy_pos += real_read;
                    }
                    if (!keep_open) {
                        this.usb_ctx.mDevConnetion_.releaseInterface(this.usb_ctx.mIntf_);
                        this.usb_ctx.mHandleClaimed_ = false;
                    }
                    res = true;
                } catch (Exception e) {
                    Log.e(log_tag, String.format("Data exchange fail %s", new Object[]{e.toString()}));
                }
                return res;
            }
            return false;
        }
    }

    public boolean DataExchange1(ByteBuffer out_data, ByteBuffer in_data, int in_time_out, int out_time_out, boolean keep_open, boolean use_max_end_point_size, int trace_level) {
        synchronized (this) {
            boolean res = false;
            boolean check_res = false;
            if (this.usb_ctx != null) {
                check_res = (this.usb_ctx.mIntf_ == null || this.usb_ctx.mDevConnetion_ == null || this.usb_ctx.mReadPoint_ == null || this.usb_ctx.mWritePoint_ == null) ? false : true;
            }
            if (check_res) {
                if (!this.usb_ctx.mHandleClaimed_) {
                    this.usb_ctx.mDevConnetion_.claimInterface(this.usb_ctx.mIntf_, false);
                    this.usb_ctx.mHandleClaimed_ = true;
                }
                if (out_data != null) {
                    byte[] out_data_transfer = new byte[out_data.capacity()];
                    out_data.get(out_data_transfer);
                    if (this.usb_ctx.mDevConnetion_.bulkTransfer(this.usb_ctx.mWritePoint_, out_data_transfer, out_data_transfer.length, out_time_out) == -1) {
                        Log.e(log_tag, String.format("Send %d bytes failed", new Object[]{Integer.valueOf(out_data_transfer.length)}));
                        return false;
                    }
                }
                if (in_data != null) {
                    int transfer_bytes;
                    int to_read_size = in_data.capacity();
                    int copy_pos = 0;
                    while (to_read_size >= getTransferBuffer().length) {
                        transfer_bytes = this.usb_ctx.mDevConnetion_.bulkTransfer(this.usb_ctx.mReadPoint_, getTransferBuffer(), getTransferBuffer().length, in_time_out);
                        if (transfer_bytes == -1) {
                            Log.e(log_tag, String.format("Receive(1) %d bytes failed", new Object[]{Integer.valueOf(getTransferBuffer().length)}));
                            return false;
                        } else if (copy_pos + transfer_bytes > in_data.capacity()) {
                            Log.e(log_tag, String.format("Small receive buffer. Need %d bytes", new Object[]{Integer.valueOf((copy_pos + transfer_bytes) - in_data.capacity())}));
                            return false;
                        } else {
                            try {
                                in_data.put(getTransferBuffer());
                                to_read_size -= transfer_bytes;
                                copy_pos += transfer_bytes;
                            } catch (Exception e) {
                                Log.e(log_tag, String.format("Data exchange fail %s", new Object[]{e.toString()}));
                            }
                        }
                    }
                    if (to_read_size > this.usb_ctx.mReadPoint_.getMaxPacketSize()) {
                        int data_left = to_read_size - (to_read_size % this.usb_ctx.mReadPoint_.getMaxPacketSize());
                        if (data_left > 0) {
                            transfer_bytes = this.usb_ctx.mDevConnetion_.bulkTransfer(this.usb_ctx.mReadPoint_, getTransferBuffer(), data_left, in_time_out);
                            if (transfer_bytes == -1) {
                                Log.e(log_tag, String.format("Receive(2) %d bytes failed", new Object[]{Integer.valueOf(data_left)}));
                                return false;
                            } else if (copy_pos + transfer_bytes > in_data.capacity()) {
                                Log.e(log_tag, String.format("Small receive buffer. Need %d bytes", new Object[]{Integer.valueOf((copy_pos + transfer_bytes) - in_data.capacity())}));
                                return false;
                            } else {
                                in_data.put(getTransferBuffer(), 0, transfer_bytes);
                                to_read_size -= transfer_bytes;
                                copy_pos += transfer_bytes;
                            }
                        }
                    }
                    while (to_read_size > 0) {
                        int maxPacketSize;
                        UsbDeviceConnection usbDeviceConnection = this.usb_ctx.mDevConnetion_;
                        UsbEndpoint usbEndpoint = this.usb_ctx.mReadPoint_;
                        byte[] transferBuffer = getTransferBuffer();
                        if (use_max_end_point_size) {
                            maxPacketSize = this.usb_ctx.mReadPoint_.getMaxPacketSize();
                        } else {
                            maxPacketSize = to_read_size;
                        }
                        if (usbDeviceConnection.bulkTransfer(usbEndpoint, transferBuffer, maxPacketSize, in_time_out) == -1) {
                            Log.e(log_tag, String.format("Receive(3) %d bytes failed", new Object[]{Integer.valueOf(to_read_size)}));
                            return false;
                        }
                        int real_read;
                        if (use_max_end_point_size) {
                            real_read = this.usb_ctx.mReadPoint_.getMaxPacketSize();
                        } else {
                            real_read = to_read_size;
                        }
                        in_data.put(getTransferBuffer(), 0, to_read_size);
                        to_read_size -= real_read;
                        copy_pos += real_read;
                    }
                }
                if (!keep_open) {
                    this.usb_ctx.mDevConnetion_.releaseInterface(this.usb_ctx.mIntf_);
                    this.usb_ctx.mHandleClaimed_ = false;
                }
                res = true;
                return res;
            }
            return false;
        }
    }

    public boolean ValidateContext() {
        boolean res;
        synchronized (this) {
            res = false;
            if (this.usb_ctx != null) {
                res = (this.usb_ctx.mIntf_ == null || this.usb_ctx.mDevConnetion_ == null || this.usb_ctx.mReadPoint_ == null || this.usb_ctx.mWritePoint_ == null) ? false : true;
            }
        }
        return res;
    }

    public void DataExchangeEnd() {
        synchronized (this) {
            if (this.usb_ctx != null && this.usb_ctx.mHandleClaimed_) {
                this.usb_ctx.mDevConnetion_.releaseInterface(this.usb_ctx.mIntf_);
                this.usb_ctx.mHandleClaimed_ = false;
            }
        }
    }

    public boolean GetDeviceInfo(byte[] pack_data) {
        Exception e;
        boolean res = false;
        synchronized (this) {
            if (this.usb_ctx != null) {
                try {
                    int vendorId = this.usb_ctx.mDev_.getVendorId();
                    int pack_data_index = 0 + 1;
                    int i;
                    try {
                        pack_data[0] = (byte) vendorId;
                        i = pack_data_index + 1;
                        pack_data[pack_data_index] = (byte) (vendorId >> 8);
                        pack_data_index = i + 1;
                        pack_data[i] = (byte) (vendorId >> 16);
                        i = pack_data_index + 1;
                        pack_data[pack_data_index] = (byte) (vendorId >> 24);
                        int productId = this.usb_ctx.mDev_.getProductId();
                        pack_data_index = i + 1;
                        pack_data[i] = (byte) productId;
                        i = pack_data_index + 1;
                        pack_data[pack_data_index] = (byte) (productId >> 8);
                        pack_data_index = i + 1;
                        pack_data[i] = (byte) (productId >> 16);
                        i = pack_data_index + 1;
                        pack_data[pack_data_index] = (byte) (productId >> 24);
                        String sn = this.usb_ctx.mDevConnetion_.getSerial();
                        if (sn != null) {
                            pack_data_index = i + 1;
                            pack_data[i] = (byte) 1;
                            byte[] string_bytes = sn.getBytes();
                            int sn_size = string_bytes.length;
                            i = pack_data_index + 1;
                            pack_data[pack_data_index] = (byte) sn_size;
                            pack_data_index = i + 1;
                            pack_data[i] = (byte) (sn_size >> 8);
                            i = pack_data_index + 1;
                            pack_data[pack_data_index] = (byte) (sn_size >> 16);
                            pack_data_index = i + 1;
                            pack_data[i] = (byte) (sn_size >> 24);
                            System.arraycopy(string_bytes, 0, pack_data, pack_data_index, string_bytes.length);
                            i = string_bytes.length + 13;
                        } else {
                            pack_data_index = i + 1;
                            pack_data[i] = (byte) 0;
                            i = pack_data_index;
                        }
                        res = true;
                    } catch (Exception e2) {
                        e = e2;
                        i = pack_data_index;
                        Log.e(log_tag, "Get device info failed: " + e.toString());
                        return res;
                    }
                } catch (Exception e3) {
                    e = e3;
                    Log.e(log_tag, "Get device info failed: " + e.toString());
                    return res;
                }
            }
        }
        return res;
    }

    public static boolean IsFutronicDevice(int idVendor, int idProduct) {
        if (!((idVendor == 2100 && idProduct == 32) || ((idVendor == 2392 && idProduct == 775) || (idVendor == 5265 && (idProduct == 32 || idProduct == 37 || idProduct == 136 || idProduct == 144 || idProduct == 80 || idProduct == 96 || idProduct == 152 || idProduct == 32920 || idProduct == 39008))))) {
            if (idVendor != 8122) {
                return false;
            }
            if (!(idProduct == 19 || idProduct == 18 || idProduct == 39)) {
                return false;
            }
        }
        return true;
    }

    private FTR_USB_DEVICE_INTERNAL OpenDevice(UsbDevice device) {
        UsbInterface intf = device.getInterface(0);
        if (intf != null) {
            UsbEndpoint readpoint = null;
            UsbEndpoint writepoint = null;
            for (int i = 0; i < intf.getEndpointCount(); i++) {
                if (intf.getEndpoint(i).getDirection() == 0) {
                    writepoint = intf.getEndpoint(i);
                }
                if (intf.getEndpoint(i).getDirection() == 128) {
                    readpoint = intf.getEndpoint(i);
                }
            }
            if (readpoint == null || writepoint == null) {
                Log.e(log_tag, "End points not found in device: " + device);
                return null;
            }
            UsbDeviceConnection connection = this.mDevManager.openDevice(device);
            if (connection != null) {
                return new FTR_USB_DEVICE_INTERNAL(device, intf, readpoint, writepoint, connection);
            }
            Log.e(log_tag, "open device failed: " + device);
            return null;
        }
        Log.e(log_tag, "Get interface failed failed in device: " + device);
        return null;
    }

    public byte[] getTransferBuffer() {
        return this.max_transfer_buffer;
    }

    public void setTransferBuffer(byte[] max_transfer_buffer) {
        this.max_transfer_buffer = max_transfer_buffer;
    }

    public static void GetInterfaces(Context ctx, byte[] pInterfaceList) {
        for (int index = 0; index < 128; index++) {
            pInterfaceList[index] = (byte) 1;
        }
        for (UsbDevice device : ((UsbManager) ctx.getSystemService(Context.USB_SERVICE)).getDeviceList().values()) {
            if (IsFutronicDevice(device.getVendorId(), device.getProductId())) {
                pInterfaceList[0] = (byte) 0;
            }
        }
    }
}
