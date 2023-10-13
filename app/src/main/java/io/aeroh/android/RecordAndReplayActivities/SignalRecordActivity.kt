package io.aeroh.android.RecordAndReplayActivities

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.aeroh.android.R

@Composable
fun PulseLoading(
    durationMillis: Int = 1000,
    maxPulseSize: Float = 300f,
    minPulseSize: Float = 50f,
    pulseColor: Color = Color(234, 240, 246),
    centreColor: Color = Color(66, 133, 244)
) {
    val infiniteTransition = rememberInfiniteTransition()
    val size by infiniteTransition.animateFloat(
        initialValue = minPulseSize,
        targetValue = maxPulseSize,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    val alpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )
    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
        Card(
            shape = CircleShape,
            modifier = Modifier
                .size(size.dp)
                .align(Alignment.Center)
                .alpha(alpha),
//            colors = pulseColor,
//            elevation = 1.dp
        ) {}
        Card(
            modifier = Modifier
                .size(minPulseSize.dp)
                .align(Alignment.Center),
            shape = CircleShape,
//            cardcontainerColor = centreColor
        )
        {}
    }
}

@Composable
fun Demo_ExposedDropdownMenuBox() {

    val list = listOf("Toggle", "Stepper")
    val currentValue = remember { mutableStateOf(list[0]) }
    val expanded = remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .clickable {
                expanded.value = !expanded.value
            }
    ) {
        Text(
            text = currentValue.value,
            style = TextStyle(
                fontSize = 14.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFFBEBEBF),
            )
        )
        Icon(
            imageVector = Icons.Filled.ArrowDropDown,
            contentDescription = null,
            tint = Color.White
        )

        DropdownMenu(expanded = expanded.value, onDismissRequest = {
            expanded.value = false // Set expanded to false when the menu is dismissed
        }) {

            list.forEach {
                DropdownMenuItem(text = { Text(text = it) }, onClick = {
                    expanded.value = false
                    currentValue.value = it
                })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignalRecordActivity() {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
    var signalRecieved = false
    var buttonName by remember { androidx.compose.runtime.mutableStateOf("") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF100E14)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
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
                            "Record and Replay",
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight(400),
                                color = Color(0xFFBEBEBF),
                            )
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
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .padding(innerPadding)
                    .fillMaxSize()
                    .background(Color(0xFF100E14)),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (signalRecieved) {
                    Spacer(modifier = Modifier.height(80.dp))
                    Image(
                        painter = painterResource(id = R.drawable.aeroh_link_device),
                        contentDescription = null,
                        modifier = Modifier
                            .width(196.dp)
                            .height(118.48587.dp)
                    )
                    Spacer(modifier = Modifier.height(56.dp))

                    Text(
                        text = "Aeroh link recieved a signal",
                        style = TextStyle(
                            fontSize = 20.sp,
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFFFFF),
                            textAlign = TextAlign.Center,
                        )
                    )
                    Spacer(modifier = Modifier.height(56.dp))
                    Row(
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier

                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 20.dp)
                            .background(
                                color = Color(0xFF2A2A2A),
                                shape = RoundedCornerShape(size = 16.dp)
                            )

                    ) {
                        Text(
                            text = "Button type",
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontWeight = FontWeight(400),
                                color = Color(0xFFFFFFFF),
                            )
                        )
                        Spacer(modifier = Modifier.width(180.dp))
                        Demo_ExposedDropdownMenuBox()

                    }

                    Column(
                        horizontalAlignment = Alignment.Start, modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Spacer(modifier = Modifier.height(19.dp))

                        Text(
                            text = "Button Name",
                            modifier = Modifier.padding(bottom = 4.dp),
                            style = TextStyle(
                                fontSize = 14.sp,
                                fontWeight = FontWeight(400)
                            )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        TextField(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    Color(0xFF2A2A2A),
                                    shape = MaterialTheme.shapes.medium
                                ),
                            value = buttonName,
                            onValueChange = { buttonName = it },
                            singleLine = true,
                            colors = TextFieldDefaults.textFieldColors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                containerColor = Color.Transparent
                            )
                        )
                        Spacer(modifier = Modifier.height(200.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Button(
                                onClick = { /* Retry button click logic */ },
                                modifier = Modifier
                                    .height(50.dp)
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                Text(text = "Retry")
                            }

                            Spacer(modifier = Modifier.width(16.dp))

                            OutlinedButton(
                                onClick = { /* Abort button click logic */ },
                                modifier = Modifier
                                    .clip(RoundedCornerShape(percent = 50))
                                    .fillMaxWidth()
                                    .border(1.dp, Color.White, RoundedCornerShape(percent = 50))
                                    .height(50.dp)
                                    .weight(1f)
                            ) {
                                Text(text = "Abort", color = Color.White)
                            }
                        }

                    }
                } else {
                    Spacer(modifier = Modifier.height(80.dp))
                    Image(
                        painter = painterResource(id = R.drawable.aeroh_link_device),
                        contentDescription = null,
                        modifier = Modifier
                            .width(196.dp)
                            .height(118.48587.dp)
                    )
                    Spacer(modifier = Modifier.height(56.dp))
                    Text(
                        text = "When the light changes, give a signal from your device remote to aeroh link",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight(600),
                            color = Color(0xFFBEBEBF),
                            textAlign = TextAlign.Center,
                        ),
                        modifier = Modifier
                            .width(270.dp)
                    )
                    Spacer(modifier = Modifier.height(56.dp))
                    Text(
                        text = "Waiting for signal...",
                        style = TextStyle(
                            fontSize = 24.sp,
                            fontWeight = FontWeight(700),
                            color = Color(0xFFFFFFFF),
                        ), modifier = Modifier
                            .width(239.dp)
                    )
                    PulseLoading()
                }
            }

        }
    }
}

@Composable
@Preview
fun SignalRecordActivityPreview() {
    SignalRecordActivity()
}