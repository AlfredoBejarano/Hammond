package com.alfredobejarano.fingerprintscanner

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.alfredobejarano.hammond.receiver.FingerprintReader
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        FingerprintReader.status.observe(this, Observer {
            textView.text = it?.toString()
        })
    }
}