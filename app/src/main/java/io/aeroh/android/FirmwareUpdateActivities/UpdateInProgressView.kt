package io.aeroh.android.FirmwareUpdateActivities

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aeroh.android.FirmwareUpdateActivity
import io.aeroh.android.R
import io.aeroh.android.ui.theme.AerohAndroidTheme

@Composable
fun UpdateInProgressView(otaStatus: FirmwareUpdateActivity.OTAStatus) {
    if (false) {
        return Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(color = Color(0xFF100E14))
                .padding(20.dp)
        ) {

            Box(
                modifier = Modifier
                    .size(245.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .border(1.dp, color = Color(0xFF2A2A2A), CircleShape)
                    .padding(4.dp),
                Alignment.Center
            ) {
                Text(
                    text = "Requested",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight(400),
                        color = Color(0xFFBEBEBF),
                    )
                )
            }
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Update has been requested",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFFFFFFFF)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Please do not close the app or turn off the internet or disconnect the device",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFBEBEBF),
                    textAlign = TextAlign.Center
                )
            )

            Spacer(modifier = Modifier.height(80.dp))
            Image(
                painter = painterResource(id = R.drawable.download_illustration),
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            )
            Spacer(modifier = Modifier.height(80.dp))
            LinearProgressIndicator(
                progress = 50 / 100f,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }
    } else if (true) {
        return Column(
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .background(color = Color(0xFF100E14))
                .padding(20.dp)
        ) {

            Spacer(modifier = Modifier.height(40.dp))

            Box(
                modifier = Modifier
                    .size(245.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .padding(4.dp),
                Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary, // Set the color
                    strokeWidth = 4.dp, // Set the stroke width
                    modifier = Modifier
                        .size(245.dp)
                )
                Text(
                    text = "Installing",
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight(400),
                        color = Color(0xFFBFBFBF),
                    )
                )
            }

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "New firmware is being installed",
                style = TextStyle(
                    fontSize = 20.sp,
                    fontWeight = FontWeight(600),
                    color = Color(0xFFFFFFFF)
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Please do not close the app or turn off the internet or disconnect the device",
                style = TextStyle(
                    fontSize = 16.sp,
                    fontWeight = FontWeight(400),
                    color = Color(0xFFBEBEBF),
                    textAlign = TextAlign.Center
                )
            )
            Spacer(modifier = Modifier.height(80.dp))
            Image(
                painter = painterResource(id = R.drawable.installing_illustration),
                contentDescription = null,
                modifier = Modifier
                    .width(100.dp)
                    .height(100.dp)
            )
            Spacer(modifier = Modifier.height(80.dp))
            LinearProgressIndicator(
                progress = 100 / 100f,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
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