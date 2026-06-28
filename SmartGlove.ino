// ============================================================
//  SMART GLOVE FOR SIGN LANGUAGE TRANSLATION USING IoT
//  Arduino UNO + 5x ADXL335 Accelerometers + HC-05 Bluetooth
//
//  Wiring:
//    Thumb  → A0    Index  → A1    Middle → A2
//    Ring   → A3    Pinky  → A4
//    HC-05 TX → Pin 0 (Arduino RX)
//    HC-05 RX → Pin 1 (Arduino TX) via voltage divider
//    All ADXL335 VCC → 3.3V,  GND → GND
//
//  Threshold: reading < 400 = finger BENT
//             reading > 400 = finger STRAIGHT
//
//  Each phrase ends with '#' so the Android app
//  knows where the message ends.
// ============================================================

// --- Sensor pins ---
int sensorPin1 = A0;   // Thumb
int sensorPin2 = A1;   // Index
int sensorPin3 = A2;   // Middle
int sensorPin4 = A3;   // Ring
int sensorPin5 = A4;   // Pinky

// --- Sensor readings ---
int a = 0;  // Thumb
int b = 0;  // Index
int c = 0;  // Middle
int d = 0;  // Ring
int e = 0;  // Pinky

void setup() {
  pinMode(sensorPin1, INPUT);
  pinMode(sensorPin2, INPUT);
  pinMode(sensorPin3, INPUT);
  pinMode(sensorPin4, INPUT);
  pinMode(sensorPin5, INPUT);

  // HC-05 default baud rate
  Serial.begin(9600);
}

void loop() {
  // Read all 5 sensors
  a = analogRead(sensorPin1);  // Thumb
  b = analogRead(sensorPin2);  // Index
  c = analogRead(sensorPin3);  // Middle
  d = analogRead(sensorPin4);  // Ring
  e = analogRead(sensorPin5);  // Pinky

  // --------------------------------------------------------
  // UNCOMMENT BELOW DURING CALIBRATION to see sensor values:
  // Serial.print("a:"); Serial.print(a);
  // Serial.print(" b:"); Serial.print(b);
  // Serial.print(" c:"); Serial.print(c);
  // Serial.print(" d:"); Serial.print(d);
  // Serial.print(" e:"); Serial.println(e);
  // --------------------------------------------------------

  // ============================================================
  //  29 GESTURE CONDITIONS
  //  Pattern format: T=Thumb  I=Index  M=Middle  R=Ring  P=Pinky
  //  B = bent (<400)   S = straight (>400)
  // ============================================================

  // Gesture 1:  T=B  I=S  M=S  R=S  P=S  → only thumb bent
  if (a < 400 && b > 400 && c > 400 && d > 400 && e > 400) {
    Serial.println("Hi I am Alexa #");
  }

  // Gesture 2:  T=S  I=B  M=S  R=S  P=S  → only index bent
  else if (a > 400 && b < 400 && c > 400 && d > 400 && e > 400) {
    Serial.println("Side please. I need to move#");
  }

  // Gesture 3:  T=S  I=S  M=B  R=S  P=S  → only middle bent
  else if (a > 400 && b > 400 && c < 400 && d > 400 && e > 400) {
    Serial.println("May i have some water#");
  }

  // Gesture 4:  T=S  I=S  M=S  R=B  P=S  → only ring bent
  else if (a > 400 && b > 400 && c > 400 && d < 400 && e > 400) {
    Serial.println("Can you help me for taking bus ticket#");
  }

  // Gesture 5:  T=S  I=S  M=S  R=S  P=B  → only pinky bent
  else if (a > 400 && b > 400 && c > 400 && d > 400 && e < 400) {
    Serial.println("Thanks for helping me#");
  }

  // Gesture 6:  T=B  I=B  M=S  R=S  P=S
  else if (a < 400 && b < 400 && c > 400 && d > 400 && e > 400) {
    Serial.println("What's your name?#");
  }

  // Gesture 7:  T=B  I=S  M=B  R=S  P=S
  else if (a < 400 && b > 400 && c < 400 && d > 400 && e > 400) {
    Serial.println("How are you?#");
  }

  // Gesture 8:  T=B  I=S  M=S  R=B  P=S
  else if (a < 400 && b > 400 && c > 400 && d < 400 && e > 400) {
    Serial.println("I'm fine#");
  }

  // Gesture 9:  T=B  I=S  M=S  R=B  P=B
  else if (a < 400 && b > 400 && c > 400 && d < 400 && e < 400) {
    Serial.println("This my number 9-4-4-0-8-1-5-6-1-4...Text me#");
  }

  // Gesture 10: T=S  I=B  M=B  R=S  P=S
  else if (a > 400 && b < 400 && c < 400 && d > 400 && e > 400) {
    Serial.println("May I come in#");
  }

  // Gesture 11: T=S  I=B  M=S  R=B  P=S
  else if (a > 400 && b < 400 && c > 400 && d < 400 && e > 400) {
    Serial.println("Could you repeat that please#");
  }

  // Gesture 12: T=S  I=B  M=S  R=S  P=B
  else if (a > 400 && b < 400 && c > 400 && d > 400 && e < 400) {
    Serial.println("I can't hear#");
  }

  // Gesture 13: T=S  I=S  M=B  R=B  P=S
  else if (a > 400 && b > 400 && c < 400 && d < 400 && e > 400) {
    Serial.println("Can you help me for calling him#");
  }

  // Gesture 14: T=S  I=S  M=B  R=S  P=B
  else if (a > 400 && b > 400 && c < 400 && d > 400 && e < 400) {
    Serial.println("I want some tablets#");
  }

  // Gesture 15: T=B  I=B  M=B  R=S  P=S
  else if (a < 400 && b < 400 && c < 400 && d > 400 && e > 400) {
    Serial.println("Could you lend me your pen#");
  }

  // Gesture 16: T=S  I=B  M=B  R=B  P=S
  else if (a > 400 && b < 400 && c < 400 && d < 400 && e > 400) {
    Serial.println("My D.O.B is 22/05/1999#");
  }

  // Gesture 17: T=S  I=S  M=B  R=B  P=B
  else if (a > 400 && b > 400 && c < 400 && d < 400 && e < 400) {
    Serial.println("okay#");
  }

  // Gesture 18: T=B  I=S  M=B  R=B  P=S
  else if (a < 400 && b > 400 && c < 400 && d < 400 && e > 400) {
    Serial.println("I'm Sorry#");
  }

  // Gesture 19: T=B  I=B  M=S  R=B  P=S
  else if (a < 400 && b < 400 && c > 400 && d < 400 && e > 400) {
    Serial.println("I'm Leaving bye#");
  }

  // Gesture 20: T=B  I=B  M=S  R=S  P=B
  else if (a < 400 && b < 400 && c > 400 && d > 400 && e < 400) {
    Serial.println("Could you please help me in solving this#");
  }

  // Gesture 21: T=S  I=B  M=B  R=S  P=B
  else if (a > 400 && b < 400 && c < 400 && d > 400 && e < 400) {
    Serial.println("I'm waiting for your message#");
  }

  // Gesture 22: T=B  I=S  M=S  R=B  P=B  (was: a<400 b>400 c>400 d<400 e<400 — duplicate of Gesture 9, fixed)
  else if (a < 400 && b > 400 && c > 400 && d < 400 && e < 400) {
    Serial.println("I'm reached here before 1 hour#");
  }

  // Gesture 23: T=S  I=B  M=S  R=B  P=B
  else if (a > 400 && b < 400 && c > 400 && d < 400 && e < 400) {
    Serial.println("Excuse Me #");
  }

  // Gesture 24: T=B  I=B  M=B  R=B  P=S
  else if (a < 400 && b < 400 && c < 400 && d < 400 && e > 400) {
    Serial.println("Way to washroom please... #");
  }

  // Gesture 25: T=S  I=B  M=B  R=B  P=B
  else if (a > 400 && b < 400 && c < 400 && d < 400 && e < 400) {
    Serial.println("My address is house number 111, second road, Chennai #");
  }

  // Gesture 26: T=B  I=S  M=B  R=B  P=B
  else if (a < 400 && b > 400 && c < 400 && d < 400 && e < 400) {
    Serial.println("I'm working as a software engineer in Dell#");
  }

  // Gesture 27: T=B  I=B  M=S  R=B  P=B
  else if (a < 400 && b < 400 && c > 400 && d < 400 && e < 400) {
    Serial.println("I'm feeling hungry#");
  }

  // Gesture 28: T=B  I=B  M=B  R=B  P=B  → all fingers bent (fist)
  else if (a < 400 && b < 400 && c < 400 && d < 400 && e < 400) {
    Serial.println("Glad to meet you#");
  }

  // Gesture 29 / Default: all fingers straight or unrecognized pattern
  else {
    Serial.println("idle#");
  }

  // Wait 3 seconds before next reading
  // Reduce to 1500 for faster response if needed
  delay(3000);
}
