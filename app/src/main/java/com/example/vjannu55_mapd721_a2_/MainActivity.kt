package com.example.vjannu55_mapd721_a2_

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.format.DateUtils.formatDateTime
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.HeartRateRecord
import com.malikosft.assignment.ui.theme.AssignmentTheme
import java.time.LocalDateTime
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
        val date = remember { mutableStateOf(formatDateTime(LocalDateTime.now())) } // Converted to String
//    val date = remember { mutableStateOf(LocalDateTime.now()) }
        val permissionsGranted = remember { mutableStateOf(false) }
        val heartRateRecords = remember { mutableStateOf<List<String>>(emptyList()) }






    }

    private fun formatDateTime(dateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
        return dateTime.format(formatter)
    }
}