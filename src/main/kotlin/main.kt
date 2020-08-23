import java.io.File
import java.io.FileNotFoundException
import kotlin.random.Random
import java.util.logging.*

const val MAX_NUMBER_OF_CARDS = 100000
const val INPUT_THE_ACTION = "Input the action (add, remove, import, export, ask, exit):"
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
const val ALREADY_EXIST = "The card \"1\" already exists. Try again:"
const val CARD_REMOVED = "The card has been removed."
const val NO_CARD_FOR_REMOVAL = "Can't remove \"1\": there is no such card."
const val NO_CARDS_FOR_TEST = "No cards for testing."
const val FILE_NAME = "File name:"
const val CARDS_SAVED = "1 cards have been saved."
const val FILE_NOT_FOUND = "File not found."
const val CARDS_LOADED = "1 cards have been loaded."

object Flashcards {
    private val cards: MutableMap<String, String> = mutableMapOf()
    private var isExit: Boolean = false
    private var numOfTests: Int = 0
    private val logger = Logger.getLogger(Flashcards::class.qualifiedName)
    private val fileHandler = FileHandler("flashcards.log")
//    private var lastCardNum: Int = 0

    override fun toString(): String {
        var res = ""
        for (el in cards) res += "term: ${el.key}     definition: ${el.value}\n"
        return res
//        return super.toString()
    }

    init {
        logger.addHandler(fileHandler)
        logger.useParentHandlers = false
        fileHandler.formatter = SimpleFormatter()
    }

    private fun addCard() {
        var resultAccepted = false
        try {
//            lastCardNum++
            println(CARD_TERM)
            var s1 = ""
            while (!resultAccepted) {
                s1 = readLine()!!.trim()
                when (s1) {
                    "" -> {
                        println(INVALID_DATA)
                        println(CARD_TERM)
                        resultAccepted = false
                    }
                    in cards.keys -> {
                        println(ALREADY_EXIST.replace("1", s1))
                        resultAccepted = false
                    }
                    else -> resultAccepted = true
                }
            }

            println(CARD_DEFINITION)
            resultAccepted = false
            while (!resultAccepted) {
                when (val s2 = readLine()!!.trim()) {
                    "" /*|| s2.length == 1*/ -> {
                        println(INVALID_DATA)
                        println(CARD_DEFINITION)
                        resultAccepted = false
                    }
                    in cards.values -> {
                        println(ALREADY_EXIST.replace("1", s2).replace("card", "definition"))
                        resultAccepted = false
                    }
                    else -> {
                        cards[s1] = s2
                        resultAccepted = true
                        println(PAIR_ADDED.replace("1","\"${s1}\":\"${s2}\""))
                    }
                }
            }
        } catch (e: Exception) {
            println(INVALID_DATA)
        }
    }

    private fun removeCard() {
        println(CARD_TERM)
        val s = readLine()!!.toLowerCase().trim()
        when (s) {
            "" -> println(INVALID_DATA)
            else -> {
                if (s in cards.keys) {
                    cards.remove(s)
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
            try {
                numOfTests = readLine()!!.toInt()
                if (numOfTests in 1..cards.size) numberIsCorrect = true else println(INVALID_NUMBER.replace("XX", cards.size.toString()))
            }
            catch (e: Exception) {
                println(INVALID_NUMBER)
            }
        } while (!numberIsCorrect)

        val cardsToTest:MutableList<String> = mutableListOf()
        for (el in cards) cardsToTest.add(el.key)

//        logger.info("сформировали cardsToTest")
//        for (el in cardsToTest) logger.info(el)

        for (i in 1..numOfTests) {
            try {
                val elToRemove = cardsToTest.removeAt(Random.nextInt(cardsToTest.size))
                val el = cards.entries.find { it.key == elToRemove }

//                logger.info("выбрали случайный элемент: ${el.toString()}")
//                logger.info("размер cardsToTest: ${cardsToTest.size}")
//                logger.info("остались в cardsToTest")
//                for (el in cardsToTest) logger.info(el)

                if (el != null) {
                    println("$PRINT_DEFINITION \"${el.key}\"")
                    val s = readLine()!!.trim().toLowerCase()
                    if (s == el.value.toLowerCase()) println(CORRECT) else
                        if (s in cards.values) {
                            println(WRONG_BUT_PARTIALLY1.replace("1", el.value) + WRONG_BUT_PARTIALLY2.replace("2", cards.keys.first { cards[it] == s }))
                        }
                        else println("$WRONG_DEFINITION \"${el.value}\"")
                }
            }
            catch (e: Exception) {
                println(INVALID_DATA)
            }
        }
    }

    private fun exportCards() {
        println(FILE_NAME)
        try {
            val fileName = readLine()!!
            val myFile = File(fileName)
            myFile.writeText("Cards and definitions\n")
            cards.forEach { myFile.appendText("${it.key}\n${it.value}\n") }
            println(CARDS_SAVED.replace("1",cards.size.toString()))
        }
        catch (e: Exception) {
            println(INVALID_DATA)
        }
    }

    private fun importCards() {
        println(FILE_NAME)
        try {
            val fileName = readLine()!!
            val myFile = File(fileName)
            var redLines = 0
            var temp = ""
            myFile.forEachLine {
                when {
                    redLines == 0 -> redLines++       // skip first line
                    redLines % 2 == 1 -> {
                        temp = it
                        redLines++
                    }
                    else -> {
                        cards[temp] = it    // update definition of existing in memory card
                        redLines++
                    }
                }
            }
            println(CARDS_LOADED.replace("1", (redLines / 2).toString()))
//            println(Flashcards)
        }
        catch (e: FileNotFoundException) {
            println(FILE_NOT_FOUND)
        }
        catch (e: Exception) {
            println(INVALID_DATA)
        }
    }

    fun executeCmd(s: String) {
        when (s.trim()) {
            "exit" -> isExit = true
            "add" -> addCard()
            "remove" -> removeCard()
            "import" -> importCards()
            "export" -> exportCards()
            "ask" -> askDefinition()
            else -> println(UNKNOWN_CMD)
        }
        println("\n$INPUT_THE_ACTION")
    }

    fun isExit(): Boolean = isExit

}

fun main() {
    println(INPUT_THE_ACTION)
    while (!Flashcards.isExit()) Flashcards.executeCmd(readLine()!!)
    println("Bye bye!")
//    println(Flashcards)
}