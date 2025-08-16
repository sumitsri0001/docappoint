package com.example.doctorappointment

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.JavascriptInterface

class WebAppInterface(val context: Context, val onLocationPicked: (Double, Double, String) -> Unit) {
    @JavascriptInterface
    fun onLocationSelected(lat: Double, lon: Double, address: String) {
        Handler(Looper.getMainLooper()).post {
            onLocationPicked(lat, lon, address)
        }
    }
}