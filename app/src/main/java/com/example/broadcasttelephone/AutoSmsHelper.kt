package com.example.broadcasttelephone

import android.content.Context
import android.telephony.SmsManager
import android.util.Log

object AutoSmsHelper {
    private const val TAG = "AutoSmsHelper"

    fun sendSms(context: Context, phoneNumber: String, message: String) {
        try {
            val smsManager = SmsManager.getDefault()
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            Log.d(TAG, "SMS enviado a $phoneNumber")
        } catch (e: Exception) {
            Log.e(TAG, "Error al enviar SMS a $phoneNumber", e)
        }
    }
}
