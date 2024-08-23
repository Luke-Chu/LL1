/**
 *
 * @author Luke
 * @Date 2022/06/23
 * @Time 17:03
 */
fun main() {
    //文件(p1 - p7)里的都文法
    val path = "src\\resources\\p4.txt"
    //w1 - w8 是输入串，用于测试
    val w1 ="(3 + 12) * 8 - 6 / 2 + 1" //测试算数文法P5: 加减乘除(+,-,*,/)P1(存在直接左递归)
    val w2 = "ab ab "    //测试二义文法P2(二义文法): no和not个数相等的所有串、等效的非二义文法P3:
    val w4 = "5<=6 and 7 >= 3 and not(12==12) or 8<2" //测试词法分析器/布尔文法：P4: true false or not and
    val w5 = "(5)>=6" //测试类型定义文法P5(存在直接左递归): int|float id,id;
    val w6 = "+3 * - 5"
    val w7 = "int var1,  var2;"
    val w8 = "30 0.5 23 "
    val lL1Analysis = LL1Analysis(path,w7)
    //可以指定getValues的参数，output是所有结果
    val output = lL1Analysis.getValues("output")
    println(output)
    Thread.sleep(500)
    /**
     * 
     * 以下皆为编程过程中的各种测试
     */

    /*val str1 = "E'→+TE'|ε"
    val str2 = "T→FT'"
    println(str1.startsWith("E'"))
    println(str2.startsWith("T"))*/

    /*val str = "AET'EBD"
    val s = "T'"
    println(str.length)
    println(str.indexOf(s)+s.length)
    println(str.substring(str.indexOf(s)+s.length))
    println(str)*/

    //println("E'S'GS'T".indexOf("S'"))

    /*val map = HashMap<TabItem,String>()
    map[TabItem("E","id")] = "E→TE'"
    val str = map[TabItem("E","id")]
    println(str)*/

    //println("\tE".format("%-10"))
    //println('a'.category.name)

    /*val str = "Eid"
    val id = "id"
    println(str.lastIndex)
    println(str.substring(0,str.lastIndex))*/

    /*val stack = Stack<String>()
    stack.push("$")
    stack.push("E")
    val s = StringBuilder()
    val queue = ArrayDeque<String>()
    queue.add("A")
    queue.add("B")
    queue.add("id")
    queue.forEach {
        s.append(it)
    }
    s.append("\n")
    s.append(stack)
    s.append("\n")
    s.append("EEE\n").append("AGAG\n").append("EFA")
    println(s.lines())*/

    /*val builder = StringBuilder()
    builder.append("PA'|")
    builder.apply { deleteAt(lastIndex) }
    println(builder)*/

    //println("E'→E'T|F".substring(2))
    //showProcess()
    /**
     * 下面是我对格式化输出的各种测试
     */
    /*println("abc".yellow())
    println("abc".blue())
    println("abc".red())
    println("abc".green())
    println("abc".magenta())
    println("abc".cyan())
    println("abc".lightGray())
    println("abc".lightRed())
    println("abc".lightGreen())
    println("abc".lightYellow())
    println("abc".lightBlue())
    println("abc".lightMagenta())
    println("abc".lightCyan())
    println("abc".black())
    println("文法如下".yellow().center(29,"–"))*/

    /*var lexicalAnalysis:LexicalAnalysis=LexicalAnalysis()
    var my_map=lexicalAnalysis.scanner(mutableListOf('a','+','(','b','-','c',')'))
    for (token in my_map)
    {
        print(token.getType())
    }*/

    /*val regexNum = Regex("\\d+")
    val regexOp = Regex("<=|>=|:=|<>|\\+|-|\\*|/|=|#|<|>|\\(|\\)|,|;|\\.|\\[|]|:|\\{|}|\"")
    val regTerminal = Regex("ac|acd")
    val str = "48 + 28 * 12 - 16/2"
    println(str.replace(regexNum,"id"))*/

    /*val str = "5<(6/4) and 7 - (2+1) < 3-1 (and not(12>13) or 3*5>2)"
    println("str1: $str")
    val regAdd = Regex("\\d+\\+\\d+")
    val regSub = Regex("\\d+-\\d+")
    val regMul = Regex("\\d+\\*\\d+")
    val regDiv = Regex("\\d+/\\d+")

    val str2 = str.replace(" ", "").replace(regAdd) {
        val i = it.value.split("+")
        (i[0].toInt() + i[1].toInt()).toString()
    }.replace(regSub) {
        val i = it.value.split("-")
        (i[0].toInt() - i[1].toInt()).toString()
    }.replace(regMul) {
        val i = it.value.split("*")
        (i[0].toInt() * i[1].toInt()).toString()
    }.replace(regDiv) {
        val i = it.value.split("/")
        (i[0].toInt() / i[1].toInt()).toString()
    }
    println("str2: $str2")
    val regOpe = Regex(
        "(\\(\\d+\\)|\\d+)\\+(\\(\\d+\\)|\\d+)|" +
                "(\\(\\d+\\)|\\d+)-(\\(\\d+\\)|\\d+)|" +
                "(\\(\\d+\\)|\\d+)\\*(\\(\\d+\\)|\\d+)|" +
                "(\\(\\d+\\)|\\d+)/(\\(\\d+\\)|\\d+)|"
    )
    val str3 = str2.replace(regOpe) {
        val b = it.value.replace("(", "").replace(")", "")
        if (b.contains("+")) {
            val i = b.split("+")
            (i[0].toInt() + i[1].toInt()).toString()
        } else if (b.contains("-")) {
            val i = b.split("-")
            (i[0].toInt() - i[1].toInt()).toString()
        } else if (b.contains("*")) {
            val i = b.split("*")
            (i[0].toInt() * i[1].toInt()).toString()
        } else if (b.contains("/")){
            val i = b.split("/")
            (i[0].toInt() / i[1].toInt()).toString()
        }else{
            ""
        }
    }
    println("str3: $str3")
    val regB1 = Regex("\\(\\d+\\)")
    val regLessOrEqu = Regex("\\d+<=\\d+")
    val regBiggerOrEqu = Regex("\\d+>=\\d+")
    val regLess = Regex("\\d+<\\d+")
    val regBigger = Regex("\\d+>\\d+")
    val regexBoolean = Regex(
        "(\\(\\d+\\)|\\d+)<=(\\(\\d+\\)|\\d+)|" +
                "(\\(\\d+\\)|\\d+)>=(\\(\\d+\\)|\\d+)|" +
                "(\\(\\d+\\)|\\d+)<(\\(\\d+\\)|\\d+)|" +
                "(\\(\\d+\\)|\\d+)>(\\(\\d+\\)|\\d+)"
    )
    val str4 = str3.replace(regB1) {
        it.value.replace("(", "").replace(")", "")
    }.replace(regexBoolean) {
        val b = it.value.replace("(", "").replace(")", "")
        if (b.contains("<=")) {
            val e = b.split("<=")
            (e[0].toInt() <= e[1].toInt()).toString()
        } else if (b.contains(">=")) {
            val e = b.split(">=")
            (e[0].toInt() >= e[1].toInt()).toString()
        } else if (b.contains("<")) {
            val e = b.split("<")
            (e[0].toInt() < e[1].toInt()).toString()
        } else if (b.contains(">")){
            val e = b.split(">")
            (e[0].toInt() > e[1].toInt()).toString()
        }else{
            ""
        }
    }
    println("str4: $str4")*/
    /*val Vt = mutableSetOf<String>()
    Vt.add("ac")
    Vt.add("acd")
    println(Vt)
    val set = mutableSetOf<String>()
    set.addAll(Vt.sortedDescending())
    println(set)*/
    /*val regex by lazy {
        val builder = StringBuilder()
        Vt.sortDescending()
        Vt.forEach { builder.append("$it|") }
        builder.deleteAt(builder.lastIndex)
        Regex(builder.toString())
    }
    val str = "acacdacacacd"
    regex.findAll(str).forEach { println(it.value) }*/
    /*val regexDem = Regex("[(),;.\\[\\]:{}\"]")
    val regexOp = Regex("<=|>=|:=|<>|\\+|-|\\*|/|=|#|<|>")
    val regexOpDouble = Regex("<=|>=|:=|<>")
    val str = "if id1 <= id2 and 8/2 >= 3-2 or 7*8 and a+b"
    regexOp.findAll(str).forEach { println(it.value) }*/
    /*val r1 = Regex("\\s")
    val r2 = Regex("[a-z]+")
    val a = "int  \tid, a = 85;"
    r2.findAll(a).forEach { println(it.value) }*/
    /*val s = "andF'"
    val Vt = mutableListOf<String>()
    var newS = s.map { c ->
        if (!c.isLowerCase()){
            "$"
        }else{
            c
        }
    }
    val builder = StringBuilder()
    newS.forEach { builder.append(it) }
    for ((index, c) in builder.toString().withIndex()){
        if (c == '$' && !s[index].isUpperCase() && s[index] != '\''){
            Vt.add(s[index].toString())
        }
    }
    val cList = builder.toString().split("$")
    for (c in cList){
        if (c.isNotEmpty()){
            Vt.add(c)
        }
    }
    println(Vt)*/
    /*val newS = "andFT'".filter {
        it.category.value != 1 || it.code != 39
    }

    println(newS)*/
    //println('\''.category.value)
    //println("andFT'".)
}

fun showProcess() {
    val path = "src\\main\\resources\\p3.txt"
    val w = "id*id+id"
    val w2 = "aabbbbaa"
    val lL1Analysis = LL1Analysis(path, w)
    //println(lL1Analysis.getValues("output"))
    /*lL1Analysis.readFile()
    println("原文法为：")
    println(lL1Analysis.getValues("grammar"))
    lL1Analysis.getValues("check")
    lL1Analysis.getVnVt()
    print("开始符号：")
    println(lL1Analysis.getValues("start"))
    print("非终结符：")
    println(lL1Analysis.getValues("Vn"))
    print("终结符：")
    println(lL1Analysis.getValues("Vt"))
    lL1Analysis.calFirstAndFollow()
    println("FIRST集合：")
    println(lL1Analysis.getValues("FIRST"))
    println("FOLLOW集合：")
    println(lL1Analysis.getValues("FOLLOW"))
    lL1Analysis.conPreAnaTab()
    println("预测分析表：")
    lL1Analysis.getValues("Tab")
    val w = "id*id+id"
    val w2 = "aabbbbaa"
    lL1Analysis.anaProgram(w)
    println("$w 的分析过程：")
    lL1Analysis.getValues("Pro")*/
}