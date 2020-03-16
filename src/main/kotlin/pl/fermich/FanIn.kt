package pl.fermich

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.util.*

// Multiple coroutines can write their results to the same channel, collecting their work

private fun worker1(collector: Channel<String>) = GlobalScope.launch {
    repeat(10) {
        delay(Random().nextInt(100).toLong())
        collector.send("Result1")
    }
}

private fun worker2(collector: Channel<String>) = GlobalScope.launch {
    repeat(10) {
        delay(Random().nextInt(100).toLong())
        collector.send("Result2")
    }
}

fun main(vararg args: String) {
    val collector = Channel<String>()

    worker1(collector)
    worker2(collector)

    runBlocking {
        // Channel operators are deprecated in favour of Flow:
        collector.consumeAsFlow().collect {
            println("Got result: $it")
        }
    }
}