# Smart Glove for Sign Language Translation Using IoT
### Complete Project Setup Guide

---

## Project Overview

A wearable smart glove with 5 ADXL335 accelerometers (one per finger) connected to
an Arduino UNO. When you make a hand gesture, the glove sends the matching phrase
over HC-05 Bluetooth to an Android app, which displays and speaks the text.

**Supported: 29 predefined phrases / gestures**

---

## Folder Structure

```
SmartGloveProject/
├── Arduino/
│   └── SmartGlove.ino          ← Upload this to Arduino UNO
└── AndroidApp/
    └── app/src/main/
        ├── AndroidManifest.xml
        ├── java/com/smartglove/app/
        │   ├── LoginActivity.java     ← Login screen
        │   └── MainActivity.java     ← Bluetooth + TTS screen
        └── res/
            ├── layout/
            │   ├── activity_login.xml
            │   └── activity_main.xml
            ├── values/styles.xml
            └── drawable/
                ├── bg_edittext.xml
                └── bg_status.xml
```

---

## Hardware Wiring

### ADXL335 → Arduino UNO (×5, one per finger)

| Finger | ADXL335 XOUT | Arduino Pin | ADXL335 VCC | ADXL335 GND |
|--------|-------------|-------------|-------------|-------------|
| Thumb  | XOUT        | A0          | 3.3V        | GND         |
| Index  | XOUT        | A1          | 3.3V        | GND         |
| Middle | XOUT        | A2          | 3.3V        | GND         |
| Ring   | XOUT        | A3          | 3.3V        | GND         |
| Pinky  | XOUT        | A4          | 3.3V        | GND         |

> ⚠️ Use 3.3V for ADXL335, NOT 5V — you will damage the sensor.

### HC-05 Bluetooth → Arduino UNO

| HC-05 Pin | Arduino Pin | Notes                                           |
|-----------|-------------|-------------------------------------------------|
| VCC       | 5V          |                                                 |
| GND       | GND         |                                                 |
| TX        | Pin 0 (RX)  | Direct connection                               |
| RX        | Pin 1 (TX)  | Via voltage divider: 1kΩ (to TX) + 2kΩ (to GND) |

> ⚠️ HC-05 RX tolerates only 3.3V. The voltage divider drops Arduino's 5V TX to ~3.3V.
> Without it you may damage the HC-05 over time.

### Voltage Divider for HC-05 RX
```
Arduino TX (5V) ──[ 1kΩ ]──┬── HC-05 RX
                            │
                          [ 2kΩ ]
                            │
                           GND
```

### Power
- Connect a **5V USB power bank** to the Arduino's USB port.
- The power bank powers both Arduino and HC-05.

---

## Step 1 — Upload Arduino Code

1. Open Arduino IDE.
2. Open `Arduino/SmartGlove.ino`.
3. **Disconnect HC-05 TX/RX wires** from pins 0 and 1 (required for upload).
4. Go to **Tools → Board → Arduino UNO**.
5. Go to **Tools → Port** → select your Arduino COM port.
6. Click **Upload** (→).
7. Once upload finishes, reconnect HC-05 wires.

### Calibrate sensor threshold (important!)
Before running the full code, test individual sensors:
1. Temporarily uncomment the `Serial.print` lines in `loop()`.
2. Open **Tools → Serial Monitor** at 9600 baud.
3. Bend each finger and note the values.
4. Straight finger → typical value 450–550. Bent finger → typical value 300–380.
5. Set threshold (currently 400) to midpoint of your values.
6. Re-comment the `Serial.print` lines when done.

---

## Step 2 — Pair HC-05 with Android Phone

1. Power on the glove (connect power bank).
2. HC-05 LED blinks **rapidly** = not paired.
3. On your Android phone: **Settings → Bluetooth → Scan**.
4. Tap **HC-05** in the available list.
5. Enter PIN: **1234** (or **0000** if 1234 fails).
6. HC-05 LED now blinks **slowly** (every ~2 seconds) = paired successfully.

---

## Step 3 — Build & Install the Android App

### Option A: Import into Android Studio (recommended)
1. Open Android Studio → **Open an existing project**.
2. Navigate to `SmartGloveProject/AndroidApp/` and click OK.
3. Wait for Gradle sync.
4. Connect your Android phone via USB (USB Debugging enabled).
5. Click **Run** (▶) → select your phone.

### Option B: Create a new project and copy files
1. Android Studio → New Project → Empty Activity.
2. Package name: `com.smartglove.app`, Language: Java, Min SDK: API 21.
3. Replace generated files with the files in this project:
   - `app/src/main/AndroidManifest.xml`
   - `app/src/main/java/com/smartglove/app/LoginActivity.java`
   - `app/src/main/java/com/smartglove/app/MainActivity.java`
   - All layout and drawable XML files.
4. Add CardView dependency in `app/build.gradle`:
   ```
   implementation 'androidx.cardview:cardview:1.0.0'
   ```
5. Sync Gradle and Run.

### Login credentials (demo)
- Email: `user@smartglove.com`
- Password: `1234`

---

## Step 4 — Run the Full System

1. Power on the glove.
2. Open **Smart Glove** app on your phone.
3. Log in with demo credentials.
4. Tap **Connect to Glove** → app will find HC-05 automatically.
5. Make a gesture (e.g. bend only your thumb).
6. The app displays the phrase and speaks it aloud.
7. Wait 3 seconds before the next gesture (delay is in Arduino code).

---

## Gesture Reference Table (all 29 gestures)

| # | T | I | M | R | P | Phrase |
|---|---|---|---|---|---|--------|
| 1 | B | S | S | S | S | Hi I am Alexa |
| 2 | S | B | S | S | S | Side please. I need to move |
| 3 | S | S | B | S | S | May i have some water |
| 4 | S | S | S | B | S | Can you help me for taking bus ticket |
| 5 | S | S | S | S | B | Thanks for helping me |
| 6 | B | B | S | S | S | What's your name? |
| 7 | B | S | B | S | S | How are you? |
| 8 | B | S | S | B | S | I'm fine |
| 9 | B | S | S | B | B | This my number 9-4-4-0-8-1-5-6-1-4...Text me |
| 10 | S | B | B | S | S | May I come in |
| 11 | S | B | S | B | S | Could you repeat that please |
| 12 | S | B | S | S | B | I can't hear |
| 13 | S | S | B | B | S | Can you help me for calling him |
| 14 | S | S | B | S | B | I want some tablets |
| 15 | B | B | B | S | S | Could you lend me your pen |
| 16 | S | B | B | B | S | My D.O.B is 22/05/1999 |
| 17 | S | S | B | B | B | okay |
| 18 | B | S | B | B | S | I'm Sorry |
| 19 | B | B | S | B | S | I'm Leaving bye |
| 20 | B | B | S | S | B | Could you please help me in solving this |
| 21 | S | B | B | S | B | I'm waiting for your message |
| 22 | B | S | S | B | B | I'm reached here before 1 hour |
| 23 | S | B | S | B | B | Excuse Me |
| 24 | B | B | B | B | S | Way to washroom please |
| 25 | S | B | B | B | B | My address is house number 111, second road, Chennai |
| 26 | B | S | B | B | B | I'm working as a software engineer in Dell |
| 27 | B | B | S | B | B | I'm feeling hungry |
| 28 | B | B | B | B | B | Glad to meet you |
| 29 | - | - | - | - | - | idle (any unrecognized pattern) |

**T=Thumb, I=Index, M=Middle, R=Ring, P=Pinky**
**B = Bent (reading < 400), S = Straight (reading > 400)**

---

## Troubleshooting

| Problem | Cause | Fix |
|---------|-------|-----|
| Wrong gesture output | Threshold mismatch | Calibrate — uncomment Serial.print lines, measure your values |
| HC-05 not found in app | Not paired | Pair in phone Settings → Bluetooth first |
| Upload fails | HC-05 connected during upload | Disconnect HC-05 TX/RX before uploading |
| No speech output | TTS not ready | Wait 2-3 seconds after app starts; TTS engine needs to initialise |
| Sensor reads 0 always | Wiring issue | Check VCC=3.3V (not 5V), GND connected, XOUT to correct analog pin |
| Bluetooth permission denied | Android 12+ | Go to App Settings → Permissions → Nearby Devices → Allow |

---

## Possible Enhancements (for better project marks)

1. Add ISL (Indian Sign Language) alphabets A–Z.
2. Reduce delay from 3000ms to 1500ms for faster response.
3. Add a 16×2 LCD display on the glove for offline text display.
4. Add a buzzer to beep when a gesture is recognized.
5. Add camera-based CNN recognition (see reference paper from BNMIT 2024).
6. Store gesture history in a ScrollView in the app.

---

*Project by — B.E. Computer Science, grahith rao*
*Smart Glove for Sign Language Translation 
