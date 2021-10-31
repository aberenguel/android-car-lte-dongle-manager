# LTE Dongle Manager

This app has some features to handle LTE dongles in Android Car Head Units

## USB Reset

Executes `setprop sys.usb.config none` as root.

## USB_ModeSwitch

Executes the command `usb_modeswitch` with some hard-coded parametes. For now, you have to change them manually 
in the file [UsbModeSwitchManager.kt](src/main/java/org/berenguel/carheadunitconfigurer/managers/UsbModeSwitchManager.kt) and generate the APK.

## Huawei switchDebugMode

Access the address `http://192.168.8.1/html/switchDebugMode.html` in a WebView so that it can execute the JavaScript code provided by the LTE dongle.

## Wi-Fi Scan

Starts a Wi-Fi scan so that the application can see if selected APs are available. If so, the application shuts down the ethernet interface in order 
to establish the Wi-Fi connection. The parametes (interface name and selected APs) can be changed at Settings.
