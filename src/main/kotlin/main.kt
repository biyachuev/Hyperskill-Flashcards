import java.io.File
import java.io.FileNotFoundException
import kotlin.random.Random
import kotlin.math.ceil
import kotlin.system.exitProcess

const val MAX_NUMBER_OF_CARDS = 100000
const val INPUT_THE_ACTION = "Input the action (add, remove, import, export, ask, log, reset stats, hardest card, exit):"
const val INPUT_NUMBER = "How many times to ask?"
const val UNKNOWN_CMD = "Unknown command"
const val INVALID_NUMBER = "Invalid data. Number must be between 1 and XX. Please try again."
const val INVALID_DATA = "Invalid data"
const val CARD_TERM = "The card:"
const val PAIR_ADDED = "The pair (1) has been added."
const val CARD_DEFINITION = "The definition of the card:"
const val PRINT_DEFINITION = "Print the definition of"
const val CORRECT = "Correct!"
const val WRONG_DEFINITION = "Wrong. The right answer is"
const val WRONG_BUT_PARTIALLY1 = "Wrong. The right answer is \"1\""
const val WRONG_BUT_PARTIALLY2 = ", but your definition is correct for \"2\""
const val ALREADY_EXIST = "The card \"1\" already exists."
const val CARD_REMOVED = "The card has been removed."
const val NO_CARD_FOR_REMOVAL = "Can't remove \"1\": there is no such card."
const val NO_CARDS_FOR_TEST = "No cards for testing."
const val FILE_NAME = "File name:"
const val CARDS_SAVED = "1 cards have been saved."
const val FILE_NOT_FOUND = "File not found."
const val CARDS_LOADED = "1 cards have been loaded."
const val LOG_SAVED = "The log has been saved."
const val HARDEST_CARD_ABSENT = "There are no cards with errors."
const val HARDEST_CARD_1 = "The hardest card is 1. "
const val HARDEST_CARD_2 = "You have 1 errors answering it."
const val CARD_STAT_RESET = "Card statistics has been reset."
const val UNKNOWN_ERROR = "Unknown error."

object Flashcards {
    private val cards: MutableMap<String, String> = mutableMapOf()
    private val cardsMistakes: MutableMap<String, Int> = mutableMapOf()
    private val appLog: MutableList<String> = mutableListOf()
    private var isExit: Boolean = false
    private var numOfTests: Int = 0
    private var fileForAutomaticExport: String = ""

    override fun toString(): String {
        var res = ""
        for (el in cards) res += "term: ${el.key}     definition: ${el.value}\n"
        return res
    }

    private fun addCard() {
        try {
            println(CARD_TERM)
            val s1 = readLine()!!.trim()
            when {
                s1 == "" -> {
                    println(INVALID_DATA)
                    return
                }
                cards.keys.any { it.equals(s1, ignoreCase = true) } -> {
                    println(ALREADY_EXIST.replace("1", s1))
                    return
                }
            }

            println(CARD_DEFINITION)
            val s2 = readLine()!!.trim()
            when {
                s2 == "" -> {
                    println(INVALID_DATA)
                    return
                }
                cards.values.any { it.equals(s2, ignoreCase = true) } -> {
                    println(ALREADY_EXIST.replace("1", s2).replace("card", "definition"))
                    return
                }
                else -> {
                    cards[s1] = s2
                    cardsMistakes[s1] = 0
                    println(PAIR_ADDED.replace("1", "\"${s1}\":\"${s2}\""))
                }
            }
        } catch (e: Exception) {
            println(INVALID_DATA)
        }
    }

    private fun removeCard() {
        println(CARD_TERM)
        when (val s = readLine()!!.trim()) {
            "" -> println(INVALID_DATA)
            else -> {
                val myKey = cards.keys.firstOrNull { it.equals(s, ignoreCase = true) }
                if (myKey != null) {
                    cards.remove(myKey)
                    cardsMistakes.remove(myKey)
                    println(CARD_REMOVED)
                }
                else println(NO_CARD_FOR_REMOVAL.replace("1", s))
            }
        }
    }

    private fun askDefinition() {
        var numberIsCorrect = false
        if (cards.isEmpty()) {
            println(NO_CARDS_FOR_TEST)
            return
        }
        do {
            println(INPUT_NUMBER)
            appLog.add(INPUT_NUMBER)
            try {
                numOfTests = readLine()!!.toInt()
                if (numOfTests in 1..MAX_NUMBER_OF_CARDS) numberIsCorrect = true else println(INVALID_NUMBER.replace("XX", cards.size.toString()))
            }
            catch (e: Exception) {
                println(INVALID_NUMBER.replace("XX", cards.size.toString()))
            }
        } while (!numberIsCorrect)
        appLog.add(numOfTests.toString())

        val cardsToTest:MutableList<String> = mutableListOf()
        for (i in 1..(ceil(numOfTests.toDouble() / cards.size).toInt()))
            for (el in cards) cardsToTest.add(el.key)

        for (i in 1..numOfTests) {
            try {
                val elToRemove = cardsToTest.removeAt(Random.nextInt(cardsToTest.size))
                val el = cards.entries.find { it.key == elToRemove }

                if (el != null) {
                    println("$PRINT_DEFINITION \"${el.key}\"")
                    appLog.add("$PRINT_DEFINITION \"${el.key}\"")
                    val s = readLine()!!.trim()
                    appLog.add(s)
                    when {
                        s.equals(el.value, ignoreCase = true) -> {
                            println(CORRECT)
                            appLog.add(CORRECT)
                        }
                        cards.values.any { it.equals(s, ignoreCase = true) } -> {
                            if (cardsMistakes[el.key] != null) cardsMistakes[el.key] = cardsMistakes[el.key]!!.plus(1) else cardsMistakes[el.key] = 1
                            val wrongAnswer = WRONG_BUT_PARTIALLY1.replace("1", el.value) + WRONG_BUT_PARTIALLY2.replace("2", cards.keys.first { cards[it].equals(s, ignoreCase = true) })
                            println(wrongAnswer)
                            appLog.add(wrongAnswer)
                        }
                        else -> {
                            if (cardsMistakes[el.key] != null) cardsMistakes[el.key] = cardsMistakes[el.key]!!.plus(1) else cardsMistakes[el.key] = 1
                            println("$WRONG_DEFINITION \"${el.value}\"")
                            appLog.add("$WRONG_DEFINITION \"${el.value}\"")
                        }
                    }
                }
            }
            catch (e: Exception) {
                println(e)
            }
        }
    }

    private fun exportCards(fName: String?) {
        if (fName == null) println(FILE_NAME)
        try {
            val fileName = fName ?: readLine()!!
            val myFile = File(fileName)
            myFile.writeText("Cards and definitions\n")
            cards.forEach { myFile.appendText("${it.key}\n${it.value}\n${cardsMistakes[it.key]}\n") }
            println(CARDS_SAVED.replace("1",cards.size.toString()))
        }
        catch (e: Exception) {
            println(INVALID_DATA)
        }
    }

    private fun importCards(fName: String?) {
        if (fName == null) println(FILE_NAME)
        try {
            val fileName = fName ?: readLine()!!
            val myFile = File(fileName)
            var redLines = 0
            var theKey = ""
            var theValue = ""
            var theNumber = 0
            myFile.forEachLine {
                when {
                    redLines == 0 -> {}  // skip headline
                    redLines % 3 == 1 -> theKey = it
                    redLines % 3 == 2 -> theValue = it
                    else -> {
                        val myKey = cards.keys.firstOrNull { it1 -> it1.equals(theKey, ignoreCase = true) }
                        if (myKey != null) {
                            theNumber = if (it == "null") 0 else it.toInt()
                            cards[myKey] = theValue
                            cardsMistakes[myKey] = theNumber
                        } else {
                            cards[theKey] = theValue
                            cardsMistakes[theKey] = theNumber
                        }
                    }
                }
                redLines++
            }
            println(CARDS_LOADED.replace("1", (redLines / 3).toString()))
        }
        catch (e: FileNotFoundException) {
            println(FILE_NOT_FOUND)
        }
        catch (e: Exception) {
            println(INVALID_DATA)
            exitProcess(1)
        }
    }

    private fun saveLog() {
        println(FILE_NAME)
        try {
            val fileName = readLine()!!
            val myFile = File(fileName)
            myFile.writeText("Activity log\n")
            appLog.forEach { myFile.appendText("$it\n") }
            println(LOG_SAVED)
        }
        catch (e: Exception) {
            println(UNKNOWN_ERROR)
        }
    }

    private fun hardestCard() {
        val hcMaxMistakes = cardsMistakes.values.maxOrNull()
        appLog.add("hardest card")
        if (hcMaxMistakes in listOf(0, null) ) {
            println(HARDEST_CARD_ABSENT)
            appLog.add(HARDEST_CARD_ABSENT)
            return
        }

        val hcList = cardsMistakes.filter { it.value == hcMaxMistakes }.keys.toList()
        var hcString = ""
        hcList.indices.forEach { i ->
            hcString += if (i == hcList.lastIndex) "\"${hcList[i]}\"" else "\"${hcList[i]}\", "
        }
        hcString = HARDEST_CARD_1.replace("1", hcString) + HARDEST_CARD_2.replace("1", hcMaxMistakes.toString())
        if (hcList.size > 1) hcString = hcString.replace("is", "are").replace("it", "them")
        println(hcString)
        appLog.add(hcString)
    }

    private fun resetStats() {
        cardsMistakes.clear()
        println(CARD_STAT_RESET)
        appLog.add(CARD_STAT_RESET)
    }

    fun executeCmd(s: String) {
        when (s.trim()) {
            "exit" -> exitProg()
            "add" -> addCard()
            "remove" -> removeCard()
            "import" -> importCards(null)
            "export" -> exportCards(null)
            "ask" -> askDefinition()
            "log" -> saveLog()
            "hardest card" -> hardestCard()
            "reset stats" -> resetStats()
            else -> println(UNKNOWN_CMD)
        }
        if (!isExit) println("\n$INPUT_THE_ACTION")
    }

    fun cmdLineProcess(args: Array<String>) {
        var i = 0
        var importCmdProcessed = false
        var exportCmdProcessed = false
        do {
            when {
                args[i] == "-import" && !importCmdProcessed -> {
                    if (i < args.lastIndex) importCards(args[++i])
                    importCmdProcessed = true
                }
                args[i] == "-export" && !exportCmdProcessed -> {
                    if (i < args.lastIndex) fileForAutomaticExport = args[++i]
                    exportCmdProcessed = true
                }
            }
            i++
        } while (i <= args.lastIndex)
    }

    private fun exitProg() {
        isExit = true
        println("Bye bye!")
        if (fileForAutomaticExport !in listOf("", null)) exportCards(fileForAutomaticExport)
    }

    fun isExit(): Boolean = isExit

}

fun main(args: Array<String>) {
    if (args.isNotEmpty()) Flashcards.cmdLineProcess(args)
    println(INPUT_THE_ACTION)
    while (!Flashcards.isExit()) Flashcards.executeCmd(readLine()!!)
}