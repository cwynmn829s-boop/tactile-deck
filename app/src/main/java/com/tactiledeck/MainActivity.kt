package com.tactiledeck

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.foundation.isSystemInDarkTheme
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
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin

private val Ice = Color(0xFF007AFF)
private val Emerald = Color(0xFF38C895)
private val Amber = Color(0xFFFFB84A)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { Deck() }
    }
}

private enum class Mechanism { SWITCH, KEY, DETENT, PAD, SOLENOID }
private data class ControlKey(val title: String, val detail: String, val tint: Color, val sound: Mechanism)

@Composable
private fun Deck() {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val dark = isSystemInDarkTheme()
    val ink = if (dark) Color(0xFF050608) else Color(0xFFF2F4F7)
    val text = if (dark) Color(0xFFF6F8FC) else Color(0xFF151A21)
    val muted = if (dark) Color(0xFF8994A3) else Color(0xFF687382)
    val panelTop = if (dark) Color(0xFF232C38).copy(.83f) else Color.White.copy(.91f)
    val panelBottom = if (dark) Color(0xFF10151D).copy(.92f) else Color(0xFFE1E6ED).copy(.86f)
    val edge = if (dark) Color.White else Color(0xFF455366)
    val shape = RoundedCornerShape(22.dp)
    var power by remember { mutableStateOf(true) }
    var armed by remember { mutableStateOf(false) }
    var count by remember { mutableIntStateOf(0) }
    var dial by remember { mutableFloatStateOf(.62f) }
    val active = remember { mutableStateListOf(false, false, false, false) }
    val pads = remember { mutableStateListOf(*Array(16) { false }) }
    val audio = remember(context) { TactileAudio(context) }
    DisposableEffect(audio) { onDispose { audio.release() } }
    val keys = listOf(
        ControlKey("IGNITION", "GUARDED / 01", Ice, Mechanism.SOLENOID),
        ControlKey("AUX POWER", "LATCH / 02", Emerald, Mechanism.KEY),
        ControlKey("SILENT MODE", "TOGGLE / 03", Ice, Mechanism.SWITCH),
        ControlKey("BEACON", "PULSE / 04", Amber, Mechanism.KEY)
    )
    val touch: (Mechanism) -> Unit = { mechanism ->
        count++
        haptic.performHapticFeedback(if (mechanism == Mechanism.PAD) HapticFeedbackType.LongPress else HapticFeedbackType.TextHandleMove)
        audio.play(mechanism)
    }

    MaterialTheme {
        Surface(color = ink) {
            Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(if (dark) Color(0xFF111722) else Color.White, ink, if (dark) Color(0xFF080A0E) else Color(0xFFE8ECF1)))).padding(horizontal = 20.dp)) {
                Row(Modifier.fillMaxWidth().padding(top = 17.dp, bottom = 16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("TACTILE / DECK", color = text, fontSize = 19.sp, letterSpacing = 2.6.sp, fontWeight = FontWeight.SemiBold)
                        Text("FIELD CONTROL SYSTEM  •  MK.02", color = muted, fontSize = 8.sp, letterSpacing = 1.6.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 5.dp))
                    }
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        Box(Modifier.size(7.dp).background(if (power) Emerald else muted, CircleShape))
                        Text(if (power) "ONLINE" else "STANDBY", color = if (power) Emerald else muted, fontSize = 9.sp, letterSpacing = 1.2.sp, fontFamily = FontFamily.Monospace)
                    }
                }
                Panel(Modifier.fillMaxWidth(), shape, panelTop, panelBottom, edge) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 15.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column { Micro("OUTPUT / 01", muted); Text(if (power) "NOMINAL" else "STANDBY", color = if (power) Emerald else Amber, fontSize = 13.sp, letterSpacing = 1.1.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 5.dp)) }
                        Column(horizontalAlignment = Alignment.End) { Micro("EVENT COUNT", muted); Text(count.toString().padStart(4, '0'), color = text, fontSize = 23.sp, letterSpacing = 3.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 2.dp)) }
                        Toggle(power, Ice, Modifier.size(width = 48.dp, height = 30.dp), text) { power = !power; touch(Mechanism.SWITCH) }
                    }
                }
                Section("TACTICAL CONTROLS", "01—04", text, muted)
                LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.weight(1f, fill = false), contentPadding = PaddingValues(bottom = 2.dp), horizontalArrangement = Arrangement.spacedBy(11.dp), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                    itemsIndexed(keys) { index, key ->
                        GlassControl(key, active[index], Modifier.height(91.dp), shape, panelTop, panelBottom, edge, text, muted) {
                            if (index == 0 && !armed) { armed = true; active[index] = true } else active[index] = !active[index]
                            touch(key.sound)
                        }
                    }
                }
                Section("PRECISION / GAIN", "ANALOG", text, muted)
                Panel(Modifier.fillMaxWidth(), shape, panelTop, panelBottom, edge) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) { Micro("INPUT SENSITIVITY", muted); Text("${(dial * 100).toInt()}%", color = text, fontSize = 20.sp, letterSpacing = 1.5.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(top = 3.dp)); Text("FINE RESOLUTION / ±0.5 dB", color = muted, fontSize = 7.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace) }
                        RotaryDial(dial, Modifier.size(72.dp), ink, text, dark, muted) { next -> dial = next; touch(Mechanism.DETENT) }
                    }
                }
                Section("MECHANICAL INTERLOCKS", "RING STATUS", text, muted)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    (0..1).forEach { index -> Panel(Modifier.weight(1f), shape, panelTop, panelBottom, edge) {
                        Column(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            RingSwitch(active[index + 2], Modifier.size(41.dp), ink, edge) { active[index + 2] = !active[index + 2]; touch(if (index == 0) Mechanism.SOLENOID else Mechanism.SWITCH) }
                            Text(if (index == 0) "LOCKOUT" else "COMMS", color = text, fontSize = 8.sp, letterSpacing = 1.3.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 5.dp))
                            Micro(if (active[index + 2]) "ENGAGED" else "READY", muted)
                        }
                    } }
                }
                Section("PERFORMANCE MATRIX", "4 × 4 / LIVE", text, muted)
                LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.weight(1f, fill = false), contentPadding = PaddingValues(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(pads) { i, on -> Pad(i + 1, on, Modifier.aspectRatio(1.12f), shape, text, dark) { pads[i] = !pads[i]; touch(Mechanism.PAD) } }
                }
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable private fun Panel(modifier: Modifier, shape: RoundedCornerShape, top: Color, bottom: Color, edge: Color, content: @Composable () -> Unit) {
    Box(modifier.clip(shape).background(Brush.linearGradient(listOf(top, bottom))).border(1.dp, Brush.linearGradient(listOf(edge.copy(.21f), edge.copy(.06f))), shape)) { content() }
}
@Composable private fun Micro(value: String, muted: Color) { Text(value, color = muted, fontSize = 7.sp, letterSpacing = 1.35.sp, fontFamily = FontFamily.Monospace) }
@Composable private fun Section(title: String, meta: String, text: Color, muted: Color) {
    Row(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, color = text, fontSize = 9.sp, letterSpacing = 1.7.sp, fontWeight = FontWeight.Medium)
        Text(meta, color = muted.copy(.88f), fontSize = 7.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace)
    }
}

@Composable private fun Toggle(on: Boolean, tint: Color, modifier: Modifier, thumb: Color, action: () -> Unit) {
    var down by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (down) .95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "togglePress")
    Box(modifier.scale(scale).clip(CircleShape).background(if (on) tint.copy(.29f) else Color.Gray.copy(.16f)).border(1.dp, if (on) tint.copy(.62f) else thumb.copy(.18f), CircleShape).pointerInput(on) { detectTapGestures(onPress = { down = true; action(); tryAwaitRelease(); down = false }) }, contentAlignment = Alignment.CenterStart) {
        Box(Modifier.padding(start = if (on) 23.dp else 3.dp).size(22.dp).background(Brush.linearGradient(listOf(Color.White, Color(0xFFBEC7D1))), CircleShape).border(1.dp, thumb.copy(.55f), CircleShape))
    }
}

@Composable private fun GlassControl(key: ControlKey, on: Boolean, modifier: Modifier, shape: RoundedCornerShape, top: Color, bottom: Color, edge: Color, text: Color, muted: Color, action: () -> Unit) {
    var down by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (down) .95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "controlPress")
    Column(modifier.scale(scale).clip(shape).background(Brush.linearGradient(listOf(top, bottom))).border(1.dp, Brush.linearGradient(listOf(edge.copy(if (on) .24f else .15f), key.tint.copy(if (on) .28f else .06f), edge.copy(.07f))), shape).pointerInput(on) { detectTapGestures(onPress = { down = true; action(); tryAwaitRelease(); down = false }) }.padding(horizontal = 12.dp, vertical = 11.dp), verticalArrangement = Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(key.detail, color = muted, fontSize = 7.sp, letterSpacing = .8.sp, fontFamily = FontFamily.Monospace)
            Box(Modifier.size(7.dp).background(if (on) key.tint else muted.copy(.48f), CircleShape))
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
            Text(key.title, color = text, fontSize = 10.sp, letterSpacing = .75.sp, fontWeight = FontWeight.SemiBold)
            Text(if (on) "LIVE" else "READY", color = if (on) key.tint else muted, fontSize = 7.sp, letterSpacing = .9.sp, fontFamily = FontFamily.Monospace)
        }
    }
}

@Composable private fun RotaryDial(value: Float, modifier: Modifier, ink: Color, text: Color, dark: Boolean, muted: Color, onChange: (Float) -> Unit) {
    var down by remember { mutableStateOf(false) }
    var lastStep by remember { mutableIntStateOf((value * 24).toInt()) }
    val scale by animateFloatAsState(if (down) .95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "dialPress")
    Box(modifier.scale(scale).pointerInput(value) {
        detectTapGestures(onPress = { down = true; val next = (value + .04f).let { if (it > 1f) .08f else it }; val step = (next * 24).toInt(); if (step != lastStep) { lastStep = step; onChange(next) }; tryAwaitRelease(); down = false })
    }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val radius = size.minDimension * .43f
            val center = Offset(size.width / 2, size.height / 2)
            drawCircle(Brush.linearGradient(listOf(if (dark) Color(0xFF344253) else Color(0xFFFCFDFE), if (dark) Color(0xFF10151C) else Color(0xFFD9DFE7))), radius, center)
            drawCircle((if (dark) Color.White else Color(0xFF3C4653)).copy(.2f), radius, center, style = Stroke(1.dp.toPx()))
            for (i in 0 until 25) {
                val angle = (135 + i * 11.25) * PI / 180.0
                val inner = radius * if (i % 3 == 0) .72f else .8f
                val outer = radius * .91f
                val p1 = Offset(center.x + cos(angle).toFloat() * inner, center.y + sin(angle).toFloat() * inner)
                val p2 = Offset(center.x + cos(angle).toFloat() * outer, center.y + sin(angle).toFloat() * outer)
                drawLine(if (i / 24f <= value) Ice else (if (dark) Color.White else Color(0xFF465366)).copy(.25f), p1, p2, strokeWidth = if (i % 3 == 0) 1.4.dp.toPx() else .7.dp.toPx(), cap = StrokeCap.Round)
            }
            val angle = (135 + value * 270) * PI / 180.0
            drawCircle(Ice.copy(.14f), radius * .57f, center)
            drawLine(Ice, center, Offset(center.x + cos(angle).toFloat() * radius * .58f, center.y + sin(angle).toFloat() * radius * .58f), 2.dp.toPx(), StrokeCap.Round)
            drawCircle(text, 2.dp.toPx(), center)
        }
    }
}

@Composable private fun RingSwitch(on: Boolean, modifier: Modifier, ink: Color, edge: Color, action: () -> Unit) {
    var down by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (down) .95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "ringPress")
    Box(modifier.scale(scale).pointerInput(on) { detectTapGestures(onPress = { down = true; action(); tryAwaitRelease(); down = false }) }, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            drawCircle(if (ink.red < .1f) Color(0xFF171E27) else Color.White, size.minDimension * .48f)
            drawCircle(edge.copy(.2f), size.minDimension * .42f, style = Stroke(1.dp.toPx()))
            drawArc(if (on) Ice else edge.copy(.52f), -90f, 300f, false, style = Stroke(3.dp.toPx(), cap = StrokeCap.Round))
            drawCircle(if (on) Ice.copy(.2f) else ink, size.minDimension * .25f)
            drawCircle(if (on) Ice else edge.copy(.72f), size.minDimension * .12f)
        }
    }
}

@Composable private fun Pad(number: Int, on: Boolean, modifier: Modifier, shape: RoundedCornerShape, text: Color, dark: Boolean, action: () -> Unit) {
    var down by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (down) .95f else 1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessHigh), label = "padPress")
    val base = if (dark) listOf(Color(0xFF252D39), Color(0xFF10151D)) else listOf(Color.White, Color(0xFFDDE3EB))
    Box(modifier.scale(scale).clip(shape).background(Brush.linearGradient(if (on) listOf(Color(0xFF174577), Color(0xFF111B2B)) else base)).border(1.dp, if (on) Ice.copy(.58f) else text.copy(.14f), shape).pointerInput(on) { detectTapGestures(onPress = { down = true; action(); tryAwaitRelease(); down = false }) }, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(3.dp)) {
            Box(Modifier.size(4.dp).background(if (on) Ice else text.copy(.4f), CircleShape))
            Text(number.toString().padStart(2, '0'), color = if (on) Color.White else text.copy(.78f), fontSize = 10.sp, letterSpacing = 1.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Medium)
        }
    }
}

private class TactileAudio(context: Context) {
    private val sampleRate = 44100
    private val pool = SoundPool.Builder().setMaxStreams(8).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    private val sounds = Mechanism.entries.associateWith { mechanism ->
        val bytes = synthesize(mechanism)
        pool.load(bytes.inputStream(), 1)
    }
    fun play(mechanism: Mechanism) {
        sounds[mechanism]?.let { id -> if (id != 0) pool.play(id, 0.72f, 0.72f, 1, 0, 1f) }
    }
    fun release() = pool.release()

    private fun synthesize(kind: Mechanism): ByteArray {
        val duration = when (kind) { Mechanism.PAD -> .105; Mechanism.SOLENOID -> .145; Mechanism.SWITCH -> .095; Mechanism.KEY -> .058; Mechanism.DETENT -> .032 }
        val n = (sampleRate * duration).toInt()
        val pcm = ByteArray(44 + n * 2)
        fun put16(at: Int, v: Int) { pcm[at] = v.toByte(); pcm[at + 1] = (v ushr 8).toByte() }
        fun put32(at: Int, v: Int) { put16(at, v and 0xffff); put16(at + 2, v ushr 16) }
        pcm[0] = 'R'.code.toByte(); pcm[1] = 'I'.code.toByte(); pcm[2] = 'F'.code.toByte(); pcm[3] = 'F'.code.toByte()
        put32(4, 36 + n * 2); "WAVEfmt ".forEachIndexed { i, c -> pcm[8 + i] = c.code.toByte() }
        put32(16, 16); put16(20, 1); put16(22, 1); put32(24, sampleRate); put32(28, sampleRate * 2); put16(32, 2); put16(34, 16)
        "data".forEachIndexed { i, c -> pcm[36 + i] = c.code.toByte() }; put32(40, n * 2)
        val freq = when (kind) { Mechanism.SWITCH -> 2200.0; Mechanism.KEY -> 3100.0; Mechanism.DETENT -> 4200.0; Mechanism.PAD -> 145.0; Mechanism.SOLENOID -> 78.0 }
        for (i in 0 until n) {
            val t = i.toDouble() / sampleRate
            val env = when (kind) {
                Mechanism.SWITCH -> exp(-t * 32) + .48 * exp(-((t - .022).coerceAtLeast(0.0)) * 45)
                Mechanism.KEY -> exp(-t * 68) + .27 * exp(-((t - .019).coerceAtLeast(0.0)) * 80)
                Mechanism.DETENT -> exp(-t * 110)
                Mechanism.PAD -> exp(-t * 22)
                Mechanism.SOLENOID -> exp(-t * 20) + .34 * exp(-((t - .045).coerceAtLeast(0.0)) * 28)
            }
            val fundamental = sin(2 * PI * freq * t)
            val harmonic = when (kind) {
                Mechanism.SOLENOID, Mechanism.PAD -> sin(2 * PI * freq * 2.17 * t) * .38
                else -> sin(2 * PI * freq * 2.73 * t) * .32
            }
            val noise = (Math.random() * 2.0 - 1.0) * when (kind) { Mechanism.DETENT -> .25; Mechanism.SWITCH -> .17; Mechanism.KEY -> .13; else -> .07 }
            val gain = when (kind) { Mechanism.SOLENOID -> .52; Mechanism.PAD -> .48; else -> .34 }
            val sample = ((fundamental + harmonic + noise) * env * gain).coerceIn(-.95, .95)
            val value = (sample * Short.MAX_VALUE).toInt(); pcm[44 + i * 2] = value.toByte(); pcm[45 + i * 2] = (value shr 8).toByte()
        }
        return pcm
    }
}

@Suppress("DEPRECATION")
private fun buzz(context: Context) {
    try {
        val vibrator = if (android.os.Build.VERSION.SDK_INT >= 31) context.getSystemService(VibratorManager::class.java).defaultVibrator else context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        if (android.os.Build.VERSION.SDK_INT >= 26) vibrator.vibrate(VibrationEffect.createOneShot(14, 48)) else vibrator.vibrate(14)
    } catch (_: Exception) { }
}
