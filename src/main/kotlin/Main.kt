fun main() {
    val foobar = listOf(12, 13, 2341, 3211)
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

fun <T,V> List<T>.foldRight(v: V, f: (T, V) -> V): V =
    this.reverse().foldLeft(v,f)

fun <T,V> List<T>.unsafeFoldRight(v: V, f: (T, V) -> V): V =
    when (this) {
        is List.Empty -> v
        is List.Cons -> f(head, tail.unsafeFoldRight(v,f))
    }

fun <T,V> List<T>.unsafeFoldLeft(v: V, f: (T,V) -> V): V =
    this.reverse().unsafeFoldRight(v,f)

fun <T> List<T>.append(t: T): List<T> =
    this.foldRight(List.Cons(t, List.Empty)) { a,b -> List.Cons(a, b) }

fun <T> List<T>.appendList(list: List<T>): List<T> =
    TODO()

// Write a function that concatenates a list of lists into a single list. Its runtime
// should be linear in the total length of all lists. Use functions already defined.
fun <T> concatLists(list: List<List<T>>): List<T> =
    TODO()

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
fun  <T> product(t1: T, t2: T): T where T: Number =
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
    this.unsafeFoldRight(0) { _, v -> v + 1 }

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
