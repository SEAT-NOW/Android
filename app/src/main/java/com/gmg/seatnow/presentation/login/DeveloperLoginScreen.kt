package com.gmg.seatnow.presentation.login

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.gmg.seatnow.presentation.component.SeatNowTextField
import com.gmg.seatnow.presentation.component.SeatNowTopAppBar
import com.gmg.seatnow.presentation.theme.PointRed
import com.gmg.seatnow.presentation.theme.White

@Composable
fun DeveloperLoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToBack: () -> Unit,
    onNavigateToUserMain: () -> Unit
) {
    var code by remember { mutableStateOf("") }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            SeatNowTopAppBar(
                title = "개발자 로그인",
                onBackClick = onNavigateToBack
            )
        },
        containerColor = White
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "테스트 코드 입력",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(16.dp))

            SeatNowTextField(
                value = code,
                onValueChange = { code = it },
                placeholder = "코드를 입력하세요",
                imeAction = ImeAction.Done
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    // ★ 비밀 코드 검증 로직
                    if (code == "seatnow!!testID") {
                        viewModel.onDeveloperLoginSuccess() // ViewModel에 상태 저장 요청
                        Toast.makeText(context, "개발자 모드로 진입합니다.", Toast.LENGTH_SHORT).show()
                        onNavigateToUserMain()
                    } else {
                        Toast.makeText(context, "코드가 올바르지 않습니다.", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PointRed)
            ) {
                Text(text = "로그인", color = White, fontWeight = FontWeight.Bold)
            }
        }
    }
}