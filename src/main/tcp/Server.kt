package main.tcp

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import kotlin.concurrent.thread

fun main(args: Array<String>) {
    val TCP_PORT = 12345
    Server(TCP_PORT).run()
}

lateinit var socketOutput: ObjectOutputStream
lateinit var socketInput: ObjectInputStream


class TCPConnectionManager(val server: Server) : Thread() {
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

class UDPMatchingManager() : Thread() {
    override fun run() {

    }
}


class Server(val port: Int) {
    var tcpClients: List<ConnectedTCPUnit> = emptyList()
    var udpClients: List<MatchedUDPUnit> = emptyList()

    fun run() {
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

class MatchedUDPUnit()

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