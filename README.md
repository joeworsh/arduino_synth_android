## ArduinoSynth for Android

ArduinoSynth Android App is a an AndroidApp to wirelessly play notes on an [ArduinoSynth](https://github.com/joeworsh/arduino_synth_board) device.

Minimum Android API level 23.

## Bluetooth

The app uses the Bluetooth LE Scanner to find the ArduinoSynth device. Currently all found devices are displayed and the user must select the ArduinoSynth device. Once selected, the app will connect over UART and allow the user to send synthesizer notes to the board. The app acts in the client role.