package com.example.broadcasttelephone

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.provider.CallLog
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat

private const val TAG = "TelefonoBroadcastReceiver"

class TelefonoBroadcastReceiver : BroadcastReceiver() {

    companion object {
        //Guarda el número para el cual ya se envió un SMS
        private var lastSentNumber: String? = null
    }

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            TelephonyManager.ACTION_PHONE_STATE_CHANGED -> {

                val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)
                var incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

                Log.d(TAG, "Phone state changed: $state, incoming: $incomingNumber")

                //Si incomingNumber es nulo y el estado es RINGING, intenta recuperar el número del Call Log
                if (incomingNumber == null && state == TelephonyManager.EXTRA_STATE_RINGING) {
                    //Verifica que se tenga el permiso READ_CALL_LOG
                    if (ActivityCompat.checkSelfPermission(
                            context, Manifest.permission.READ_CALL_LOG
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        //Consulta el Call Log ordenado por fecha descendente
                        val cursor = context.contentResolver.query(
                            CallLog.Calls.CONTENT_URI,
                            null,
                            null,
                            null,
                            "${CallLog.Calls.DATE} DESC"
                        )
                        //Si el cursor tiene datos, mueve a la primera fila
                        if (cursor != null && cursor.moveToFirst()) {
                            //Obtiene el índice de la columna del número
                            val numberColumnIndex = cursor.getColumnIndex(CallLog.Calls.NUMBER)
                            //Si la columna existe, asigna el número recuperado
                            if (numberColumnIndex != -1) {
                                incomingNumber = cursor.getString(numberColumnIndex)
                                Log.d(TAG, "Recuperado del Call Log: $incomingNumber")
                            }
                        }
                        //Cierra el cursor para liberar recursos
                        cursor?.close()
                    } else {
                        //Log en caso de no tener el permiso necesario
                        Log.d(TAG, "No se tiene permiso READ_CALL_LOG para recuperar el número")
                    }
                }

                if (incomingNumber != null) {
                    Log.d(TAG, "Número entrante: $incomingNumber")

                    if (state == TelephonyManager.EXTRA_STATE_RINGING) {
                        val prefs = context.getSharedPreferences("autoResponderPrefs", Context.MODE_PRIVATE)

                        //Recupera el código de país y el número (sin código) guardados
                        val savedPrefix = prefs.getString("countryCode", "") ?: ""
                        val savedNumberOnly = prefs.getString("phoneNumber", "") ?: ""

                        //Combina ambos para formar el número completo guardado
                        val fullSavedNumber ="+"+ savedPrefix + savedNumberOnly
                        //Recupera el mensaje
                        val savedMessage = prefs.getString("message", "")

                        Log.d(TAG, "Valor guardado - Prefijo: $savedPrefix, " +
                                "Número: $savedNumberOnly, " +
                                "Completo: $fullSavedNumber, " +
                                "Mensaje: $savedMessage")

                        //Compara directamente el número entrante con el número guardado
                        if (incomingNumber == fullSavedNumber && !savedMessage.isNullOrEmpty()) {
                            if (lastSentNumber != incomingNumber) {
                                AutoSmsHelper.sendSms(context, incomingNumber, savedMessage)
                                lastSentNumber = incomingNumber
                                Log.d(TAG, "SMS enviado a $incomingNumber")
                            } else {
                                Log.d(TAG, "SMS ya fue enviado previamente a este número")
                            }
                        } else {
                            Log.d(TAG, "Número entrante no coincide o mensaje vacío")
                        }
                    }

                    //Cuando la llamada finaliza, reseteamos lastSentNumber
                    if (state == TelephonyManager.EXTRA_STATE_OFFHOOK ||
                        state == TelephonyManager.EXTRA_STATE_IDLE
                    ) {
                        lastSentNumber = null
                    }
                } else {
                    Log.d(TAG, "No se recibió número entrante (incomingNumber es null)")
                }
            }

            Intent.ACTION_BOOT_COMPLETED -> {
                Toast.makeText(context, "El sistema se ha iniciado", Toast.LENGTH_LONG).show()
            }

            else -> {
                Log.d(TAG, "Action: ${intent.action}")
            }
        }
    }
}