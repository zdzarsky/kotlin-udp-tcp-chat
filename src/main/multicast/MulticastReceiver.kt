package main.multicast

import main.Message
import main.convertFromBytes
import main.printMessage
import main.server.BUFFER_SIZE
import java.net.DatagramPacket
import java.net.MulticastSocket

class MulticastReceiver(val multicastSocket: MulticastSocket, val username : String) : Thread() {
    override fun run() {
        while (true) {
            val readBytes = ByteArray(BUFFER_SIZE)
            val datagram = DatagramPacket(readBytes, readBytes.size)
            multicastSocket.receive(datagram)
            val msg: Message = convertFromBytes(datagram.data) as Message
            if (msg.username != username) printMessage(msg)
        }
    }
}