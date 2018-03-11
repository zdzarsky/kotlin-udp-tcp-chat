package main.udp

import main.convertToBytes
import main.Message
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address

class MatchedUDPUnit(val address: Inet4Address, val port: Int, val username: String) {
    fun send(message: Message, socket: DatagramSocket) {
        val data = convertToBytes(message)
        val packet = DatagramPacket(data, data.size, address, port)
        socket.send(packet)
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MatchedUDPUnit) {
            this.username == other.username
        } else {
            false
        }
    }

    override fun hashCode(): Int {
        var result = address.hashCode()
        result = 31 * result + port
        result = 31 * result + username.hashCode()
        return result
    }
}