# LTE Dongle Manager

This app has some features to handle LTE dongles in Android Car Head Units.

## Features

### USB Reset ([UsbResetManager.kt](app/src/main/java/org/berenguel/carheadunitconfigurer/managers/UsbResetManager.kt))
Executes `setprop sys.usb.config none` as root.

### USB_ModeSwitch ([UsbModeSwitchManager.kt](app/src/main/java/org/berenguel/carheadunitconfigurer/managers/UsbModeSwitchManager.kt))
Executes the command `usb_modeswitch` with some hard-coded parameters. For now, you have to change them manually
in the file `UsbModeSwitchManager.kt` and generate the APK.

### Huawei switchDebugMode ([HuaweiSwitchDebugModeManager.kt](app/src/main/java/org/berenguel/carheadunitconfigurer/managers/HuaweiSwitchDebugModeManager.kt))
Access the address `http://192.168.8.1/html/switchDebugMode.html` in a WebView so that it can execute the JavaScript code provided by the LTE dongle.

### Wi-Fi Scan ([EthernetWifiManager.kt](app/src/main/java/org/berenguel/carheadunitconfigurer/managers/EthernetWifiManager.kt))
Starts a Wi-Fi scan so that the application can see if prioritized APs are available. If so, the application shuts down the ethernet interface in order 
to establish the Wi-Fi connection. The parameters (interface name and selected APs) can be changed at Settings.

### Ethernet / Wi-Fi Switch ([EthernetWifiManager.kt](app/src/main/java/org/berenguel/carheadunitconfigurer/managers/EthernetWifiManager.kt))
If there is a prioritized Access Point available, the app shuts down the ethernet interface in order to establish the Wi-Fi connection.
If a Wi-Fi connection is no more available, the app turns the ethernet interface on again.

Some events trigger the Ethernet / Wi-Fi switch procedure:
- Application starting (at the boot)
- Screen turns on (after standby)
- Wi-Fi disconnection
- Bluetooth connection
- Button "Wi-Fi Scan" at the main screen
