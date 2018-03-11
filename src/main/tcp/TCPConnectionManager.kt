package main.tcp

import main.Message
import main.server.Server
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
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

            println(connectionMessage.content + " " + connectionMessage.username)
            if (server.tcpClients.any { connectionMessage.username == it.username }) {
                socketOutput.writeObject(Message("Server", "Reject"))
                println("Connection rejected")

            } else {
                socketOutput.writeObject(Message("Server", "Hello"))
                println("Connection accepted")
            }
            val connectedUnit = ConnectedTCPUnit(connectionMessage.username, client, socketInput, socketOutput)
            server.tcpClients += connectedUnit
            server.tcpClients.forEach { c -> if (!c.closed) c.send(Message("Server", "${connectedUnit.username} entered channel.")) }
        }

    }
}