package main.tcp

import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.net.InetAddress
import java.net.Socket


fun main(args: Array<String>) {
    val me = Client("localhost", 12345)
    println("Enter username: ")
    val username = readLine()
    println("Read username: $username")
    me.connect(username!!)
    while (me.isConnected) {
        print(">>> ")
        val messageStr = readLine()
        if (messageStr == "exit") break
        me.send(messageStr!!)
    }
}

class Client(val host: String, val port: Int) {
    lateinit var socket: Socket
    var isConnected: Boolean = false
    lateinit var username: String
    lateinit var output: ObjectOutputStream
    lateinit var input: ObjectInputStream


    fun connect(username: String) {
        socket = Socket(InetAddress.getByName(this.host), port)
        this.username = username
        output = ObjectOutputStream(socket.getOutputStream())
        input = ObjectInputStream(socket.getInputStream())
        send("Hello")
        val connectionResponse: Message = input.readObject() as Message
        if (connectionResponse.content == "Hello") {
            isConnected = true
            println(" **** Succesfully connected ****")
            MessageReceiver(input).start()
        } else {
            isConnected = false
            println("*** Unable to connect ***")
        }


    }

    fun send(message: String) {
        val msg = Message(username, message)
        output.writeObject(msg)
    }

}

class MessageReceiver(val input: ObjectInputStream) : Thread() {
    override fun run() {
        while (true) {
            val message: Message = input.readObject() as Message
            print("${message.username} says: ${message.content}\n")
        }
    }
}