package com.sakana.he.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakana.he.ui.components.WaterCup
import com.sakana.he.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    themeColor: Color,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val context = LocalContext.current
    val amount by viewModel.selectedAmount.collectAsStateWithLifecycle()
    val dailyProgress by viewModel.dailyProgress.collectAsStateWithLifecycle()
    val todayTotal by viewModel.todayTotal.collectAsStateWithLifecycle()
    val dailyGoal by viewModel.dailyGoal.collectAsStateWithLifecycle()

    var previousAmount by remember { mutableIntStateOf(amount) }
    val vibrator = remember(context) { getVibrator(context) }

    LaunchedEffect(amount) {
        if (amount != previousAmount) {
            vibrateForAmount(vibrator, amount)
            previousAmount = amount
        }
    }

    val drinkInteractionSource = remember { MutableInteractionSource() }
    val isDrinkPressed by drinkInteractionSource.collectIsPressedAsState()
    val drinkScale by animateFloatAsState(
        targetValue = if (isDrinkPressed) 0.86f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "drink_scale"
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onNavigateToHistory) {
                    Icon(
                        Icons.Default.DateRange,
                        contentDescription = "历史记录",
                        tint = themeColor
                    )
                }
                IconButton(onClick = onNavigateToSettings) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "设置",
                        tint = themeColor
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            // Progress bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "今日 ${todayTotal}ml",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                    Text(
                        text = "目标 ${dailyGoal}ml",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
                    )
                }
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier.fillMaxWidth().height(26.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val fillFraction = dailyProgress.coerceIn(0f, 1f)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val r = size.height / 2f
                        drawRoundRect(
                            color = themeColor.copy(alpha = 0.12f),
                            cornerRadius = CornerRadius(r)
                        )
                        if (fillFraction > 0f) {
                            val fillPx = (size.width * fillFraction).coerceAtLeast(size.height)
                            drawRoundRect(
                                color = themeColor,
                                cornerRadius = CornerRadius(r),
                                size = Size(fillPx, size.height)
                            )
                        }
                    }
                    Text(
                        text = "${(dailyProgress * 100).toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (fillFraction > 0.35f) Color.White else themeColor
                    )
                }
            }

            // Water cup — horizontal padding restricts draggable area to cup bounds
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(vertical = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                WaterCup(
                    amount = amount,
                    themeColor = themeColor,
                    modifier = Modifier.fillMaxSize(),
                    onAmountChanged = { delta -> viewModel.adjustAmount(delta) }
                )
            }

            // Amount display
            Text(
                text = "${amount}ml",
                fontSize = 48.sp,
                fontWeight = FontWeight.SemiBold,
                color = themeColor,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = "上下滑动水杯调节用量",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )

            Spacer(Modifier.height(20.dp))

            // Record button with spring press animation
            Button(
                onClick = { viewModel.recordDrink() },
                interactionSource = drinkInteractionSource,
                modifier = Modifier
                    .fillMaxWidth(0.45f)
                    .height(52.dp)
                    .graphicsLayer { scaleX = drinkScale; scaleY = drinkScale },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = themeColor,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
            ) {
                Text(
                    text = "喝",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 4.sp
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }
}

private fun getVibrator(context: Context): Vibrator {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        (context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
}

private fun vibrateForAmount(vibrator: Vibrator, amount: Int) {
    if (!vibrator.hasVibrator()) return
    val intensity = ((amount - 10f) / (300f - 10f) * 180 + 40).toInt().coerceIn(1, 220)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator.vibrate(VibrationEffect.createOneShot(22, intensity))
    } else {
        @Suppress("DEPRECATION")
        vibrator.vibrate(22)
    }
}
