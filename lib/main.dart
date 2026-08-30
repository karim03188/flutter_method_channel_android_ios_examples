import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(const MethodChannelDemoApp());
}

class MethodChannelDemoApp extends StatelessWidget {
  const MethodChannelDemoApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'MethodChannel Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.blue),
        useMaterial3: true,
      ),
      home: const MethodChannelHomePage(),
    );
  }
}

class DemoItem {
  const DemoItem({
    required this.title,
    required this.description,
    required this.method,
    this.arguments,
  });

  final String title;
  final String description;
  final String method;
  final Map<String, dynamic>? arguments;
}

class MethodChannelHomePage extends StatefulWidget {
  const MethodChannelHomePage({super.key});

  @override
  State<MethodChannelHomePage> createState() => _MethodChannelHomePageState();
}

class _MethodChannelHomePageState extends State<MethodChannelHomePage> {
  static const MethodChannel _channel = MethodChannel('com.example.methodchannel_demo');

  final List<DemoItem> _items = const [
    DemoItem(title: 'Battery level', description: 'Read iPhone battery percentage.', method: 'getBatteryLevel'),
    DemoItem(title: 'Device info', description: 'Read model, OS, and vendor info.', method: 'getDeviceInfo'),
    DemoItem(title: 'Current locale', description: 'Read active device locale.', method: 'getLocale'),
    DemoItem(title: 'Native alert', description: 'Show an iOS alert dialog.', method: 'showAlert', arguments: {'title': 'Flutter -> iOS', 'message': 'This alert came from native code.'}),
    DemoItem(title: 'Haptic feedback', description: 'Trigger success vibration.', method: 'triggerHapticFeedback'),
    DemoItem(title: 'Screen size', description: 'Read screen bounds.', method: 'getScreenSize'),
    DemoItem(title: 'System version', description: 'Read iOS version.', method: 'getSystemVersion'),
    DemoItem(title: 'App name', description: 'Read app display name.', method: 'getAppName'),
    DemoItem(title: 'Bundle ID', description: 'Read app bundle identifier.', method: 'getBundleId'),
    DemoItem(title: 'App version', description: 'Read app version number.', method: 'getAppVersion'),
    DemoItem(title: 'Build number', description: 'Read app build version.', method: 'getBuildNumber'),
    DemoItem(title: 'Time zone', description: 'Read current timezone.', method: 'getTimeZone'),
    DemoItem(title: 'Current time', description: 'Read iOS system time.', method: 'getCurrentTime'),
    DemoItem(title: 'Date format', description: 'Read locale date format.', method: 'getDateFormat'),
    DemoItem(title: 'Clipboard text', description: 'Read clipboard content.', method: 'getClipboardText'),
    DemoItem(title: 'Copy to clipboard', description: 'Copy text to native clipboard.', method: 'copyToClipboard', arguments: {'text': 'Hello from Flutter'}),
    DemoItem(title: 'Share sheet', description: 'Share text through iOS share sheet.', method: 'shareText', arguments: {'text': 'Hello from MethodChannel'}),
    DemoItem(title: 'Open URL', description: 'Open a URL in Safari.', method: 'openUrl', arguments: {'url': 'https://flutter.dev'}),
    DemoItem(title: 'Open settings', description: 'Open iPhone Settings app.', method: 'openSettings'),
    DemoItem(title: 'Open maps', description: 'Open Apple Maps with coordinates.', method: 'openMaps', arguments: {'latitude': 35.6895, 'longitude': 51.3890}),
    DemoItem(title: 'Call phone', description: 'Open dialer.', method: 'openDialer', arguments: {'phone': '+123456789'}),
    DemoItem(title: 'Send email', description: 'Open mail composer.', method: 'openMail', arguments: {'to': 'hello@example.com', 'subject': 'MethodChannel test'}),
    DemoItem(title: 'Network status', description: 'Check if device is connected.', method: 'getNetworkStatus'),
    DemoItem(title: 'Airplane mode', description: 'Check airplane mode status.', method: 'isAirplaneModeEnabled'),
    DemoItem(title: 'Charging state', description: 'Check if battery is charging.', method: 'isCharging'),
    DemoItem(title: 'Dark mode', description: 'Check if dark mode is active.', method: 'isDarkMode'),
    DemoItem(title: 'Face ID available', description: 'Check biometric support.', method: 'isFaceIDAvailable'),
    DemoItem(title: 'Touch ID available', description: 'Check Touch ID support.', method: 'isTouchIDAvailable'),
    DemoItem(title: 'Low power mode', description: 'Check battery saver state.', method: 'isLowPowerModeEnabled'),
    DemoItem(title: 'Proximity sensor', description: 'Check proximity sensor.', method: 'getProximityState'),
    DemoItem(title: 'Brightness', description: 'Read screen brightness.', method: 'getBrightness'),
    DemoItem(title: 'Set brightness', description: 'Set screen brightness.', method: 'setBrightness', arguments: {'value': 0.7}),
    DemoItem(title: 'Vibration light', description: 'Trigger light haptic vibration.', method: 'vibrateLight'),
    DemoItem(title: 'Vibration medium', description: 'Trigger medium vibration.', method: 'vibrateMedium'),
    DemoItem(title: 'Vibration heavy', description: 'Trigger heavy vibration.', method: 'vibrateHeavy'),
    DemoItem(title: 'Notification sound', description: 'Play system notification sound.', method: 'playNotificationSound'),
    DemoItem(title: 'Impact sound', description: 'Play impact feedback sound.', method: 'playImpactSound'),
    DemoItem(title: 'Flashlight toggle', description: 'Toggle torch on/off.', method: 'toggleTorch', arguments: {'enabled': true}),
    DemoItem(title: 'Torch status', description: 'Read flashlight state.', method: 'getTorchStatus'),
    DemoItem(title: 'Device ID', description: 'Read device identifier.', method: 'getDeviceId'),
    DemoItem(title: 'Vendor ID', description: 'Read vendor identifier.', method: 'getVendorId'),
    DemoItem(title: 'Disk usage', description: 'Read total disk space.', method: 'getDiskUsage'),
    DemoItem(title: 'Free storage', description: 'Read free storage space.', method: 'getFreeStorage'),
    DemoItem(title: 'Memory usage', description: 'Read memory information.', method: 'getMemoryUsage'),
    DemoItem(title: 'CPU usage', description: 'Read CPU usage metric.', method: 'getCPUUsage'),
    DemoItem(title: 'Uptime', description: 'Read device uptime.', method: 'getUptime'),
    DemoItem(title: 'Locale list', description: 'Read available locale identifiers.', method: 'getAvailableLocales'),
    DemoItem(title: 'Status bar height', description: 'Read iOS status bar height.', method: 'getStatusBarHeight'),
    DemoItem(title: 'Keyboard height', description: 'Read keyboard height estimate.', method: 'getKeyboardHeight'),
    DemoItem(title: 'Badge count', description: 'Read app badge count.', method: 'getBadgeCount'),
    DemoItem(title: 'Set badge', description: 'Set application badge number.', method: 'setBadge', arguments: {'value': 7}),
    DemoItem(title: 'Clear badge', description: 'Clear app badge value.', method: 'clearBadge'),
    DemoItem(title: 'Is simulator', description: 'Check if running on simulator.', method: 'isSimulator'),
    DemoItem(title: 'Orientation', description: 'Read current device orientation.', method: 'getOrientation'),
    DemoItem(title: 'Locale language', description: 'Read current language code.', method: 'getLanguageCode'),
    DemoItem(title: 'Country code', description: 'Read current country code.', method: 'getCountryCode'),
    DemoItem(title: 'UUID', description: 'Generate session UUID.', method: 'generateUuid'),
    DemoItem(title: 'Random number', description: 'Generate random number.', method: 'generateRandomNumber', arguments: {'min': 1, 'max': 100}),
    DemoItem(title: 'Action sheet', description: 'Open a native action sheet.', method: 'showActionSheet', arguments: {'title': 'Choose an action', 'message': 'Native action sheet'}),
    DemoItem(title: 'Local notification', description: 'Schedule a native notification.', method: 'scheduleLocalNotification', arguments: {'title': 'Flutter Notification', 'body': 'This notification came from native code'}),
    DemoItem(title: 'Permissions status', description: 'Check notification permission.', method: 'getNotificationPermissionStatus'),
    DemoItem(title: 'Request permission', description: 'Request notification permission.', method: 'requestNotificationPermission'),
    DemoItem(title: 'Sound toggle', description: 'Toggle sound setting placeholder.', method: 'toggleSound'),
    DemoItem(title: 'Volume level', description: 'Read volume level placeholder.', method: 'getVolumeLevel'),
  ];

  String _status = 'Tap any example to run native iOS code.';

  String _formatResult(dynamic result) {
    if (result is Map) {
      return result.entries.map((entry) => '${entry.key}: ${entry.value}').join('\n');
    }

    return result.toString();
  }

  Future<void> _runExample(DemoItem item) async {
    try {
      final dynamic result = await _channel.invokeMethod(
        item.method,
        item.arguments,
      );

      setState(() {
        _status = _formatResult(result);
      });
    } on PlatformException catch (e) {
      setState(() {
        _status = 'PlatformException: ${e.message}';
      });
    } catch (e) {
      setState(() {
        _status = 'Error: $e';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('iOS MethodChannel Demo'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            const Text(
              'A real-world pattern in mobile apps: Flutter requests native iOS features through one channel.',
              style: TextStyle(fontSize: 16),
            ),
            const SizedBox(height: 16),
            Expanded(
              child: ListView.builder(
                itemCount: _items.length,
                itemBuilder: (context, index) {
                  final item = _items[index];

                  return Card(
                    margin: const EdgeInsets.only(bottom: 12),
                    child: Padding(
                      padding: const EdgeInsets.all(12),
                      child: Row(
                        crossAxisAlignment: CrossAxisAlignment.center,
                        children: [
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  item.title,
                                  style: const TextStyle(
                                    fontSize: 18,
                                    fontWeight: FontWeight.w600,
                                  ),
                                ),
                                const SizedBox(height: 6),
                                Text(item.description),
                              ],
                            ),
                          ),
                          const SizedBox(width: 12),
                          ElevatedButton(
                            onPressed: () => _runExample(item),
                            child: const Text('Run'),
                          ),
                        ],
                      ),
                    ),
                  );
                },
              ),
            ),
            const SizedBox(height: 12),
            Container(
              width: double.infinity,
              padding: const EdgeInsets.all(16),
              decoration: BoxDecoration(
                color: Colors.blue.shade50,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.blue.shade200),
              ),
              child: Text(
                _status,
                style: const TextStyle(
                  fontFamily: 'monospace',
                  fontSize: 14,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
