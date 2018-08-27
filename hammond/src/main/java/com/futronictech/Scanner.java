package com.futronictech;

import android.content.Context;

public class Scanner {
    public static final byte FTRSCAN_INTERFACE_STATUS_CONNECTED = (byte) 0;
    public static final byte FTRSCAN_INTERFACE_STATUS_DISCONNECTED = (byte) 1;
    public static final byte FTR_DEVICE_USB_2_0_TYPE_50 = (byte) 7;
    public static final byte FTR_DEVICE_USB_2_0_TYPE_60 = (byte) 8;
    public static final byte FTR_DEVICE_USB_2_0_TYPE_64 = (byte) 15;
    public static final int FTR_MAX_INTERFACE_NUMBER = 128;
    public static final int FTR_TIMEOUT_INFINITE = -1;
    public final int FTR_ERROR_EMPTY_FRAME = 4306;
    public final int FTR_ERROR_FIRMWARE_INCOMPATIBLE = 536870917;
    public final int FTR_ERROR_HARDWARE_INCOMPATIBLE = 536870916;
    public final int FTR_ERROR_INVALID_AUTHORIZATION_CODE = 536870918;
    public final int FTR_ERROR_LIBUSB_ERROR = 536870929;
    public final int FTR_ERROR_MOVABLE_FINGER = 536870913;
    public final int FTR_ERROR_NOT_ENOUGH_MEMORY = 8;
    public final int FTR_ERROR_NOT_READY = 21;
    public final int FTR_ERROR_NOT_SUPPORTED = 50;
    public final int FTR_ERROR_NO_ERROR = 0;
    public final int FTR_ERROR_NO_FRAME = 536870914;
    public final int FTR_ERROR_ROLL_ABORTED = 536870922;
    public final int FTR_ERROR_ROLL_ALREADY_STARTED = 536870923;
    public final int FTR_ERROR_ROLL_NOT_STARTED = 536870919;
    public final int FTR_ERROR_ROLL_PROGRESS_DATA = 536870920;
    public final int FTR_ERROR_ROLL_PROGRESS_POST_PROCESSING = 536870926;
    public final int FTR_ERROR_ROLL_PROGRESS_PUT_FINGER = 536870925;
    public final int FTR_ERROR_ROLL_PROGRESS_REMOVE_FINGER = 536870924;
    public final int FTR_ERROR_ROLL_TIMEOUT = 536870921;
    public final int FTR_ERROR_WRITE_PROTECT = 19;
    public final int FTR_OPTIONS_CHECK_FAKE_REPLICA = 1;
    public final int FTR_OPTIONS_DETECT_FAKE_FINGER = 1;
    public final int FTR_OPTIONS_ELIMINATE_BACKGROUND = 2048;
    public final int FTR_OPTIONS_IMAGE_FORMAT_1 = 256;
    public final int FTR_OPTIONS_IMAGE_FORMAT_MASK = 1792;
    public final int FTR_OPTIONS_IMPROVE_IMAGE = 32;
    public final int FTR_OPTIONS_INVERT_IMAGE = 64;
    public final int FTR_OPTIONS_PREVIEW_MODE = 128;
    private final int kDefaultDeviceInstance = 0;
    private byte m_DeviceCompatibility = (byte) 0;
    private int m_ErrorCode;
    private int m_ImageHeight = 0;
    private int m_ImageWidth = 0;
    private int m_NFIQ;
    private boolean m_bIsJobAborted = false;
    private long m_hDevice = 0;

    public native boolean CloseDevice();

    public native boolean ControlPin3(int i, int i2, int i3);

    public native boolean GetButtonState(byte[] bArr);

    public native boolean GetDiodesStatus(byte[] bArr);

    public native boolean GetFrame(byte[] bArr);

    public native boolean GetImage(int i, byte[] bArr);

    public native boolean GetImage2(int i, byte[] bArr);

    public native boolean GetImageByVariableDose(int i, byte[] bArr);

    public native boolean GetImageSize();

    public native boolean GetInterfaces(byte[] bArr);

    public native String GetMathAPIVersion();

    public native boolean GetNfiqFromImage(byte[] bArr, int i, int i2);

    public native boolean GetSerialNumber(byte[] bArr);

    public native String GetVersionInfo();

    public native boolean ImageSegment(byte[] bArr, int i, int i2, byte[] bArr2, byte[] bArr3, int[] iArr, long[] jArr, double[] dArr, int[] iArr2, int[] iArr3);

    public native boolean ImageSegmentAuto(byte[] bArr, int i, int i2, byte[] bArr2, byte[] bArr3, int[] iArr, long[] jArr, double[] dArr, int[] iArr2, int[] iArr3, int[] iArr4);

    public native boolean IsFingerPresent();

    public native boolean OpenDevice();

    public native boolean OpenDeviceCtx(Object obj);

    public native boolean OpenDeviceOnInterface(int i);

    public native boolean Restore7Bytes(byte[] bArr);

    public native boolean RestoreSecret7Bytes(byte[] bArr, byte[] bArr2);

    public native boolean RollAbort(boolean z);

    public native boolean RollGetFrameParameters(int i, byte[] bArr);

    public native boolean RollGetImage(int i, byte[] bArr);

    public native boolean RollStart();

    public native boolean Save7Bytes(byte[] bArr);

    public native boolean SaveSecret7Bytes(byte[] bArr, byte[] bArr2);

    public native boolean ScanDoseSegment(int i, byte[] bArr, byte[] bArr2, byte[] bArr3, int[] iArr, long[] jArr, double[] dArr, int[] iArr2, int[] iArr3);

    public native boolean ScanDoseSegmentPreview(int i, byte[] bArr, byte[] bArr2, byte[] bArr3, int[] iArr, long[] jArr, double[] dArr, int[] iArr2, int[] iArr3);

    public native boolean ScanFrameSegment(byte[] bArr, byte[] bArr2, byte[] bArr3, int[] iArr, long[] jArr, double[] dArr, int[] iArr2, int[] iArr3);

    public native boolean ScanFrameSegmentPreview(byte[] bArr, byte[] bArr2, byte[] bArr3, int[] iArr, long[] jArr, double[] dArr, int[] iArr2, int[] iArr3);

    public native boolean ScanFrameSegmentPreviewAuto(byte[] bArr, byte[] bArr2, byte[] bArr3, int[] iArr, long[] jArr, double[] dArr, int[] iArr2, int[] iArr3, int[] iArr4);

    public native boolean SetDiodesStatus(int i, int i2);

    public native boolean SetGlobalSyncDir(String str);

    public native boolean SetNewAuthorizationCode(byte[] bArr);

    public native boolean SetOptions(int i, int i2);

    public int GetImageWidth() {
        return this.m_ImageWidth;
    }

    public int GetImaegHeight() {
        return this.m_ImageHeight;
    }

    public int GetErrorCode() {
        return this.m_ErrorCode;
    }

    public int GetNIFQValue() {
        return this.m_NFIQ;
    }

    public byte GetDeviceCompatibility() {
        return this.m_DeviceCompatibility;
    }

    public boolean IsJobAborted() {
        return this.m_bIsJobAborted;
    }

    public String GetErrorMessage() {
        this.m_bIsJobAborted = false;
        String strErrMsg;
        switch (this.m_ErrorCode) {
            case 0:
                return "OK";
            case 19:
                return "Write Protect";
            case 50:
                strErrMsg = "- This feature is not supported -";
                this.m_bIsJobAborted = true;
                return strErrMsg;
            case 4306:
                return "Empty Frame";
            case 536870913:
                return "Moveable Finger";
            case 536870914:
                return "Fake Finger";
            case 536870916:
                return "Hardware Incompatible";
            case 536870917:
                return "Firmware Incompatible";
            case 536870918:
                return "Invalid Authorization Code";
            case 536870919:
                strErrMsg = "- The roll operation is not started - ";
                this.m_bIsJobAborted = true;
                return strErrMsg;
            case 536870920:
                return "- Processing... -";
            case 536870921:
                return "- Operation timeout... -";
            case 536870922:
                strErrMsg = "- Operation canceled by user -";
                this.m_bIsJobAborted = true;
                return strErrMsg;
            case 536870923:
                strErrMsg = "- Operation is already started -";
                this.m_bIsJobAborted = true;
                return strErrMsg;
            case 536870924:
                return "- Please remove finger - ";
            case 536870925:
                return "- Please put finger - ";
            case 536870926:
                return "- Post processing - ";
            case 536870929:
                return "System libusb error!";
            default:
                return String.format("Error code is %d", new Object[]{Integer.valueOf(this.m_ErrorCode)});
        }
    }

    public boolean OpenDeviceOnInterfaceUsbHost(UsbDeviceDataExchangeImpl usb_host_ctx) {
        boolean res;
        synchronized (this) {
            res = OpenDeviceCtx(usb_host_ctx);
        }
        return res;
    }

    public boolean GetInterfacesUsbHost(Context ctx, byte[] pInterfaceList) {
        if (pInterfaceList.length < 128) {
            this.m_ErrorCode = 8;
            return false;
        }
        UsbDeviceDataExchangeImpl.GetInterfaces(ctx, pInterfaceList);
        return true;
    }

    public void CloseDeviceUsbHost() {
        synchronized (this) {
            CloseDevice();
        }
    }

    public long GetDeviceHandle() {
        return this.m_hDevice;
    }

    static {
        System.loadLibrary("usb-1.0");
        System.loadLibrary("ftrScanAPI");
        System.loadLibrary("ftrMathAPIAndroid");
        System.loadLibrary("ftrScanApiAndroidJni");
    }
}
