package com.tactiledeck

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

private val Blue = Color(0xFF007AFF)
class MainActivity : ComponentActivity() { override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); setContent { Deck() } } }
private enum class Sound { SWITCH, TOGGLE, KNOB, PAD, RELAY }

@Composable private fun Deck() {
    val dark = androidx.compose.foundation.isSystemInDarkTheme()
    val context = LocalContext.current
    val haptics = LocalHapticFeedback.current
    val bg = if (dark) Color(0xFF0D0E11) else Color(0xFFF5F6F8)
    val fg = if (dark) Color(0xFFF1F4F8) else Color(0xFF202833)
    val muted = if (dark) Color(0xFF9AA6B3) else Color(0xFF697684)
    val panel = if (dark) Color(0xFF1A1E24) else Color(0xFFEAECEF)
    val audio = remember { ClickAudio(context) }
    DisposableEffect(audio) { onDispose { audio.close() } }
    var power by remember { mutableStateOf(true) }
    var knob by remember { mutableIntStateOf(62) }
    var count by remember { mutableIntStateOf(0) }
    val enabled = remember { mutableStateListOf(false, false, false, false) }
    val pads = remember { mutableStateListOf(*Array(16) { false }) }
    fun engage(sound: Sound) { count++; haptics.performHapticFeedback(HapticFeedbackType.TextHandleMove); audio.play(sound) }
    MaterialTheme { Surface(color = bg) {
        Column(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(bg, panel, bg))).padding(horizontal = 20.dp)) {
            Row(Modifier.fillMaxWidth().padding(top = 22.dp, bottom = 20.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("触控甲板", color = fg, fontSize = 24.sp, fontWeight = FontWeight.Bold, letterSpacing = 2.sp); Text("工业面板  /  精密触控系统", color = muted, fontSize = 11.sp, modifier = Modifier.padding(top = 5.dp)) }
                Column(horizontalAlignment = Alignment.End) { Text("系统状态", color = muted, fontSize = 10.sp); Text(if (power) "就绪" else "离线", color = if (power) Blue else muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) }
            }
            CardPanel(panel) { Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("系统状态", color = muted, fontSize = 10.sp); Text(if (power) "120Hz 极速响应" else "待机", color = fg, fontSize = 15.sp, fontWeight = FontWeight.SemiBold) }
                Column(horizontalAlignment = Alignment.End) { Text("操作次数", color = muted, fontSize = 10.sp); Text(count.toString().padStart(4, '0'), color = Blue, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
                Action("电源", power, panel) { power = !power; engage(Sound.SWITCH) }
            } }
            SectionTitle("机械微动", "工业弹簧拨动", fg, muted)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { listOf("电磁吸合", "辅助电源", "静音切换", "信标脉冲").forEachIndexed { i, name -> Box(Modifier.weight(1f)) { Action(name, enabled[i], panel) { enabled[i] = !enabled[i]; engage(if (i == 0) Sound.RELAY else Sound.TOGGLE) } } } }
            SectionTitle("铝制滚珠旋钮", "零延迟声学反馈", fg, muted)
            CardPanel(panel) { Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 15.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column { Text("输入灵敏度", color = muted, fontSize = 11.sp); Text("$knob%", color = fg, fontSize = 23.sp, fontWeight = FontWeight.Bold); Text(if (dark) "深色模式 · 黑曜石/太空灰" else "浅色模式 · 陶瓷白/喷砂银", color = muted, fontSize = 10.sp) }
                Pressable({ knob = if (knob >= 96) 8 else knob + 4; engage(Sound.KNOB) }, Modifier.size(72.dp)) { pressed -> Box(Modifier.fillMaxSize().shadow(if (pressed) 2.dp else 9.dp, CircleShape).background(Brush.linearGradient(if (dark) listOf(Color(0xFF46515D), Color(0xFF171B20)) else listOf(Color.White, Color(0xFFBEC7D1))), CircleShape).border(2.dp, Blue.copy(.65f), CircleShape), contentAlignment = Alignment.Center) { Text("◉", color = Blue, fontSize = 34.sp) } }
            } }
            SectionTitle("电磁吸合继电器", "就绪 / 已吸合", fg, muted)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) { listOf("锁止", "通信").forEachIndexed { i, name -> Box(Modifier.weight(1f)) { Action(name, enabled[i + 2], panel) { enabled[i + 2] = !enabled[i + 2]; engage(Sound.RELAY) } } } }
            SectionTitle("4x4 硅胶打击垫", "触觉与声学联动", fg, muted)
            LazyVerticalGrid(columns = GridCells.Fixed(4), modifier = Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(bottom = 14.dp)) {
                items((1..16).toList()) { n -> val i = n - 1; Pressable({ pads[i] = !pads[i]; haptics.performHapticFeedback(HapticFeedbackType.LongPress); engage(Sound.PAD) }, Modifier.aspectRatio(1f)) { pressed -> Box(Modifier.fillMaxSize().shadow(if (pressed) 1.dp else 5.dp, RoundedCornerShape(13.dp)).background(if (pads[i] || pressed) Blue.copy(if (dark) .34f else .18f) else panel, RoundedCornerShape(13.dp)).border(1.dp, if (pressed || pads[i]) Blue.copy(.85f) else fg.copy(.09f), RoundedCornerShape(13.dp)), contentAlignment = Alignment.Center) { Text(n.toString().padStart(2, '0'), color = if (pressed || pads[i]) Blue else muted, fontSize = 13.sp, fontWeight = FontWeight.SemiBold) } } }
            }
            Text(if (dark) "深色模式 · 黑曜石/太空灰" else "浅色模式 · 陶瓷白/喷砂银", color = muted, fontSize = 10.sp, modifier = Modifier.align(Alignment.CenterHorizontally).padding(bottom = 10.dp))
        }
    } }
}

@Composable private fun Pressable(onClick: () -> Unit, modifier: Modifier = Modifier, content: @Composable (Boolean) -> Unit) {
    val source = remember { MutableInteractionSource() }
    val pressed by source.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) .95f else 1f, spring(dampingRatio = .6f, stiffness = Spring.StiffnessMedium), label = "pressScale")
    Box(modifier.scale(scale).clickable(interactionSource = source, indication = null, onClick = onClick)) { content(pressed) }
}
@Composable private fun Action(label: String, active: Boolean, panel: Color, onClick: () -> Unit) {
    Pressable(onClick, Modifier.fillMaxWidth().height(68.dp)) { pressed -> Column(Modifier.fillMaxSize().shadow(if (pressed) 1.dp else 7.dp, RoundedCornerShape(14.dp)).background(Brush.linearGradient(listOf(panel, Color.White.copy(.25f))), RoundedCornerShape(14.dp)).border(1.dp, if (active || pressed) Blue.copy(.58f) else Color.White.copy(.7f), RoundedCornerShape(14.dp)).padding(horizontal = 10.dp, vertical = 10.dp), verticalArrangement = Arrangement.SpaceBetween) { Box(Modifier.size(7.dp).background(if (active || pressed) Blue else Color(0xFF94A0AC), CircleShape)); Text(label, color = if (active) Blue else Color(0xFF34404D), fontSize = 10.sp, fontWeight = FontWeight.SemiBold, maxLines = 2) } }
}
@Composable private fun CardPanel(panel: Color, content: @Composable () -> Unit) { Box(Modifier.fillMaxWidth().shadow(5.dp, RoundedCornerShape(18.dp)).background(panel, RoundedCornerShape(18.dp)).border(1.dp, Color.White.copy(.7f), RoundedCornerShape(18.dp))) { content() } }
@Composable private fun SectionTitle(title: String, subtitle: String, fg: Color, muted: Color) { Row(Modifier.fillMaxWidth().padding(top = 16.dp, bottom = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) { Text(title, color = fg, fontSize = 14.sp, fontWeight = FontWeight.Bold); Text(subtitle, color = muted, fontSize = 10.sp) } }
private class ClickAudio(context: Context) {
    private val pool = SoundPool.Builder().setMaxStreams(5).setAudioAttributes(AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()).build()
    private val samples = Sound.entries.associateWith { sound -> val rate=22050; val size=when(sound){Sound.RELAY->2800;Sound.PAD->2300;Sound.KNOB->900;Sound.TOGGLE->1500;Sound.SWITCH->1800}; val bytes=ByteArray(44+size*2)
        fun p16(a:Int,v:Int){bytes[a]=v.toByte();bytes[a+1]=(v ushr 8).toByte()}; fun p32(a:Int,v:Int){p16(a,v and 65535);p16(a+2,v ushr 16)}
        "RIFF".forEachIndexed{i,c->bytes[i]=c.code.toByte()};p32(4,36+size*2);"WAVEfmt ".forEachIndexed{i,c->bytes[8+i]=c.code.toByte()};p32(16,16);p16(20,1);p16(22,1);p32(24,rate);p32(28,rate*2);p16(32,2);p16(34,16);"data".forEachIndexed{i,c->bytes[36+i]=c.code.toByte()};p32(40,size*2)
        val f=when(sound){Sound.RELAY->120.0;Sound.PAD->170.0;Sound.KNOB->3200.0;Sound.TOGGLE->2400.0;Sound.SWITCH->1800.0};for(i in 0 until size){val t=i.toDouble()/rate;val sample=(sin(2*PI*f*t)*exp(-t*45)*.38*32767).toInt();bytes[44+i*2]=sample.toByte();bytes[45+i*2]=(sample shr 8).toByte()}; val file=java.io.File.createTempFile("deck",".wav",context.cacheDir);file.writeBytes(bytes);pool.load(file.absolutePath,1)
    }
    fun play(sound:Sound){samples[sound]?.let{if(it!=0)pool.play(it,.7f,.7f,1,0,1f)}}
    fun close(){pool.release()}
}
