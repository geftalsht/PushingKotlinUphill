package monad

import list.List

sealed class Option<out T> {
    companion object {
        fun <T> of(value: T?): Option<T> = when (value == null) {
            true -> None
            false -> Some(value)
        }
    }
}

data object None : Option<Nothing>()

data class Some<T>(val value: T): Option<T>()

fun <T,V> Option<T>.map(f: (T) -> V): Option<V> = when (this) {
    is None -> None
    is Some -> Some(f(this.value))
}

fun <T,V> Option<T>.flatMap(f: (T) -> Option<V>): Option<V> = when (this) {
    is None -> None
    is Some -> f(this.value)
}

fun <T> Option<T>.getOrElse(f: () -> T): T = when (this) {
    is None -> f()
    is Some -> this.value
}

fun <T> Option<T>.orElse(f: () -> Option<T>): Option<T> = when (this) {
    is None -> f()
    is Some -> this
}

fun <T> Option<T>.filter(f: (T) -> Boolean): Option<T> = when (this) {
    is None -> None
    is Some -> when (f(value)) {
        true -> this
        false -> None
    }
}

fun <T,V,R> map2(a: Option<T>, b: Option<V>, f: (T,V) -> R): Option<R> = a.flatMap { av -> b.map { bv -> f(av,bv) } }
// bv -> f(av,bv) = (V) -> R
// b.map { bv -> f(av,bv) } = (Option<V>, (V) -> R) -> Option<R> = Option<V> -> (V -> R) -> Option<R>
// av -> b.map { bv -> f(av,bv) } = (T) -> Option<R>
// a.flatMap { av -> b.map { bv -> f(av,bv) } } = (Option<T>, (T) -> Option<R>) -> Option<R>

fun <T,V,R> map2e(a: Option<T>, b: Option<V>, f: (T,V) -> R): Option<R> = b.flatMap { bv -> a.map { av -> f(av,bv) } }

fun <T,V,W,R> map3(a: Option<T>, b: Option<V>, c: Option<W>, f: (T,V,W) -> R): Option<R> = a.flatMap {
    av -> b.flatMap { bv -> c.map { cv -> f(av,bv,cv) } }
}

fun <T,V,W,X,R> map4(a: Option<T>, b: Option<V>, c: Option<W>, d: Option<X>, f: (T,V,W,X) -> R): Option<R> = a.flatMap {
    av -> b.flatMap { bv -> c.flatMap { cv -> d.map { dv -> f(av,bv,cv,dv) } } }
}

fun <T,V> Option<T>.traverse(f: (T) -> Option<V>): Option<Option<V>> = when (this) {
    is None -> Some(None)
    is Some -> Some(f(value))
}

// If something failed Option.Empty
fun <T, V> List<T>.traverse(f: (T) -> Option<V>): Option<List<V>> = TODO()

fun <A,B> lift(f: (A) -> B): (Option<A>) -> Option<B> = { a: Option<A> -> a.map(f) }
