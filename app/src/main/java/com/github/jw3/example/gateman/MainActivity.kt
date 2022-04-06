package com.github.jw3.example.gateman

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Message
import android.os.Messenger
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener

class MainActivity : AppCompatActivity() {
    private lateinit var ws: WebSocket

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ws = connectWs()
    }

    fun connectWs(): WebSocket {
        return http.client(applicationContext)
            .newWebSocket(http.wsreq("ws://192.168.1.98/gate"), wsHandler)
    }

    val wsHandler = object : WebSocketListener() {
        override fun onMessage(webSocket: WebSocket, text: String) {
            print("recv: $text")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            // todo;; an actor should manage reconnects
            println("websocket onFailure")
            webSocket.cancel()
            ws = connectWs()
        }

        override fun onOpen(webSocket: WebSocket, response: Response) {
            println("websocket onOpen")
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            println("websocket onClosing")
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            println("websocket onClosed")
        }
    }
}
