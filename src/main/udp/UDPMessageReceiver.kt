package main.udp

import main.convertFromBytes
import main.Message
import main.printMessage
import java.net.DatagramPacket
import java.net.DatagramSocket

class UDPMessageReceiver(val socket : DatagramSocket) : Thread() {
    override fun run() {
        while(true){
            val buff = ByteArray(1024, {0.toByte()})
            val packet = DatagramPacket(buff, buff.size)
            socket.receive(packet)
            val received : Message = convertFromBytes(buff) as Message
            printMessage(received)
        }
    }
}