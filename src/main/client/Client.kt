package main.client

import main.*
import main.multicast.MulticastReceiver
import main.server.PORT
import main.tcp.TCPMessageReceiver
import main.udp.UDPMessageReceiver
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.*


fun main(args: Array<String>) {
    val me = Client("localhost", 12345, 12344)
    println("Enter username: ")
    val username = readLine()
    println("Read username: $username")
    me.initialiseSockets()
    me.tcpConnect(username!!)
    me.udpInitialise()
    me.multicastInitialise()
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


class Client(val host: String, val port: Int, val multicastPort: Int) {
    var currentCommunicationChannel: CommunicationChannel = CommunicationChannel.TCP
    /* TCP */
    lateinit var tcpSocket: Socket
    var isConnected: Boolean = false
    lateinit var username: String
    lateinit var output: ObjectOutputStream
    lateinit var input: ObjectInputStream

    /*UDP */
    lateinit var udpSocket: DatagramSocket

    /* Multicast */
    lateinit var multicastSocket: MulticastSocket
    val multicast = InetAddress.getByName("224.2.2.4")


    fun initialiseSockets() {
        tcpSocket = Socket(InetAddress.getByName(host), port)
        println("TCP Initialised")
        udpSocket = DatagramSocket(tcpSocket.localPort)
        println("UDP Initialised")
        multicastSocket = MulticastSocket(multicastPort)
        multicastSocket.joinGroup(multicast)
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

    fun multicastInitialise(){
        MulticastReceiver(multicastSocket, username).start()
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
                val bytes = convertToBytes(msg)
                val packet = DatagramPacket(bytes, bytes.size, multicast, multicastSocket.localPort)
                multicastSocket.send(packet)
            }
        }
    }



}

