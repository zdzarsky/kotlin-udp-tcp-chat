package main.tcp

import main.Message
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Socket
import java.net.SocketException
import kotlin.concurrent.thread

class ConnectedTCPUnit(val username: String, val socket: Socket, inputStream: ObjectInputStream, outputStream: ObjectOutputStream) {
    @Volatile
    var closed: Boolean = false
    @Volatile
    var newMessage: Boolean = false
    lateinit var message: Message
    private val myInputStream = inputStream
    private val myOutputStream = outputStream
    init {
        thread {
            try {
                while (!socket.isClosed) {
                    message = myInputStream.readObject() as Message
                    newMessage = true
                }
            } catch (e: SocketException) {
                closed = true
            }

        }
    }
    fun send(message: Message) {
        myOutputStream.writeObject(message)
    }
    fun receive(): Message {
        newMessage = false
        return message
    }
}