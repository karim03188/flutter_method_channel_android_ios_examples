import AVFoundation
import AudioToolbox
import Flutter
import UIKit
import UserNotifications

class SceneDelegate: FlutterSceneDelegate {
  private let channelName = "com.example.methodchannel_demo"

  override func scene(
    _ scene: UIScene,
    willConnectTo session: UISceneSession,
    options connectionOptions: UIScene.ConnectionOptions
  ) {
    super.scene(scene, willConnectTo: session, options: connectionOptions)

    guard let windowScene = scene as? UIWindowScene,
          let rootViewController = windowScene.windows.first?.rootViewController as? FlutterViewController else {
      return
    }

    registerMethodChannel(on: rootViewController.binaryMessenger)
  }

  private func registerMethodChannel(on messenger: FlutterBinaryMessenger) {
    let channel = FlutterMethodChannel(
      name: channelName,
      binaryMessenger: messenger
    )

    channel.setMethodCallHandler { [weak self] call, result in
      switch call.method {
      case "getBatteryLevel":
        UIDevice.current.isBatteryMonitoringEnabled = true
        let level = UIDevice.current.batteryLevel
        result(level >= 0 ? ["status": "success", "value": Int(level * 100)] : ["status": "unavailable", "value": -1])

      case "getDeviceInfo":
        let device = UIDevice.current
        result([
          "name": device.name,
          "model": device.model,
          "systemVersion": device.systemVersion,
          "identifierForVendor": device.identifierForVendor?.uuidString ?? "",
          "isPad": device.userInterfaceIdiom == .pad
        ])

      case "getLocale":
        result(Locale.current.identifier)

      case "triggerHapticFeedback":
        let generator = UINotificationFeedbackGenerator()
        generator.notificationOccurred(.success)
        result("success")

      case "showAlert":
        guard let args = call.arguments as? [String: Any] else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Missing arguments", details: nil))
          return
        }
        let title = args["title"] as? String ?? "Native Alert"
        let message = args["message"] as? String ?? "This is a native iOS alert."
        DispatchQueue.main.async { self?.presentAlert(title: title, message: message) }
        result("success")

      case "getScreenSize":
        let size = UIScreen.main.bounds.size
        result(["width": size.width, "height": size.height])

      case "getSystemVersion":
        result(UIDevice.current.systemVersion)

      case "getAppName":
        result(Bundle.main.infoDictionary?[kCFBundleNameKey as String] ?? "Unknown")

      case "getBundleId":
        result(Bundle.main.bundleIdentifier ?? "")

      case "getAppVersion":
        result(Bundle.main.infoDictionary?["CFBundleShortVersionString"] ?? "")

      case "getBuildNumber":
        result(Bundle.main.infoDictionary?["CFBundleVersion"] ?? "")

      case "getTimeZone":
        result(TimeZone.current.identifier)

      case "getCurrentTime":
        let formatter = DateFormatter(); formatter.dateStyle = .medium; formatter.timeStyle = .medium
        result(formatter.string(from: Date()))

      case "getDateFormat":
        result(DateFormatter.dateFormat(fromTemplate: "yyyyMMMdd", options: 0, locale: Locale.current) ?? "yyyy-MM-dd")

      case "getClipboardText":
        result(UIPasteboard.general.string ?? "")

      case "copyToClipboard":
        guard let args = call.arguments as? [String: Any], let text = args["text"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Missing text", details: nil)); return }
        UIPasteboard.general.string = text
        result("copied")

      case "shareText":
        guard let args = call.arguments as? [String: Any], let text = args["text"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Missing text", details: nil)); return }
        if let root = UIApplication.shared.connectedScenes.compactMap({ $0 as? UIWindowScene }).flatMap({ $0.windows }).first(where: { $0.isKeyWindow })?.rootViewController {
          let activity = UIActivityViewController(activityItems: [text], applicationActivities: nil)
          root.present(activity, animated: true)
        }
        result("shared")

      case "openUrl":
        guard let args = call.arguments as? [String: Any], let urlString = args["url"] as? String, let url = URL(string: urlString) else {
          result(FlutterError(code: "INVALID_URL", message: "Bad URL", details: nil)); return }
        UIApplication.shared.open(url)
        result("opened")

      case "openSettings":
        if let url = URL(string: UIApplication.openSettingsURLString) { UIApplication.shared.open(url) }
        result("opened")

      case "openMaps":
        guard let args = call.arguments as? [String: Any], let lat = args["latitude"] as? Double, let lon = args["longitude"] as? Double else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Latitude/Longitude missing", details: nil)); return }
        let url = URL(string: "http://maps.apple.com/?ll=\(lat),\(lon)")
        if let url = url { UIApplication.shared.open(url) }
        result("opened")

      case "openDialer":
        guard let args = call.arguments as? [String: Any], let phone = args["phone"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Phone missing", details: nil)); return }
        if let url = URL(string: "tel://\(phone)"), UIApplication.shared.canOpenURL(url) { UIApplication.shared.open(url) }
        result("opened")

      case "openMail":
        guard let args = call.arguments as? [String: Any], let to = args["to"] as? String else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Recipient missing", details: nil)); return }
        let subject = args["subject"] as? String ?? ""
        let encoded = "mailto:\(to)?subject=\(subject)".addingPercentEncoding(withAllowedCharacters: .urlQueryAllowed) ?? ""
        if let url = URL(string: encoded) { UIApplication.shared.open(url) }
        result("opened")

      case "getNetworkStatus":
        result(["connected": true, "type": "wifi"]) 

      case "isAirplaneModeEnabled":
        result(false)

      case "isCharging":
        result(UIDevice.current.batteryState == .charging || UIDevice.current.batteryState == .full)

      case "isDarkMode":
        result(UITraitCollection.current.userInterfaceStyle == .dark)

      case "isFaceIDAvailable":
        result(false)

      case "isTouchIDAvailable":
        result(true)

      case "isLowPowerModeEnabled":
        result(ProcessInfo.processInfo.isLowPowerModeEnabled)

      case "getProximityState":
        result(UIDevice.current.proximityState ?? false)

      case "getBrightness":
        result(UIScreen.main.brightness)

      case "setBrightness":
        guard let args = call.arguments as? [String: Any], let value = args["value"] as? Double else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Brightness value missing", details: nil)); return }
        UIScreen.main.brightness = CGFloat(value)
        result("updated")

      case "vibrateLight":
        let generator = UIImpactFeedbackGenerator(style: .light); generator.prepare(); generator.impactOccurred(); result("vibrated")

      case "vibrateMedium":
        let generator = UIImpactFeedbackGenerator(style: .medium); generator.prepare(); generator.impactOccurred(); result("vibrated")

      case "vibrateHeavy":
        let generator = UIImpactFeedbackGenerator(style: .heavy); generator.prepare(); generator.impactOccurred(); result("vibrated")

      case "playNotificationSound":
        AudioServicesPlaySystemSound(SystemSoundID(1002))
        result("played")

      case "playImpactSound":
        AudioServicesPlaySystemSound(SystemSoundID(1520))
        result("played")

      case "toggleTorch":
        let device = AVCaptureDevice.default(for: .video)
        let hasTorch = device?.hasTorch ?? false
        result(["available": hasTorch, "toggled": hasTorch])

      case "getTorchStatus":
        let device = AVCaptureDevice.default(for: .video)
        result(["available": device?.hasTorch ?? false, "enabled": false])

      case "getDeviceId":
        result(UIDevice.current.identifierForVendor?.uuidString ?? "")

      case "getVendorId":
        result(UIDevice.current.identifierForVendor?.uuidString ?? "")

      case "getDiskUsage":
        let attrs = try? FileManager.default.attributesOfFileSystem(forPath: NSHomeDirectory())
        let total = attrs?[.systemSize] as? Double ?? 0
        result(["total": Int(total)])

      case "getFreeStorage":
        let attrs = try? FileManager.default.attributesOfFileSystem(forPath: NSHomeDirectory())
        let free = attrs?[.systemFreeSize] as? Double ?? 0
        result(["free": Int(free)])

      case "getMemoryUsage":
        let total = ProcessInfo.processInfo.physicalMemory
        result(["total": Int(total), "used": 0])

      case "getCPUUsage":
        result(["usage": 22.5])

      case "getUptime":
        result(ProcessInfo.processInfo.systemUptime)

      case "getAvailableLocales":
        result(Locale.availableIdentifiers)

      case "getStatusBarHeight":
        result(UIApplication.shared.statusBarFrame.height)

      case "getKeyboardHeight":
        result(0.0)

      case "getBadgeCount":
        result(0)

      case "setBadge":
        guard let args = call.arguments as? [String: Any], let value = args["value"] as? Int else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Badge value missing", details: nil)); return }
        UIApplication.shared.applicationIconBadgeNumber = value
        result("updated")

      case "clearBadge":
        UIApplication.shared.applicationIconBadgeNumber = 0
        result("cleared")

      case "isSimulator":
        #if targetEnvironment(simulator)
        result(true)
        #else
        result(false)
        #endif

      case "getOrientation":
        let orientation = UIDevice.current.orientation
        result(orientation == .portrait ? "portrait" : orientation == .landscapeLeft ? "landscape" : "unknown")

      case "getLanguageCode":
        result(Locale.current.languageCode ?? "en")

      case "getCountryCode":
        result(Locale.current.regionCode ?? "US")

      case "generateUuid":
        result(UUID().uuidString)

      case "generateRandomNumber":
        guard let args = call.arguments as? [String: Any], let min = args["min"] as? Int, let max = args["max"] as? Int else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Min/max missing", details: nil)); return }
        let value = Int.random(in: min...max)
        result(value)

      case "showActionSheet":
        guard let args = call.arguments as? [String: Any] else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Missing args", details: nil)); return }
        let title = args["title"] as? String ?? "Action"
        let message = args["message"] as? String ?? ""
        DispatchQueue.main.async {
          guard let root = UIApplication.shared.connectedScenes.compactMap({ $0 as? UIWindowScene }).flatMap({ $0.windows }).first(where: { $0.isKeyWindow })?.rootViewController else { return }
          let alert = UIAlertController(title: title, message: message, preferredStyle: .actionSheet)
          alert.addAction(UIAlertAction(title: "OK", style: .default))
          alert.addAction(UIAlertAction(title: "Cancel", style: .cancel))
          root.present(alert, animated: true)
        }
        result("opened")

      case "scheduleLocalNotification":
        guard let args = call.arguments as? [String: Any] else {
          result(FlutterError(code: "INVALID_ARGUMENTS", message: "Missing args", details: nil)); return }
        let title = args["title"] as? String ?? "Notification"
        let body = args["body"] as? String ?? ""
        let content = UNMutableNotificationContent(); content.title = title; content.body = body; content.sound = .default
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: 1, repeats: false)
        let request = UNNotificationRequest(identifier: UUID().uuidString, content: content, trigger: trigger)
        UNUserNotificationCenter.current().add(request)
        result("scheduled")

      case "getNotificationPermissionStatus":
        result("not_determined")

      case "requestNotificationPermission":
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { _, _ in }
        result("requested")

      case "toggleSound":
        result("toggled")

      case "getVolumeLevel":
        result(0.5)

      default:
        result(FlutterMethodNotImplemented)
      }
    }
  }

  private func presentAlert(title: String, message: String) {
    guard let root = UIApplication.shared.connectedScenes
      .compactMap({ $0 as? UIWindowScene })
      .flatMap({ $0.windows })
      .first(where: { $0.isKeyWindow })?.rootViewController else {
      return
    }

    let alert = UIAlertController(
      title: title,
      message: message,
      preferredStyle: .alert
    )

    alert.addAction(UIAlertAction(title: "OK", style: .default))
    root.present(alert, animated: true)
  }
}
