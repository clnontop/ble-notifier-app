# BLE Real-Time Notifier (Consent-Only)

## ⚠️ IMPORTANT LEGAL AND ETHICAL DISCLAIMER

This application is designed **STRICTLY FOR EDUCATIONAL PURPOSES** and must only be used with **EXPLICIT CONSENT** from all nearby device owners. 

### Legal Requirements
- **ONLY use with explicit consent** from all nearby device owners
- **COMPLY with all local laws** regarding radio transmission and device interference
- **RESPECT privacy** - this app does not collect personal data but may trigger notifications on other devices
- **USE RESPONSIBLY** - unauthorized use may violate local regulations

## Project Overview

The BLE Real-Time Notifier is an Android application that demonstrates Bluetooth Low Energy (BLE) advertising capabilities by broadcasting advertisements that can trigger pairing notifications on nearby BLE-capable devices.

### Key Features

- **Consent-First Design**: Mandatory consent dialog with legal disclaimers
- **BLE Advertising**: Broadcasts Fast Pair and standard BLE advertisements
- **Device Name Customization**: Allows custom device names with presets
- **Permission Management**: Handles all required Bluetooth permissions
- **Activity Logging**: Logs advertising events without collecting personal data
- **Safety Controls**: Built-in timeouts and error handling

## Technical Specifications

### Requirements
- **Android API Level**: 21+ (Android 5.0)
- **Target API**: 34 (Android 14)
- **Language**: Kotlin
- **Build System**: Gradle with Kotlin DSL

### Required Permissions
- `BLUETOOTH_ADVERTISE` (Android 12+)
- `BLUETOOTH_SCAN` (Android 12+)
- `BLUETOOTH_CONNECT` (Android 12+)
- `BLUETOOTH` (Legacy)
- `BLUETOOTH_ADMIN` (Legacy)
- `ACCESS_FINE_LOCATION` (Required for BLE operations)

### Key Components

1. **BleAdvertisingManager**: Handles BLE advertising with Fast Pair support
2. **ConsentManager**: Manages user consent with expiration
3. **PermissionManager**: Handles runtime permissions
4. **LoggingManager**: Logs events locally without personal data

## Setup Instructions

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK with API level 34
- Physical Android device with BLE support (emulator won't work for BLE advertising)

### Building the Project

1. **Clone/Download** the project to your local machine
2. **Open in Android Studio**
3. **Sync Gradle** files
4. **Build the project** using `Build > Make Project`
5. **Install on device** using `Run > Run 'app'`

### Testing Requirements

- **Physical Android device** with BLE advertising support
- **Android 5.0+** (API level 21 or higher)
- **Bluetooth enabled** on the device
- **Location permissions** granted (required for BLE)

## Usage Instructions

### First Launch
1. App will show **consent dialog** - read carefully and agree only if you understand the implications
2. **Grant permissions** when prompted (Bluetooth and Location)
3. **Enable Bluetooth** if not already enabled

### Broadcasting Advertisements
1. **Enter device name** or select from presets (AirPods Pro, Car Audio, etc.)
2. **Tap "Start Advertising"** to begin broadcasting
3. **Monitor status** indicator and logs
4. **Tap "Stop Advertising"** when finished

### Safety Features
- **Consent expires** after 30 days and must be renewed
- **Clear warnings** about legal and ethical use
- **No personal data collection** from nearby devices
- **Activity logging** for transparency

## Fast Pair Implementation

The app implements Google's Fast Pair specification to trigger pairing notifications:

### Service UUIDs Used
- **Fast Pair Service**: `0000FE2C-0000-1000-8000-00805F9B34FB`
- **Audio Service**: `0000110B-0000-1000-8000-00805F9B34FB`
- **HID Service**: `00001812-0000-1000-8000-00805F9B34FB`

### Model IDs
- Configurable model IDs for different device types
- Mimics legitimate device advertising patterns
- Triggers native pairing dialogs on compatible devices

## Privacy and Data Protection

### Data Collection
- **NO personal data** is collected from nearby devices
- **NO device identifiers** are stored
- **Only local activity logs** (start/stop times, device names used)

### Data Storage
- All data stored **locally on device only**
- **No cloud backup** of sensitive information
- **Logs can be cleared** by user at any time

## Legal Compliance

### Google Play Policy Compliance
- Clear consent mechanisms
- Educational purpose disclosure
- No malicious functionality
- Respects user privacy

### Regulatory Compliance
- Uses standard Bluetooth protocols
- No interference with critical systems
- Complies with radio transmission regulations
- Includes appropriate warnings and disclaimers

## Troubleshooting

### Common Issues

1. **"BLE advertising not supported"**
   - Device doesn't support BLE advertising
   - Try on a different Android device

2. **"Permission denied"**
   - Grant all Bluetooth and Location permissions
   - Check Android Settings > Apps > Permissions

3. **"Bluetooth not enabled"**
   - Enable Bluetooth in Android Settings
   - App will prompt to enable automatically

4. **No pairing notifications on nearby devices**
   - Ensure nearby devices have Bluetooth enabled
   - Some devices may not show notifications for all advertisement types
   - Try different device name presets

### Debug Information
- Check **View Logs** in the app for detailed activity
- Monitor **Android Studio Logcat** for technical details
- Verify **Bluetooth permissions** in Android Settings

## Development Notes

### Architecture
- **MVVM pattern** with ViewBinding
- **Separation of concerns** with dedicated managers
- **Error handling** throughout the application
- **Material Design 3** UI components

### Testing Considerations
- **Real device required** (BLE advertising doesn't work in emulator)
- **Multiple devices helpful** for testing pairing notifications
- **Various Android versions** for compatibility testing

## Contributing

This project is for educational purposes. If you make modifications:

1. **Maintain ethical guidelines** and consent mechanisms
2. **Ensure legal compliance** in your jurisdiction
3. **Test thoroughly** on real devices
4. **Document changes** clearly

## License and Disclaimer

This software is provided for **educational purposes only**. Users are **solely responsible** for ensuring compliance with all applicable laws and regulations. The developers assume **no liability** for misuse of this application.

**USE AT YOUR OWN RISK AND RESPONSIBILITY**

---

## Contact and Support

For technical questions or educational use cases, please refer to the Android BLE documentation and Fast Pair specification from Google.
