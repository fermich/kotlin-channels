package pl.fermich

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel

//channels allow two coroutines to communicate with each other
//instead of blocking a thread, channels suspend a coroutine, which is a lot cheaper.

fun user(name: String,
         input: Channel<Long>,
         output: Channel<Long>) = GlobalScope.launch {

    for (m in input) {
        println("$name got $m and sends ${m + 1}")
        delay(10)
        output.send(m + 1)
    }
}

fun pingPong() {      // aka Rendezvous
    val u1u2 = Channel<Long>(capacity = Channel.RENDEZVOUS)
    val u2u1 = Channel<Long>(capacity = Channel.RENDEZVOUS)

    user("User 1", u2u1, u1u2)
    user("User 2", u1u2, u2u1)

    runBlocking {
        u2u1.send(10)
        // u1u2.send(1)          // breaks infinite communication
        delay(50)
    }
}

fun unbuffered() {
    val channel = Channel<Int>()

    val j = GlobalScope.launch {
        for (i in 1..10) {
            channel.send(i)     // waiting for someone to read from the channel
            println("Sent $i")
        }
    }

    runBlocking {
        withTimeout(500) {
            try {
                j.join()
            } catch (e: TimeoutCancellationException) {
                e.printStackTrace()
            }
        }
    }
}

fun buffered() {
    val channel = Channel<Int>(10)

    // consider also:
    // val producer = produce<Int>(capacity = 10)
    // val actor = actor<Int>(capacity = 10)

    val j = GlobalScope.launch {
        for (i in 1..10) {
            channel.send(i)     // suspension will occur only when the channel capacity is reached
            println("Sent $i")
        }
    }

    runBlocking {
        j.join()
    }
}


fun main(vararg args: String) {
    pingPong()
    buffered()
    unbuffered()
}
