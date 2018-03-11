package main.tcp

import main.Message
import main.printMessage
import java.io.ObjectInputStream

class TCPMessageReceiver(val input: ObjectInputStream) : Thread() {
    override fun run() {
        while (true) {
            val message: Message = input.readObject() as Message
            printMessage(message)
        }
    }
}