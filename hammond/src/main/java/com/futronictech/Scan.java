package com.futronictech;

import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;

public class Scan {
    public static final int MESSAGE_ADDITIONAL_INFO = 6;
    public static final int MESSAGE_EMPTY_FRAME = 7;
    public static final int MESSAGE_END_OPERATION = 5;
    public static final int MESSAGE_ERROR = 4;
    public static final int MESSAGE_SHOW_IMAGE = 3;
    public static final int MESSAGE_SHOW_MSG = 1;
    public static final int MESSAGE_SHOW_SCANNER_INFO = 2;
    private UsbDeviceDataExchangeImpl ctx = null;
    private boolean fingerCaptured;
    private final Handler mHandler;
    private byte[] mImageFP;
    private int mImageHeight;
    private int mImageWidth;
    private ScanThread mScanThread;
    private byte[] serialNumber;

    private class ScanThread extends Thread {
        private boolean bGetInfo;
        private boolean bRet;
        private Scanner devScan;
        private int errCode;
        private int flag;
        private int mask;
        private int nNfiq;
        private String strInfo;
        String strSN;

        public ScanThread() {
            this.devScan = null;
            this.nNfiq = 0;
            this.strSN = "";
            this.bGetInfo = false;
            this.devScan = new Scanner();
        }

        public void run() {
            Scan();
        }

        public void Scan() {
            int i;
            if (!this.bGetInfo) {
                if (OpenDevice()) {
                    byte[] SN = new byte[8];
                    if (this.devScan.GetSerialNumber(SN)) {
                        this.strSN = new String(SN);
                        Log.i("FUTRONIC", "SN: " + this.strSN);
                    }
                    if (this.devScan.GetImageSize()) {
                        Scan.this.mImageWidth = this.devScan.GetImageWidth();
                        Scan.this.mImageHeight = this.devScan.GetImaegHeight();
                        Scan.this.mImageFP = new byte[(Scan.this.mImageWidth * Scan.this.mImageHeight)];
                        this.bGetInfo = true;
                    } else {
                        Scan.this.mHandler.obtainMessage(4, -1, -1, this.devScan.GetErrorMessage()).sendToTarget();
                        CloseDevice();
                        return;
                    }
                }
                return;
            }
            this.flag = 0;
            Scanner scanner = this.devScan;
            this.devScan.getClass();
            if (!scanner.SetOptions(1, this.flag)) {
                Scan.this.mHandler.obtainMessage(4, -1, -1, this.devScan.GetErrorMessage()).sendToTarget();
            }
            this.devScan.getClass();
            this.flag = 64;
            scanner = this.devScan;
            this.devScan.getClass();
            if (!scanner.SetOptions(64, this.flag)) {
                Scan.this.mHandler.obtainMessage(4, -1, -1, this.devScan.GetErrorMessage()).sendToTarget();
            }
            long lT1 = SystemClock.uptimeMillis();
            this.bRet = this.devScan.GetFrame(Scan.this.mImageFP);
            if (this.bRet) {
                if (this.devScan.GetNfiqFromImage(Scan.this.mImageFP, Scan.this.mImageWidth, Scan.this.mImageHeight)) {
                    this.nNfiq = this.devScan.GetNIFQValue();
                    this.strInfo = String.format("OK. GetImage2 time is %d(ms).", new Object[]{Long.valueOf(SystemClock.uptimeMillis() - lT1)});
                }
                this.strInfo += String.format("NFIQ=%d", new Object[]{Integer.valueOf(this.nNfiq)});
            } else {
                Scan.this.mHandler.obtainMessage(4, -1, -1, this.devScan.GetErrorMessage()).sendToTarget();
                this.errCode = this.devScan.GetErrorCode();
                i = this.errCode;
                this.devScan.getClass();
                if (i != 4306) {
                    i = this.errCode;
                    this.devScan.getClass();
                    if (i != 536870913) {
                        i = this.errCode;
                        this.devScan.getClass();
                        if (i != 536870914) {
                            CloseDevice();
                            return;
                        }
                    }
                }
            }
            i = this.errCode;
            this.devScan.getClass();
            if (i != 4306) {
                Scan.this.mHandler.obtainMessage(3, Scan.this.mImageWidth, Scan.this.mImageHeight, Scan.this.mImageFP).sendToTarget();
                if (this.nNfiq <= 5) {
                    Scan.this.fingerCaptured = true;
                } else {
                    Scan.this.fingerCaptured = false;
                }
            } else {
                Scan.this.mHandler.obtainMessage(7).sendToTarget();
                Scan.this.fingerCaptured = false;
            }
            Scan.this.mHandler.obtainMessage(6, -1, this.nNfiq, this.strSN).sendToTarget();
            Scan.this.mHandler.obtainMessage(5, -1, -1, this.devScan.GetErrorMessage()).sendToTarget();
            CloseDevice();
        }

        boolean OpenDevice() {
            if (this.devScan == null) {
                return false;
            }
            boolean bRet;
            if (Scan.this.ctx != null) {
                bRet = this.devScan.OpenDeviceOnInterfaceUsbHost(Scan.this.ctx);
            } else {
                bRet = this.devScan.OpenDevice();
            }
            if (bRet) {
                this.strInfo = this.devScan.GetVersionInfo();
                Scan.this.mHandler.obtainMessage(2, -1, -1, this.strInfo).sendToTarget();
                return true;
            }
            Scan.this.mHandler.obtainMessage(4, -1, -1, this.devScan.GetErrorMessage()).sendToTarget();
            Scan.this.mHandler.obtainMessage(5, -1, -1, Scan.this.fingerCaptured).sendToTarget();
            return false;
        }

        public void CloseDevice() {
            if (this.devScan != null) {
                if (Scan.this.ctx != null) {
                    this.devScan.CloseDeviceUsbHost();
                } else {
                    this.devScan.CloseDevice();
                }
            }
        }

        public void cancel() {
            try {
                join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Scan(UsbDeviceDataExchangeImpl context, Handler handler) {
        this.mHandler = handler;
        this.ctx = context;
    }

    public synchronized void start() {
        if (this.mScanThread == null) {
            this.mScanThread = new ScanThread();
            this.mScanThread.start();
        }
    }

    public synchronized void stop() {
        if (this.mScanThread != null) {
            this.mScanThread.cancel();
            this.mScanThread = null;
        }
    }
}
