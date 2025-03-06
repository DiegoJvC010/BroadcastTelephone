package com.example.broadcasttelephone

import android.app.Service
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.telephony.TelephonyManager


//Servicio que registra un BroadcastReceiver para monitorear cambios en el estado de llamadas
class ServicePhoneState : Service() {

    //Instancia del BroadcastReceiver que se encarga de manejar los eventos de estado de llamada
    val br: TelefonoBroadcastReceiver = TelefonoBroadcastReceiver()
    //IntentFilter para capturar cambios en el estado del teléfono (RINGING, OFFHOOK, IDLE, etc)
    val intentFilter = IntentFilter(TelephonyManager.ACTION_PHONE_STATE_CHANGED)

    //Funcion que se invoca cuando otro componente se vincula al servicio
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    //Se ejecuta cuando el servicio es creado
    override fun onCreate() {
        super.onCreate()
        //Registra el BroadcastReceiver para comenzar a recibir eventos de estado de llamada
        registerReceiver(br, intentFilter)
    }

    //Se ejecuta cuando el servicio se destruye
    override fun onDestroy() {
        super.onDestroy()
        //Quita el registro del BroadcastReceiver para evitar fugas de memoria
        unregisterReceiver(br)
    }
}
