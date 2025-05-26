package com.example.julestimer

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat

class MainActivity : AppCompatActivity() {

    private lateinit var timerText: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var startButton: Button
    private var countDownTimer: CountDownTimer? = null
    private var timerRunning = false
    private var timerLengthSeconds = 60L // default 1 minute

    private val channelId = "egg_timer_channel"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        timerText = findViewById(R.id.timerText)
        seekBar = findViewById(R.id.seekBar)
        startButton = findViewById(R.id.startButton)

        seekBar.max = 15 * 60 // up to 15 minutes
        seekBar.progress = timerLengthSeconds.toInt()
        updateTimerText(timerLengthSeconds)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                timerLengthSeconds = progress.toLong()
                updateTimerText(timerLengthSeconds)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        startButton.setOnClickListener {
            if (!timerRunning) {
                startEggTimer(timerLengthSeconds)
            } else {
                cancelEggTimer()
            }
        }

        createNotificationChannel()
    }

    private fun updateTimerText(seconds: Long) {
        val min = seconds / 60
        val sec = seconds % 60
        timerText.text = String.format("%02d:%02d", min, sec)
    }

    private fun startEggTimer(seconds: Long) {
        if (seconds == 0L) {
            Toast.makeText(this, "Set timer for more than 0 seconds!", Toast.LENGTH_SHORT).show()
            return
        }
        timerRunning = true
        seekBar.isEnabled = false
        startButton.text = "Cancel"
        countDownTimer = object : CountDownTimer(seconds * 1000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                updateTimerText(millisUntilFinished / 1000)
            }

            override fun onFinish() {
                timerRunning = false
                seekBar.isEnabled = true
                startButton.text = "Start"
                updateTimerText(0)
                Toast.makeText(
                    this@MainActivity,
                    "Egg is ready! 🥚",
                    Toast.LENGTH_LONG
                ).show()
                showNotification()
            }
        }.start()
    }

    private fun cancelEggTimer() {
        timerRunning = false
        countDownTimer?.cancel()
        seekBar.isEnabled = true
        startButton.text = "Start"
        updateTimerText(timerLengthSeconds)
    }

    private fun showNotification() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_egg)
            .setContentTitle("Egg Timer")
            .setContentText("Your egg is ready! 🥚")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(1, notificationBuilder.build())
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Egg Timer Channel"
            val descriptionText = "Channel for egg timer notifications"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
}