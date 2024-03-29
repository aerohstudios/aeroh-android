package io.aeroh.android

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aeroh.android.FirmwareUpdateActivity.FirmwareUpdateStatus.*
import io.aeroh.android.models.Device
import io.aeroh.android.ui.theme.AerohAndroidTheme
import io.aeroh.android.utils.MQTTClient
import org.eclipse.paho.client.mqttv3.IMqttToken
import org.json.JSONObject
import java.util.UUID

class FirmwareUpdateActivity : ComponentActivity() {
    var device: Device? = null
    enum class FirmwareUpdateStatus {
        CHECKING_DEVICE_VERSION,
        NO_UPDATE_REQUIRED,
        DEVICE_UNREACHABLE,
        START_UPDATE_PROMPT,
        UPDATE_CANCELLED,
        UPDATE_IN_PROGRESS,
        UPDATE_COMPLETE,
        UPDATE_FAILED,
        DISMISS
    }

    enum class OTAStatus {
        NOT_STARTED,
        REQUESTED,
        DOWNLOADING,
        COMPLETE,
        FAILED,
    }

    enum class OTAFailReason {
        UNDEFINED,
        DEVICE_NOT_RESPONDING_TO_UPDATE_REQUEST,
        FAILURE_AFTER_OTA_REQUEST // can mean device started with the old firmware, or it didn't start or the new firmware has the same version as the old firmware
    }
    enum class CallbackName {
        GOT_VERSION,
        GOT_UPDATE_STATUS,
        CHECK_NEW_VERSION
    }

    private var firmwareUpdateStatus = mutableStateOf(CHECKING_DEVICE_VERSION)
    private var otaStatus = mutableStateOf(OTAStatus.NOT_STARTED)
    private var otaFailReason = mutableStateOf(OTAFailReason.UNDEFINED)

    private var mqttClient: MQTTClient? = null
    private var mqttCallbackRegistry: HashMap<String, CallbackName> = HashMap()

    private var deviceFirmwareVersion: String = "0.0.0";
    private var latestFirmwareVersion: String = "0.0.0"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        device = getIntent()?.getExtras()?.get("device") as Device
        mqttClient = MQTTClient(applicationContext, device!!.mqtt_uri, device!!.thing_name)
        connectToMQTTServer()

        setContent {
            AerohAndroidTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (firmwareUpdateStatus.value) {
                        CHECKING_DEVICE_VERSION -> CheckDeviceVersionView()
                        NO_UPDATE_REQUIRED -> NoUpdateRequiredView(deviceFirmwareVersion, latestFirmwareVersion)
                        DEVICE_UNREACHABLE -> DeviceUnreachableView()
                        START_UPDATE_PROMPT -> StartUpdatePromptView(firmwareUpdateStatus, deviceFirmwareVersion, latestFirmwareVersion, this)
                        UPDATE_CANCELLED -> finish()
                        UPDATE_IN_PROGRESS -> UpdateInProgressView(otaStatus.value)
                        UPDATE_COMPLETE -> UpdateCompleteView(firmwareUpdateStatus)
                        UPDATE_FAILED -> UpdateFailedView()
                        DISMISS -> finish()
                        else -> {}
                    }
                }
            }
        }
    }

    // --> Ask for current version to check against latest version
    private fun askDeviceFirmwareVersion(retryCount: Int, retryDelay: Long?, originalRequestToken: String?) {
        Log.d(this.javaClass.simpleName, "Step 1. > askDeviceFirmwareVersion: $retryCount")
        if (retryCount == 0) {
            firmwareUpdateStatus.value = DEVICE_UNREACHABLE
            return
        }
        val requestMessage = JSONObject()
        requestMessage.put("command", "firmware")
        requestMessage.put("action_type", "version")

        publishMQTTMessage(requestMessage, CallbackName.GOT_VERSION, originalRequestToken ?: UUID.randomUUID().toString())

        Handler(Looper.getMainLooper()).postDelayed({
            if (firmwareUpdateStatus.value == CHECKING_DEVICE_VERSION) {
                askDeviceFirmwareVersion(retryCount-1, retryDelay, originalRequestToken);
            }
        }, retryDelay ?: 3000)
    }

    // --> GOT_VERSION Callback for askDeviceFirmwareVersion
    private fun requestFirmwareVersionCallback(response: JSONObject) {
        Log.d(this.javaClass.simpleName, "Step 1. > RequestFirmwareVersionCallback")
        val version = response.get("version") as String
        Log.d(this.javaClass.simpleName, "Got version: $version")
        setDeviceFirmwareVersion(version)
    }

    // --> Ask the device to start the update process
    fun updateFirmware(version: String) {
        Log.d(this.javaClass.simpleName, "Step 2. > Update Firmware")
        if (otaStatus.value == OTAStatus.NOT_STARTED) {
            requestFirmwareUpdateWithRetry(version, 10, null, null)
            otaStatus.value = OTAStatus.REQUESTED
        }
    }

    // --> update request will be retried till we get a response
    private fun requestFirmwareUpdateWithRetry(version: String, retryCount: Int, retryDelay: Long?, originalRequestToken: String?) {
        Log.d(this.javaClass.simpleName, "Step 2. > Update Firmware with Retry: $retryCount")
        if (otaStatus.value == OTAStatus.REQUESTED && retryCount == 0) {
            otaStatus.value = OTAStatus.FAILED
            firmwareUpdateStatus.value = UPDATE_FAILED
            otaFailReason.value = OTAFailReason.DEVICE_NOT_RESPONDING_TO_UPDATE_REQUEST
            return
        }

        val requestMessage = JSONObject()
        requestMessage.put("command", "firmware")
        requestMessage.put("action_type", "update")
        requestMessage.put("action_value", version)
        requestMessage.put("firmware_url", device!!.latestFirmwareURL)

        publishMQTTMessage(requestMessage, CallbackName.GOT_UPDATE_STATUS, originalRequestToken ?: UUID.randomUUID().toString())

        Handler(Looper.getMainLooper()).postDelayed({
            if (otaStatus.value == OTAStatus.REQUESTED) {
                requestFirmwareUpdateWithRetry(version, retryCount-1, retryDelay, originalRequestToken);
            }
        }, retryDelay ?: 3000)
    }

    // --> GOT_UPDATE_STATUS Callback for requestFirmwareUpdateWithRetry
    private fun requestFirmwareUpdateCallback(response: JSONObject) {
        Log.d(this.javaClass.simpleName, "Step 2. > Request Firmware Update Callback")
        if (otaStatus.value == OTAStatus.REQUESTED) {
            when(response.get("action_type")) {
                "downloading" -> {
                    otaStatus.value = OTAStatus.DOWNLOADING
                    Log.d(this.javaClass.simpleName, "DOWNLOADING")
                    waitForUpdateToComplete()
                }
            }
        }
    }

    // --> Device has now started the update, ask the device for
    // the current version to check against latest version
    private fun waitForUpdateToComplete() {
        Log.d(this.javaClass.simpleName, "Step 3. > WaitForUpdateComplete")
        waitForUpdateToCompleteWithRetry(30, 3000, null)
    }

    // --> Version check with retry
    private fun waitForUpdateToCompleteWithRetry(retryCount: Int, retryDelay: Long?, originalRequestToken: String?) {
        Log.d(this.javaClass.simpleName, "Step 3. > WaitForUpdateCompleteWithRetry: $retryCount")
        if (retryCount == 0) {
            if (otaStatus.value == OTAStatus.DOWNLOADING) {
                otaStatus.value = OTAStatus.FAILED
                firmwareUpdateStatus.value = UPDATE_FAILED
                otaFailReason.value = OTAFailReason.FAILURE_AFTER_OTA_REQUEST
            }
            return
        }

        val requestMessage = JSONObject()
        requestMessage.put("command", "firmware")
        requestMessage.put("action_type", "version")

        Log.d(this.javaClass.simpleName, "Publish Send Message")
        publishMQTTMessage(requestMessage, CallbackName.CHECK_NEW_VERSION, originalRequestToken ?: UUID.randomUUID().toString())

        Handler(Looper.getMainLooper()).postDelayed({
            if (otaStatus.value == OTAStatus.DOWNLOADING) {
                waitForUpdateToCompleteWithRetry(retryCount-1, retryDelay, originalRequestToken);
            }
        }, retryDelay ?: 3000)
    }

    // --> Version check with retry
    private fun checkNewVersionCallback(response: JSONObject) {
        Log.d(this.javaClass.simpleName, "Step 3. > checkNewVersionCallback")
        val version = response.get("version") as String
        Log.d(this.javaClass.simpleName, "Got version: $version")
        if (version == latestFirmwareVersion) {
            otaStatus.value = OTAStatus.COMPLETE
            firmwareUpdateStatus.value = UPDATE_COMPLETE
        }
    }

    private fun runMQTTCallback(callbackName: CallbackName, response: JSONObject) {
        when (callbackName) {
            CallbackName.GOT_VERSION -> {
                requestFirmwareVersionCallback(response)
            }
            CallbackName.GOT_UPDATE_STATUS -> {
                requestFirmwareUpdateCallback(response)
            }
            CallbackName.CHECK_NEW_VERSION -> {
                checkNewVersionCallback(response)
            }
            else -> {
                Log.e(this.javaClass.simpleName, "Can't identify callback")
            }
        }
    }

    private fun publishMQTTMessage(payload: JSONObject, callbackName: CallbackName, originalRequestToken: String) {
        if (!mqttCallbackRegistry.containsKey(originalRequestToken)) {
            mqttCallbackRegistry[originalRequestToken] = callbackName
        }

        val requestId = UUID.randomUUID().toString()
        payload.put("request_id", requestId)
        payload.put("original_request_token", originalRequestToken)
        val topic = String.format("%s/commands", device!!.thing_name)
        mqttClient!!.publish(topic, payload.toString(), null)
    }

    private fun setDeviceFirmwareVersion(deviceFirmwareVersion: String) {
        latestFirmwareVersion = device!!.latestFirmwareVersion;
        this.deviceFirmwareVersion = deviceFirmwareVersion
        if (isUpdateRequired(deviceFirmwareVersion, latestFirmwareVersion)) {
            firmwareUpdateStatus.value = START_UPDATE_PROMPT
        } else {
            firmwareUpdateStatus.value = NO_UPDATE_REQUIRED
        }
    }

    private fun isUpdateRequired(deviceVersionStr: String, cloudVersionStr: String): Boolean {
        val deviceVersion = FirmwareVersion(deviceVersionStr)
        val cloudVersion = FirmwareVersion(cloudVersionStr)

        if (cloudVersion.major > deviceVersion.major) {
            return true
        } else if (cloudVersion.major == deviceVersion.major) {
            if (cloudVersion.minor > deviceVersion.minor) {
                return true
            } else if (cloudVersion.minor == deviceVersion.minor) {
                if (cloudVersion.patch > deviceVersion.patch) {
                    return true
                }
            }
        }

        return false;
    }

    fun connectToMQTTServer() {
        mqttClient!!.connect(object : MQTTClient.Callback {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                subscribeToMQTTServer()
                Toast.makeText(
                    applicationContext,
                    "Connected to the MQTT Server!",
                    Toast.LENGTH_SHORT
                ).show()
            }

            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Toast.makeText(
                    applicationContext,
                    "Failed to connect to MQTT Server!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun subscribeToMQTTServer() {
        val topic = String.format("%s/responses", device!!.thing_name)

        mqttClient!!.subscribe(topic, object : MQTTClient.Callback {
            override fun onSuccess(asyncActionToken: IMqttToken) {
                if (firmwareUpdateStatus.value == CHECKING_DEVICE_VERSION) {
                    askDeviceFirmwareVersion(10, null, null)
                }
            }
            override fun onFailure(asyncActionToken: IMqttToken, exception: Throwable) {
                Log.d("FirmwareUpdateActivity", "MQTT Subscribe Failure")
            }
        }) { _, message ->
            val response = JSONObject(message.toString())
            if (response.has("original_request_token")) {
                val originalRequestToken = response.get("original_request_token") as String
                val callbackName = mqttCallbackRegistry[originalRequestToken]!!
                runMQTTCallback(callbackName, response)
            }
        }

    }
}

class FirmwareVersion(val version: String) {
    var major: Int = 0;
    var minor: Int = 0;
    var patch: Int = 0;
    init {
        var (majorStr, minorStr, patchStr) = version.split(".")
        major = majorStr.toInt()
        minor = minorStr.toInt()
        patch = patchStr.toInt()
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
fun NoUpdateRequiredView(deviceFirmwareVersion: String, latestFirmwareVersion: String) {
    PlaceHolderView("Your device is up to date \nDevice Firmware Version: $deviceFirmwareVersion\nLatest Firmware Version: $latestFirmwareVersion")
}

@Composable
fun DeviceUnreachableView() {
    PlaceHolderView("Device unreachable!\nMake sure it's connected and provisioned.")
}

@Composable
fun StartUpdatePromptView(firmwareUpdateStatus: MutableState<FirmwareUpdateActivity.FirmwareUpdateStatus>, deviceFirmwareVersion: String, latestFirmwareVersion: String, context: FirmwareUpdateActivity?) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PlaceHolderView("Shall we update the firmware?\nCurrent Firmware Version: $deviceFirmwareVersion\nNew Firmware Version: $latestFirmwareVersion")
        Row (
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp, alignment = Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Button(
                onClick = {
                    firmwareUpdateStatus.value = UPDATE_IN_PROGRESS
                    context?.updateFirmware(latestFirmwareVersion)
                }
            ) {
                Text(
                    text = "Update",
                )
            }
            Button(
                onClick = {
                    firmwareUpdateStatus.value = UPDATE_CANCELLED
                }
            ) {
                Text(text = "Cancel")
            }
        }
    }
}

@Composable
fun UpdateInProgressView(otaStatus: FirmwareUpdateActivity.OTAStatus) {
    if (otaStatus == FirmwareUpdateActivity.OTAStatus.REQUESTED) {
        return PlaceHolderView("Asking the device to get the new firmware...")
    } else if (otaStatus == FirmwareUpdateActivity.OTAStatus.DOWNLOADING) {
        return PlaceHolderView("Downloading the new firmware...")
    }
}

@Composable
fun UpdateCompleteView(firmwareUpdateStatus: MutableState<FirmwareUpdateActivity.FirmwareUpdateStatus>) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        PlaceHolderView("Update completed successfully")
        Button(
            onClick = {
                firmwareUpdateStatus.value = DISMISS
            }
        ) {
            Text(text = "Done")
        }
    }
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
    AerohAndroidTheme {
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
    AerohAndroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            NoUpdateRequiredView("0.1.2", "0.1.2")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DeviceUnreachableViewPreview() {
    AerohAndroidTheme {
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
    var firmwareUpdateStatus = remember {
        mutableStateOf(START_UPDATE_PROMPT)
    }
    AerohAndroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            StartUpdatePromptView(firmwareUpdateStatus, "0.1.1", "0.1.2", null)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateInProgressViewPreview() {
    AerohAndroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UpdateInProgressView(FirmwareUpdateActivity.OTAStatus.DOWNLOADING)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateCompleteViewPreview() {
    var firmwareUpdateStatus = remember {
        mutableStateOf(UPDATE_COMPLETE)
    }
    AerohAndroidTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UpdateCompleteView(firmwareUpdateStatus)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun UpdateFailedViewPreview() {
    AerohAndroidTheme(
        darkTheme = true
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            UpdateFailedView()
        }
    }
}