data class Category(
    val id: Int,
    val name: String,
    val requiredCoins: Int
)

val categories = listOf(
    Category(1, "Cartoons", 0),    // free
    Category(2, "Animals", 400),
    Category(3, "Birds", 400),
    Category(4, "Fishes", 500),
    Category(5, "Fruits", 600),
    Category(6, "Cars", 700),
    Category(7, "Bikes", 800)
)
