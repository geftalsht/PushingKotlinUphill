package functions

fun <A,B,C> partial1(a: A, f: (A,B) -> C): (B) -> C =
    { b -> f(a,b) }

fun <A,B,C> curry(f: (A, B) -> C): (A) -> (B) -> C =
    { a -> { b -> f(a,b) } }

fun <A,B,C> uncurry(f: (A) -> (B) -> C): (A, B) -> C =
    { a,b -> f(a)(b) }

fun <A,B,C> compose(f: (A) -> B, g: (B) -> C): (A) -> C =
    { a -> g(f(a)) }
