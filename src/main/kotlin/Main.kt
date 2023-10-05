import list.*
import list.List
import monad.read
import monad.print

fun main() {
    val foobar = listOf(2, 4, 6, 8)
    println("foobar: $foobar")
    println()

    val foobarTimes2 = foobar.map { x -> x * 2 }
    println("foobarTimes2: $foobarTimes2")
    println()

    val foobarNotDivisibleBy4 = foobar.filter { x -> x % 4 != 0 }
    println("foobarFiltered: $foobarNotDivisibleBy4")
    println()

    val foobarFlatMapped1 = foobar.flatMapR { x -> listOf(x, x * 2) }
    val foobarFlatMapped2 = foobar.flatMapL { x -> listOf(x, x * 2) }
    println("foobarFlatMapped1: $foobarFlatMapped1")
    println("foobarFlatMapped2: $foobarFlatMapped2")
    println()

    val moreFoobar = listOf(4, 2, 1)
    val evenMoreFoobar = foobar.appendList(moreFoobar)
    val evenMoreFoobar1 = foobar.appendList1(moreFoobar)
    val evenMoreFoobar2 = foobar.appendList2(moreFoobar)
    println("evenMoreFoobar: $evenMoreFoobar")
    println("evenMoreFoobar1: $evenMoreFoobar1")
    println("evenMoreFoobar2: $evenMoreFoobar2")
    println()
    println(evenMoreFoobar.hasSubSequence(listOf(4)))
    println(evenMoreFoobar.hasSubSequence(listOf(5)))
    println(evenMoreFoobar.hasSubSequence(listOf(4,2)))
    println(evenMoreFoobar.hasSubSequence(listOf(2,1)))
    println(evenMoreFoobar.hasSubSequence(listOf(2,4,6,8,4,2,1)))
    println(evenMoreFoobar.hasSubSequence(listOf(1)))
    println(evenMoreFoobar.hasSubSequence(listOf(2,4,6,8,4,2)))
    println()

    val something = print("What is your name?")
        .flatMap { read() } // IO<String>
        .flatMap { name -> print("Hello $name") }
        .toOption()

    println(something)

    val streamBar = Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
    println(streamBar.sum())

    val listLessThan6 = streamBar.takeWhile { it < 6 }.toList()
    println(listLessThan6)
}

fun List<Int>.add(other: List<Int>): List<Int> =
    when {
        this is List.Cons && other is List.Cons -> List.Cons(this.head + other.head, tail.add(other.tail))
        else -> List.Empty
    }

@Suppress("UNCHECKED_CAST")
fun <T> sum(t1: T, t2: T): T where T: Number =
    when (t1) {
        is Byte -> (t1 + t2.toByte()) as T
        is Short -> (t1 + t2.toShort()) as T
        is Int -> (t1 + t2.toInt()) as T
        is Long -> (t1 + t2.toLong()) as T
        is Float -> (t1 + t2.toFloat()) as T
        is Double -> (t1 + t2.toDouble()) as T
        else -> throw UnsupportedOperationException("Unsupported type")
    }

@Suppress("UNCHECKED_CAST")
fun <T> product(t1: T, t2: T): T where T: Number =
    when (t1) {
        is Byte -> (t1 * t2.toByte()) as T
        is Short -> (t1 * t2.toShort()) as T
        is Int -> (t1 * t2.toInt()) as T
        is Long -> (t1 * t2.toLong()) as T
        is Float -> (t1 * t2.toFloat()) as T
        is Double -> (t1 * t2.toDouble()) as T
        else -> throw UnsupportedOperationException("Unsupported type")
    }

fun factorial(n: Int): Int {
    tailrec fun go(i: Int, acc: Int): Int =
        if (i == 0) acc
        else go(i-1, acc*i)
    return go(n, 1)
}

fun fibonacci(n: Int): Int {
    tailrec fun go(i: Int, acc: Int, next: Int): Int =
        if (i == 0) acc
        else go(i-1, next, acc+next)
    return go(n, 0, 1)
}

fun <T> Array<T>.findFirst(key: T): Int? {
    tailrec fun go(n: Int): Int? =
        when {
            n == this.size -> null
            this[n] == key -> n
            else -> go(n+1)
        }
    return go(0)
}

fun <T> Array<T>.findFirst(f: (T) -> Boolean): Int? {
    tailrec fun go(n: Int): Int? =
        when {
            n == this.size -> null
            f(this[n]) -> n
            else -> go(n+1)
        }
    return go(0)
}

fun <T> Array<T>.contains(key: T): Boolean {
    tailrec fun go(n: Int): Boolean =
        when {
            n == this.size -> false
            this[n] == key -> true
            else -> go(n+1)
        }
    return go(0)
}
