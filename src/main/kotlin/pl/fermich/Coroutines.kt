package pl.fermich

import kotlinx.coroutines.*
import java.util.*
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
- Coroutines use thread pool behind the scenes (e.g. CommonPool)
- Instead of blocking an entire thread, coroutine suspends (yield()s its execution)
 */

object GreedyCoroutineFactory {
    val latch = CountDownLatch(10_000)
    val c = AtomicInteger()

    fun longCoroutine(index: Int) = GlobalScope.async {
        var uuid = UUID.randomUUID()
        for (i in 1..100_000) {
            val newUuid = UUID.randomUUID()

            if (newUuid < uuid) {
                uuid = newUuid
            }
        }

        println("Done greedyLongCoroutine $index")
        latch.countDown()
    }

    fun shortCoroutine(index: Int) = GlobalScope.async {
        println("Done shortCoroutine $index!")
        latch.countDown()
    }

    fun starvingDemo() {
        val latch = CountDownLatch(10 * 2)

        for (i in 1..10) {
            GreedyCoroutineFactory.longCoroutine(i)
        }
        for (i in 1..10) {
            GreedyCoroutineFactory.shortCoroutine(i)
        }

        latch.await(10, TimeUnit.SECONDS)
    }
}

object YieldingCoroutineFactory {
    val latch = CountDownLatch(10_000)

    //suspend fun longCoroutine(index: Int) {
    fun longCoroutine(index: Int) = GlobalScope.launch {  //without the suspend modifier or coroutine generator, such as launch(), we can't call yield()
        var uuid = UUID.randomUUID()
        for (i in 1..100_000) {
            val newUuid = UUID.randomUUID()

            if (newUuid < uuid) {
                uuid = newUuid
            }

            if (i % 100 == 0) {
                yield()   // Asks the pool whether there is anybody else that wants to do some work.
                // If there's nobody else, the execution of the current coroutine will resume. Otherwise,
                // another coroutine will start or resume from the point where it stopped earlier.
            }
        }

        println("Done longCoroutine $index")
        latch.countDown()
    }

    fun shortCoroutine(index: Int) = GlobalScope.async {
        println("Done shortCoroutine $index!")
        latch.countDown()
    }

    fun suspendingDemo() {
        val latch = CountDownLatch(10 * 2)

        for (i in 1..10) {
            YieldingCoroutineFactory.longCoroutine(i)
        }
        for (i in 1..10) {
            YieldingCoroutineFactory.shortCoroutine(i)
        }

        latch.await(10, TimeUnit.SECONDS)
    }

    fun joinAndWaitForResults() {
        val j = YieldingCoroutineFactory.longCoroutine(10)

        runBlocking {
            j.join()            //should be called from a coroutine
        }
    }
}

object CancelingCoroutineFactory {
    fun cancellableCoroutine() = GlobalScope.launch {
        try {
            for (i in 1..1000) {
                println("Cancellable: $i")
                yield()
            }
        }
        catch (e: CancellationException) {
            println("Exception on Cancelling Cancellable: ${e.message}")
        }
    }

    fun notCancellable() = GlobalScope.launch {
        for (i in 1..1000) {
            println("Not cancellable $i")
        }
    }

    fun cancelDemo() {
        val cancellable = CancelingCoroutineFactory.cancellableCoroutine()
        val notCancellable = CancelingCoroutineFactory.notCancellable()

        println("Canceling cancellable")
        cancellable.cancel()
        println("Canceling not cancellable")
        notCancellable.cancel()

        runBlocking {
            cancellable.join()
            notCancellable.join()
        }
    }
}


object ReturningCoroutineFactory {

    /**
    A job has a simple lifecycle:

    New: Created, but not started yet.
    Active: Just created by launch() function, for example. This is the default state.
    Completed: Everything went well.
    Canceled: Something went wrong.
    There are two more states relevant to jobs that have child jobs:

    Completing: Waiting to finish executing children before completing
    Canceling: Waiting to finish executing children before canceling
     */

    fun jobCoroutine() = GlobalScope.launch {
        delay(1000)
        println("result from inside")
    }

    // async also launches a coroutine, but instead of returning a job, it returns Deferred<T>
    fun returningResultCoroutine() = GlobalScope.async {
        delay(1000)
        "returned result"
    }

    fun readResult() {
        val job: Job = jobCoroutine()
        println("Job.isActive? ${job.isActive}")

        val deferred: Deferred<String> = returningResultCoroutine()
        runBlocking {
            println(deferred.await())
        }
    }
}

fun main(args: Array<String>) {
    GreedyCoroutineFactory.starvingDemo()
    println("==============")
    YieldingCoroutineFactory.suspendingDemo()
    println("==============")
    YieldingCoroutineFactory.joinAndWaitForResults()
    println("==============")
    CancelingCoroutineFactory.cancelDemo()
    println("==============")
    ReturningCoroutineFactory.readResult()
}
