package list

import list.Stream.Companion.cons
import list.Stream.Companion.empty
import monad.None
import monad.Option
import monad.Some

sealed interface Stream<out T> {
    data object Empty : Stream<Nothing>
    data class Cons<T> (val head: () -> T, val tail: () -> Stream<T>): Stream<T>

    companion object {
        fun <T> cons(head1: () -> T, tail1: () -> Stream<T>): Stream<T> {
            val head: T by lazy(head1)
            val tail: Stream<T> by lazy(tail1)
            return Cons({ head  }, { tail })
        }

        fun <T> empty(): Stream<T> = Empty

        // HOMEWORK
        fun <T> of(vararg values: T): Stream<T> = when (values.isEmpty()) {
            true -> empty()
            false -> cons({ values.first() }, { of(*values.sliceArray(1 until values.size)) })
        }
    }
}

fun <T> Stream<T>.headOption(): Option<T> = when (this) {
    is Stream.Empty -> None
    is Stream.Cons -> Some(head())
}

// Will cause a stack overflow
@Deprecated(
    message = "Use Stream<T>.toList() instead",
    replaceWith = ReplaceWith(expression = "this.toList()", imports = [])
)
fun <T> Stream<T>.unsafeToList(): List<T> = when (this) {
    is Stream.Empty -> List.Empty
    is Stream.Cons -> List.Cons(head(), tail().unsafeToList())
}

fun <T> Stream<T>.toList(): List<T> {
    tailrec fun <T> go(stream: Stream<T>, acc: List<T>): List<T> = when (stream) {
        is Stream.Empty -> acc
        is Stream.Cons -> go(stream.tail(), List.Cons(stream.head(), acc))
    }
    return go(this, List.Empty).reverse()
}

// HOMEWORK
fun <T> Stream<T>.take(n: Int): Stream<T> = when (this) {
    is Stream.Empty -> empty()
    is Stream.Cons -> when (n == 0) {
        true -> empty()
        false -> cons(head) { tail().take(n-1) }
    }
}

// HOMEWORK
tailrec fun <T> Stream<T>.drop(n: Int): Stream<T> = when (this) {
    is Stream.Empty -> empty()
    is Stream.Cons -> when (n == 0) {
        true -> this
        false -> tail().drop(n-1)
    }
}

// This will retain lazy evaluation, but can overflow the call stack
fun <T> Stream<T>.lazyDrop(n: Int): Stream<T> = when (this) {
    is Stream.Empty -> empty()
    is Stream.Cons -> when (n == 0) {
        true -> this
        false -> cons({ head() }, { tail().lazyDrop(n-1) } )
    }
}

// HOMEWORK
fun <T> Stream<T>.takeWhile(p: (T) -> Boolean): Stream<T> = when (this) {
    is Stream.Empty -> empty()
    is Stream.Cons -> when (p(head())) {
        false -> empty()
        true -> cons(head) { tail().takeWhile(p) }
    }
}

// HOMEWORK
fun <T,V> Stream<T>.foldRight(acc: () -> V, f: (T, () -> V) -> V): V = when (this) {
    is Stream.Empty -> acc()
    is Stream.Cons -> f(head()) { tail().foldRight(acc, f) }
}

// HOMEWORK
fun Stream<Int>.sum(): Int = foldRight({ 0 }) { num,acc -> num + acc() }

tailrec fun <T> Stream<T>.forAll(p: (T) -> Boolean): Boolean = when (this) {
    is Stream.Empty -> true
    is Stream.Cons -> when (p(head())) {
        false -> false
        true -> tail().forAll(p)
    }
}

fun <T> Stream<T>.forAllFold(p: (T) -> Boolean): Boolean = foldRight({ true }) { t,acc ->
    when (p(t)) {
        false -> false
        true -> acc()
    }
}

// Implement takeWhile with foldRight
fun <T> Stream<T>.takeWhileFold(p: (T) -> Boolean): Stream<T> = foldRight({ empty() }) { t,acc ->
    when (p(t)) {
        false -> acc()
        true -> cons({ t }, acc)
    }}

// Implement headOption with foldRight
fun <T> Stream<T>.headOptionFold(): Option<T> = foldRight<T,Option<T>>({ None }) { t,_ -> Some(t) }

// TODO take(n elements), takeWhile(fun), drop(n), (terminal) foldRight(), Stream<Int>.sum() through foldRight (to try),
// TODO in case succeed write to Pavel ask more

// map, filter, append, flatmap through foldRight
// append should be non-strict in its argument
