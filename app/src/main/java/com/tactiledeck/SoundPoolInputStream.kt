package com.tactiledeck

import android.media.SoundPool
import java.io.File
import java.io.InputStream

/** Bridges generated in-memory WAV data to SoundPool's supported path-based loader. */
fun SoundPool.load(input: InputStream, priority: Int): Int {
    val wav = File.createTempFile("tactile-sound-", ".wav")
    input.use { source -> wav.outputStream().use { sink -> source.copyTo(sink) } }
    wav.deleteOnExit()
    return load(wav.absolutePath, priority)
}
