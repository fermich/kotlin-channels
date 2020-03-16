package pl.fermich

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.channels.receiveOrNull

// actor() is a coroutine bound to a channel. But instead of a channel going out of the coroutine, there's a channel going into the coroutine.
// Actors act upon requests.
// Actors are a very useful for background tasks that need to maintain some kind of state. For example, you could create an actor that would generate reports.

data class Task (val description: String)

//1. check if channel closed
val closingWorker = GlobalScope.actor<Task> {
    while (!isClosedForReceive) {
        println(receive().description.repeat(10))       //receive will execute once, wrap it in a loop
    }
}

//2. Instead of checking whether the actor's channel has been closed, our code is receiving null on the channel.
// This approach may be preferable, if the actor receives tasks from many generators
val nullSupportingWorker = GlobalScope.actor<Task> {
    var next = receiveOrNull()

    while (next != null) {
        println(next.description.toUpperCase())
        next = receiveOrNull()
    }
}

//3. the most preferable method is to iterate over the channel
val iteratingWorker = GlobalScope.actor<Task> {
    for (t in channel) {
        println(t.description)
    }

    println("Done everything")
}


fun taskGenerator(actor: SendChannel<Task>) {

    runBlocking {
        for (i in 'a'..'z') {
            actor.send(Task(i.toString()))
        }
        actor.close()
    }
}

fun main(vararg args: String) {
    taskGenerator(closingWorker)
    taskGenerator(nullSupportingWorker)
    taskGenerator(iteratingWorker)
}


