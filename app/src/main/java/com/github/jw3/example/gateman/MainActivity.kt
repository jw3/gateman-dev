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
            eventCounter.text = "$eventCount"

            when (e) {
                is Stopped -> {
                    slider.isEnabled = true
                }
                is Closing -> {
                    slider.value = 0f
//                    slider.isEnabled = false
                }
                is Moving -> {
//                    progressBar.progress = e.to
                    currentPosition.text = "${e.to}"
                }
                is Moved -> {
                    progressBar.progress = e.at
                    currentPosition.text = "${e.at}"
                }
                is Connected -> {
                    switch1.isChecked = true
                    switch1.isEnabled = true
                }
                is Connecting -> {
                    switch1.isEnabled = false
                }
                is Disconnected -> {
                    switch1.isChecked = false
                    switch1.isEnabled = true
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ws = EventsServiceConnection(gateGui)
        bindService(Intent(this, WsService::class.java), ws, Context.BIND_AUTO_CREATE)

        closeButton.setOnLongClickListener {
            launch {
                ws.send(Close)
            }
            true
        }

        switch1.setOnCheckedChangeListener { _, checked ->
            launch {
                if (checked) ws.send(Connect) else ws.send(Disconnect)
            }
        }

        slider.addOnChangeListener(Slider.OnChangeListener { slider, value, _ ->
            launch {
                ws.send(Move(value.toInt()))
                println("slider value ${slider.value}")
            }
        })

        progressBar.min = 0
        progressBar.max = 10
        progressBar.progress = 7
    }
}
