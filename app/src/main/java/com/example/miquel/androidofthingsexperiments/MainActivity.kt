package com.example.miquel.androidofthingsexperiments

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManagerService

import java.io.IOException

class MainActivity : AppCompatActivity() {
    private var buttonGpio: Gpio? = null

    // Step 4. Register an event callback.
    private val callback = object : GpioCallback() {
        override fun onGpioEdge(gpio: Gpio?): Boolean {
            Log.i(TAG, "GPIO changed, button pressed")
            // Step 5. Return true to keep callback active.
            return true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val service = PeripheralManagerService()
        Log.d(TAG, "Available GPIO: ${service.gpioList}")

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
        } catch (e: IOException) {
            Log.e(TAG, "Error on PeripheralIO API", e)
        }

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
    }

    companion object {
        private val TAG = "MainActivity"
        private val BUTTON_PIN_NAME = "BCM21"
    }
}
