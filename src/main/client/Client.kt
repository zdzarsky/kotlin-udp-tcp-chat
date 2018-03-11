package main.client

import main.CommunicationChannel
import main.Message
import main.convertToBytes
import main.tcp.TCPMessageReceiver
import main.udp.UDPMessageReceiver
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.Socket
import kotlin.concurrent.thread


fun main(args: Array<String>) {
    val me = Client("localhost", 12345)
    println("Enter username: ")
    val username = readLine()
    println("Read username: $username")
    me.initialiseSockets()
    me.tcpConnect(username!!)
    me.udpInitialise()
    while (true) {
        val message = readLine()
        when (message) {
            "[U]" -> {
                me.currentCommunicationChannel = CommunicationChannel.UDP
            }
            "[T]" -> {
                me.currentCommunicationChannel = CommunicationChannel.TCP
            }
            "[M]" -> {
                me.currentCommunicationChannel = CommunicationChannel.MULTICAST
            }
            else -> {
                if (!message.isNullOrBlank()) {
                    me.send(message!!)
                }
            }
        }
    }
}


class Client(val host: String, val port: Int) {
    var currentCommunicationChannel: CommunicationChannel = CommunicationChannel.TCP
    /* TCP */
    lateinit var tcpSocket: Socket
    var isConnected: Boolean = false
    lateinit var username: String
    lateinit var output: ObjectOutputStream
    lateinit var input: ObjectInputStream
    /*UDP */
    lateinit var udpSocket: DatagramSocket

    fun initialiseSockets() {
        tcpSocket = Socket(InetAddress.getByName(host), port)
        println("TCP Initialised")
        udpSocket = DatagramSocket(tcpSocket.localPort)
        println("UDP Initialised")
    }

    fun tcpConnect(username: String) {
        this.username = username
        print(username)
        output = ObjectOutputStream(tcpSocket.getOutputStream())
        input = ObjectInputStream(tcpSocket.getInputStream())
        send("Hello")
        val connectionResponse: Message = input.readObject() as Message
        if (connectionResponse.content == "Hello") {
            isConnected = true
            println(" **** Succesfully connected ****")
            TCPMessageReceiver(input).start()
        } else {
            isConnected = false
            println("*** Unable to tcpConnect ***")
        }
    }

    fun udpInitialise() {
        UDPMessageReceiver(udpSocket).start()
    }

    fun send(message: String) {
        val msg = Message(username, message)
        when (currentCommunicationChannel) {
            CommunicationChannel.TCP -> {
                output.writeObject(msg)
            }
            CommunicationChannel.UDP -> {
                val bytes = convertToBytes(msg)
                val packet = DatagramPacket(bytes, bytes.size, InetAddress.getByName(host), port)
                udpSocket.send(packet)
            }
            CommunicationChannel.MULTICAST -> {
            }
        }
    }


}

