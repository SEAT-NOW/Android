package com.gmg.seatnow.presentation.owner.store.storeManage.storeManageEdit

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.gmg.seatnow.R
import com.gmg.seatnow.domain.model.StoreMenuCategory
import com.gmg.seatnow.presentation.extension.bottomShadow
import com.gmg.seatnow.presentation.theme.*
import java.text.DecimalFormat

@Composable
fun MenuEditScreen(
    initialCategoryId: Long = -1,
    // ★ [추가] 초기값 파라미터 (수정 모드용)
    initialName: String = "",
    initialPrice: String = "",
    initialImageUri: String? = null,

    categoryList: List<StoreMenuCategory> = emptyList(),
    onBackClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onSaveClick: (String, String, Long, Uri?) -> Unit
) {
    // 가격 포맷터 (예: 10,000)
    val decimalFormat = remember { DecimalFormat("#,###") }

    // --- State 관리 ---
    var name by remember { mutableStateOf(initialName) }

    // 가격 초기값 포맷팅 (숫자만 있다면 쉼표 추가)
    var price by remember {
        mutableStateOf(
            if (initialPrice.isNotEmpty()) {
                try {
                    decimalFormat.format(initialPrice.replace(",", "").toLong())
                } catch (e: Exception) { initialPrice }
            } else ""
        )
    }

    var selectedCategoryId by remember { mutableStateOf(initialCategoryId) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val selectedCategoryName = categoryList.find { it.id == selectedCategoryId }?.name
        ?: categoryList.firstOrNull()?.name
        ?: ""

    // 사진 상태
    var photoUri by remember {
        mutableStateOf(if (initialImageUri != null) Uri.parse(initialImageUri) else null)
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) photoUri = uri
    }

    Scaffold(
        containerColor = White,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            MenuEditTopBar(onBackClick = onBackClick)
        },
        bottomBar = {
            MenuEditBottomBar(
                onDeleteClick = onDeleteClick,
                onSaveClick = {
                    if (name.isNotBlank() && price.isNotBlank()) {
                        val cleanPrice = price.replace(",", "")
                        onSaveClick(name, cleanPrice, selectedCategoryId, photoUri)
                    }
                },
                isSaveEnabled = name.isNotBlank() && price.isNotBlank()
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(60.dp))

            // 1. 메뉴명
            InputLabel(text = "메뉴명")
            Spacer(modifier = Modifier.height(8.dp))
            MenuEditTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = "메뉴명을 입력해주세요"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 2. 가격
            InputLabel(text = "가격")
            Spacer(modifier = Modifier.height(8.dp))
            MenuEditTextField(
                value = price,
                onValueChange = { input ->
                    val cleanString = input.filter { it.isDigit() }
                    if (cleanString.isNotEmpty()) {
                        val longVal = cleanString.toLongOrNull()
                        if (longVal != null) {
                            price = decimalFormat.format(longVal)
                        }
                    } else {
                        price = ""
                    }
                },
                placeholder = "가격을 입력해주세요",
                keyboardType = KeyboardType.Number,
                suffixText = "원"
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 3. 메뉴 카테고리
            InputLabel(text = "메뉴 카테고리")
            Spacer(modifier = Modifier.height(8.dp))

            Box {
                MenuEditTextField(
                    value = selectedCategoryName,
                    onValueChange = {},
                    placeholder = "카테고리를 선택해주세요",
                    readOnly = true,
                    isDropdown = true
                )

                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { isDropdownExpanded = true }
                )

                DropdownMenu(
                    expanded = isDropdownExpanded,
                    onDismissRequest = { isDropdownExpanded = false },
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .background(White)
                ) {
                    categoryList.forEach { category ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = category.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SubBlack
                                )
                            },
                            onClick = {
                                selectedCategoryId = category.id
                                isDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // 4. 메뉴 사진
            InputLabel(text = "메뉴 사진")
            Spacer(modifier = Modifier.height(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .border(1.dp, SubLightGray, RectangleShape)
                        .clickable {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "사진 추가",
                        tint = SubGray,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                if (photoUri != null) {
                    Box(
                        modifier = Modifier.size(100.dp)
                    ) {
                        Image(
                            painter = rememberAsyncImagePainter(model = photoUri),
                            contentDescription = "선택된 메뉴 사진",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .border(1.dp, SubLightGray, RectangleShape)
                        )

                        Box(
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .offset(x = (-6).dp, y = (-6).dp)
                                .size(20.dp)
                                .background(SubLightGray, CircleShape)
                                .clickable { photoUri = null },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "삭제",
                                tint = White,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(100.dp)
                            .background(SubLightGray, RectangleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_row_logo),
                            contentDescription = "기본 이미지",
                            tint = White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

// -----------------------------------------------------------------------------
// 하위 컴포넌트들
// -----------------------------------------------------------------------------

@Composable
fun MenuEditTopBar(onBackClick: () -> Unit) {
    Surface(color = White, shadowElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(56.dp)
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "닫기",
                tint = SubBlack,
                modifier = Modifier
                    .size(24.dp)
                    .clickable(onClick = onBackClick)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "메뉴 상세 편집",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = SubBlack
            )
        }
    }
}

@Composable
fun InputLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
        color = SubBlack
    )
}

@Composable
fun MenuEditTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 52.dp,
    readOnly: Boolean = false,
    isDropdown: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    suffixText: String? = null
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(modifier = modifier.fillMaxWidth()) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            interactionSource = interactionSource,
            readOnly = readOnly,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            textStyle = MaterialTheme.typography.bodyMedium.copy(color = SubBlack),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .bottomShadow(offsetY = 2.dp, shadowBlurRadius = 4.dp, alpha = 0.15f, cornersRadius = 12.dp)
                .background(color = White, shape = RoundedCornerShape(12.dp))
                .border(width = 1.dp, color = SubLightGray, shape = RoundedCornerShape(12.dp)),
            decorationBox = { innerTextField ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        if (value.isEmpty()) {
                            Text(text = placeholder, color = SubLightGray, style = MaterialTheme.typography.bodyMedium)
                        }
                        innerTextField()
                    }

                    if (suffixText != null) {
                        Text(text = suffixText, color = SubLightGray, style = MaterialTheme.typography.bodyMedium)
                    }

                    if (isDropdown) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = SubGray
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun MenuEditBottomBar(
    onDeleteClick: () -> Unit,
    onSaveClick: () -> Unit,
    isSaveEnabled: Boolean
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = White,
        shadowElevation = 16.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onDeleteClick,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = SubLightGray
                ),
                border = BorderStroke(1.dp, SubLightGray),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "삭제",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }

            Button(
                onClick = onSaveClick,
                enabled = isSaveEnabled,
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PointRed,
                    disabledContainerColor = SubLightGray,
                    contentColor = White,
                    disabledContentColor = White
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "저장",
                    style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}

@Preview(showBackground = true, heightDp = 1000)
@Composable
fun PreviewMenuEditScreen() {
    SeatNowTheme {
        MenuEditScreen(
            initialCategoryId = 1,
            categoryList = listOf(
                StoreMenuCategory(1, "메인메뉴"),
                StoreMenuCategory(2, "사이드메뉴"),
                StoreMenuCategory(3, "주류")
            ),
            onBackClick = {},
            onDeleteClick = {},
            onSaveClick = { _, _, _, _ -> }
        )
    }
}