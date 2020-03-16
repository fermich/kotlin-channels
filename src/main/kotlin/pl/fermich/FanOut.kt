package pl.fermich

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.runBlocking

// The number of coroutines may read from the same channel, distributing the work

private fun produceTasks() = GlobalScope.produce {
    for (i in 1..10_000) {
        for (c in 'a'..'z') {
            send(i to "Task$c")
        }
    }
}

private fun consumeTask(channel: ReceiveChannel<Pair<Int, String>>) = GlobalScope.async {
    for (p in channel) {
        println(p)
    }
}

fun main(vararg args: String) {
    val producer = produceTasks()

    val consumers = List(10) {
        consumeTask(producer)
    }

    runBlocking {
        consumers.forEach {
            it.await()
        }
    }
}
