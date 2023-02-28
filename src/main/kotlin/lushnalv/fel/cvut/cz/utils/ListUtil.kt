package lushnalv.fel.cvut.cz.utils

fun <T1,T2> List<T1>.pairByIndex(other: List<T2>): List<Pair<T1,T2>> {
    if (this.size != other.size) {
        throw IllegalStateException("List should have same size")
    }

    val result = mutableListOf<Pair<T1,T2>>()

    for (i in indices) {
        result.add(this[i] to other[i])
    }

    return result
}