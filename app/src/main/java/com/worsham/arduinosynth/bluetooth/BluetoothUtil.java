package com.worsham.arduinosynth.bluetooth;

import java.util.UUID;

/**
 * Static set of utils for connecting over Bluetooth.
 */
public class BluetoothUtil
{
    // UUIDs for UART service and associated characteristics.
    public static UUID UART_UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID TX_UUID   = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E");
    public static UUID RX_UUID   = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E");

    // UUID for the UART BTLE client characteristic which is necessary for notifications.
    public static UUID CLIENT_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

    // UUIDs for the Device Information service and associated characeristics.
    public static UUID DIS_UUID       = UUID.fromString("0000180a-0000-1000-8000-00805f9b34fb");
    public static UUID DIS_MANUF_UUID = UUID.fromString("00002a29-0000-1000-8000-00805f9b34fb");
    public static UUID DIS_MODEL_UUID = UUID.fromString("00002a24-0000-1000-8000-00805f9b34fb");
    public static UUID DIS_HWREV_UUID = UUID.fromString("00002a26-0000-1000-8000-00805f9b34fb");
    public static UUID DIS_SWREV_UUID = UUID.fromString("00002a28-0000-1000-8000-00805f9b34fb");
}
