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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
fun PlaceHolderView(message: String) {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)
    ) {
        Text(
            text = message,
            fontSize = 24.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun CheckDeviceVersionView() {
    PlaceHolderView("Checking for updates...")
}

@Composable
fun NoUpdateRequiredView() {
    PlaceHolderView("Your device is up to date")
}

@Composable
fun DeviceUnreachableView() {
    PlaceHolderView("Device unreachable!\nMake sure it's connected and provisioned.")
}

@Composable
fun StartUpdatePromptView() {
    PlaceHolderView("Shall we update the firmware?")
}

@Composable
fun UpdateInProgressView() {
    PlaceHolderView("Updating right now...")
}

@Composable
fun UpdateCompleteView() {
    PlaceHolderView("Update completed successfully")
}

@Composable
fun UpdateFailedView() {
    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
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
fun CheckDeviceVersionViewPreview() {
    AerohandroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            CheckDeviceVersionView()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun NoUpdateRequiredViewPreview() {
    AerohandroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NoUpdateRequiredView()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceUnreachableViewPreview() {
    AerohandroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            DeviceUnreachableView()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun StartUpdatePromptViewPreview() {
    AerohandroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            StartUpdatePromptView()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateInProgressViewPreview() {
    AerohandroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UpdateInProgressView()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateCompleteViewPreview() {
    AerohandroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UpdateCompleteView()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateFailedViewPreview() {
    AerohandroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UpdateFailedView()
        }
    }
}