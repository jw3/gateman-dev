package com.github.jw3.example.gateman

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.channels.sendBlocking
import kotlinx.coroutines.delay
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class WsService : Service(), CoroutineScope by MainScope() {
    val endpoint = "192.168.1.66:3030"

    private lateinit var ws: WebSocket
    private val binder = WsBinder()
    private val listeners: MutableList<SendChannel<Event>> = mutableListOf()

    override fun onBind(intent: Intent): IBinder {
        ws = http.client(applicationContext)
            .newWebSocket(http.ws("ws://$endpoint/gate"), wsHandler(gateWs))
        return binder
    }

    val gateWs = actor<Cmd> {
        var timer = ping(channel)

        for (msg in channel) {
            when (msg) {
                is Ping -> {
                    timer = ping(channel)
                    println("ping!")
                    ws.send("ping")
                }
                is Move -> {
                    println("move!")
                    // ws send move-to
                    timer.cancel()
                    ws.send(msg.to.toString())
                    timer = ping(channel)
                }
                is Close -> {
                    println("close!")
                    // ws send move-to
                    timer.cancel()
                    ws.send("close")
                    timer = ping(channel)
                }
            }
        }
    }

    private fun ping(channel: Channel<Cmd>) = async {
        delay(2000)
        channel.send(Ping)
    }

    suspend fun send(cmd: Cmd): Unit =
        gateWs.send(cmd)

    fun listen(channel: SendChannel<Event>): Boolean =
        listeners.add(channel)

    private fun wsHandler(channel: SendChannel<Cmd>) = object : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            println("recv: $text")
            text.toIntOrNull()?.let { at ->
                val moved = Stopped(at)
                listeners.forEach { l -> l.sendBlocking(moved) }
            }
            // todo;; second channel to handle incoming
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            // todo;; an actor should manage reconnects
            println("======================== websocket onFailure ======================== ")
            println(t.message)
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("websocket onOpen")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            println("websocket onClosing")
            // notify gui
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            println("websocket onClosed")
            // notify gui
        }
    }

    inner class WsBinder : Binder() {
        fun service(): WsService = this@WsService
    }
}
