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
import androidx.compose.material3.Button
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
import io.aeroh.android.R
import io.aeroh.android.ui.theme.AerohAndroidTheme

@Composable
fun NoUpdateRequiredView(deviceFirmwareVersion: String, latestFirmwareVersion: String) {
    Column(
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(color = Color(0xFF100E14))
            .padding(20.dp)
    ) {

        Text(
            text = "Your device is running the\nlatest firmware version",
            style = TextStyle(
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color(0xFFFFFFFF),
                textAlign = TextAlign.Center,
            )
        )

        Spacer(modifier = Modifier.height(40.dp))

        Box(
            modifier = Modifier
                .size(245.dp)
                .clip(CircleShape)
                .background(Color.Transparent)
                .border(1.dp, color = Color(0xFF2A2A2A), CircleShape)
                .padding(4.dp),
            Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.aeroh_link_device),
                contentDescription = null,
                modifier = Modifier
                    .width(127.373.dp)
                    .height(77.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Aeroh Link",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFFFFFFFF)
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "v$deviceFirmwareVersion", style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFFBEBEBF)
            )
        )
        Spacer(modifier = Modifier.height(240.dp))
        Button(
            onClick = { /*TODO*/ }, modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Okay")
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