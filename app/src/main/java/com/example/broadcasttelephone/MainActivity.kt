package com.example.broadcasttelephone

import android.Manifest
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.activity.result.contract.ActivityResultContracts


class MainActivity : ComponentActivity() {

    //Launcher para solicitar múltiples permisos, incluyendo READ_CALL_LOG
    private val requestMultiplePermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.all { it.value }) {
            Toast.makeText(this, "Todos los permisos fueron otorgados", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Algunos permisos fueron denegados", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //Solicita los permisos necesarios (READ_PHONE_STATE, SEND_SMS, RECEIVE_SMS y READ_CALL_LOG)
        requestAppPermissions()

        //Configura la UI con Compose
        setContent {
            MainScreen()
        }
    }

    private fun requestAppPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val requiredPermissions = arrayOf(
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.SEND_SMS,
                Manifest.permission.RECEIVE_SMS,
                Manifest.permission.READ_CALL_LOG
            )
            requestMultiplePermissionsLauncher.launch(requiredPermissions)
        }
    }
}


@Composable
fun MainScreen() {
    val context = LocalContext.current
    //SharedPreferences para guardar la configuración
    val prefs = context.getSharedPreferences("autoResponderPrefs", Context.MODE_PRIVATE)

    //Estados para el código de país, número y mensaje
    var countryCode by rememberSaveable { mutableStateOf(prefs.getString("countryCode", "") ?: "") }
    var phoneNumber by rememberSaveable { mutableStateOf(prefs.getString("phoneNumber", "") ?: "") }
    var message by rememberSaveable { mutableStateOf(prefs.getString("message", "") ?: "") }

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Top
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            //Campo para el código de país (solo dígitos, máximo 3)
            OutlinedTextField(
                value = countryCode,
                onValueChange = { newVal ->
                    if (newVal.all { it.isDigit() } && newVal.length <= 3) {
                        countryCode = newVal
                    }
                },
                label = { Text("Código de país (Ej: 52)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            //Campo para el número de teléfono (sin el codigo)
            OutlinedTextField(
                value = phoneNumber,
                onValueChange = { newVal ->
                    if (newVal.all { it.isDigit() } && newVal.length <= 15) {
                        phoneNumber = newVal
                    }
                },
                label = { Text("Número de Teléfono") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))
            //Campo para el mensaje de respuesta
            OutlinedTextField(
                value = message,
                onValueChange = { newVal ->
                    if (newVal.length <= 200) {
                        message = newVal
                    }
                },
                label = { Text("Mensaje de Respuesta Automática") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            //Boton para guardar la configuración
            Button(
                onClick = {
                    //Validación del código de país: debe tener de 1 a 3 dígitos
                    if (!countryCode.matches(Regex("^[0-9]{1,3}\$"))) {
                        Toast.makeText(
                            context,
                            "El código de país debe tener de 1 a 3 dígitos",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    //Validación del número: entre 7 y 15 dígitos
                    if (!phoneNumber.matches(Regex("^\\d{7,15}\$"))) {
                        Toast.makeText(
                            context,
                            "El número debe tener entre 7 y 15 dígitos",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    if (message.isBlank()) {
                        Toast.makeText(
                            context,
                            "El mensaje no puede estar vacío",
                            Toast.LENGTH_LONG
                        ).show()
                        return@Button
                    }

                    prefs.edit().apply {
                        putString("countryCode", countryCode)
                        putString("phoneNumber", phoneNumber)
                        putString("message", message)
                        apply()
                    }
                    Toast.makeText(context, "Configuración guardada", Toast.LENGTH_SHORT).show()
                    Log.d("MainScreen", "Valor guardado - Código de país: $countryCode, Número: $phoneNumber, Mensaje: $message")

                },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text("Guardar Configuración")
            }
        }
    }
}
