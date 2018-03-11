package main.server

import main.convertFromBytes
import main.tcp.ConnectedTCPUnit
import main.Message
import main.tcp.TCPConnectionManager
import main.udp.MatchedUDPUnit
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.Inet4Address
import kotlin.concurrent.thread


val PORT = 12345
val BUFFER_SIZE = 1024

fun main(args: Array<String>) {
    val server = Server(PORT)
    server.serveTCPClients()
    server.serveUDPClients()
}

class Server(val port: Int) {
    /* clients */
    var tcpClients: List<ConnectedTCPUnit> = emptyList()
    var udpClients: List<MatchedUDPUnit> = emptyList()
    /* sockets */
    val datagramSocket: DatagramSocket = DatagramSocket(PORT)

    fun serveTCPClients() {
        thread {
            TCPConnectionManager(this).start()
            while (true) {
                var messagesToDistribute = emptyList<Message>()
                val dividedClients = tcpClients.groupBy { it.closed }
                val connectedClients = dividedClients[false]
                connectedClients?.filter { it.newMessage }?.forEach { messagesToDistribute += it.receive() }
                messagesToDistribute.forEach { msg -> connectedClients?.filter { it.username != msg.username }?.forEach { c -> c.send(msg) } }
            }
        }
    }

    fun serveUDPClients() {
        thread {
            while (true) {
                val initialBuffer = ByteArray(BUFFER_SIZE, { 0.toByte() })
                val packet = DatagramPacket(initialBuffer, initialBuffer.size)
                datagramSocket.receive(packet)
                val msg: Message = convertFromBytes(packet.data) as Message
                val unit = MatchedUDPUnit(packet.address as Inet4Address, packet.port, msg.username)
                if (!udpClients.contains(unit)) {
                    udpClients += unit
                }
                udpClients.filter { client -> client.username != msg.username }
                        .forEach { client -> client.send(msg, datagramSocket) }
            }
        }
    }


}


