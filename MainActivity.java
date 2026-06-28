package com.smartglove.app;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

/**
 * MainActivity
 *
 * Connects to HC-05 Bluetooth module on the smart glove.
 * Reads text phrases ending with '#' delimiter.
 * Displays the phrase on screen and speaks it using Text-to-Speech.
 *
 * HC-05 uses Serial Port Profile (SPP) UUID:
 *   00001101-0000-1000-8000-00805F9B34FB
 */
public class MainActivity extends AppCompatActivity implements TextToSpeech.OnInitListener {

    // -----------------------------------------------------------------------
    // UI references
    // -----------------------------------------------------------------------
    private TextView tvStatus;      // connection status line
    private TextView tvOutput;      // received phrase display
    private TextView tvLastPhrase;  // small "last phrase" label
    private Button   btnConnect;    // connect / disconnect toggle
    private Button   btnSpeak;      // manually trigger TTS for last phrase

    // -----------------------------------------------------------------------
    // Bluetooth
    // -----------------------------------------------------------------------
    private BluetoothAdapter  mBluetoothAdapter;
    private BluetoothSocket   mBluetoothSocket;
    private InputStream       mInputStream;
    private ConnectedThread   mConnectedThread;

    // HC-05 device name — must match exactly what appears in your phone's paired list
    private static final String HC05_NAME = "HC-05";

    // SPP (Serial Port Profile) UUID — standard, never change this
    private static final UUID SPP_UUID =
            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // -----------------------------------------------------------------------
    // Text-to-Speech
    // -----------------------------------------------------------------------
    private TextToSpeech mTTS;
    private boolean      mTTSReady = false;
    private String       mLastPhrase = "";

    // -----------------------------------------------------------------------
    // Thread safety: post UI updates back to main thread
    // -----------------------------------------------------------------------
    private final Handler mHandler = new Handler(Looper.getMainLooper());

    // -----------------------------------------------------------------------
    // Permission request codes
    // -----------------------------------------------------------------------
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 101;
    private static final int REQUEST_ENABLE_BT             = 102;

    // -----------------------------------------------------------------------
    // Lifecycle
    // -----------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Bind views
        tvStatus     = findViewById(R.id.tvStatus);
        tvOutput     = findViewById(R.id.tvOutput);
        tvLastPhrase = findViewById(R.id.tvLastPhrase);
        btnConnect   = findViewById(R.id.btnConnect);
        btnSpeak     = findViewById(R.id.btnSpeak);

        // Init Text-to-Speech engine
        mTTS = new TextToSpeech(this, this);

        // Get Bluetooth adapter
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth not supported on this device",
                    Toast.LENGTH_LONG).show();
            btnConnect.setEnabled(false);
            return;
        }

        btnConnect.setOnClickListener(v -> {
            if (mConnectedThread != null && mConnectedThread.isAlive()) {
                disconnectBluetooth();
            } else {
                connectToHC05();
            }
        });

        btnSpeak.setOnClickListener(v -> {
            if (!mLastPhrase.isEmpty()) {
                speakOut(mLastPhrase);
            } else {
                Toast.makeText(this, "No phrase received yet", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnectBluetooth();
        if (mTTS != null) {
            mTTS.stop();
            mTTS.shutdown();
        }
    }

    // -----------------------------------------------------------------------
    // TextToSpeech.OnInitListener
    // -----------------------------------------------------------------------
    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = mTTS.setLanguage(Locale.ENGLISH);
            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Toast.makeText(this, "TTS language not supported", Toast.LENGTH_SHORT).show();
            } else {
                mTTSReady = true;
            }
        } else {
            Toast.makeText(this, "TTS initialisation failed", Toast.LENGTH_SHORT).show();
        }
    }

    // -----------------------------------------------------------------------
    // Bluetooth connect / disconnect
    // -----------------------------------------------------------------------
    private void connectToHC05() {
        // --- Permission check (Android 12+) ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{
                                Manifest.permission.BLUETOOTH_CONNECT,
                                Manifest.permission.BLUETOOTH_SCAN
                        },
                        REQUEST_BLUETOOTH_PERMISSIONS);
                return;
            }
        }

        // --- Enable Bluetooth if off ---
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            return;
        }

        // --- Find HC-05 in paired devices ---
        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        BluetoothDevice hc05Device = null;

        for (BluetoothDevice device : pairedDevices) {
            if (device.getName() != null && device.getName().equals(HC05_NAME)) {
                hc05Device = device;
                break;
            }
        }

        if (hc05Device == null) {
            Toast.makeText(this,
                    "HC-05 not found in paired devices.\n" +
                            "Go to phone Settings → Bluetooth → Pair 'HC-05' (PIN: 1234)",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // --- Connect in background thread ---
        final BluetoothDevice deviceToConnect = hc05Device;
        tvStatus.setText("Connecting to HC-05...");
        btnConnect.setEnabled(false);

        new Thread(() -> {
            try {
                BluetoothSocket socket = deviceToConnect.createRfcommSocketToServiceRecord(SPP_UUID);
                mBluetoothAdapter.cancelDiscovery(); // always cancel before connecting
                socket.connect();

                mBluetoothSocket = socket;
                mInputStream     = socket.getInputStream();

                mHandler.post(() -> {
                    tvStatus.setText("Connected to HC-05 ✓");
                    btnConnect.setText("Disconnect");
                    btnConnect.setEnabled(true);
                    Toast.makeText(MainActivity.this,
                            "Connected! Make a gesture with the glove.", Toast.LENGTH_SHORT).show();
                });

                // Start reading loop
                mConnectedThread = new ConnectedThread();
                mConnectedThread.start();

            } catch (IOException e) {
                mHandler.post(() -> {
                    tvStatus.setText("Connection failed");
                    btnConnect.setEnabled(true);
                    Toast.makeText(MainActivity.this,
                            "Could not connect: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private void disconnectBluetooth() {
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        try {
            if (mBluetoothSocket != null) {
                mBluetoothSocket.close();
                mBluetoothSocket = null;
            }
        } catch (IOException ignored) {}

        mHandler.post(() -> {
            tvStatus.setText("Disconnected");
            btnConnect.setText("Connect to Glove");
        });
    }

    // -----------------------------------------------------------------------
    // Background thread: reads bytes from HC-05 InputStream
    // Accumulates bytes until '#' delimiter, then extracts the phrase
    // -----------------------------------------------------------------------
    private class ConnectedThread extends Thread {
        private volatile boolean mRunning = true;

        @Override
        public void run() {
            StringBuilder buffer = new StringBuilder();
            byte[] bytes = new byte[1];

            while (mRunning) {
                try {
                    int bytesRead = mInputStream.read(bytes);
                    if (bytesRead > 0) {
                        String ch = new String(bytes, 0, bytesRead);

                        if (ch.equals("#")) {
                            // Full phrase received — clean and display
                            String phrase = buffer.toString().trim();
                            buffer.setLength(0); // clear buffer

                            if (!phrase.isEmpty() && !phrase.equals("idle")) {
                                mLastPhrase = phrase;
                                mHandler.post(() -> {
                                    tvOutput.setText(phrase);
                                    tvLastPhrase.setText("Last: " + phrase);
                                    speakOut(phrase);
                                });
                            }
                        } else {
                            buffer.append(ch);

                            // Safety: prevent buffer growing too large
                            if (buffer.length() > 300) {
                                buffer.setLength(0);
                            }
                        }
                    }
                } catch (IOException e) {
                    if (mRunning) {
                        mHandler.post(() -> {
                            tvStatus.setText("Connection lost");
                            btnConnect.setText("Connect to Glove");
                            Toast.makeText(MainActivity.this,
                                    "Connection lost. Please reconnect.", Toast.LENGTH_SHORT).show();
                        });
                    }
                    break;
                }
            }
        }

        void cancel() {
            mRunning = false;
            try {
                if (mInputStream != null) mInputStream.close();
            } catch (IOException ignored) {}
        }
    }

    // -----------------------------------------------------------------------
    // Text-to-Speech helper
    // -----------------------------------------------------------------------
    private void speakOut(String text) {
        if (mTTSReady) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null, "smartglove_utterance");
            } else {
                //noinspection deprecation
                mTTS.speak(text, TextToSpeech.QUEUE_FLUSH, null);
            }
        }
    }

    // -----------------------------------------------------------------------
    // Permission result
    // -----------------------------------------------------------------------
    @Override
    public void onRequestPermissionsResult(int requestCode,
            String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                connectToHC05(); // retry
            } else {
                Toast.makeText(this,
                        "Bluetooth permission denied. Cannot connect.",
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT && resultCode == RESULT_OK) {
            connectToHC05(); // Bluetooth now on, retry
        }
    }
}
