package main.tcp

import main.Message
import main.printMessage
import java.io.ObjectInputStream
import java.net.SocketException

class TCPMessageReceiver(val input: ObjectInputStream) : Thread() {
    var isServerConnected = true
    override fun run() {
        while (isServerConnected) {
            try {
                val message: Message = input.readObject() as Message
                printMessage(message)
            }catch (e : SocketException){
                isServerConnected = false
                println("TCP and UDP server is down, communication available only by Multicast.\n Type [M] to enable multicast communication")
            }
        }
    }
}