package com.tactiledeck

import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.content.Context
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Ink = Color(0xFF090C0F)
private val Panel = Color(0xFF151A1E)
private val Line = Color(0xFF303940)
private val Amber = Color(0xFFFFB84A)
private val Mint = Color(0xFF67F0BC)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); window.statusBarColor=android.graphics.Color.rgb(9,12,15); window.navigationBarColor=android.graphics.Color.rgb(9,12,15); setContent { Deck() } }
}

data class Key(val title:String,val kind:String,val color:Color)

@Composable fun Deck() {
    val ctx= LocalContext.current
    val haptic= LocalHapticFeedback.current
    var power by remember { mutableStateOf(true) }
    var armed by remember { mutableStateOf(false) }
    var count by remember { mutableIntStateOf(0) }
    val keys=listOf(Key("IGNITION","GUARDED",Amber),Key("AUX POWER","LATCH",Mint),Key("SILENT MODE","TOGGLE",Color(0xFF8FA8FF)),Key("BEACON","PULSE",Color(0xFFFF6978)),Key("SYSTEM LOCK","GUARDED",Amber),Key("COMMS","LATCH",Mint))
    Column(Modifier.fillMaxSize().background(Ink).padding(horizontal=18.dp)) {
        Row(Modifier.fillMaxWidth().padding(top=18.dp,bottom=12.dp), horizontalArrangement=Arrangement.SpaceBetween, verticalAlignment=Alignment.CenterVertically) {
            Column { Text("TACTILE / DECK", color=Color.White, fontWeight=FontWeight.Black, letterSpacing=2.sp, fontSize=19.sp); Text("FIELD CONTROL SYSTEM  •  MK.02",color=Color(0xFF7D8A92),fontSize=9.sp,letterSpacing=1.5.sp) }
            Text("● ONLINE", color=Mint, fontSize=10.sp, fontWeight=FontWeight.Bold)
        }
        Row(Modifier.fillMaxWidth().background(Panel, RoundedCornerShape(14.dp)).border(1.dp,Line,RoundedCornerShape(14.dp)).padding(16.dp),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) {
            Column { Text("OUTPUT / 01",color=Color(0xFF829098),fontSize=9.sp,letterSpacing=2.sp); Text(if(power) "NOMINAL" else "STANDBY",color=if(power) Mint else Amber,fontSize=13.sp,fontWeight=FontWeight.Bold) }
            Text("${count.toString().padStart(4,'0')}",color=Amber,fontSize=27.sp,fontWeight=FontWeight.Black,letterSpacing=4.sp)
            Switch(checked=power,onCheckedChange={power=it; buzz(ctx); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)},colors=SwitchDefaults.colors(checkedThumbColor=Ink,checkedTrackColor=Mint,uncheckedThumbColor=Color.LightGray,uncheckedTrackColor=Line))
        }
        Text("TACTICAL CONTROLS",color=Color(0xFF829098),fontSize=10.sp,letterSpacing=2.sp,modifier=Modifier.padding(top=20.dp,bottom=10.dp))
        LazyVerticalGrid(columns=GridCells.Fixed(2), modifier=Modifier.weight(1f), contentPadding=PaddingValues(bottom=10.dp),horizontalArrangement=Arrangement.spacedBy(10.dp),verticalArrangement=Arrangement.spacedBy(10.dp)) {
            items(keys) { key ->
                val active= remember { mutableStateOf(false) }
                Control(key,active.value) { if(key.kind=="GUARDED" && !armed) { armed=true; active.value=true } else { active.value=!active.value }; count++; buzz(ctx); haptic.performHapticFeedback(HapticFeedbackType.LongPress) }
            }
        }
        Text("MATRIX / TRIGGER PAD",color=Color(0xFF829098),fontSize=10.sp,letterSpacing=2.sp,modifier=Modifier.padding(vertical=9.dp))
        Column(verticalArrangement=Arrangement.spacedBy(8.dp),modifier=Modifier.padding(bottom=18.dp)) {
            repeat(2) { r -> Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) { repeat(4) { c -> var on by remember { mutableStateOf(false) }; Box(Modifier.weight(1f).height(49.dp).background(if(on) Brush.verticalGradient(listOf(Color(0xFF35483F),Panel)) else Brush.verticalGradient(listOf(Color(0xFF242B30),Panel)),RoundedCornerShape(10.dp)).border(1.dp,if(on) Mint.copy(.65f) else Line,RoundedCornerShape(10.dp)).clickable { on=!on; count++; buzz(ctx); haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove) },contentAlignment=Alignment.Center) { Text("${(r*4+c+1).toString().padStart(2,'0')}",color=if(on) Mint else Color(0xFF829098),fontWeight=FontWeight.Bold,fontSize=12.sp) } } } }
        }
    }
}

@Composable fun Control(key:Key,on:Boolean,onClick:()->Unit) {
    val scale by animateFloatAsState(if(on) .95f else 1f,label="press")
    Column(Modifier.fillMaxWidth().height(105.dp).scale(scale).background(Brush.verticalGradient(listOf(Color(0xFF252D32),Panel)),RoundedCornerShape(14.dp)).border(1.dp,if(on) key.color.copy(.7f) else Line,RoundedCornerShape(14.dp)).clickable(onClick=onClick).padding(12.dp),verticalArrangement=Arrangement.SpaceBetween) {
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.CenterVertically) { Text(key.kind,color=Color(0xFF77858D),fontSize=8.sp,letterSpacing=1.sp); Box(Modifier.size(8.dp).background(if(on) key.color else Color(0xFF3A4348),RoundedCornerShape(50))) }
        Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceBetween,verticalAlignment=Alignment.Bottom) { Text(key.title,color=Color(0xFFE6ECEE),fontSize=11.sp,fontWeight=FontWeight.Bold,letterSpacing=.5.sp); Text(if(on) "ON" else "OFF",color=if(on) key.color else Color(0xFF748087),fontSize=9.sp,fontWeight=FontWeight.Bold) }
    }
}

@Suppress("DEPRECATION") fun buzz(context:Context) { try { val v=if(android.os.Build.VERSION.SDK_INT>=31) context.getSystemService(VibratorManager::class.java).defaultVibrator else context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator; if(android.os.Build.VERSION.SDK_INT>=26) v.vibrate(VibrationEffect.createOneShot(22,75)) else v.vibrate(22) } catch (_:Exception) {} }
