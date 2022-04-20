package com.github.jw3.example.gateman

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.slider.Slider
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.actor

@ExperimentalCoroutinesApi
class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    private lateinit var ws: EventsServiceConnection
    private var eventCount = 0L

    @ObsoleteCoroutinesApi
    val gateGui = actor<Event> {
        for (e in channel) {
            ++eventCount
            counter.text = "$eventCount"

            when (e) {
                Connected -> {
                    switch1.isChecked = true
                    switch1.isEnabled = true
                }
                Connecting -> {
                    switch1.isEnabled = false
                }
                Disconnected -> {
                    switch1.isChecked = false
                    switch1.isEnabled = true
                }
                else -> {}
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ws = EventsServiceConnection(gateGui)
        bindService(Intent(this, WsService::class.java), ws, Context.BIND_AUTO_CREATE)

        closeButton.setOnLongClickListener {
            async {
                ws.send(Close)
            }
            true
        }

        switch1.setOnCheckedChangeListener { _, checked ->
            async {
                if (checked) ws.send(Connect) else ws.send(Disconnect)
            }
        }

        findViewById<Slider>(R.id.slider).apply {
            this.addOnChangeListener(Slider.OnChangeListener { slider, value, _ ->
                async {
                    ws.send(Move(value.toInt()))
                    println("slider value ${slider.value}")
                }
            })
        }
    }
}
