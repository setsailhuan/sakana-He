package com.sakana.he.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakana.he.data.WaterRecord
import com.sakana.he.viewmodel.AppViewModel
import com.sakana.he.viewmodel.HistoryViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    appViewModel: AppViewModel,
    themeColor: Color,
    onBack: () -> Unit
) {
    val displayMonth by viewModel.displayMonth.collectAsStateWithLifecycle()
    val selectedDay by viewModel.selectedDay.collectAsStateWithLifecycle()
    val selectedDayRecords by viewModel.selectedDayRecords.collectAsStateWithLifecycle()
    val allRecords by viewModel.allRecords.collectAsStateWithLifecycle()
    val totalDays by viewModel.totalDays.collectAsStateWithLifecycle()
    val totalVolume by viewModel.totalVolume.collectAsStateWithLifecycle()
    val dailyGoal by appViewModel.dailyGoal.collectAsStateWithLifecycle()
    // Recompute whenever records, month, or goal changes
    val dayProgressMap = remember(allRecords, displayMonth, dailyGoal) {
        viewModel.getDayTotalForMonth(allRecords, dailyGoal)
    }

    var editingRecord by remember { mutableStateOf<WaterRecord?>(null) }
    var addingRecord by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("历史记录") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Card 1: Summary
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatItem(label = "累计天数", value = "${totalDays}天", themeColor = themeColor)
                        StatItem(label = "总饮水量", value = "${totalVolume}ml", themeColor = themeColor)
                    }
                }
            }

            // Card 2: Calendar
            item {
                CalendarCard(
                    year = displayMonth.first,
                    month = displayMonth.second,
                    selectedDay = selectedDay,
                    dayProgressMap = dayProgressMap,
                    themeColor = themeColor,
                    onPreviousMonth = { viewModel.previousMonth() },
                    onNextMonth = { viewModel.nextMonth() },
                    onDaySelected = { y, m, d -> viewModel.selectDay(y, m, d) }
                )
            }

            // Card 3: Daily records
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp)) {
                        val fmt = SimpleDateFormat("MM月dd日", Locale.CHINESE)
                        val cal = Calendar.getInstance().apply {
                            set(selectedDay.year, selectedDay.month, selectedDay.day)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = fmt.format(cal.time) + " 饮水记录",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            IconButton(onClick = { addingRecord = true }) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "新增记录",
                                    tint = themeColor
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        if (selectedDayRecords.isEmpty()) {
                            Text(
                                "暂无记录，点击 + 添加",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier.padding(vertical = 12.dp)
                            )
                        } else {
                            selectedDayRecords.forEach { record ->
                                RecordItem(
                                    record = record,
                                    themeColor = themeColor,
                                    onEdit = { editingRecord = record },
                                    onDelete = { viewModel.deleteRecord(record.id) }
                                )
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    // Edit dialog
    editingRecord?.let { record ->
        EditRecordDialog(
            record = record,
            themeColor = themeColor,
            onDismiss = { editingRecord = null },
            onSave = { amount, timestamp ->
                viewModel.updateRecord(record.id, amount, timestamp)
                editingRecord = null
            }
        )
    }

    // Add record dialog
    if (addingRecord) {
        AddRecordDialog(
            themeColor = themeColor,
            onDismiss = { addingRecord = false },
            onSave = { amount, hour, minute ->
                viewModel.addRecord(amount, hour, minute)
                addingRecord = false
            }
        )
    }
}

@Composable
private fun StatItem(label: String, value: String, themeColor: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = themeColor)
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
    }
}

@Composable
private fun CalendarCard(
    year: Int, month: Int,
    selectedDay: com.sakana.he.viewmodel.SelectedDay,
    dayProgressMap: Map<Int, Float>,
    themeColor: Color,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDaySelected: (Int, Int, Int) -> Unit
) {
    val monthNames = listOf("1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月")
    val dayLabels = listOf("日", "一", "二", "三", "四", "五", "六")

    val cal = Calendar.getInstance().apply { set(year, month, 1) }
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)

    var dragAcc by remember { mutableStateOf(0f) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragStart = { dragAcc = 0f },
                    onDragEnd = { dragAcc = 0f },
                    onHorizontalDrag = { _, dragAmount ->
                        dragAcc += dragAmount
                        if (dragAcc > 80f) { onPreviousMonth(); dragAcc = 0f }
                        else if (dragAcc < -80f) { onNextMonth(); dragAcc = 0f }
                    }
                )
            },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Month nav
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "上月", tint = themeColor)
                }
                Text(
                    text = "${year}年 ${monthNames[month]}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "下月", tint = themeColor)
                }
            }

            Spacer(Modifier.height(8.dp))

            // Day labels
            Row(modifier = Modifier.fillMaxWidth()) {
                dayLabels.forEach { label ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // Calendar grid
            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val dayNum = cellIndex - firstDayOfWeek + 1
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (dayNum in 1..daysInMonth) {
                                val progress = dayProgressMap[dayNum] ?: 0f
                                val isSelected = selectedDay.year == year &&
                                        selectedDay.month == month &&
                                        selectedDay.day == dayNum
                                DayCell(
                                    day = dayNum,
                                    progress = progress,
                                    isSelected = isSelected,
                                    themeColor = themeColor,
                                    onClick = { onDaySelected(year, month, dayNum) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(
    day: Int, progress: Float, isSelected: Boolean, themeColor: Color, onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable { onClick() }
            .then(if (isSelected) Modifier.background(themeColor.copy(alpha = 0.1f)) else Modifier),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeW = 2.5.dp.toPx()
            val inset = strokeW / 2
            if (progress > 0f) {
                // Track
                drawArc(
                    color = themeColor.copy(alpha = 0.15f),
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - strokeW, size.height - strokeW),
                    style = Stroke(width = strokeW)
                )
                // Progress
                drawArc(
                    color = if (progress >= 1f) themeColor else themeColor.copy(alpha = 0.75f),
                    startAngle = -90f,
                    sweepAngle = (progress * 360f).coerceIn(0f, 360f),
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = Size(size.width - strokeW, size.height - strokeW),
                    style = Stroke(width = strokeW, cap = StrokeCap.Round)
                )
            }
        }
        Text(
            text = day.toString(),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (progress >= 1f) FontWeight.Bold else FontWeight.Normal,
            color = when {
                progress >= 1f -> themeColor
                progress > 0f -> MaterialTheme.colorScheme.onSurface
                else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            }
        )
    }
}

@Composable
private fun RecordItem(
    record: WaterRecord, themeColor: Color, onEdit: () -> Unit, onDelete: () -> Unit
) {
    val timeFmt = SimpleDateFormat("HH:mm", Locale.getDefault())
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(themeColor)
            )
            Text(
                text = "  ${timeFmt.format(record.timestamp)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "${record.amount}ml",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = themeColor
            )
            IconButton(onClick = onEdit, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Edit, contentDescription = "编辑", modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.Delete, contentDescription = "删除", modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun EditRecordDialog(
    record: WaterRecord,
    themeColor: Color,
    onDismiss: () -> Unit,
    onSave: (Int, Long) -> Unit
) {
    val cal = Calendar.getInstance().apply { timeInMillis = record.timestamp }
    var amountText by remember { mutableStateOf(record.amount.toString()) }
    var hourText by remember { mutableStateOf(cal.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')) }
    var minuteText by remember { mutableStateOf(cal.get(Calendar.MINUTE).toString().padStart(2, '0')) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑记录") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("饮水量 (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { hourText = it },
                        label = { Text("时") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { minuteText = it },
                        label = { Text("分") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            DialogButton(
                text = "保存",
                onClick = {
                    val amount = amountText.toIntOrNull()?.coerceIn(1, 9999) ?: record.amount
                    val hour = hourText.toIntOrNull()?.coerceIn(0, 23) ?: cal.get(Calendar.HOUR_OF_DAY)
                    val minute = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: cal.get(Calendar.MINUTE)
                    val newTs = Calendar.getInstance().apply {
                        timeInMillis = record.timestamp
                        set(Calendar.HOUR_OF_DAY, hour)
                        set(Calendar.MINUTE, minute)
                    }.timeInMillis
                    onSave(amount, newTs)
                },
                containerColor = themeColor,
                contentColor = Color.White
            )
        },
        dismissButton = {
            DialogButton(
                text = "取消",
                onClick = onDismiss,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun AddRecordDialog(
    themeColor: Color,
    onDismiss: () -> Unit,
    onSave: (amount: Int, hour: Int, minute: Int) -> Unit
) {
    val now = Calendar.getInstance()
    var amountText by remember { mutableStateOf("150") }
    var hourText by remember { mutableStateOf(now.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')) }
    var minuteText by remember { mutableStateOf(now.get(Calendar.MINUTE).toString().padStart(2, '0')) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("新增记录") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("饮水量 (ml)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { hourText = it },
                        label = { Text("时") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { minuteText = it },
                        label = { Text("分") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            DialogButton(
                text = "保存",
                onClick = {
                    val amount = amountText.toIntOrNull()?.coerceIn(1, 9999) ?: 150
                    val hour = hourText.toIntOrNull()?.coerceIn(0, 23) ?: now.get(Calendar.HOUR_OF_DAY)
                    val minute = minuteText.toIntOrNull()?.coerceIn(0, 59) ?: now.get(Calendar.MINUTE)
                    onSave(amount, hour, minute)
                },
                containerColor = themeColor,
                contentColor = Color.White
            )
        },
        dismissButton = {
            DialogButton(
                text = "取消",
                onClick = onDismiss,
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    )
}

@Composable
private fun DialogButton(
    text: String,
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color
) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (pressed) 0.91f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "dialog_btn_scale"
    )
    Button(
        onClick = onClick,
        interactionSource = source,
        modifier = Modifier.graphicsLayer { scaleX = scale; scaleY = scale },
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor
        ),
        shape = RoundedCornerShape(10.dp)
    ) {
        Text(text, fontWeight = FontWeight.Medium)
    }
}
