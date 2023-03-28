fun main() {
    println("Hello World!")
    val foo = listOf(1, 53, 234, 11, 323, 59)
}

sealed interface List<out T> {
    object Empty: List<Nothing>
    data class Cons<T> (val head: T, val tail: List<T>): List<T>
}

fun <T> listOf(vararg t: T): List<T>  {
    tailrec fun go(list: List<T>, i: Int): List<T> =
        if (i == -1) list
        else go(List.Cons(t[i], list), i-1)
    return go(List.Empty, t.size-1)
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
        is List.Cons -> if (f(head)) this.dropWhile(f) else this
    }

fun <T> List<T>.init(): List<T> =
    when (this) {
        is List.Empty -> this
        is List.Cons ->
            when (tail()) {
                is List.Empty -> tail()
                is List.Cons -> List.Cons(head, tail.init())
            }
    }

fun <T> List<T>.append(t: T): List<T> =
    when (this) {
        is List.Empty -> List.Cons(t, this)
        is List.Cons -> List.Cons(head, tail.append(t))
    }

tailrec fun <T,V> List<T>.foldLeft(v: V, f: (T, V) -> V): V =
    when (this) {
        is List.Empty -> v
        is List.Cons -> tail().foldLeft(f(head, v), f)
    }

// TODO Implement this tail-recursively using foldLeft
fun <T,V> List<T>.foldRight(v: V, f: (T, V) -> V): V =
    when (this) {
        is List.Empty -> v
        is List.Cons -> f(head, tail().foldRight(v, f))
    }

fun <T> List<T>.reverse(): List<T> =
    foldLeft(List.Empty as List<T>) { x, y -> List.Cons(x, y) }

fun <T> List<T>.length(): Int =
    foldRight(0) { _, y -> y + 1 }

fun <T> List<T>.leftLength(): Int =
    foldLeft(0) { _, y -> y + 1 }

fun sum(a: Int, b: Int): Int = a + b

fun <A,B,C> partial1(a: A, f: (A, B) -> C): (B) -> C =
    { b -> f(a,b) }

fun <A,B,C> curry(f: (A,B) -> C): (A) -> (B) -> C =
    { a -> { b -> f(a,b) } }

fun <A,B,C> uncurry(f: (A) -> (B) -> C): (A,B) -> C =
    { a,b -> f(a)(b) }

fun <A,B,C> compose(f: (A) -> B, g: (B) -> C): (A) -> C =
    { a -> g(f(a)) }
