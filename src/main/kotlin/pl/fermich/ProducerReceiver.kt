package pl.fermich

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.selects.select
import java.util.*

// producer() is coroutine bounded to channel- it has channel's methods e.g. consumeEach(), send(), onReceive() ...

// Producer 1
val firstProducer = GlobalScope.produce {
    for (c in 'a'..'z') {
        delay(Random().nextInt(100).toLong())
        send(c.toString())      // Producers have a  send() function
    }

}

// Producer 2
val secondProducer = GlobalScope.produce {
    for (c in 'A'..'Z') {
        delay(Random().nextInt(100).toLong())
        send(c.toString())
    }
}

// Producer 3
val closingProducer = GlobalScope.produce {
    for (c in '1'..'9') {
        delay(Random().nextInt(100).toLong())
        send(c.toString())
    }
    close()
}

fun main(vararg args: String) {
    // Receiver
    runBlocking {

        //If we have more than one producer, we can subscribe to their channels, and take the first result available:
        while(true) {
            val received = select<String> {    //  channel's select() happens only once, remember wrapping it in a loop when necessary
                firstProducer.onReceive {
                    it
                }
                secondProducer.onReceive {
                    it
                }
                closingProducer.onReceiveOrClosed() {
                    if (it.isClosed) "closed" else it.value
                }
            }

            if (received.equals("closed"))
                break
            else
                println(received)
        }
    }
}
