package com.example.method_channel_test

import android.Manifest
import android.app.AlertDialog
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.os.SystemClock
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.Settings
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import java.util.Locale
import java.util.UUID
import kotlin.random.Random

class MainActivity : FlutterActivity() {
    private val channelName = "com.example.methodchannel_demo"
    private lateinit var channel: MethodChannel
    private var pendingPermissionResult: MethodChannel.Result? = null
    private var pendingPermissionCode = 0
    private var pendingTorchEnabled = true
    private var torchState = false

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)
        channel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, channelName)
        channel.setMethodCallHandler { call, result ->
            handleMethodCall(call, result)
        }
    }

    private fun handleMethodCall(call: MethodCall, result: MethodChannel.Result) {
        val args = call.arguments
        @Suppress("UNCHECKED_CAST")
        val map = args as? Map<String, Any?>

        try {
            when (call.method) {
                "getBatteryLevel" -> result.success(getBatteryLevel())
                "getDeviceInfo" -> result.success(getDeviceInfo())
                "getLocale" -> result.success(Locale.getDefault().toString())
                "showAlert" -> showAlert(map, result)
                "triggerHapticFeedback" -> { vibrate(40); result.success(true) }
                "getScreenSize" -> result.success(getScreenSize())
                "getSystemVersion" -> result.success("Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})")
                "getAppName" -> result.success(getAppName())
                "getBundleId" -> result.success(packageName)
                "getAppVersion" -> result.success(getPackageInfo().versionName)
                "getBuildNumber" -> result.success(getPackageInfo().longVersionCode.toString())
                "getTimeZone" -> result.success(java.util.TimeZone.getDefault().id)
                "getCurrentTime" -> result.success(System.currentTimeMillis())
                "getDateFormat" -> result.success(android.text.format.DateFormat.getBestDateTimePattern(Locale.getDefault(), "yyyyMMddHHmmss"))
                "getClipboardText" -> result.success(getClipboardText())
                "copyToClipboard" -> copyToClipboard(map, result)
                "shareText" -> shareText(map)
                "openUrl" -> openUrl(map, result)
                "openSettings" -> openSettings(result)
                "openMaps" -> openMaps(map, result)
                "openDialer" -> openDialer(map, result)
                "openMail" -> openMail(map, result)
                "getNetworkStatus" -> result.success(isNetworkConnected())
                "isAirplaneModeEnabled" -> result.success(isAirplaneModeEnabled())
                "isCharging" -> result.success(isCharging())
                "isDarkMode" -> result.success(isDarkMode())
                "isFaceIDAvailable" -> result.success(hasBiometricFeature(PackageManager.FEATURE_FACE) || hasBiometricFeature(PackageManager.FEATURE_IRIS))
                "isTouchIDAvailable" -> result.success(hasFingerprint())
                "isLowPowerModeEnabled" -> result.success(isPowerSaveMode())
                "getProximityState" -> readProximity(result)
                "getBrightness" -> result.success(getBrightness())
                "setBrightness" -> result.success(setBrightness(map))
                "vibrateLight" -> { vibrate(20); result.success(true) }
                "vibrateMedium" -> { vibrate(60); result.success(true) }
                "vibrateHeavy" -> { vibrate(120); result.success(true) }
                "playNotificationSound" -> { playNotificationSound(); result.success(true) }
                "playImpactSound" -> { playImpactSound(); result.success(true) }
                "toggleTorch" -> toggleTorch(map, result)
                "getTorchStatus" -> result.success(torchState)
                "getDeviceId" -> result.success(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))
                "getVendorId" -> result.success("${Build.MANUFACTURER} ${Build.MODEL}")
                "getDiskUsage" -> result.success(getDiskUsage())
                "getFreeStorage" -> result.success(getFreeStorage())
                "getMemoryUsage" -> result.success(getMemoryUsage())
                "getCPUUsage" -> result.success(getCpuUsage())
                "getUptime" -> result.success(SystemClock.elapsedRealtime())
                "getAvailableLocales" -> result.success(Locale.getAvailableLocales().map { it.toLanguageTag() })
                "getStatusBarHeight" -> result.success(getStatusBarHeight())
                "getKeyboardHeight" -> result.success(0)
                "getBadgeCount" -> result.success(0)
                "setBadge" -> result.success(false)
                "clearBadge" -> result.success(true)
                "isSimulator" -> result.success(false)
                "getOrientation" -> result.success(getOrientation())
                "getLanguageCode" -> result.success(Locale.getDefault().language)
                "getCountryCode" -> result.success(Locale.getDefault().country)
                "generateUuid" -> result.success(UUID.randomUUID().toString())
                "generateRandomNumber" -> result.success(generateRandomNumber(map))
                "showActionSheet" -> showActionSheet(map, result)
                "scheduleLocalNotification" -> scheduleLocalNotification(map, result)
                "getNotificationPermissionStatus" -> result.success(hasNotificationPermission())
                "requestNotificationPermission" -> requestNotificationPermission(result)
                "toggleSound" -> result.success(true)
                "getVolumeLevel" -> result.success(getVolumeLevel())
                else -> result.notImplemented()
            }
        } catch (e: Exception) {
            result.error("ANDROID_ERROR", e.message, null)
        }
    }

    // ---------------- Device / System info ----------------

    private fun getPackageInfo() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
    else
        @Suppress("DEPRECATION") packageManager.getPackageInfo(packageName, 0)

    private fun getAppName(): String {
        val label = packageManager.getApplicationInfo(packageName, 0).loadLabel(packageManager)
        return label.toString()
    }

    private fun getDeviceInfo(): Map<String, Any> = mapOf(
        "model" to Build.MODEL,
        "brand" to Build.BRAND,
        "manufacturer" to Build.MANUFACTURER,
        "os" to "Android",
        "osVersion" to Build.VERSION.RELEASE,
        "apiLevel" to Build.VERSION.SDK_INT,
        "device" to Build.DEVICE,
        "product" to Build.PRODUCT,
    )

    private fun getBatteryLevel(): Int {
        val bm = getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        return bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
    }

    private fun isCharging(): Boolean {
        val intent = registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        return status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL
    }

    private fun getScreenSize(): Map<String, Any> {
        val dm = resources.displayMetrics
        return mapOf("width" to dm.widthPixels, "height" to dm.heightPixels, "density" to dm.density)
    }

    private fun isAirplaneModeEnabled(): Boolean =
        Settings.Global.getInt(contentResolver, Settings.Global.AIRPLANE_MODE_ON, 0) != 0

    private fun isDarkMode(): Boolean =
        resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

    private fun isPowerSaveMode(): Boolean {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        return pm.isPowerSaveMode
    }

    private fun hasFingerprint(): Boolean {
        val pm = packageManager
        return pm.hasSystemFeature(PackageManager.FEATURE_FINGERPRINT)
    }

    private fun hasBiometricFeature(feature: String): Boolean = packageManager.hasSystemFeature(feature)

    // ---------------- Clipboard / share / intents ----------------

    private fun getClipboardText(): String? {
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = cm.primaryClip ?: return null
        return if (clip.itemCount > 0) clip.getItemAt(0).coerceToText(this).toString() else null
    }

    private fun copyToClipboard(map: Map<String, Any?>?, result: MethodChannel.Result) {
        val text = map?.get("text") as? String ?: ""
        val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("Flutter", text))
        result.success(true)
    }

    private fun shareText(map: Map<String, Any?>?) {
        val text = map?.get("text") as? String ?: ""
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        startActivity(Intent.createChooser(send, "Share via"))
    }

    private fun openUrl(map: Map<String, Any?>?, result: MethodChannel.Result) {
        val url = map?.get("url") as? String ?: return result.error("INVALID_ARGUMENTS", "url missing", null)
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        runCatching { startActivity(intent) }
            .onFailure { result.error("NO_APP", "No app to open URL", null) }
            .onSuccess { result.success(true) }
    }

    private fun openSettings(result: MethodChannel.Result) {
        runCatching { startActivity(Intent(Settings.ACTION_SETTINGS)); result.success(true) }
            .onFailure { result.error("NO_APP", "Cannot open settings", null) }
    }

    private fun openMaps(map: Map<String, Any?>?, result: MethodChannel.Result) {
        val lat = map?.get("latitude") as? Double ?: return result.error("INVALID_ARGUMENTS", "latitude missing", null)
        val lon = map?.get("longitude") as? Double ?: return result.error("INVALID_ARGUMENTS", "longitude missing", null)
        val geo = Uri.parse("geo:$lat,$lon?q=$lat,$lon")
        runCatching { startActivity(Intent(Intent.ACTION_VIEW, geo)); result.success(true) }
            .onFailure { result.error("NO_APP", "Cannot open maps", null) }
    }

    private fun openDialer(map: Map<String, Any?>?, result: MethodChannel.Result) {
        val phone = map?.get("phone") as? String ?: return result.error("INVALID_ARGUMENTS", "phone missing", null)
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
        runCatching { startActivity(intent); result.success(true) }
            .onFailure { result.error("NO_APP", "Cannot open dialer", null) }
    }

    private fun openMail(map: Map<String, Any?>?, result: MethodChannel.Result) {
        val to = map?.get("to") as? String ?: ""
        val subject = map?.get("subject") as? String ?: ""
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$to")
            putExtra(Intent.EXTRA_SUBJECT, subject)
        }
        runCatching { startActivity(intent); result.success(true) }
            .onFailure { result.error("NO_APP", "No mail app", null) }
    }

    // ---------------- Network ----------------

    private fun isNetworkConnected(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    // ---------------- UI (dialogs, orientation) ----------------

    private fun showAlert(map: Map<String, Any?>?, result: MethodChannel.Result) {
        val title = map?.get("title") as? String ?: ""
        val message = map?.get("message") as? String ?: ""
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton("OK") { _, _ -> result.success(true) }
                .setOnDismissListener { }
                .show()
        }
    }

    private fun showActionSheet(map: Map<String, Any?>?, result: MethodChannel.Result) {
        val title = map?.get("title") as? String ?: ""
        val message = map?.get("message") as? String ?: ""
        val options = arrayOf("Option 1", "Option 2", "Option 3")
        runOnUiThread {
            AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setItems(options) { _, which -> result.success(options[which]) }
                .setNegativeButton("Cancel") { _, _ -> result.success("cancelled") }
                .show()
        }
    }

    private fun getOrientation(): String =
        when (resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> "landscape"
            Configuration.ORIENTATION_PORTRAIT -> "portrait"
            else -> "unknown"
        }

    // ---------------- Brightness ----------------

    private fun getBrightness(): Double {
        val value = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS, 128)
        return value / 255.0
    }

    private fun setBrightness(map: Map<String, Any?>?): Boolean {
        val value = map?.get("value") as? Double ?: 0.5
        val brightness = (value * 255).toInt().coerceIn(1, 255)
        runOnUiThread {
            val attributes = window.attributes
            attributes.screenBrightness = value.toFloat().coerceIn(0.01f, 1f)
            window.attributes = attributes
        }
        return true
    }

    // ---------------- Haptics & sound ----------------

    private fun vibrate(ms: Long) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vm = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            val v = vm.defaultVibrator
            if (v.hasVibrator()) v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
        } else {
            @Suppress("DEPRECATION")
            val v = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (v.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    v.vibrate(ms)
                }
            }
        }
    }

    private fun playNotificationSound() {
        val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(this, uri)
        r?.play()
    }

    private fun playImpactSound() {
        val tone = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 70)
        tone.startTone(ToneGenerator.TONE_PROP_BEEP, 100)
    }

    private fun getVolumeLevel(): Int {
        val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
        val current = am.getStreamVolume(AudioManager.STREAM_MUSIC)
        return if (max == 0) 0 else (current * 100 / max)
    }

    // ---------------- Torch / flashlight ----------------

    private fun hasFlash(): Boolean = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)

    private fun toggleTorch(map: Map<String, Any?>?, result: MethodChannel.Result) {
        val enabled = map?.get("enabled") as? Boolean ?: true
        if (!hasFlash()) return result.error("NO_FLASH", "Device has no flashlight", null)
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return result.error("UNSUPPORTED", "Torch needs API 23+", null)
        val cam = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val flashId = try {
            cam.cameraIdList.firstOrNull { id ->
                cam.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) { null }

        if (flashId == null) return result.error("NO_FLASH", "No flash camera found", null)

        pendingPermissionCode = 100
        pendingPermissionResult = result
        pendingTorchEnabled = enabled
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 100)
        } else {
            setTorch(flashId, enabled, result)
        }
    }

    private fun setTorch(flashId: String, enabled: Boolean, result: MethodChannel.Result) {
        try {
            val cam = getSystemService(Context.CAMERA_SERVICE) as CameraManager
            cam.setTorchMode(flashId, enabled)
            torchState = enabled
            result.success(enabled)
        } catch (e: Exception) {
            result.error("TORCH_ERROR", e.message, null)
        }
    }

    // ---------------- Proximity ----------------

    private fun readProximity(result: MethodChannel.Result) {
        val sm = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val prox = sm.getDefaultSensor(Sensor.TYPE_PROXIMITY) ?: return result.error("NO_SENSOR", "No proximity sensor", null)
        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                sm.unregisterListener(this)
                val distance = event.values[0]
                result.success(distance < prox.maximumRange)
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {}
        }
        sm.registerListener(listener, prox, SensorManager.SENSOR_DELAY_NORMAL)
    }

    // ---------------- Storage / memory / CPU ----------------

    private fun getDiskUsage(): Long {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        return stat.totalBytes
    }

    private fun getFreeStorage(): Long {
        val path = Environment.getDataDirectory()
        val stat = StatFs(path.path)
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
            stat.availableBytes
        else
            stat.availableBlocks.toLong() * stat.blockSize.toLong()
    }

    private fun getMemoryUsage(): Map<String, Any> {
        val am = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val mi = android.app.ActivityManager.MemoryInfo()
        am.getMemoryInfo(mi)
        return mapOf(
            "total" to mi.totalMem,
            "available" to mi.availMem,
            "lowMemory" to mi.lowMemory,
            "threshold" to mi.threshold,
        )
    }

    private fun getCpuUsage(): Double {
        return try {
            fun readCpu(): Pair<Long, Long>? {
                val line = java.io.File("/proc/stat").readLines().firstOrNull() ?: return null
                val parts = line.split(Regex("\\s+"))
                if (parts.size < 5) return null
                val user = parts[1].toLong()
                val nice = parts[2].toLong()
                val system = parts[3].toLong()
                val idle = parts[4].toLong()
                val iowait = if (parts.size > 5) parts[5].toLong() else 0L
                val irq = if (parts.size > 6) parts[6].toLong() else 0L
                val softirq = if (parts.size > 7) parts[7].toLong() else 0L
                val steal = if (parts.size > 8) parts[8].toLong() else 0L
                val total = user + nice + system + idle + iowait + irq + softirq + steal
                return Pair(total, idle + iowait)
            }
            val first = readCpu() ?: return 0.0
            Thread.sleep(300)
            val second = readCpu() ?: return 0.0
            val totalDiff = second.first - first.first
            val idleDiff = second.second - first.second
            if (totalDiff == 0L) 0.0 else (1.0 - idleDiff.toDouble() / totalDiff) * 100.0
        } catch (e: Exception) {
            0.0
        }
    }

    private fun getStatusBarHeight(): Int {
        val id = resources.getIdentifier("status_bar_height", "dimen", "android")
        return if (id > 0) resources.getDimensionPixelSize(id) else 0
    }

    // ---------------- Random ----------------

    private fun generateRandomNumber(map: Map<String, Any?>?): Int {
        val min = (map?.get("min") as? Number)?.toInt() ?: 0
        val max = (map?.get("max") as? Number)?.toInt() ?: 100
        return if (max <= min) min else Random.nextInt(min, max)
    }

    // ---------------- Notification ----------------

    private fun hasNotificationPermission(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED

    private fun requestNotificationPermission(result: MethodChannel.Result) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            result.success(true)
            return
        }
        pendingPermissionCode = 200
        pendingPermissionResult = result
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 200)
    }

    private fun scheduleLocalNotification(map: Map<String, Any?>?, result: MethodChannel.Result) {
        if (!hasNotificationPermission()) {
            result.error("NO_PERMISSION", "Notification permission not granted", null)
            return
        }
        val title = map?.get("title") as? String ?: "Notification"
        val body = map?.get("body") as? String ?: ""
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "method_channel_notifications"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Flutter Notifications", NotificationManager.IMPORTANCE_DEFAULT)
            nm.createNotificationChannel(channel)
        }
        val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification.Builder(this, channelId)
        } else {
            @Suppress("DEPRECATION") Notification.Builder(this)
        }
        builder.setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.stat_notify_more)
            .setAutoCancel(true)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            builder.setBadgeIconType(Notification.BADGE_ICON_SMALL)
        }
        nm.notify(1, builder.build())
        result.success(true)
    }

    // ---------------- Permission callback ----------------

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            100 -> {
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                pendingPermissionResult?.let { result ->
                    if (granted) {
                        val cam = getSystemService(Context.CAMERA_SERVICE) as CameraManager
                        val flashId = cam.cameraIdList.firstOrNull { id ->
                            cam.getCameraCharacteristics(id).get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
                        }
                        if (flashId == null) result.error("NO_FLASH", "No flash camera found", null)
                        else setTorch(flashId, pendingTorchEnabled, result)
                    } else {
                        result.error("NO_PERMISSION", "Camera permission denied", null)
                    }
                }
                pendingPermissionResult = null
            }
            200 -> {
                val granted = grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                pendingPermissionResult?.success(granted)
                pendingPermissionResult = null
            }
        }
    }
}
