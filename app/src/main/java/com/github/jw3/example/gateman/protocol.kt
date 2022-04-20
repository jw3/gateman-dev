package com.github.jw3.example.gateman

import android.content.ComponentName
import android.content.ServiceConnection
import android.os.IBinder
import kotlinx.coroutines.channels.SendChannel

sealed interface Cmd
object Ping : Cmd
object Close : Cmd
class Move(val to: Int) : Cmd

sealed interface Event
object Closing : Event
class Moving(val to: Int) : Event
class Stopped(val at: Int) : Event

class EventsServiceConnection(val channel: SendChannel<Event>) : ServiceConnection {
    private lateinit var ws: WsService
    private var bound: Boolean = false

    suspend fun send(cmd: Cmd) {
        ws.send(cmd)
    }

    override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
        val binder = service as WsService.WsBinder
        ws = binder.service()
        ws.listen(channel)
        bound = true
    }

    override fun onServiceDisconnected(className: ComponentName?) {
        bound = false
    }
}
