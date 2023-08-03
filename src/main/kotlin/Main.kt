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
    println("evenMoreFoobar: $evenMoreFoobar")
    println("evenMoreFoobar1: $evenMoreFoobar1")
}

sealed interface List<out T> {
    object Empty: List<Nothing>
    data class Cons<T> (val head: T, val tail: List<T>): List<T>
}

fun <T> listOf(vararg t: T): List<T> {
    tailrec fun go(n: Int, list: List<T>): List<T> =
        if (n == -1) list
        else go(n-1, List.Cons(t[n], list))
    return go(t.size-1, List.Empty)
}

fun <T> List<T>.head(): T? =
    when (this) {
        is List.Empty -> null
        is List.Cons -> head
    }

fun <T> List<T>.tail(): List<T> =
    when (this) {
        is List.Empty -> this
        is List.Cons -> tail
    }

tailrec fun <T> List<T>.drop(n: Int): List<T> =
    if (n == 0) this
    else tail().drop(n-1)

tailrec fun <T> List<T>.dropWhile(f: (T) -> Boolean): List<T> =
    when (this) {
        is List.Empty -> this
        is List.Cons -> if (f(head)) tail.dropWhile(f) else this
    }

fun <T> List<T>.init(): List<T> =
    when (this) {
        is List.Empty -> throw IllegalArgumentException("Can't init an empty list")
        is List.Cons ->
            if (tail is List.Empty) List.Empty
            else List.Cons(head, tail.init())
    }

fun <T> List<T>.oldAppend(t: T): List<T> =
    when (this) {
        is List.Empty -> List.Cons(t, List.Empty)
        is List.Cons -> List.Cons(head, tail.oldAppend(t))
    }

tailrec fun <T> List<T>.isSorted(f: (T,T) -> Boolean): Boolean =
    when (this) {
        is List.Empty -> true
        is List.Cons ->
            when (tail) {
                is List.Empty -> true
                is List.Cons ->
                    if (!f(head, tail.head)) false
                    else tail.isSorted(f)
            }
    }

tailrec fun <T,V> List<T>.foldLeft(v: V, f: (T,V) -> V): V =
    when (this) {
        is List.Empty -> v
        is List.Cons -> tail.foldLeft(f(head, v), f)
    }

fun <T> List<T>.reverse(): List<T> =
    when (this) {
        is List.Empty -> List.Empty
        is List.Cons -> this.foldLeft(List.Empty as List<T>) { x,y -> List.Cons(x,y) }
    }

fun <T,V> List<T>.foldRightReverse(v: V, f: (T, V) -> V): V =
    reverse().foldLeft(v,f)

// If nothing to do, then identity
// So it makes sense for the accumulator to be the identity morphism (V) -> V
// Also (T,V) -> V becomes (T) -> V -> V
// Does that remind you of anything?
fun <T,V> List<T>.foldRight(v: V, f: (T, V) -> V): V =
    foldLeft({ v1: V -> v1 }) { i, a -> { v2: V -> a(f(i,v2)) } }(v)

// Implement foldLeft with foldRight
// HOLY SHIT I DID IT!
fun <T,V> List<T>.foldLeftR(v: V, f: (T, V) -> V): V =
    foldRight({ v1: V -> v1 }) { i, a -> { v2: V -> f(i,a(v2)) } }(v)

fun <T,V> List<T>.unsafeFoldRight(v: V, f: (T, V) -> V): V =
    when (this) {
        is List.Empty -> v
        is List.Cons -> f(head, tail.unsafeFoldRight(v,f))
    }

fun <T,V> List<T>.unsafeFoldLeft(v: V, f: (T,V) -> V): V =
    reverse().unsafeFoldRight(v,f)

fun <T> List<T>.append(t: T): List<T> =
    foldRight(List.Cons(t, List.Empty)) { a, b -> List.Cons(a, b) }

fun <T> List<T>.appendList(list: List<T>): List<T> =
    when (this) {
        is List.Empty -> list
        is List.Cons -> foldRight(list) { i,a -> List.Cons(i,a) }
    }

fun <T> List<T>.appendList1(list: List<T>): List<T> =
    foldRight(list) { i,a -> when (i) {
        is List.Empty -> a
        else -> List.Cons(i,a)
    }}

// Write a function that concatenates a list of lists into a single list. Its runtime
// should be linear in the total length of all lists. Use functions already defined.
fun <T> List<List<T>>.flatten(): List<T> =
    foldLeft(List.Empty as List<T>) { i, a -> a.appendList(i) }

fun <T,U> List<T>.zip(list: List<U>): List<Pair<T,U>> {
    tailrec fun go(list1: List<T>, list2: List<U>, acc: List<Pair<T,U>>): List<Pair<T,U>> =
        when {
            list1 is List.Cons && list2 is List.Cons -> go(list1.tail(), list2.tail(), acc.append(Pair(list1.head, list2.head)))
            else -> acc
        }
    return go(this, list, List.Empty as List<Pair<T, U>>)
}

fun <T,V,Z> List<T>.zipWith1(otherList: List<V>, f: (T, V) -> Z): List<Z> =
    zip(otherList).map { pair -> f(pair.first, pair.second) }

fun List<Int>.add(other: List<Int>): List<Int> =
    when {
        this is List.Cons && other is List.Cons -> List.Cons(this.head + other.head, tail.add(other.tail))
        else -> List.Empty
    }

fun <T,V,Z> List<T>.zipWith(other: List<V>, f: (T, V) -> Z): List<Z> =
    when {
        this is List.Cons && other is List.Cons -> List.Cons(f(head, other.head), tail.zipWith(other.tail, f))
        else -> List.Empty
    }

fun <T> List<T>.hasSubSequence(otherList: List<T>): Boolean =
    TODO()

fun <T,V> List<T>.map(f: (T) -> V): List<V> =
    foldRight(List.Empty as List<V>) { i,a -> List.Cons(f(i),a) }

fun <T,V> List<T>.flatMap(f: (T) -> List<V>): List<V> =
    map(f).flatten()

// Do it with appendList() and foldRight()
fun <T,V> List<T>.flatMapR(f: (T) -> List<V>): List<V> =
    foldRight(List.Empty as List<V>) { i, a -> f(i).appendList(a) }

fun <T,V> List<T>.flatMapL(f: (T) -> List<V>): List<V> =
    foldLeft(List.Empty as List<V>) { i,a -> a.appendList(f(i)) }

fun <T,V> List<T>.flatMap1(f: (T) -> List<V>): List<V> =
    foldRight(List.Empty as List<V>) { i,a -> f(i).foldRight(a) { i1,a1 -> List.Cons(i1,a1) } }

// Implement filter using flatMap
fun <T> List<T>.filter1(f: (T) -> Boolean): List<T> =
    flatMap { i -> when (f(i)) {
        true -> List.Cons(i, List.Empty)
        false -> List.Empty
    } }

fun <T> List<T>.filter(f: (T) -> Boolean): List<T> =
    foldRight(List.Empty as List<T>) { i,a -> when (f(i)) {
        true -> List.Cons(i,a)
        false -> a
    }}

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

fun <T> List<T>.length(): Int =
    unsafeFoldRight(0) { _, v -> v + 1 }

fun <A,B,C> partial1(a: A, f: (A,B) -> C): (B) -> C =
    { b -> f(a,b) }

fun <A,B,C> curry(f: (A,B) -> C): (A) -> (B) -> C =
    { a -> { b -> f(a,b) } }

fun <A,B,C> uncurry(f: (A) -> (B) -> C): (A,B) -> C =
    { a,b -> f(a)(b) }

fun <A,B,C> compose(f: (A) -> B, g: (B) -> C): (A) -> C =
    { a -> g(f(a)) }

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
