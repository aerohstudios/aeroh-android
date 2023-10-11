package io.aeroh.android.RecordAndReplayActivities

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aeroh.android.R
import io.aeroh.android.ui.theme.AerohAndroidTheme

class ConfigureDeviceActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AerohAndroidTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SignalRecordActivity()
                }
            }
        }
    }
}

@Composable
fun ButtonEditLogo() {
    Icon(
        painter = painterResource(id = R.drawable.ic_pencil), // Replace with your pencil icon
        contentDescription = null,
        modifier = Modifier
            .size(24.dp)
            .padding(5.dp)
            .background(color = Color(0xFF2A2A2A))
            .clip(CircleShape)
            .offset(x = -200.dp, y = -250.dp),
        tint = Color.White // Adjust the tint color as needed
    )
}

@Composable
fun CustomButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    buttonText: String,
    btnIcon: Int
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .padding(20.dp, 8.dp)
            .fillMaxWidth()
            .height(56.dp)
            .background(color = Color(0xFFFFC83A), shape = RoundedCornerShape(size = 5.dp))
    ) {
        Box {

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    painter = painterResource(id = btnIcon), // Change to the desired icon
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = buttonText)
            }

        }

    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeviceConfigureActivity() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
            .background(Color(0xFF100E14)),

        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF100E14),
                    titleContentColor = Color.White,
                ),
                title = {
                    Text(
                        "Configure",
                        fontSize = 16.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { /* do something */ }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Localized description",
                            tint = Color.White
                        )
                    }
                },
                scrollBehavior = scrollBehavior,
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { },
                shape = CircleShape,
                containerColor = Color(0xFFFFC83A)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black)
            }
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            CustomButton(
                onClick = { /* Handle button click */ },
                buttonText = "Power",
                btnIcon = R.drawable.ic_power
            )
            CustomButton(
                onClick = { /* Handle button click */ },
                buttonText = "Speed",
                btnIcon = R.drawable.ic_speed
            )
            CustomButton(
                onClick = { /* Handle button click */ },
                buttonText = "Temp up",
                btnIcon = R.drawable.ic_switch_up
            )
            CustomButton(
                onClick = { /* Handle button click */ },
                buttonText = "Temp down",
                btnIcon = R.drawable.ic_stepper_down
            )
        }

    }
}

@Preview(showBackground = true)
@Composable
fun ConfigureScreenPreview() {
    DeviceConfigureActivity()
}
