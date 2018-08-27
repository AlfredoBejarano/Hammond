package com.alfredobejarano.fingerprintscanner

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Message
import android.support.v7.app.AppCompatActivity
import com.alfredobejarano.hammond.receiver.FingerprintReader
import com.alfredobejarano.hammond.receiver.listener.OnScanResultListener
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        scan_result?.setOnClickListener {
            // Check if the device is connected.
            if (FingerprintReader.isUsable(this)) {
                // Perform a fingerprint scan.
                FingerprintReader.scan(this, object : OnScanResultListener {
                    override fun onResultFailure(msg: Message) {
                        fingerprint_status.text = msg.what.toString()
                    }

                    override fun onResultSuccess(scan: Bitmap, resolution: Int,
                                                 deviceVendorId: Int, deviceProductId: Int) {
                        scan_result?.setImageBitmap(scan)
                    }
                })
            }
        }
    }
}