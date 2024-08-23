import kolor.*
import org.apache.commons.lang3.StringUtils
import java.io.File
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayDeque
import kotlin.collections.set

/**
 * 此程序用于进行LL1分析,用法如下,先创建LL1Analysis对象,构造函数有两个参数
 * @param path 用于分析的文法的文件的硬盘上的路径
 * @param inputStr 用于分析的输入串, 该串是经过词法分析后得到的词法记号流
 * 创建LL1Analysis对象后会自动进行LL1分析,想要查看LL1分析结果可通过getValues()函数查看
 * 如想要得到最终的输出结果用于显示，则调用getValues("output")
 * 如仅仅想要查看构造的预测分析表，则调用getValues("Tab")
 * @author Luke
 * @Date 2022/06/23
 * @Time 17:32
 */
class LL1Analysis(private val path: String, inputStr: String) {

    /**
     * 各种数据结构都定义在此处
     */
    companion object {
        private lateinit var initialGrammar: List<String>   //初始文法
        private lateinit var grammar: List<String>  //消除左递归后的文法(如果不存在左递归就直接等于初始文法)
        private var isLeft_ = false //标记是否存在左递归,对外提供接口而已
        private var start = ""  //存开始文法符号
        private val reOpe = listOf("<=", ">=", "==", "!=")
        private var Vn = mutableSetOf<String>() //非终结符
        private var Vt = mutableSetOf<String>() //终结符
        private var FIRST = mutableMapOf<String, HashSet<String>>() //每个非终结符的FIRST集
        private var FOLLOW = mutableMapOf<String, HashSet<String>>()    //每个非终结符的FOLLOW集
        private var inputSym = mutableListOf<String>()  //输入符号(实际上就是终结符加$符号),方便后续构建预测分析表
        private var preAnaTab = mutableMapOf<TabItem, String>() //存预测分析表,TabItem是自定义数据结构,存非终结符和终结符
        private var stack = Stack<String>() //分析程序用的栈,存文法符号
        private var inputQueue = ArrayDeque<String>()   //分析程序用的输入队列,存输入符号
        private var stackRecord = StringBuilder()   //用于记录栈内容的变化情况，用于后续输出显示
        private var inputRecord = StringBuilder()   //用于记录输入缓冲区的变化情况，用于后续输出显示
        private var action = StringBuilder()    //用于记录分析的每一步的动作，此处仅仅输出字符串
        private var output = "" //整个程序的输出结果
    }

    init {
        output = simpleLL1ParseAutoGenePro(inputStr)
    }

    /**
     * 简易LL(1)分析自动生成程序
     * @param inputStr 词法分析后得到的输入串, 如id+id*id
     */
    private fun simpleLL1ParseAutoGenePro(inputStr: String) = StringBuilder().apply {
        var num = 0
        //第一步：从文件读取原文法
        append("读取原文法为: ".red().center(81, "—")).append("\n")
        readFile()
        initialGrammar.addGrammar(this, num)
        append("\n")
        //检查读取的原文法是否存在直接左递归
        if (checkLeftRecursion(initialGrammar)) {
            append("原文法存在直接左递归,消除直接左递归后得到如下文法: ".red().center(68, "—")).append("\n")
            grammar.addGrammar(this, num)
        } else {
            append("原文法不存在直接左递归".red().center(78, "—")).append("\n")
        }
        //第二步：根据文法划分终结符和非终结符
        append("\n")
        getVnVt()
        append("非终结符Vn: ".red().center(83, "—")).append("\n\t\t")
        Vn.forEach { append(it.lightCyan()).append("\t") }
        append("\n")
        append("终结符Vt: ".red().center(84, "—")).append("\n\t\t")
        Vt.forEach { append(it.lightCyan()).append("\t") }
        append("\n")
        //第三步：计算每个非终结符的FIRST集和FOLLOW集
        append("\n")
        calFirstAndFollow()
        append("FIRST集为: ".red().center(85, "—")).append("\n")
        FIRST.addFirstOrFollow(this, num, 0)
        append("FOLLOW集为: ".red().center(85, "—")).append("\n")
        FOLLOW.addFirstOrFollow(this, num, 1)
        //第四步：构造预测分析表(并且计算非终极符的同步记号)
        conPreAnaTab()
        comSynTokens()
        append("\n")
        append("该文法的预测分析表为: ".red().center(80, "—")).append("\n")
        val count = inputSym.size
        append("____________")
        append("".center(count * 13, "_"))
        append("\n")
        append("|          |")
        append("输入符号".center(count * 13))
        repeat(4) { deleteAt(lastIndex) }
        append("|\n")
        append("| 非终结符  |")
        append("".center(count * 13, "_"))
        deleteAt(lastIndex)
        append("|\n")
        append("|__________|")
        val aveWidth = 13
        for (input in inputSym) {
            append(input.lightYellow().center(13 - 1 + 9, "_"))
            append("|")
        }
        append("\n")
        for (vn in Vn) {
            append("%-5s".format("|").replace(" ", "_"))
            append(
                "%-15s".format(
                    if (num % 2 == 0) vn.lightBlue() else vn.lightGreen()
                ).replace(" ", "_")
            ).append("|")
            for (input in inputSym) {
                aveWidth - 10
                append("%-2s".format("").replace(" ", "_"))
                append(
                    "%-19s".format(
                        if (num % 2 == 0) (preAnaTab[TabItem(vn, input)] ?: "").lightBlue()
                        else (preAnaTab[TabItem(vn, input)] ?: "").lightGreen()
                    ).replace(" ", "_")
                )
                append("|")
            }
            num++
            append("\n")
        }
        //第五步：根据输入串w和预测分析表G分析过程
        append("\n")
        append("输入串为: ".red().center(85, "—")).append("\n\t\t")
        append(inputStr.lightMagenta()).append("\n")
        val outputStr = LexicalAnalysis(inputStr).getOutput()
        append("词法分析的输出为: ".red().center(82, "—")).append("\n\t\t")
        append(outputStr.lightMagenta()).append("\n")
        anaProgram(outputStr)
        val width = inputRecord.lines()[0].length
        append("\n")
        append("表: 预测分析器接受输入${outputStr}的动作".red().center(78, "—")).append("\n")
        append("".center(23 + width + 21 + 24, "_")).append("\n")
        append("|").append("栈".lightYellow().center(32, "_")).append("|")
        append("输入".lightYellow().center(width + 22, "_")).append("|")
        append("动作".lightYellow().center(33, "_")).append("|\n")
        val len = stackRecord.lines().lastIndex
        var index = 0
        while (index <= len) {
            append("|____").append(
                "%-29s".format(
                    if (num % 2 == 0) stackRecord.lines()[index].lightBlue()
                    else stackRecord.lines()[index].lightGreen()
                ).replace(" ", "_")
            ).append("|")
            append(
                "%${width + 19}s".format(
                    if (num % 2 == 0) inputRecord.lines()[index].lightBlue()
                    else inputRecord.lines()[index].lightGreen()
                ).replace(" ", "_")
            ).append("____|")
            if (index != 0) {
                append("____").append(
                    if (action.lines()[index - 1].contains("error")) {
                        "%-29s".format(action.lines()[index - 1].lightRed())
                            .replace(" ", "_")
                    } else {
                        "%-29s".format(
                            if (num % 2 != 0) action.lines()[index - 1].lightBlue()
                            else action.lines()[index - 1].lightGreen()
                        ).replace(" ", "_")
                    }
                ).append("|")
            } else {
                append("__________________________|")
            }
            append("\n")
            num++
            index++
        }
    }.toString()

    /**
     * 读取文件
     */
    private fun readFile() {
        try {
            initialGrammar = File(path).readLines()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * 用于判断输入的文法是否存在直接左递归，若存在则需要消除直接左递归
     * @param initialGrammar 读入的文法
     * @return true存在直接左递归 false不存在直接左递归
     */
    private fun checkLeftRecursion(initialGrammar: List<String>): Boolean {
        var boolean = false
        val tempGrammar = mutableListOf<String>()
        for (production in initialGrammar) {
            //先将产生式划分左边和右边部分
            val left = production.split("→")[0]
            val right = production.split("→")[1].split("|")
            var n = -1  //n用来记录最后一个非左递归的产生式右部的位置
            var m = -1  //m用来记录最后一个左递归的产生式右部的位置
            for ((index, item) in right.withIndex()) {
                //eg: 左部为S，右部以S开头并且不以S'开头，则存在直接左递归，用m记录位置
                if (item.startsWith(left) && !item.startsWith("$left'")) m = index
                //否则不存在直接左递归，用n记录位置
                else n = index
            }
            //如果m > -1 则说明存在直接左递归
            if (m > -1) {
                boolean = true
                //存在直接左递归，就开始拼接新产生式，先拼接左部
                val proBuilder = StringBuilder()
                proBuilder.append("$left→")
                var index = m + 1
                while (index <= n) {
                    //说明存在左递归的产生式，拼接第一个产生式
                    proBuilder.append(right[index]).append("$left'|")
                    index++
                }
                //拼接→符号
                proBuilder.apply {
                    deleteAt(lastIndex)
                    append("\n$left'→")
                }
                //拼接产生式右部
                index = 0
                while (index <= m) {
                    //新产生的第二个产生式
                    proBuilder.append(right[index].substring(left.length)).append("$left'|")
                    index++
                }
                //第二个产生式右部还需加一个ε符号
                proBuilder.apply { append("ε") }
                tempGrammar.addAll(proBuilder.lines())  //将builder转换成List集合存储
            } else {
                tempGrammar.add(production)
            }
        }
        //如果替换过文法就输入对应的最终文法，否则还是原文法
        grammar = if (boolean) {
            isLeft_ = true
            tempGrammar
        } else {
            initialGrammar
        }
        return boolean
    }

    /**
     * 获取非终结符和终结符
     */
    private fun getVnVt() {
        val tempVt = mutableSetOf<String>()
        start = grammar[0].split("→")[0].trim()
        grammar.forEach {
            //产生式左边的都是非终结符
            Vn.add(it.split("→")[0].trim())
        }
        grammar.forEach {
            val right = it.split("→")[1].split("|")
            right.forEach { s ->
                var newS = s
                //先判断有没有双元关系运算符
                reOpe.forEach { r ->
                    if (s.contains(r)) {
                        Vt.add(r)
                        newS = newS.replace(r, "@")
                    }
                }
                //newS=N@N
                //先将所有非小写字母替换成$符号，得到一个字符List集合
                val newS1 = newS.map { c ->
                    if (!c.isLowerCase() && c != '@') {
                        "$"
                    } else {
                        c
                    }
                }.joinToString("") //将List集合转换为String字符串,该字符串是不含大写字母和'符号的
                // newS1=$@$
                //遍历字符串每个字符，如果字符不是$，且不是大写字母，也不是'符号，
                // 则就是其他终结符，加入Vt(实际上这是在查找特殊字符的终结符)
                for ((index, c) in newS1.withIndex()) {
                    if (c == '$' && !newS[index].isUpperCase() && newS[index] != '\'') {
                        tempVt.add(newS[index].toString())
                    }
                }
                //然后再将String字符串通过$符号划分，划分的list集合的元素都是小写字母或者空
                //所以如果不是空串，就将小写字母加入终结符
                val cList = newS1.split("$")
                for (c in cList) {
                    if (c.isNotEmpty() && c != "@") {
                        tempVt.add(c)
                    }
                }
            }
        }
        tempVt.remove("ε")
        Vt.addAll(tempVt.sortedDescending())
        inputSym.addAll(Vt)
        inputSym.add("$")
    }

    /**
     * 计算每个非终结符的FIRST集合和FOLLOW集和
     */
    private fun calFirstAndFollow() {
        for (char in Vn) {
            //计算FIRST集
            val firstSet = HashSet<String>()
            firstHelper(char, firstSet)
            FIRST[char] = firstSet
        }
        for (char in Vn) {
            //计算FOLLOW集
            val followSet = HashSet<String>()
            followHelper(char, followSet)
            FOLLOW[char] = followSet
        }
    }

    /**
     * 构造预测分析表
     */
    private fun conPreAnaTab() {
        for (production in grammar) {
            val left = production.split("→")[0] //产生式左部
            val right = production.split("→")[1].split("|")
            for (item in right) {
                //对于产生式A→α
                val firstSet = HashSet<String>()
                getStringFirst(item, firstSet)
                if (firstSet.contains("ε")) {
                    firstSet.remove("ε")
                    //如果ε在FIRST(α)中，对FOLLOW(A)的每个终结符b,把A→α加入[M,b]
                    for (b in FOLLOW[left]!!) {
                        preAnaTab[TabItem(left, b)] = "$left→$item"
                    }
                    //包括$,把把A→α加入[M,$]
                    preAnaTab[TabItem(left, "$")] = "$left→$item"
                }
                //对FIRST(α)的每个终结符a，把A→α加入[M,a]
                for (a in firstSet) {
                    preAnaTab[TabItem(left, a)] = "$left→$item"
                }
            }
        }
    }

    /**
     * 计算每个非终结符的同步记号，根据其FOLLOW集得到
     */
    private fun comSynTokens() {
        for (vn in Vn) {
            for (input in inputSym) {
                if (preAnaTab[TabItem(vn, input)] == null && FOLLOW[vn]?.contains(input) == true) {
                    preAnaTab[TabItem(vn, input)] = "sync"
                }
            }
        }
    }

    /**
     * 根据输入串和分析表进行LL(1)预测分析
     * @param inputStr 输入文法串(要求是LL(1)文法)
     */
    private fun anaProgram(inputStr: String) {
        //初始状态：$S在栈里，S是文法开始符号并且在栈顶
        stack.push("$")
        stack.push(start)
        //stackRecord是用于记录每一步栈的内容，用于最后输出显示
        stack.forEach { stackRecord.append(it) }
        stackRecord.append("\n")
        //将w$送入输入缓冲区
        getInputChar(inputStr, inputQueue)
        var x = stack.peek()
        var a = inputQueue.first()
        //inputRecord是用于记录每一步输入缓冲区的内容，用于最后输出显示
        inputQueue.forEach { inputRecord.append(it) }
        inputRecord.append("\n")
        while (x != "$") {   //栈非空
            if (x == a) {
                //匹配成功，把x从栈顶弹出并让指针指向输入缓冲区下一个符号
                stack.pop()
                inputQueue.removeFirst()
                a = inputQueue.first()
                //action是用于记录分析的动作即输出，用于最后输出显示
                action.append("匹配$x\n")
            } else if (Vt.contains(x)) {
                //栈顶是终结符，但不是输入缓冲区第一个非终结符a，所以报错
                action.append("error,${x}不等于$a")
                break
            } else if (preAnaTab[TabItem(x, a)] == null) {
                //访问M[A,a]预测分析表空白条目，出错,跳过a
                action.append("error,跳过$a\n")
                inputQueue.removeFirst()
                a = inputQueue.first()
            } else if (preAnaTab[TabItem(x, a)] == "sync") {
                //如果a正好在X的同步记号集合中，无序跳过任何记号；X被弹出
                stack.pop()
                action.append("error,弹出${x}\n")
            } else {
                //访问M[A,a]预测分析表，先输出对应产生式，然后弹栈x，再将产生式右部从右向左入栈
                action.append("输出${preAnaTab[TabItem(x, a)]}").append("\n")
                stack.pop()
                preAnaTab[TabItem(x, a)]?.let { pushStack(it, stack) }
            }
            //记录栈和输入缓冲区内容
            inputQueue.forEach { inputRecord.append(it) }
            inputRecord.append("\n")
            stack.forEach { stackRecord.append(it) }
            stackRecord.append("\n")
            //令x等于栈顶符号
            x = stack.peek()
        }
    }

    /**
     * 查询非终结符的FIRST集合
     * @param char 非终结符
     * @param first FIRST集合
     */
    private fun firstHelper(char: String, first: HashSet<String>) {
        for (production in grammar) {
            val left = production.split("→")[0].trim()
            //找产生式左部和输入的非终结符相等的产生式
            if (left == char) {
                val right = production.split("→")[1].split("|")
                for (item in right) {
                    //(直接收取) 若X∈Vn,且有X→aα,则a∈FIRST(X),若有X→ε,则ε∈FIRST(X)
                    //isStartWithVt(item, first)函数中有判断ε∈FIRST(Yi),如果ε∈FIRST(Yi),则令ε∈FIRST(X)
                    if (isStartWithVt(item, first)) {
                        //如果该右部以终结符开头，则找到了一个元素，开始查看下一个右部
                        continue
                    } else {//(反复传送) 若X→Y1Y2···Yk,且Y1∈Vn
                        //如果是以非终结符开头，则返回该非终结符
                        val tempVn = getStartWithVn(item)
                        if (tempVn.isNotEmpty()) {
                            if (isDeduceNull(tempVn)) {
                                //(透明特性)若Yi→*ε,i=i+1,重复FIRST(Yi)-{ε} ⊆ FIRST(X)
                                //如果能推出空串，则还需对该右部进行下一个字符处理，先截取
                                val nextItem = item.substring(tempVn.length)
                                //然后获取下一个非终结符，这里显然可能获取到""字符串，但是不用处理
                                val nextVn = getStartWithVn(nextItem)
                                val nextFirst = HashSet<String>()
                                //递归调用，firstHelper()方法中，本质上已经对""串进行处理了
                                firstHelper(nextVn, nextFirst)
                                //将下一个非终结符所找到的first集加入此first集
                                first.addAll(nextFirst)
                            } else {
                                //若Yi不能推出ε,则令FIRST(Yi)-{ε} ⊆ FIRST(X)
                                val nextFirst = HashSet<String>()
                                firstHelper(tempVn, nextFirst)
                                first.addAll(nextFirst)
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 计算非终结符的FOLLOW集合
     * @param char 非终结符
     * @param follow FOLLOW集
     */
    private fun followHelper(char: String, follow: HashSet<String>) {
        if (char == start) {
            //开始符号，则$∈FOLLOW(S)
            follow.add("$")
        }
        for (production in grammar) {
            val left = production.split("→")[0]
            val right = production.split("→")[1].split("|")
            for (item in right) {
                if (item.contain(char)) {
                    val size = item.length
                    val index = item.indexOf(char) + char.length
                    if (index < size) {
                        //(直接收取)A →···Ba···，则a∈FOLLOW(B)
                        isStartWithVt(item.substring(index), follow)
                        val vn = getStartWithVn(item.substring(index))
                        //(直接收取)A →···BP···，则FIRST(P)-{ε} ⊆ FOLLOW(B)
                        getStringFirst(item.substring(index), follow)
                        if (FIRST[vn]?.contains("ε") == true) {
                            //(透明特性)A→αBβ(ε∈β)，则FOLLOW(A)⊆FOLLOW(B)
                            FOLLOW[left]?.let { follow.addAll(it) }
                        }
                    } else if (index == size) {
                        //该非终结符是最后一个字符：(反复传送)A→αB，则FOLLOW(A)⊆FOLLOW(B)
                        FOLLOW[left]?.let { follow.addAll(it) }
                    }
                }
            }
        }
    }

    /**
     * 获取一个文法符号串的FIRST集，用于计算FOLLOW集
     * @param str 文法符号串
     * @param follow FOLLOW集合
     */
    private fun getStringFirst(str: String, follow: HashSet<String>) {
        if (!isStartWithVt(str, follow)) {
            val vn = getStartWithVn(str)
            if (vn.isNotEmpty() && FIRST[vn]?.contains("ε") == true) {
                //该字符的FIRST集包含空串，还需要找下一个字符的FIRST集
                val index = str.indexOf(vn) + 1
                val size = str.length
                if (index < size) {
                    //后面还有字符，获取后面字符的FIRST集
                    val tempStr = str.substring(index)
                    val tempFollow = HashSet<String>()
                    getStringFirst(tempStr, tempFollow)
                    //tempFollow.remove("ε")
                    follow.addAll(tempFollow)
                }
            }
            if (vn.isNotEmpty()) {
                FIRST[vn]?.let {
                    val temp = HashSet<String>()
                    temp.addAll(it)
                    temp.remove("ε")
                    follow.addAll(temp)
                }
            }
        }
    }

    /**
     * 自定义扩展函数，用于判断某个文法符号串是否包含某个非终结符
     * @param char 非终结符
     */
    private fun String.contain(char: String): Boolean {
        return if (char.length > 1) {
            this.contains(char)
        } else {
            val size = this.length - 1
            val index = this.indexOf(char)
            if (index != -1 && index + 1 <= size) {
                '\'' != this[index + 1]
            } else index == size
        }
    }

    /**
     * 判断某个非终结符是否能推出空串
     * @param char 非终结符
     */
    private fun isDeduceNull(char: String): Boolean {
        for (production in grammar) {
            val left = production.split("→")[0].trim()
            if (left == char) {
                val right = production.split("→")[1].split("|")
                for (c in right) {
                    if ("ε" == c) {
                        return true
                    }
                }
            }
        }
        return false
    }

    /**
     * 判断某个产生式右部是否以终结符开始，如果是，将该终结符加入FIRST集
     * @param s 某个产生式右部的字符串
     * @return true表示是，false表示不是
     */
    private fun isStartWithVt(s: String, first: HashSet<String>): Boolean {
        for (vt in Vt) {
            if (s.startsWith(vt)) {
                //(直接收取) 若X∈Vn,且有X→aα,则a∈FIRST(X),若有X→ε,则ε∈FIRST(X)
                first.add(vt)
                return true
            }
        }
        //如果ε∈FIRST(Yi),则令ε∈FIRST(X)
        if ("ε" == s) {
            first.add("ε")
            return true
        }
        return false
    }

    /**
     * 获取某个串的开始的非终结符
     * @param s 某个产生式右部的某个字符串
     */
    private fun getStartWithVn(s: String): String {
        for (vn in Vn) {
            if (s.startsWith(vn) && !s.startsWith("$vn'")) {
                return vn
            }
        }
        return ""
    }

    /**
     * 将输入串划分成终结符存入队列中，此队列用于输入缓冲区的数据结构
     * @param w 输入串，如"id*id+id"
     * @param q 输入队列
     */
    private fun getInputChar(w: String, q: ArrayDeque<String>) {
        var tempW = w
        while (tempW.isNotEmpty()) {
            var i = -1
            for ((index, vt) in Vt.withIndex()) {
                if (tempW.startsWith(vt)) {
                    //找匹配的终结符，然后加入队列
                    q.add(vt)
                    tempW = tempW.substring(vt.length)
                    break
                }
                i = index
            }
            if (i == Vt.size - 1) {
                //这是判断错误的情况，如果一个输入串没有该文法匹配的终结符，必定出错了
                //此处不处理错误，交给分析器处理，此处选择继续将剩下的输入符号加入队列中
                q.add(tempW[0].toString())
                tempW = tempW.substring(1)
            }
        }
        q.add("$")
    }

    /**
     * 将产生式右部从右向左依次入栈
     * @param production 一个产生式
     * @param stack 存放文法符号的栈
     */
    private fun pushStack(production: String, stack: Stack<String>) {
        var right = production.split("→")[1]
        var lastIndex = right.lastIndex
        while (lastIndex != -1) {
            if (right == "ε") {
                //如果产生式右部是空串，则什么也不做，不入栈
                break
            } else if (right[lastIndex] == '\'') {
                //这是为了应对字符E'、T'···之类的
                stack.push("${right[lastIndex - 1]}'")
                right = right.substring(0, lastIndex - 1)
            } else if (right.endsWith(">=") || right.endsWith("<=")
                || right.endsWith("!=") || right.endsWith("==")){
                //这是为了应对两个符号的关系运算符：>=,<=,!=,==
                stack.push(right.substring(lastIndex-1))
                right = right.substring(0, lastIndex - 1)
            } else if (right[lastIndex].isLowerCase()) {
                //如果是小写字母，说明是终结符，根据终结符匹配，匹配到后就入栈
                for (vt in Vt) {
                    if (right.endsWith(vt)) {
                        stack.push(vt)
                        right = right.substring(0, lastIndex - vt.length + 1)
                        break
                    }
                }
            } else {
                //剩下的都是一个字符的了，直接入栈
                stack.push(right[lastIndex].toString())
                right = right.substring(0, lastIndex)
            }
            //上面产生式右部每匹配到一个符号然后入栈，就截断该符号，直到所有符号都入栈，此时该字符串就为空了
            lastIndex = right.lastIndex
        }
    }

    /**
     * 自定义扩展函数，用于将字符串居中对齐
     * @param size 字符串所占长度
     * @param padStr 空格处用什么字符串替代
     */
    private fun String.center(size: Int, padStr: String): String {
        return StringUtils.center(this, size, padStr)
    }

    /**
     * 自定义扩展函数，用于将字符串中间对齐，默认用空格填充
     * @param size 字符串所占用长度
     */
    private fun String.center(size: Int): String {
        return StringUtils.center(this, size)
    }

    /**
     * 用于拼接原文法和消除左递归后的文法(如果存在左递归)
     * @param builder StringBuilder对象,拼接文法用的
     * @param num 用于控制颜色
     */
    private fun List<String>.addGrammar(builder: StringBuilder, num: Int) {
        var tempNum = num
        this.forEach {
            val left = it.split("→")[0]
            val right = it.split("→")[1]
            if (tempNum % 2 == 0) {
                builder.append("\t\t").append(
                    "%-12s%-11s%-21s".format(
                        left.lightCyan(), "→".lightCyan(), right.lightCyan()
                    )
                ).append("\n")
            } else {
                builder.append("\t\t").append(
                    "%-12s%-11s%-21s".format(
                        left.lightMagenta(), "→".lightMagenta(), right.lightMagenta()
                    )
                ).append("\n")
            }
            tempNum++
        }
    }

    /**
     * 自定义扩展函数，用于凭借FIRST集和FOLLOW集的字符串
     * @param builder StringBuilder对象，凭借对象
     * @param num 用于控制颜色
     * @param type 0代表拼接的类型是FIRST集, 其他代表FOLLOW集
     */
    private fun MutableMap<String, HashSet<String>>.addFirstOrFollow(builder: StringBuilder, num: Int, type: Int) {
        val str: String = if (type == 0) "FIRST" else "FOLLOW"
        var tempNum = num
        this.forEach {
            builder.append("\t\t").append(
                "%-19s".format(
                    "${str}(${
                        if (tempNum % 2 == 0) it.key.lightBlue() else it.key.lightMagenta()
                    })"
                )
            ).append("%-4s".format("= {"))
            it.value.forEach { t ->
                builder.append(if (tempNum % 2 == 0) t.lightBlue() else t.lightMagenta()).append(", ")
            }
            if (it.value.isNotEmpty()) repeat(2) { builder.deleteAt(builder.lastIndex) }
            builder.append(" }\n")
            tempNum++
        }
    }

    /**
     * 该函数已废弃，不再使用。因为用了正则表达式，而LL1文法分析就是干这个的。LexicalAnalysis类代替。
     * @param w 原始输入串
     */
    @Deprecated("已弃用")
    private fun lexicalAnalysis(w: String): String {
        //先匹配算术表达式，计算对应的结果
        val regAdd = Regex("\\d+\\+\\d+")
        val regSub = Regex("\\d+-\\d+")
        val regMul = Regex("\\d+\\*\\d+")
        val regDiv = Regex("\\d+/\\d+")
        val str2 = w.replace(" ", "").replace(regAdd) {
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
        //再匹配带括号的算数表达式，并计算结果
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
            } else if (b.contains("/")) {
                val i = b.split("/")
                (i[0].toInt() / i[1].toInt()).toString()
            } else {
                it.value
            }
        }
        //最后再匹配布尔表达式，并计算对应的结果
        val regB1 = Regex("\\(\\d+\\)")
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
            } else if (b.contains(">")) {
                val e = b.split(">")
                (e[0].toInt() > e[1].toInt()).toString()
            } else {
                it.value
            }
        }
        return str4
    }

    /**
     * 这是对外提供了一个接口，用于访问各数据结构，这样可以用于后续做界面
     * @param type 输入类型，根据类型返回各种值
     */
    fun getValues(type: String): Any? {
        return when (type) {
            "initGram" -> initialGrammar
            "grammar" -> grammar
            "isLeft" -> isLeft_
            "start" -> start
            "Vn" -> Vn
            "Vt" -> Vt
            "FIRST" -> FIRST
            "FOLLOW" -> FOLLOW
            "Tab" -> preAnaTab
            "inputSym" -> inputSym
            "stack" -> stackRecord
            "input" -> inputRecord
            "action" -> action
            "output" -> output
            else -> null
        }
    }
}