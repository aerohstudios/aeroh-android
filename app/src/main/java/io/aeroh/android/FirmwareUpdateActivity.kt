package io.aeroh.android

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aeroh.android.FirmwareUpdateActivity.FirmwareUpdateStatus.*
import io.aeroh.android.ui.theme.AerohandroidTheme

import io.aeroh.android.models.Device

class FirmwareUpdateActivity : ComponentActivity() {
    var device: Device? = null
    enum class FirmwareUpdateStatus {
        CHECKING_DEVICE_VERSION,
        NO_UPDATE_REQUIRED,
        DEVICE_UNREACHABLE,
        START_UPDATE_PROMPT,
        UPDATE_IN_PROGRESS,
        UPDATE_COMPLETE,
        UPDATE_FAILED
    }

    private var firmwareUpdateStatus = mutableStateOf(CHECKING_DEVICE_VERSION)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        device = getIntent()?.getExtras()?.get("device") as Device
        getFirmwareVersion()

        setContent {
            AerohandroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (firmwareUpdateStatus.value) {
                        CHECKING_DEVICE_VERSION -> CheckDeviceVersionView()
                        NO_UPDATE_REQUIRED -> NoUpdateRequiredView()
                        DEVICE_UNREACHABLE -> DeviceUnreachableView()
                        START_UPDATE_PROMPT -> StartUpdatePromptView()
                        UPDATE_IN_PROGRESS -> UpdateInProgressView()
                        UPDATE_COMPLETE -> UpdateCompleteView()
                        UPDATE_FAILED -> UpdateFailedView()
                        else -> {}
                    }
                }
            }
        }
    }

    private fun getFirmwareVersion() {
        Handler(Looper.getMainLooper()).postDelayed({
            firmwareUpdateStatus.value = NO_UPDATE_REQUIRED
        }, 2000)
    }
}

@Composable
fun CheckDeviceVersionView() {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = "Checking for updates...",
            fontSize = 24.sp
        )
    }
}

@Composable
fun NoUpdateRequiredView() {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = "Your device is up to date",
            fontSize = 24.sp
        )
    }
}

@Composable
fun DeviceUnreachableView() {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = "Device unreachable. Make sure it's connected and provisioned.",
            fontSize = 24.sp
        )
    }
}

@Composable
fun StartUpdatePromptView() {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = "Shall we update the firmware?",
            fontSize = 24.sp
        )
    }
}

@Composable
fun UpdateInProgressView() {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = "Updating right now...",
            fontSize = 24.sp
        )
    }
}

@Composable
fun UpdateCompleteView() {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = "Update completed successfully",
            fontSize = 24.sp
        )
    }
}

@Composable
fun UpdateFailedView() {
    Row(
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text(
            text = "Update failed!",
            fontSize = 24.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun FirmwareUpdateActivityPreview() {
    AerohandroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CheckDeviceVersionView()
        }
    }
}