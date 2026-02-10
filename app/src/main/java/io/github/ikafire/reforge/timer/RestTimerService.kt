package io.github.ikafire.reforge.timer

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import io.github.ikafire.reforge.MainActivity
import io.github.ikafire.reforge.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RestTimerService : Service() {

    private val scope = CoroutineScope(Dispatchers.Main + Job())
    private var timerJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val seconds = intent.getIntExtra(EXTRA_SECONDS, 90)
                startTimer(seconds)
            }
            ACTION_ADD_30 -> {
                val current = _remainingSeconds.value
                if (current > 0) {
                    _remainingSeconds.value = current + 30
                    updateNotification()
                }
            }
            ACTION_SUBTRACT_30 -> {
                val current = _remainingSeconds.value
                if (current > 30) {
                    _remainingSeconds.value = current - 30
                    updateNotification()
                } else {
                    stopTimer()
                }
            }
            ACTION_SKIP -> {
                stopTimer()
            }
        }
        return START_NOT_STICKY
    }

    private fun startTimer(seconds: Int) {
        timerJob?.cancel()
        _remainingSeconds.value = seconds
        _isRunning.value = true

        startForeground(NOTIFICATION_ID, buildNotification(seconds))

        timerJob = scope.launch {
            while (_remainingSeconds.value > 0) {
                delay(1000)
                _remainingSeconds.value = _remainingSeconds.value - 1
                updateNotification()
            }
            // Timer complete
            _isRunning.value = false
            showCompleteNotification()
            stopForeground(STOP_FOREGROUND_REMOVE)
            stopSelf()
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        _remainingSeconds.value = 0
        _isRunning.value = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Rest Timer",
            NotificationManager.IMPORTANCE_LOW,
        ).apply {
            description = "Shows rest timer countdown"
        }
        val completeChannel = NotificationChannel(
            CHANNEL_COMPLETE_ID,
            "Timer Complete",
            NotificationManager.IMPORTANCE_HIGH,
        ).apply {
            description = "Notifies when rest timer is complete"
            enableVibration(true)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
        manager.createNotificationChannel(completeChannel)
    }

    private fun buildNotification(seconds: Int): Notification {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val skipIntent = PendingIntent.getService(
            this, 1,
            Intent(this, RestTimerService::class.java).apply { action = ACTION_SKIP },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val add30Intent = PendingIntent.getService(
            this, 2,
            Intent(this, RestTimerService::class.java).apply { action = ACTION_ADD_30 },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val mins = seconds / 60
        val secs = seconds % 60

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Rest Timer")
            .setContentText("${mins}:${"%02d".format(secs)} remaining")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openIntent)
            .addAction(0, "+30s", add30Intent)
            .addAction(0, "Skip", skipIntent)
            .setOngoing(true)
            .setSilent(true)
            .build()
    }

    private fun updateNotification() {
        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_ID, buildNotification(_remainingSeconds.value))
    }

    private fun showCompleteNotification() {
        val openIntent = PendingIntent.getActivity(
            this, 0,
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_WORKOUT, true)
            },
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT,
        )

        val notification = NotificationCompat.Builder(this, CHANNEL_COMPLETE_ID)
            .setContentTitle("Rest Complete")
            .setContentText("Time to start your next set!")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openIntent)
            .addAction(0, "Log Set", openIntent)
            .setAutoCancel(true)
            .build()

        val manager = getSystemService(NotificationManager::class.java)
        manager.notify(NOTIFICATION_COMPLETE_ID, notification)
    }

    override fun onDestroy() {
        timerJob?.cancel()
        _isRunning.value = false
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "rest_timer"
        const val CHANNEL_COMPLETE_ID = "timer_complete"
        const val NOTIFICATION_ID = 1001
        const val NOTIFICATION_COMPLETE_ID = 1002
        const val ACTION_START = "io.github.ikafire.reforge.timer.START"
        const val ACTION_ADD_30 = "io.github.ikafire.reforge.timer.ADD_30"
        const val ACTION_SUBTRACT_30 = "io.github.ikafire.reforge.timer.SUBTRACT_30"
        const val ACTION_SKIP = "io.github.ikafire.reforge.timer.SKIP"
        const val EXTRA_SECONDS = "seconds"
        const val EXTRA_OPEN_WORKOUT = "open_workout"

        private val _remainingSeconds = MutableStateFlow(0)
        val remainingSeconds: StateFlow<Int> = _remainingSeconds.asStateFlow()

        private val _isRunning = MutableStateFlow(false)
        val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

        fun start(context: Context, seconds: Int) {
            context.startForegroundService(
                Intent(context, RestTimerService::class.java).apply {
                    action = ACTION_START
                    putExtra(EXTRA_SECONDS, seconds)
                }
            )
        }

        fun skip(context: Context) {
            context.startService(
                Intent(context, RestTimerService::class.java).apply { action = ACTION_SKIP }
            )
        }
    }
}
