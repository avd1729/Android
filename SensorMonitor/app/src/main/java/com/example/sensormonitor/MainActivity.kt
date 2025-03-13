package com.example.sensormonitor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    private var currentSensor: Sensor? = null
    private var currentSensorType = Sensor.TYPE_ACCELEROMETER

    private val maxEntries = 100
    private var timeIndex = 0f

    private val entries1 = mutableStateListOf<Entry>()
    private val entries2 = mutableStateListOf<Entry>()
    private val entries3 = mutableStateListOf<Entry>()

    private var xValue by mutableStateOf("X: 0.0")
    private var yValue by mutableStateOf("Y: 0.0")
    private var zValue by mutableStateOf("Z: 0.0")
    private var sensorName by mutableStateOf("Sensor Name")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager

        setContent {
            val lifecycleOwner = LocalLifecycleOwner.current

            DisposableEffect(lifecycleOwner) {
                val observer = LifecycleEventObserver { _, event ->
                    when (event) {
                        Lifecycle.Event.ON_RESUME -> registerSensor()
                        Lifecycle.Event.ON_PAUSE -> unregisterSensor()
                        else -> {}
                    }
                }
                lifecycleOwner.lifecycle.addObserver(observer)
                onDispose {
                    lifecycleOwner.lifecycle.removeObserver(observer)
                    unregisterSensor()
                }
            }

            SensorMonitorApp()
        }
    }

    @Composable
    fun SensorMonitorApp() {
        MaterialTheme {
            Surface(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Sensor Monitor",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )

                    SensorSelector()

                    Text(text = sensorName, fontSize = 16.sp, modifier = Modifier.padding(8.dp))

                    SensorChart()

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth().padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Text(text = xValue, color = Color.Red, modifier = Modifier.weight(1f))
                        Text(text = yValue, color = Color.Green, modifier = Modifier.weight(1f))
                        if (currentSensorType != Sensor.TYPE_LIGHT) {
                            Text(text = zValue, color = Color.Blue, modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun SensorSelector() {
        val sensorOptions = listOf(
            "Accelerometer" to Sensor.TYPE_ACCELEROMETER,
            "Gyroscope" to Sensor.TYPE_GYROSCOPE,
            "Light Sensor" to Sensor.TYPE_LIGHT
        )
        var expanded by remember { mutableStateOf(false) }
        var selectedOptionText by remember { mutableStateOf(sensorOptions[0].first) }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.surface)
                .clickable { expanded = true }
                .padding(12.dp)
        ) {
            Text(selectedOptionText)
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            sensorOptions.forEach { (text, type) ->
                DropdownMenuItem(
                    text = { Text(text) },
                    onClick = {
                        selectedOptionText = text
                        expanded = false
                        currentSensorType = type
                        registerSensor()
                    }
                )
            }
        }
    }

    @Composable
    fun SensorChart() {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.White),
            factory = { context ->
                LineChart(context).apply {
                    description.isEnabled = false
                    data = LineData()
                }
            },
            update = { chart ->
                chart.data = LineData(
                    LineDataSet(entries1, "X").apply { color = android.graphics.Color.RED },
                    LineDataSet(entries2, "Y").apply { color = android.graphics.Color.GREEN },
                    LineDataSet(entries3, "Z").apply { color = android.graphics.Color.BLUE }
                )
                chart.notifyDataSetChanged()
                chart.invalidate()
            }
        )
    }

    private fun registerSensor() {
        unregisterSensor()
        currentSensor = sensorManager.getDefaultSensor(currentSensorType)
        sensorName = currentSensor?.name ?: "Sensor not available"
        sensorManager.registerListener(this, currentSensor, SensorManager.SENSOR_DELAY_UI)
    }

    private fun unregisterSensor() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent) {
        timeIndex++

        val x = event.values.getOrNull(0) ?: 0f
        val y = if (event.sensor.type != Sensor.TYPE_LIGHT) event.values.getOrNull(1) ?: 0f else 0f
        val z = if (event.sensor.type != Sensor.TYPE_LIGHT) event.values.getOrNull(2) ?: 0f else 0f

        xValue = "X: ${"%.2f".format(x)}"
        yValue = "Y: ${"%.2f".format(y)}"
        zValue = "Z: ${"%.2f".format(z)}"

        if (entries1.size > maxEntries) entries1.removeAt(0)
        if (entries2.size > maxEntries) entries2.removeAt(0)
        if (entries3.size > maxEntries) entries3.removeAt(0)

        entries1.add(Entry(timeIndex, x))
        entries2.add(Entry(timeIndex, y))
        entries3.add(Entry(timeIndex, z))
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
}
