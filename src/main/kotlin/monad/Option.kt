package monad

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

// TODO understand this
fun <T,V,R> map2(a: Option<T>, b: Option<V>, f: (T,V) -> R): Option<R> = a.flatMap { av -> b.map { bv -> f(av,bv) } }

// TODO map3 and map4

fun <T,V> Option<T>.traverse(f: (T) -> Option<V>): Option<Option<V>> =
    TODO()

// TODO parTraverse*

fun <A,B> lift(f: (A) -> B): (Option<A>) -> Option<B> = { a: Option<A> -> a.map(f) }
