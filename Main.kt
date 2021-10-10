package calculator

import java.math.BigInteger

val stack = StackWithList()
val exp = mutableListOf<String>()
val vars = mutableMapOf<String, BigInteger>()
val prior = mapOf("(" to 0, ")" to 0, "+" to 1, "-" to 1, "*" to 2, "/" to 2, "^" to 3)


fun main() {
    while (true) {
        val s = readLine()!!
        if (s.isBlank()) continue
        if (s == "/exit") break
        if (s == "/help") {
            println("The program calculates the sum of numbers")
            continue
        }
        try {
            if (s[0] == '/') throw Exception("Unknown command")
            val ss = s.checkStr()
            decomp(ss)
//println("exp= $exp.")
            if (exp.isEmpty()) {
                println("Invalid expression")
                continue
            }
            comp(exp)
        } catch (e: Exception) {
            println(e.message)
            continue
        }
    }
    println("Bye!")
}

fun String.checkStr(): List<String> {
    var str = this
    if (filter { it == '=' }.count() > 1)  throw Exception("Invalid assignment")
    if (str.contains(Regex("[*/]{2,}")))  throw Exception("Invalid expression")
    str = str.replace("---", "-")
    str = str.replace("--", "+")
    str = str.replace(Regex("[+]+"), "+")
    for (w in listOf("+", "=", "*", "/", "^", "(", ")")) {
        str = str.replace(w, " $w ")
    }
    str = str.replace("\\s+".toRegex(), " ").trim()
    return str.split(" ")
}

fun decomp(list: List<String>) {
    exp.clear()
    for (w in list) {
        when (w) {
            "(" -> stack.push(w)
            ")" -> { while (!stack.isEmpty() && stack.peek() != "(") {
                        val st = stack.pop().toString()
                        exp.add(st)
                    }
                    if (stack.isEmpty())
                        exp.add(w)
                    else
                        stack.pop()
                    }
            "+" -> pushPrior(w)
            "-" -> pushPrior(w)
            "*" -> pushPrior(w)
            "/" -> pushPrior(w)
            "^" -> pushPrior(w)
            "=" -> stack.push(w)
            else -> exp.add(w)
        }
    }
    while (!stack.isEmpty()) {
        exp.add(stack.pop().toString())
    }
    if (exp.contains("(") || exp.contains(")")) exp.clear()
}

fun comp(list: List<String>) {
    var res = BigInteger.ZERO
    var isCalc = false
    var isAss = false
    val p = mutableListOf<String>()
    for (w in list) {
        if (w in listOf("+", "-", "*", "/", "^", "=")) {
            isCalc = true
            isAss = isAss || w == "="
            val op1 = p[p.lastIndex - 1]
            val op2 = p[p.lastIndex]
            res = when (w) {
                "+" -> op1.opToInt() + op2.opToInt()
                "-" -> op1.opToInt() - op2.opToInt()
                "*" -> op1.opToInt() * op2.opToInt()
                "/" -> op1.opToInt() / op2.opToInt()
//                "^" -> op1.opToInt().pow(op2.opToInt())
                "=" -> assign(op1, op2) ?: BigInteger.ZERO
                else -> BigInteger.ZERO
            }
            p.removeAt(p.lastIndex)
            p[p.lastIndex] = res.toString()
        } else {
            p.add(w)
        }
    }
    if (!isCalc) {
        val v = p.last()
        res = if(v.isNumber()) v.toBigInteger()
            else if (vars.containsKey(v)) vars[v]
            else {
                throw Exception("Unknown variable")
            } ?: BigInteger.ZERO
    }
    if (!isAss) println(res)
}

fun Int.pow(m: Int): Int {
    var p = 1
    for (i in 1..m) p *= this
    return p
}

//fun String.opToInt(): Int = if (this.isNumber()) this.toInt() else vars[this] ?: 0
fun String.opToInt(): BigInteger = if (this.isNumber()) this.toBigInteger() else vars[this]!!

fun String.isNumber() = this.matches("[-+]?[0-9]+\\b".toRegex())

fun String.isIdent() = this.matches("[a-zA-Z]+\\b".toRegex())

fun assign(k: String, v: String): BigInteger? {
    val isKvalid = k.isIdent()
    val isVvalid = v.isIdent()
    val isVnumber = v.isNumber()

    if (!isKvalid) throw Exception("Invalid identifier")
    if (!isVnumber && !isVvalid) throw Exception("Invalid assignment")
    if (!isVnumber && !vars.containsKey(v)) throw Exception("Unknown variable")

    if (isVnumber) {
        vars[k] = v.toBigInteger()
    } else {
        vars[k] = vars[v]!!
    }
    return vars[k]
}

fun pushPrior(w: String) {
    if (!stack.isEmpty()) {
        var st = stack.peek().toString()
        while (prior[w]!! <= prior[st]!!) {
            st = stack.pop().toString()
            exp.add(st)
            if (stack.isEmpty()) break
            st = stack.peek().toString()
        }
        stack.push(w)
    } else {
        stack.push(w)
    }
}

class StackWithList{
    val elements: MutableList<Any> = mutableListOf()

    fun isEmpty() = elements.isEmpty()

    fun size() = elements.size

    fun push(item: Any) = elements.add(item)

    fun pop() : Any? {
        val item = elements.lastOrNull()
        if (!isEmpty()){
            elements.removeAt(elements.size -1)
        }
        return item
    }
    fun peek() : Any? = elements.lastOrNull()

    override fun toString(): String = elements.toString()
}
