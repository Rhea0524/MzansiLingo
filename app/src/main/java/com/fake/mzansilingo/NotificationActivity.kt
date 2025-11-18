package com.fake.mzansilingo

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import java.util.*

class NotificationActivity : BaseActivity() {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var btnMenu: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvMorningLabel: TextView
    private lateinit var tvEveningLabel: TextView
    private lateinit var switchMorningReminder: Switch
    private lateinit var switchEveningReminder: Switch
    private lateinit var btnTestNotification: Button

    companion object {
        const val MORNING_NOTIFICATION_ID = 1001
        const val EVENING_NOTIFICATION_ID = 1002
        const val CHANNEL_ID = "MZANSILINGO_REMINDERS"
        const val MORNING_HOUR = 7
        const val EVENING_HOUR = 18
        private const val TAG = "NotificationActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_notification)

        initViews()
        setupNotificationChannel()
        checkExactAlarmPermission()
        setupClickListeners()
        setupNavigationDrawer()
        setupNotifications()
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawer_layout)
        btnMenu = findViewById(R.id.btn_menu)
        tvTitle = findViewById(R.id.tv_title)
        tvMorningLabel = findViewById(R.id.tv_morning_label)
        tvEveningLabel = findViewById(R.id.tv_evening_label)
        switchMorningReminder = findViewById(R.id.switch_morning_reminder)
        switchEveningReminder = findViewById(R.id.switch_evening_reminder)
        btnTestNotification = findViewById(R.id.btn_test_notification)

        // Set text from string resources
        tvTitle.text = getString(R.string.notifications_title)
        tvMorningLabel.text = getString(R.string.morning_reminder_label)
        tvEveningLabel.text = getString(R.string.evening_reminder_label)
        btnTestNotification.text = getString(R.string.btn_test_notification)
    }

    private fun setupNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = getString(R.string.notification_channel_description)
                enableVibration(true)
                setShowBadge(true)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    getString(R.string.error_exact_alarm_permission),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun setupNotifications() {
        val sharedPrefs = getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)

        // Load saved preferences
        switchMorningReminder.isChecked = sharedPrefs.getBoolean("morning_enabled", true)
        switchEveningReminder.isChecked = sharedPrefs.getBoolean("evening_enabled", true)

        // Set up listeners
        switchMorningReminder.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("morning_enabled", isChecked).apply()
            if (isChecked) {
                scheduleMorningNotification()
                Toast.makeText(
                    this,
                    getString(R.string.morning_reminder_enabled),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                cancelNotification(MORNING_NOTIFICATION_ID)
                Toast.makeText(
                    this,
                    getString(R.string.morning_reminder_disabled),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        switchEveningReminder.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean("evening_enabled", isChecked).apply()
            if (isChecked) {
                scheduleEveningNotification()
                Toast.makeText(
                    this,
                    getString(R.string.evening_reminder_enabled),
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                cancelNotification(EVENING_NOTIFICATION_ID)
                Toast.makeText(
                    this,
                    getString(R.string.evening_reminder_disabled),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        // Schedule initial notifications if switches are enabled
        if (switchMorningReminder.isChecked) {
            scheduleMorningNotification()
        }
        if (switchEveningReminder.isChecked) {
            scheduleEveningNotification()
        }
    }

    private fun setupClickListeners() {
        btnMenu.setOnClickListener {
            if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
                drawerLayout.closeDrawer(GravityCompat.END)
            } else {
                drawerLayout.openDrawer(GravityCompat.END)
            }
        }

        btnTestNotification.setOnClickListener {
            // Send a test notification immediately
            val intent = Intent(this, NotificationReceiver::class.java).apply {
                putExtra("type", "test")
            }
            sendBroadcast(intent)
            Toast.makeText(
                this,
                getString(R.string.test_notification_sent),
                Toast.LENGTH_SHORT
            ).show()
        }

        findViewById<ImageView>(R.id.nav_back).setOnClickListener {
            finish()
        }
    }

    private fun scheduleMorningNotification() {
        scheduleNotification(
            MORNING_NOTIFICATION_ID,
            MORNING_HOUR,
            0,
            "morning"
        )
    }

    private fun scheduleEveningNotification() {
        scheduleNotification(
            EVENING_NOTIFICATION_ID,
            EVENING_HOUR,
            0,
            "evening"
        )
    }

    private fun scheduleNotification(id: Int, hour: Int, minute: Int, type: String) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // Check if exact alarm permission is granted (Android 12+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(
                    this,
                    getString(R.string.error_enable_exact_alarm),
                    Toast.LENGTH_LONG
                ).show()
                try {
                    val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
                    startActivity(intent)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to open exact alarm settings", e)
                }
                return
            }
        }

        val intent = Intent(this, NotificationReceiver::class.java).apply {
            putExtra("type", type)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)

            // If the time has already passed today, schedule for tomorrow
            if (before(Calendar.getInstance())) {
                add(Calendar.DATE, 1)
            }
        }

        try {
            // Use setExactAndAllowWhileIdle for better reliability
            // The receiver will reschedule itself after firing
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
            Log.d(TAG, "Scheduled $type notification for ${calendar.time}")
        } catch (e: SecurityException) {
            Toast.makeText(
                this,
                getString(R.string.error_permission_exact_alarms),
                Toast.LENGTH_LONG
            ).show()
            Log.e(TAG, "Failed to schedule notification", e)
        }
    }

    private fun cancelNotification(id: Int) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
        Log.d(TAG, "Cancelled notification with id: $id")
    }

    private fun setupNavigationDrawer() {
        findViewById<TextView>(R.id.nav_home).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<TextView>(R.id.nav_words).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            val intent = Intent(this, WordsActivity::class.java)
            intent.putExtra("LANGUAGE", "afrikaans")
            startActivity(intent)
        }

        findViewById<TextView>(R.id.nav_phrases).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            val intent = Intent(this, PhrasesActivity::class.java)
            intent.putExtra("LANGUAGE", "afrikaans")
            startActivity(intent)
        }

        findViewById<TextView>(R.id.nav_progress).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            val intent = Intent(this, ProgressActivity::class.java)
            intent.putExtra("LANGUAGE", "afrikaans")
            startActivity(intent)
        }

        findViewById<TextView>(R.id.nav_settings).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        findViewById<TextView>(R.id.nav_profile).setOnClickListener {
            drawerLayout.closeDrawer(GravityCompat.END)
            startActivity(Intent(this, ProfileActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()
        // Re-check permission status when returning to the activity
        checkExactAlarmPermission()
    }
}