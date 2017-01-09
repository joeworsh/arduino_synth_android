package com.worsham.arduinosynth;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.worsham.arduinosynth.bluetooth.BluetoothLeUart;

/**
 * Android activity to send Bluetooth UART synthesizer packets
 * to a Bluetooth Arduino Synthesizer.
 */
public class SynthActivity extends Activity implements AdapterView.OnItemSelectedListener
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

        // load the available octaves into the device
        Spinner spinner = (Spinner) findViewById(R.id.octave_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.octave_spinner, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(4);
        spinner.setOnItemSelectedListener(this);
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
     * When an item in the octave spinner is selected, set the octave on the BT device.
     * @param parent
     * @param view
     * @param pos
     * @param id
     */
    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String octaveStr = (String) parent.getItemAtPosition(pos);
        byte newOctave = Byte.parseByte(octaveStr);
        transmitOctave(newOctave);
    }

    /**
     * Respond to nothing be selected in the spinner.
     * @param parent
     */
    public void onNothingSelected(AdapterView<?> parent) {
        // do nothing
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
        computeChecksum(buffer, 3);

        // send the packet to the UART connection
        uart.send(buffer);
    }

    /**
     * Transmit the specified octave in a byte message to the BT device.
     * @param octave the new octave of the synth
     */
    private void transmitOctave(byte octave)
    {
        // encode the packet with the octave
        byte[] buffer = new byte[4];
        buffer[0] = '!';
        buffer[1] = 'O';
        buffer[2] = octave;

        // the last byte in the packet is the checksum
        computeChecksum(buffer, 3);

        // send the packet to the UART connection
        uart.send(buffer);
    }

    /**
     * Compute the checksum of the buffer up to the specified position
     * and then put the checksum in the checksum position.
     * @param buffer the buffer to compute
     * @param checksumPosition the position of the checksum in the buffer
     */
    private void computeChecksum(byte[] buffer, int checksumPosition)
    {
        // the last byte in the packet is the checksum
        byte xsum = 0;
        for(int i = 0; i < checksumPosition; i++)
            xsum += buffer[i];
        xsum = (byte) ~xsum;
        buffer[checksumPosition] = xsum;
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
