package org.macnigor.toothbrushingtimer

import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class MainActivity : AppCompatActivity() {

    private lateinit var countdownTimer: CountDownTimer
    private lateinit var textView: TextView
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = findViewById(R.id.textView)
        stopButton = findViewById(R.id.stopButton)

        stopButton.setOnClickListener {
            countdownTimer.cancel()

            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                mediaPlayer.stop()
                mediaPlayer.release()
            }

            finishAffinity()
        }

        countdownTimer = object : CountDownTimer(120000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsLeft = millisUntilFinished / 1000
                textView.text = "Осталось: $secondsLeft сек."
            }

            override fun onFinish() {
                lifecycleScope.launch {
                    playSound()
                    finishAffinity()
                }
            }
        }.start()
    }


    private suspend fun MediaPlayer.awaitCompletion() = suspendCancellableCoroutine<Unit> { cont ->
        setOnCompletionListener {
            cont.resume(Unit)
        }
        setOnErrorListener { _, _, _ ->
            cont.resume(Unit) // Завершаем корутину и при ошибке
            true
        }
    }

    private suspend fun playSound() {
        mediaPlayer = MediaPlayer.create(this, R.raw.alarm_sound)

        mediaPlayer.start()
        // Ждем окончания воспроизведения
        mediaPlayer.awaitCompletion()
        mediaPlayer.release()
    }

    override fun onDestroy() {
        super.onDestroy()
        countdownTimer.cancel()
        if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
            mediaPlayer.stop()
            mediaPlayer.release()
        }
    }
}
