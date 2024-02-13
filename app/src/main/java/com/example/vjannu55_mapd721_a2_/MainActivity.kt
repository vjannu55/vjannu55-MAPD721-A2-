package com.example.vjannu55_mapd721_a2_

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils.formatDateTime
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import com.malikosft.assignment.ui.theme.AssignmentTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

class MainActivity : AppCompatActivity() {
    private val healthConnectClient by lazy { HealthConnectClient.getOrCreate(baseContext) }

    val permissions = setOf(
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AssignmentTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    formFilling()
                }
            }
        }
    }

    @SuppressLint("SuspiciousIndentation")
    @OptIn(ExperimentalComposeUiApi::class)
    @Composable
    fun formFilling() {
        val heartRate = remember { mutableStateOf("") }
        val date =
            remember { mutableStateOf(formatDateTime(LocalDateTime.now())) } // Converted to String
//    val date = remember { mutableStateOf(LocalDateTime.now()) }
        val permissionsGranted = remember { mutableStateOf(false) }
        val heartRateRecords = remember { mutableStateOf<List<String>>(emptyList()) }

        fun updateHeartRateRecords(records: List<String>) {
            heartRateRecords.value = records
        }

        val permissionsLauncher = rememberLauncherForActivityResult(
            requestPermissionsActivityContract()
        ) { permissionsGranted ->
            if (permissionsGranted.containsAll(permissions)) {
                // Proceed with the action that requires permissions
            } else {
                Toast.makeText(
                    this@MainActivity,
                    "Not all permissions granted",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        val keyboardController = LocalSoftwareKeyboardController.current
        val context = LocalContext.current

        val valid = remember(heartRate.value, date.value) {
            heartRate.value.isNotEmpty() && date.value.toString().isNotEmpty()


        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(5.dp)
        ) {
            OutlinedTextField(
                value = heartRate.value,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || (newValue.toIntOrNull() ?: 0) in 1..300) {
                        heartRate.value = newValue
                    }
                },
                label = { Text(text = "Heart Rate") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            )

            OutlinedTextField(
                value = date.value,
                onValueChange = { newValue ->
                    date.value = newValue // Update date value
                },
                label = { Text("Date") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            )


            Row(
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(5.dp)
            ) {
                Button(
                    onClick = {
                        CoroutineScope(Dispatchers.Main).launch {
                            permissionsGranted.value = hasAllPermissions(permissions)
                            if (permissionsGranted.value) {
                                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                val localDateTime = LocalDateTime.parse(date.value, formatter)
                                val startOfSession = localDateTime.atZone(ZoneOffset.UTC)

                                val endOfSession = startOfSession.plusMinutes(30)
                                readStepsByTimeRange(
                                    healthConnectClient,
                                    startOfSession.toInstant(),
                                    endOfSession.toInstant(),
                                    ::updateHeartRateRecords
                                )
                                keyboardController?.hide()
                            } else {
                                permissionsLauncher.launch(permissions)
                            }
                        }
                    },
                    modifier = Modifier
                        .width(150.dp)
                        .padding(start = 10.dp, end = 10.dp)
                ) {
                    Text(text = "Load")
                }

                Button(
                    onClick = {
                        if (valid) {
                            CoroutineScope(Dispatchers.Main).launch {
                                permissionsGranted.value = hasAllPermissions(permissions)
                                if (permissionsGranted.value) {
                                    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
                                    val localDateTime = LocalDateTime.parse(date.value, formatter)
                                    val startOfSession = localDateTime.atZone(ZoneOffset.UTC)

                                    val endOfSession = startOfSession.plusMinutes(30)
                                    val heartRateValue = heartRate.value.toIntOrNull() ?: 0
                                    writeExerciseSession(
                                        startOfSession,
                                        endOfSession,
                                        heartRateValue
                                    )
                                    keyboardController?.hide()
                                } else {
                                    permissionsLauncher.launch(permissions)
                                }
                            }
                        } else {
                            Toast.makeText(context, "Fields cannot be empty", Toast.LENGTH_LONG)
                                .show()
                        }
                    },
                    modifier = Modifier
                        .width(150.dp)
                        .padding(start = 10.dp, end = 10.dp)
                ) {
                    Text(text = "Save")
                }
            }
            Spacer(modifier = Modifier.height(50.dp))


            Text(
                text = "HeartRate History",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(8.dp)
            )
        }
    }



            private fun formatDateTime(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return dateTime.format(formatter)
    }
}