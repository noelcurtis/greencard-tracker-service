import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

val formatter = DateTimeFormatter.ofPattern("dMMMyy", Locale.ENGLISH)
val text = "08Jul10"
val parsedDate = LocalDate.parse(text, formatter)

