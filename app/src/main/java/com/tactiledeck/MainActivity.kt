package com.tactiledeck

import android.content.Context
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin

private val Void = Color(0xFF050608)
private val Slate = Color(0xFF0A0C10)
private val Ice = Color(0xFF007AFF)
private val Frost = Color(0xFFFFFFFF)
private val Muted = Color(0xFF8994A3)
private val Amber = Color(0xFFFFB84A)
private val Emerald = Color(0xFF48D9A0)
private val GlassShape = RoundedCornerShape(22.dp)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = android.graphics.Color.rgb(5, 6, 8)
        window.navigationBarColor = android.graphics.Color.rgb(5, 6, 8)
        setContent { Deck() }
    }
}

private data class ControlKey(val title: String, val detail: String, val tint: Color)

@Composable
private fun Deck() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    var power by remember { mutableStateOf(true) }
    var armed by remember { mutableStateOf(false) }
    var count by remember { mutableIntStateOf(0) }
    var dial by remember { mutableFloatStateOf(.62f) }
    val activeKeys = remember { mutableStateListOf(false, false, false, false) }
    val pads = remember { mutableStateListOf(*Array(16) { false }) }
    val keys = listOf(
        ControlKey("IGNITION", "GUARDED  /  01", Ice),
        ControlKey("AUX POWER", "LATCH  /  02", Emerald),
        ControlKey("SILENT MODE", "TOGGLE  /  03", Ice),
        ControlKey("BEACON", "PULSE  /  04", Amber)
    )
    val touch: () -> Unit = { count++; haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove); buzz(context) }

    Column(
        Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color(0xFF111722), Void, Color(0xFF080A0E)))).padding(horizontal = 20.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(top = 17.dp, bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("TACTILE  /  DECK", color = Frost, fontSize = 19.sp, letterSpacing = 2.6.sp, fontWeight = FontWeight.SemiBold)
                Text("FIELD CONTROL SYSTEM     •     MK.02", color = Muted, fontSize = 8.sp, letterSpacing = 1.6.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 5.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                Box(Modifier.size(7.dp).background(if (power) Emerald else Muted, CircleShape))
                Text(if (power) "ONLINE" else "STANDBY", color = if (power) Emerald else Muted, fontSize = 9.sp, letterSpacing = 1.2.sp, fontFamily = FontFamily.Monospace)
            }
        }

        GlassPanel(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    MicroLabel("OUTPUT  /  01")
                    Text(if (power) "NOMINAL" else "STANDBY", color = if (power) Emerald else Amber, fontSize = 13.sp, letterSpacing = 1.1.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 5.dp))
                }
                Column(horizontalAlignment = Alignment.End) {
                    MicroLabel("EVENT COUNT")
                    Text(count.toString().padStart(4, '0'), color = Frost, fontSize = 23.sp, letterSpacing = 3.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 2.dp))
                }
                PremiumControl(power, Ice, Modifier.size(width = 48.dp, height = 30.dp)) { power = !power; touch() }
            }
        }

        SectionLabel("TACTICAL CONTROLS", "01 — 04")
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f, fill = false), contentPadding = PaddingValues(bottom = 2.dp), horizontalArrangement = Arrangement.spacedBy(11.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
            itemsIndexed(keys) { index, key ->
                GlassControl(key, activeKeys[index], Modifier.height(91.dp)) {
                    if (index == 0 && !armed) { armed = true; activeKeys[index] = true } else activeKeys[index] = !activeKeys[index]
                    touch()
                }
            }
        }

        SectionLabel("PRECISION  /  GAIN", "ANALOG")
        GlassPanel(Modifier.fillMaxWidth()) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    MicroLabel("INPUT SENSITIVITY")
                    Text("${(dial * 100).toInt()}%", color = Frost, fontSize = 20.sp, letterSpacing = 1.5.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 3.dp))
                    Text("FINE RESOLUTION  /  ±0.5 dB", color = Muted, fontSize = 7.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
                }
                RotaryDial(dial, Modifier.size(72.dp)) { dial = it; touch() }
            }
        }

        SectionLabel("MECHANICAL INTERLOCKS", "RING STATUS")
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            (0..1).forEach { index ->
                GlassPanel(Modifier.weight(1f)) {
                    Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        RingSwitch(activeKeys[index + 2], Modifier.size(41.dp)) { activeKeys[index + 2] = !activeKeys[index + 2]; touch() }
                        Text(if (index == 0) "LOCKOUT" else "COMMS", color = Frost, fontSize = 8.sp, letterSpacing = 1.3.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 5.dp))
                        MicroLabel(if (activeKeys[index + 2]) "ENGAGED" else "READY")
                    }
                }
            }
        }

        SectionLabel("PERFORMANCE MATRIX", "4 × 4  /  LIVE")
        LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.weight(1f, fill = false), contentPadding = PaddingValues(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            itemsIndexed(pads) { i, on ->
                TriggerPad(i + 1, on, Modifier.aspectRatio(1.12f)) { pads[i] = !pads[i]; touch() }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun GlassPanel(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(modifier.clip(GlassShape).background(Brush.linearGradient(listOf(Color(0xFF232C38).copy(.78f), Color(0xFF10151D).copy(.86f), Color(0xFF171D27).copy(.8f)))).border(1.dp, Brush.linearGradient(listOf(Frost.copy(.19f), Frost.copy(.055f), Ice.copy(.12f))), GlassShape)) { content() }
}

@Composable
private fun MicroLabel(text: String) {
    Text(text, color = Muted, fontSize = 7.sp, letterSpacing = 1.35.sp, fontFamily = FontFamily.Monospace)
}

@Composable
private fun SectionLabel(title: String, meta: String) {
    Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = Color(0xFFDCE5F0), fontSize = 9.sp, letterSpacing = 1.7.sp, fontWeight = FontWeight.Medium)
        Text(meta, color = Muted.copy(.78f), fontSize = 7.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable
private fun PremiumControl(active: Boolean, tint: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var down by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (down) .95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "press")
    Box(modifier.scale(scale).clip(CircleShape).background(if (active) tint.copy(.29f) else Color.White.copy(.06f)).border(1.dp, if (active) tint.copy(.62f) else Frost.copy(.18f), CircleShape).pointerInput(active) {
        detectTapGestures(onPress = { down = true; onClick(); tryAwaitRelease(); down = false })
    }, contentAlignment = Alignment.CenterStart) {
        Box(Modifier.padding(start = if (active) 23.dp else 3.dp).size(22.dp).background(Brush.linearGradient(listOf(Color.White, Color(0xFFBEC7D1))), CircleShape).border(1.dp, Frost.copy(.65f), CircleShape))
    }
}

@Composable
private fun GlassControl(key: ControlKey, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var down by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (down) .95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "controlPress")
    Column(modifier.scale(scale).clip(RoundedCornerShape(17.dp)).background(Brush.linearGradient(listOf(Color(0xFF27313E).copy(.9f), Color(0xFF11161E).copy(.97f)))).border(1.dp, Brush.linearGradient(listOf(Frost.copy(if (active) .25f else .15f), key.tint.copy(if (active) .28f else .06f), Frost.copy(.07f))), RoundedCornerShape(17.dp)).pointerInput(active) { detectTapGestures(onPress = { down = true; onClick(); tryAwaitRelease(); down = false }) }.padding(horizontal = 12.dp, vertical = 11.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(key.detail, color = Muted, fontSize = 7.sp, letterSpacing = .8.sp, fontFamily = FontFamily.Monospace)
            Box(Modifier.size(7.dp).background(if (active) key.tint else Color(0xFF3B4653), CircleShape).then(if (active) Modifier.border(2.dp, key.tint.copy(.25f), CircleShape) else Modifier))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(key.title, color = Frost, fontSize = 10.sp, letterSpacing = .75.sp, fontWeight = FontWeight.SemiBold)
            Text(if (active) "LIVE" else "READY", color = if (active) key.tint else Muted, fontSize = 7.sp, letterSpacing = .9.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable
private fun RotaryDial(value: Float, modifier: Modifier = Modifier, onChange: (Float) -> Unit) {
    var down by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (down) .95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "dialPress")
    Box(modifier.scale(scale).pointerInput(value) {
        detectTapGestures(onPress = { down = true; val next = (value + .08f).coerceAtMost(1f); onChange(if (next >= 1f) .12f else next); tryAwaitRelease(); down = false })
    }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension * .43f
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(Brush.linearGradient(listOf(Color(0xFF344253), Color(0xFF10151C))), radius, center)
            drawCircle(Frost.copy(.2f), radius, center, style = Stroke(1.dp.toPx()))
            for (i in 0 until 25) {
                val angle = Math.toRadians((135 + i * 11.25).toDouble())
                val inner = radius * if (i % 3 == 0) .72f else .8f
                val outer = radius * .91f
                val p1 = Offset(center.x + cos(angle).toFloat() * inner, center.y + sin(angle).toFloat() * inner)
                val p2 = Offset(center.x + cos(angle).toFloat() * outer, center.y + sin(angle).toFloat() * outer)
                drawLine(if (i / 24f <= value) Ice.copy(if (i / 24f <= value) .95f else .2f) else Frost.copy(.22f), p1, p2, strokeWidth = if (i % 3 == 0) 1.4.dp.toPx() else .7.dp.toPx(), cap = StrokeCap.Round)
            }
            val angle = Math.toRadians((135 + value * 270).toDouble())
            drawCircle(Ice.copy(.16f), radius * .57f, center)
            drawLine(Ice, center, Offset(center.x + cos(angle).toFloat() * radius * .58f, center.y + sin(angle).toFloat() * radius * .58f), 2.dp.toPx(), StrokeCap.Round)
            drawCircle(Frost, 2.dp.toPx(), center)
        }
    }
}

@Composable
private fun RingSwitch(active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var down by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (down) .95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "ringPress")
    Box(modifier.scale(scale).pointerInput(active) { detectTapGestures(onPress = { down = true; onClick(); tryAwaitRelease(); down = false }) }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(Color(0xFF171E27), size.minDimension * .48f)
            drawCircle(Frost.copy(.12f), size.minDimension * .42f, style = Stroke(1.dp.toPx()))
            drawArc(if (active) Ice else Color(0xFF394450), -90f, 300f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(if (active) Ice.copy(.22f) else Color(0xFF202833), size.minDimension * .25f)
            drawCircle(if (active) Ice else Color(0xFF728092), size.minDimension * .12f)
        }
    }
}

@Composable
private fun TriggerPad(number: Int, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    var down by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (down) .95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "padPress")
    Box(modifier.scale(scale).clip(RoundedCornerShape(13.dp)).background(Brush.linearGradient(if (active) listOf(Color(0xFF174577), Color(0xFF111B2B)) else listOf(Color(0xFF252D39), Color(0xFF10151D)))).border(1.dp, if (active) Ice.copy(.58f) else Frost.copy(.12f), RoundedCornerShape(13.dp)).pointerInput(active) { detectTapGestures(onPress = { down = true; onClick(); tryAwaitRelease(); down = false }) }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.size(4.dp).background(if (active) Ice else Color(0xFF596577), CircleShape))
            Text(number.toString().padStart(2, '0'), color = if (active) Color.White else Color(0xFFAAB5C4), fontSize = 10.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
        }
    }
}

@Suppress("DEPRECATION")
private fun buzz(context: Context) {
    try {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) context.getSystemService(VibratorManager::class.java).defaultVibrator else context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(14, 48)) else vibrator.vibrate(14)
    } catch (_: Exception) { }
}
