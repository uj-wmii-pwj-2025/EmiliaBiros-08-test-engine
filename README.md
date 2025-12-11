## Test Engine
Zadaniem jest rozbudowanie silnika testowego Test Engine z wykładu.
* Rozbudowanie (może być całkowita przebudowa) systemu adnotacji tak, aby poza parametrami uruchomieniowymi możliwe było przekazanie skorelowanego z nimi oczekiwanego wyniku.
* Sprawdzenie, czy wynik testu jest prawidłowy. Trzy możliwe stany każdego testu:
  * `PASS` jeśli test się zakończył a wynik jest prawidłowy,
  * `FAIL` jeśli test się zakończył a wynik nie jest prawidłowy,
  * `ERROR` w razie wyrzucenia nieprzechwyconego wyjątku (`java.lang.Exception`). **UWAGA**: pojawienie się tego statusu nie przerywa działania całego silnika, a jedynie kończy pojedyńczy test!
* Ładne wyświetlanie nazwy Test Engine'a jako ASCII ART oraz informacji o testach przed wykonaniem.
* Czytelne wyświetlanie informacji o wynikach wszystkich testów. Opcjonalnie: pokazywanie postępów wykonywania testów.
* Przygotowanie zestawu przykładowych testów wraz ze skryptami uruchomieniowymi, pokazującymi działanie TestEngine'a.


---
`
./gradlew build
java -cp build/classes/java/main uj.wmii.pwj.anns.MyTestEngine uj.wmii.pwj.anns.MyBeautifulTestSuite
`