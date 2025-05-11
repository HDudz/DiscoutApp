# Optymalizator Płatności


Aplikacja konsolowa w języku Java służąca do optymalizacji metod płatności dla listy zamówień w hipotetycznym supermarkecie internetowym. Głównym celem algorytmu jest dobranie dla każdego zamówienia optymalnego sposobu płatności (korzystając z tradycyjnych metod oraz punktów lojalnościowych) w taki sposób, aby zmaksymalizować łączny uzyskany rabat, przy jednoczesnym spełnieniu wszystkich zdefiniowanych ograniczeń i opłaceniu wszystkich zamówień.

Algorytm preferuje użycie punktów lojalnościowych, jeśli nie zmniejsza to należnego rabatu, oraz stara się minimalizować płatności kartami.

## Struktura Projektu
* **Kod źródłowy**: Znajduje się w katalogu `src`..
* **Pliki konfiguracyjne Mavena**: Główny plik to `pom.xml`.
* **Zbudowana aplikacja (fat-jar)**: Po zbudowaniu projektu, plik `.jar` zawierający wszystkie zależności znajduje się w katalogu `target/`.

## Logika Algorytmu
Algorytm działa w sposób wieloetapowy, aby zoptymalizować wybór płatności i zmaksymalizować rabaty, jednocześnie starając się odzwierciedlić preferencje i zasady opisane w zadaniu. Kolejność rozpatrywania typów płatności (etapy) została dobrana tak, aby jak najlepiej odtworzyć optymalne rozwiązanie dla dostarczonego przykładu oraz spełnić ogólne zasady priorytetów.

### Kolejność etapów przetwarzania:

Pełne Płatności Punktami (FULL_PUNKTY): Algorytm najpierw rozważa opłacenie zamówień w całości punktami lojalnościowymi, jeśli jest to możliwe i daje zdefiniowany rabat dla metody "PUNKTY". Ten etap ma najwyższy priorytet, aby promować wykorzystanie punktów zgodnie z ogólną zasadą ich preferowania.

Promocje Kartowe (PROMO_CARD): Następnie rozpatrywane są promocje związane z płatnością kartą konkretnego banku. Wybierane są te, które dają największy rabat. W tym etapie, jeśli dla danego zamówienia promocja kartowa oferuje mniejszy rabat niż dostępna (ale jeszcze nie wybrana) opcja pełnej płatności punktami, płatność kartą dla tego zamówienia jest odraczana (nie jest wybierana), aby umożliwić zastosowanie lepszej opcji punktowej w jej dedykowanym etapie (który już minął, ale logika porównania odnosi się do potencjalnej korzyści).

Częściowe Płatności Punktami (Rabat 10%): Kolejnym etapem jest próba zastosowania 10% rabatu na całe zamówienie, jeśli co najmniej 10% jego wartości (przed rabatem) zostanie opłacone punktami. Pozostała część (jeśli występuje) jest opłacana jedną tradycyjną metodą płatności.

Płatności Rezerwowe (Fallback): Jeśli po powyższych etapach któreś zamówienia pozostają nieopłacone, system próbuje je opłacić, najpierw wykorzystując dostępne punkty lojalnościowe (bez specjalnego rabatu), a następnie pozostałą kwotę jedną dostępną kartą (również bez rabatu promocyjnego).
