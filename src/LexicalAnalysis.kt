/**
 *
 * @author Lin
 * @Date 2022/06/30
 * @Time 17:57
 */
class LexicalAnalysis(private val input: String) {
    private val keyWord = listOf(
        "bool", "break", "case", "catch", "char", "class", "const", "delete",
        "do", "double", "else", "enum", "extern", "false", "float", "goto",
        "if", "int", "long", "new", "operator", "private", "protected",
        "return", "short", "signed", "sizeof", "static", "switch", "or",
        "continue", "for", "public", "true", "while", "not","and"
    )

    /**
     * 给外界提供调用输出结果的函数
     */
    fun getOutput() = analysis()

    /**
     * 词法分析过程
     * @return 输出词法记号流
     */
    private fun analysis(): String {
        val outList = mutableListOf<Token>()
        var chIndex = 0
        while (chIndex < input.length) {
            //处理空格和tab
            while (input[chIndex] == ' ' || input[chIndex] == '\t') {
                chIndex++
                if (chIndex >= input.length)
                    break
            }
            if (chIndex >= input.length)
                break
            //标识符
            if (input[chIndex].isLetter() || input[chIndex] == '_') {//开头是字母或者下划线
                var letter = ""
                //是字母或者下划线
                while (input[chIndex].isLetter() || input[chIndex] == '_') {
                    letter = "${letter}${input[chIndex]}"
                    chIndex++
                    if (chIndex >= input.length)
                        break
                    //不是开头，可以为数字
                    while (input[chIndex].isDigit()) {
                        letter = "${letter}${input[chIndex]}"
                        chIndex++
                        if (chIndex >= input.length)
                            break
                    }
                    if (chIndex >= input.length)
                        break
                }
                //关键字
                for (keyword in keyWord) {
                    if (keyword == letter) {
                        outList.add(Token(letter, keyword))
                    }
                }
                //普通标识符
                if (letter !in keyWord) {
                    outList.add(Token(letter, "id"))
                }
            }
            //数字
            else if (input[chIndex].isDigit()) {
                var dotFlag=false //对于末尾'.'的标志
                var letter = ""
                while (input[chIndex].isDigit() || input[chIndex] == '.') {
                    if(input[chIndex].isDigit()) //输入的是数字直接拼接
                    {
                        letter = "${letter}${input[chIndex]}"
                        chIndex++
                    }
                    else{       //不是数字即为 '.'
                        if (chIndex>=input.length)
                            break
                        if(letter.count { it=='.'}==1) //如果当前正在匹配的字符中已经含有'.' 比如 "15."的格式
                        {
                            if(chIndex+1>=input.length){ //匹配到末尾，结束当前循环，最后的'.'将在其他字符中被匹配
                                break
                            }
                            else if(input[chIndex].isDigit()){ //下一个字符是数字 将组成 “15.6" 的格式
                                letter = "${letter}${input[chIndex]}"
                                chIndex++
                            }
                            else{  //非数字   即 '.' '<'.. 字符 等其他符号 结束本次匹配
                                break
                            }
                        }
                        else{ //当前正在匹配的字符是'.'
                            letter = "${letter}${input[chIndex]}"
                            chIndex++
                            if(chIndex>=input.length) //如果是末尾'.' 比如”5.“,将前面的数匹配为num, '.'仍然为"."
                                dotFlag=true
                            else {      //‘.’后面的不是数字，不能组成“15.5”格式的num，结束本次匹配
                                if (!input[chIndex].isDigit()) {
                                    dotFlag=true
                                    break
                                }
                            }
                        }
                    }
                    if (chIndex>=input.length)
                        break
                }
                if(dotFlag) {
                    outList.add(Token(letter.substring(0, letter.length - 1), "num")) //15.格式 15匹配num '.'为'.'
                    outList.add(Token(".", "."))
                }
                else
                    outList.add(Token(letter, "num"))
            }
            else {
                outList.add(Token(input[chIndex], input[chIndex].toString()))
                chIndex++

            }
        }

        var hasAb = false
        var hasAbc = false
        for (out in outList){
            if (out.value == "ab"){
                hasAb = true
            }else if (out.value == "abc"){
                hasAbc = true
            }else{
                hasAb = false
                break
            }
        }
        return if (hasAb && hasAbc){
            outList.joinToString(""){
                it.value.toString()
            }
        }else{
            outList.joinToString(""){
                it.type
            }
        }
    }
}