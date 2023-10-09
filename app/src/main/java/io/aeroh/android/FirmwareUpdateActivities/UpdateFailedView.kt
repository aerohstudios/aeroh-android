import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
fun UpdateFailedView() {
    Column(
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
                .border(1.dp, color = Color(0xFF2A2A2A), CircleShape)
                .padding(4.dp),
            Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.warning),
                contentDescription = null,
                modifier = Modifier
                    .width(120.dp)
                    .height(120.dp)
            )
        }

        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "There was an error installing the update",
            style = TextStyle(
                fontSize = 20.sp,
                fontWeight = FontWeight(600),
                color = Color(0xFFFFFFFF),
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Please select the below option to continue", style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight(400),
                color = Color(0xFFBEBEBF),
                textAlign = TextAlign.Center
            )
        )
        Spacer(modifier = Modifier.height(200.dp))
        Button(
            onClick = { /* Retry button click logic */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text(text = "Retry")
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = { /* Abort button click logic */ },
            modifier = Modifier
                .clip(RoundedCornerShape(percent = 50))
                .border(1.dp, Color.White, RoundedCornerShape(percent = 50))
                .height(50.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(8.dp)
        ) {
            Text(text = "Abort", color = Color.White)
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
