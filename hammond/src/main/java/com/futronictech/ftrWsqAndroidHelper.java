package com.futronictech;

public class ftrWsqAndroidHelper {
    public int mBMP_size = 0;
    public float mBitrate = 0.75f;
    public int mDPI = 0;
    public int mHeight = 0;
    public int mRAW_size = 0;
    public int mWSQ_size = 0;
    public int mWidth = 0;

    private native boolean JNIGetImageParameters(byte[] bArr);

    private native boolean JNIRawToWsqImage(long j, int i, int i2, float f, byte[] bArr, byte[] bArr2);

    private native boolean JNIWsqToRawImage(byte[] bArr, byte[] bArr2);

    static {
        System.loadLibrary("usb-1.0");
        System.loadLibrary("ftrScanAPI");
        System.loadLibrary("ftrWSQAndroid");
        System.loadLibrary("ftrWSQAndroidJni");
    }

    public boolean ConvertRawToWsq(long hDevice, int nWidth, int nHeight, float fBitrate, byte[] rawImg, byte[] wsqImg) {
        if (rawImg.length == nWidth * nHeight && wsqImg.length == nWidth * nHeight && ((double) fBitrate) <= 2.25d && ((double) fBitrate) >= 0.75d) {
            return JNIRawToWsqImage(hDevice, nWidth, nHeight, fBitrate, rawImg, wsqImg);
        }
        return false;
    }

    public int GetWsqImageRawSize(byte[] wsqImg) {
        if (JNIGetImageParameters(wsqImg)) {
            return this.mWidth * this.mHeight;
        }
        return 0;
    }

    public boolean ConvertWsqToRaw(byte[] wsqImg, byte[] rawImg) {
        if (rawImg.length < this.mWidth * this.mHeight) {
            return false;
        }
        return JNIWsqToRawImage(wsqImg, rawImg);
    }
}
