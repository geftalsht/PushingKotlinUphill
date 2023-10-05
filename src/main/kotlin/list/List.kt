package list

sealed interface List<out T> {
    data object Empty: List<Nothing>
    data class Cons<T> (val head: T, val tail: List<T>): List<T>

    companion object {
        fun <T> of(vararg t: T): List<T> = when (t.isEmpty()) {
            true -> Empty
            false -> Cons(t.first(), of(*t.sliceArray(1 until t.size)))
        }
    }
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

@Deprecated(
    message = "Use List<T>.append(T) instead",
    replaceWith = ReplaceWith(expression = "this.append(t)", imports = [])
)
fun <T> List<T>.oldAppend(t: T): List<T> =
    when (this) {
        is List.Empty -> List.Cons(t, List.Empty)
        is List.Cons -> List.Cons(head, tail.oldAppend(t))
    }

tailrec fun <T> List<T>.isSorted(f: (T, T) -> Boolean): Boolean =
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

tailrec fun <T,V> List<T>.foldLeft(v: V, f: (T, V) -> V): V =
    when (this) {
        is List.Empty -> v
        is List.Cons -> tail.foldLeft(f(head, v), f)
    }

fun <T> List<T>.reverse(): List<T> =
    when (this) {
        is List.Empty -> List.Empty
        is List.Cons -> this.foldLeft(List.Empty as List<T>) { x, y -> List.Cons(x,y) }
    }

fun <T,V> List<T>.foldRightReverse(v: V, f: (T, V) -> V): V =
    reverse().foldLeft(v,f)

// TODO Understand
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

@Deprecated(
    message = "Use List<T>.foldRight(V, (T,V) -> V) instead",
    replaceWith = ReplaceWith(expression = "this.foldRight(v, f)", imports = [])
)
fun <T,V> List<T>.unsafeFoldRight(v: V, f: (T, V) -> V): V =
    when (this) {
        is List.Empty -> v
        is List.Cons -> f(head, tail.unsafeFoldRight(v,f))
    }

@Deprecated(
    message = "Use List<T>.foldLeft(V, (T,V) -> V) instead",
    replaceWith = ReplaceWith(expression = "this.foldLeft(v, f)", imports = [])
)
fun <T,V> List<T>.unsafeFoldLeft(v: V, f: (T, V) -> V): V =
    reverse().unsafeFoldRight(v,f)

fun <T> List<T>.append(t: T): List<T> =
    foldRight(List.Cons(t, List.Empty)) { a, b -> List.Cons(a, b) }

fun <T> List<T>.appendList(list: List<T>): List<T> =
    when (this) {
        is List.Empty -> list
        is List.Cons -> foldRight(list) { i, a -> List.Cons(i,a) }
    }

fun <T> List<T>.appendList1(list: List<T>): List<T> =
    foldRight(list) { i,a -> when (i) {
        is List.Empty -> a
        else -> List.Cons(i,a)
    }}

fun <T> List<T>.appendList2(list: List<T>): List<T> =
    when (this) {
        is List.Empty -> list
        is List.Cons -> List.Cons(head, tail.appendList2(list))
    }

// Write a function that concatenates a list of lists into a single list. Its runtime
// should be linear in the total length of all lists. Use functions already defined.
fun <T> List<List<T>>.flatten(): List<T> =
    foldLeft(List.Empty as List<T>) { i, a -> a.appendList(i) }

fun <T,U> List<T>.zip(list: List<U>): List<Pair<T, U>> {
    tailrec fun go(list1: List<T>, list2: List<U>, acc: List<Pair<T, U>>): List<Pair<T, U>> =
        when {
            list1 is List.Cons && list2 is List.Cons -> go(list1.tail(), list2.tail(), acc.append(Pair(list1.head, list2.head)))
            else -> acc
        }
    return go(this, list, List.Empty as List<Pair<T, U>>)
}

fun <T,V,Z> List<T>.zipWith1(otherList: List<V>, f: (T, V) -> Z): List<Z> =
    zip(otherList).map { pair -> f(pair.first, pair.second) }

fun <T,V,Z> List<T>.zipWith(other: List<V>, f: (T, V) -> Z): List<Z> =
    when {
        this is List.Cons && other is List.Cons -> List.Cons(f(head, other.head), tail.zipWith(other.tail, f))
        else -> List.Empty
    }

fun <T,V,Z> List<T>.zipWith2(other: List<V>, f: (T, V) -> Z): List<Z> = when (this) {
    is List.Empty -> List.Empty
    is List.Cons -> when (other) {
        is List.Empty -> List.Empty
        is List.Cons -> List.Cons(f(head, other.head), tail.zipWith(other.tail, f))
    }
}

tailrec fun <T> List<T>.hasSubSequence(otherList: List<T>): Boolean = when (otherList) {
    is List.Empty -> true
    is List.Cons -> when (this) {
        is List.Empty -> false
        is List.Cons -> when (head == otherList.head) {
            true -> tail.hasSubSequence(otherList.tail)
            false -> tail.hasSubSequence(otherList)
        }
    }
}

fun <T,V> List<T>.map(f: (T) -> V): List<V> =
    foldRight(List.Empty as List<V>) { i, a -> List.Cons(f(i),a) }

fun <T,V> List<T>.flatMap(f: (T) -> List<V>): List<V> =
    map(f).flatten()

// Do it with appendList() and foldRight()
fun <T,V> List<T>.flatMapR(f: (T) -> List<V>): List<V> =
    foldRight(List.Empty as List<V>) { i, a -> f(i).appendList(a) }

fun <T,V> List<T>.flatMapL(f: (T) -> List<V>): List<V> =
    foldLeft(List.Empty as List<V>) { i, a -> a.appendList(f(i)) }

fun <T,V> List<T>.flatMap1(f: (T) -> List<V>): List<V> =
    foldRight(List.Empty as List<V>) { i, a -> f(i).foldRight(a) { i1, a1 -> List.Cons(i1,a1) } }

// Implement filter using flatMap
fun <T> List<T>.filter1(f: (T) -> Boolean): List<T> =
    flatMap { i -> when (f(i)) {
        true -> List.Cons(i, List.Empty)
        false -> List.Empty
    } }

fun <T> List<T>.filter(f: (T) -> Boolean): List<T> =
    foldRight(List.Empty as List<T>) { i, a -> when (f(i)) {
        true -> List.Cons(i,a)
        false -> a
    }}

fun <T> List<T>.length(): Int =
    foldRight(0 ) { _, v -> v + 1 }
