package monad

import java.lang.Exception

class IO<T>(val run: () -> T) {

    fun <V> map(f: (T) -> V): IO<V> = IO { f(run()) }

    fun <V> flatMap(f: (T) -> IO<V>): IO<V> = IO { f(run()).run() }

    fun toOption(): Option<T> = try {
        Some(run())
    } catch (e: Throwable) {
        None
    }

}

fun print(str: String): IO<Unit> = IO { println(str) }

fun read(): IO<String> = IO { readln() }

fun badread(): IO<String> = IO {
    readln()
    throw Exception()
}

// TODO Read why there is no IO in Arrow (Effect instead)
// TODO Read about monad comprehension in Kotlin
// TODO Read about suspend in Kotlin
// TODO Read about coroutines in Kotlin
// Streams next time
