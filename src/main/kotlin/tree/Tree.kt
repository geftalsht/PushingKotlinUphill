package tree

sealed class Tree<out A> {
    data class Leaf<A>(val value: A): Tree<A>()
    data class Branch<A>(val left: Tree<A>, val right: Tree<A>): Tree<A>()
}

fun <A> Tree<A>.size(): Int = when (this) {
    is Tree.Leaf -> 1
    is Tree.Branch -> left.size() + right.size()
}

fun Tree<Int>.maximum(): Int = when (this) {
    is Tree.Leaf -> value
    is Tree.Branch -> maxOf(left.maximum(), right.maximum())
}

fun <A> Tree<A>.depth(): Int =
    TODO()
