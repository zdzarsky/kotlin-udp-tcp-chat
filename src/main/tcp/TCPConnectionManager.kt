package main.tcp

import main.HandshakeMessage
import main.Message
import main.server.Server
import main.udp.MatchedUDPUnit
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.Inet4Address
import java.net.ServerSocket

class TCPConnectionManager(val server: Server) : Thread() {

    lateinit var socketOutput: ObjectOutputStream
    lateinit var socketInput: ObjectInputStream

    override fun run() {
        val serversocket = ServerSocket(server.port)
        println("Connection manager started")
        while (true) {
            val client = serversocket.accept()
            socketOutput = ObjectOutputStream(client.getOutputStream())
            socketInput = ObjectInputStream(client.getInputStream())
            val connectionMessage = socketInput.readObject() as Message

            if (server.tcpClients.any { connectionMessage.username == it.username }) {
                socketOutput.writeObject(Message("Server", "Reject"))
                println("Connection rejected")

            } else {
                socketOutput.writeObject(Message("Server", "Hello"))
                println("Connection accepted")
            }
            val connectedUnit = ConnectedTCPUnit(connectionMessage.username, client, socketInput, socketOutput)
            val udpUnit = MatchedUDPUnit(client.inetAddress as Inet4Address, client.port, connectionMessage.username)
            server.tcpClients += connectedUnit
            server.udpClients += udpUnit
            server.tcpClients.forEach { c -> if (!c.closed) c.send(Message("Server", "\n╔═════════════════════════╗\n" +
                    "\n" +
                    " Hello ${connectedUnit.username}\n" +
                    "\n" +
                    "╚═════════════════════════╝")) }
        }

    }
}