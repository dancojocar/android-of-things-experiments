package com.example.miquel.androidofthingsexperiments

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.things.pio.*

import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var buttonGpio: Gpio? = null
    private var ledGpio: Gpio? = null

    // Step 4. Register an event callback.
    private val callback = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio?): Boolean {
            Log.i(TAG, "GPIO changed, button pressed")

            ledGpio?.apply {
                value = !value
            }

            writeUartData(uart, "T")
            Thread.sleep(500)
            readUartBuffer(uart)

            // Step 5. Return true to keep callback active.
            return true
        }
    }

    private val UART_DEVICE = "UART0"
    lateinit var uart: UartDevice

    fun readUartBuffer(uart: UartDevice) {
        // Maximum amount of data to read at one time
        val maxCount = 8
        val buffer = ByteArray(maxCount)
        do {
            val count = uart.read(buffer, buffer.size)
            Log.d(TAG, "Read $count : ${buffer.toReadableString()} from peripheral")
            if(count == 0) break
        } while (true)
    }

    fun ByteArray.toReadableString() = filter { it > 0.toByte() }
            .joinToString(separator = "") { it.toChar().toString() }

    fun writeUartData(uart: UartDevice, command: String) {
        val count = uart.write(command.toByteArray(), command.length)
        Log.d(TAG, "Wrote $command $count bytes to peripheral")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val service = PeripheralManagerService()
        Log.d(TAG, "Available GPIO: ${service.gpioList}")

        try {
            val deviceList = service.uartDeviceList
            if (deviceList.isEmpty()) {
                Log.i(TAG, "No UART port available on this device.")
            } else {
                Log.i(TAG, "List of available devices: " + deviceList)
            }
            uart = service.openUartDevice(UART_DEVICE)
            uart.setBaudrate(115200)
            uart.setDataSize(8)
            uart.setParity(UartDevice.PARITY_NONE)
            uart.setStopBits(1)
        } catch (e: IOException) {
            Log.w(TAG, "Unable to access UART device", e)
        }


        try {
            // Step 1. Create GPIO connection.
            buttonGpio = service.openGpio(BUTTON_PIN_NAME)
            buttonGpio?.apply {
                // Step 2. Configure as an input.
                setDirection(Gpio.DIRECTION_IN)
                // Step 3. Enable edge trigger events.
                setEdgeTriggerType(Gpio.EDGE_FALLING)
                // Step 4. Register an event callback.
                registerGpioCallback(callback)
            }

            // Step 1. Create GPIO connection.
            ledGpio = service.openGpio(LED_PIN_NAME)
            // Step 2. Configure as an output.
            ledGpio?.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e)
        }

    }

    val uartCallback = object : UartDeviceCallback() {
        override fun onUartDeviceError(uart: UartDevice?, error: Int) {
            Log.e(TAG, "UART device error: $error")
        }

        override fun onUartDeviceDataAvailable(uart: UartDevice?): Boolean {
            uart?.let { readUartBuffer(it) }
            return true
        }
    }

    override fun onStart() {
        super.onStart()
//        uart.registerUartDeviceCallback(uartCallback)
    }

    override fun onStop() {
        super.onStop()
//        uart.unregisterUartDeviceCallback(uartCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        // Step 6. Close the resource
        buttonGpio?.apply {
            unregisterGpioCallback(callback)
            try {
                close()
            } catch (e: IOException) {
                Log.e(TAG, "Error on PeripheralIO API", e)
            }
        }
        // Step 5. Close the resource.
        try {
            ledGpio?.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e)
        }

        try {
            uart.close()
        } catch (e: IOException) {
            Log.e(TAG, "Error on UART", e)
        }
    }

    companion object {
        private val TAG = "MainActivity"
        private val BUTTON_PIN_NAME = "BCM21"
        private val LED_PIN_NAME = "BCM6"
    }
}
