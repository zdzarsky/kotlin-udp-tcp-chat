package main

import java.io.*


@Throws(IOException::class)
fun convertToBytes(`object`: Any): ByteArray {
    ByteArrayOutputStream().use { bos ->
        ObjectOutputStream(bos).use { out ->
            out.writeObject(`object`)
            return bos.toByteArray()
        }
    }
}

@Throws(IOException::class, ClassNotFoundException::class)
fun convertFromBytes(bytes: ByteArray): Any {
    ByteArrayInputStream(bytes).use { bis ->
        ObjectInputStream(bis).use { `in` ->
            return `in`.readObject()
        }
    }
}

fun printMessage(message : Message){
    println("${message.username} says: ${message.content}")
}