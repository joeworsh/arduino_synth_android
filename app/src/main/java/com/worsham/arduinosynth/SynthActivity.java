package com.worsham.arduinosynth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.worsham.arduinosynth.bluetooth.BluetoothLeUart;

/**
 * Android activity to send Bluetooth UART synthesizer packets
 * to a Bluetooth Arduino Synthesizer.
 */
public class SynthActivity extends AppCompatActivity
{
    private final static String TAG = SynthActivity.class.getSimpleName();

    // keys for passing data into the activity
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    // the selected bluetooth device passed into this activity
    private String deviceName;
    private String deviceAddress;

    // the bluetooth connections to the BT device
    private BluetoothLeUart uart;

    /**
     * Called when the activity is initiated. Will consume the bluetooth device info that is
     * passed into the activity.
     * @param savedInstanceState the saved state
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_synth);

        // get access to the bluetooth device passed into the activity
        final Intent intent = getIntent();
        deviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        deviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);

        // put the name of the device into activity
        ((TextView) findViewById(R.id.device_name_view)).setText(deviceName);
    }

    /**
     * Called when the activity is left but not closed.
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        if (uart != null)
            uart.disconnect();
        uart = null;
    }

    /**
     * Called when the activity is returned to.
     */
    @Override
    protected void onResume()
    {
        super.onResume();

        // connect to the bluetooth service
        if (uart == null)
            uart = new BluetoothLeUart(getApplicationContext(), deviceAddress);
    }

    /**
     * Called when the activity is destroyed.
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        if (uart != null)
            uart.disconnect();
        uart = null;
    }

    /**
     * Called to the playtone of the associated button that has been clicked.
     * @param view the clicked view item
     */
    public void playTone(View view)
    {
        int id = view.getId();
        byte note = getNoteNumber(id);
        transmitTone(note);
    }

    /**
     * Transmit the given note in a byte message to the BT device
     * @param note the note to send
     */
    private void transmitTone(byte note)
    {
        // encode the packet with the note
        byte[] buffer = new byte[4];
        buffer[0] = '!';
        buffer[1] = 'N';
        buffer[2] = note;

        // the last byte in the packet is the checksum
        byte xsum = 0;
        for(int i = 0; i < buffer.length - 1; i++)
            xsum += buffer[i];
        xsum = (byte) ~xsum;
        buffer[3] = xsum;

        // send the packet to the UART connection
        uart.send(buffer);
    }

    /**
     * Get the synth service note number of the provided view ID that has been clicked.
     * @param viewId the item that has been clicked.
     * @return the note number to send to the device
     */
    private byte getNoteNumber(int viewId)
    {
        switch (viewId)
        {
            case R.id.middle_c_tone:
                return 1;
            case R.id.d_tone:
                return 2;
            case R.id.e_tone:
                return 3;
            case R.id.f_tone:
                return 4;
            case R.id.g_tone:
                return 5;
            case R.id.a_tone:
                return 6;
            case R.id.b_tone:
                return 7;
            case R.id.high_c_tone:
                return 8;
            default:
                return 0;
        }
    }
}
