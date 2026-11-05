# Alfa Launcher AAOS — Kokpit Samochodowy z Obsługą Gestów i Personalizacją

Nowoczesny, w pełni konfigurowalny launcher samochodowy (AAOS / Android Auto Head Unit) inspirowany stylistyką Alfa Romeo, łączący podzielony ekran roboczy, wskaźniki telemetryczne `Cannocchiale`, sterowanie gestami wielodotykowymi oraz lokalne profile kierowców z układem widżetów zapisywanym w pamięci urządzenia.

### User Review & Critical Decisions

> [!IMPORTANT]
> Poniższe decyzje produktowe oraz konfiguracja systemowa Android Automotive OS (AAOS) zostały uwzględnione w planie:

- **Potwierdzona decyzja 1 — Konfiguracja Manifestu bezpośrednio pod Android Automotive OS (AAOS) i Android Auto**:
  - Deklaracja cechy sprzętowej `<uses-feature android:name="android.hardware.type.automotive" />` oraz deskryptora samochodowego `@xml/automotive_app_desc` powiązanego przez `<meta-data android:name="com.google.android.gms.car.application" />`.
  - Oznaczenie głównej aktywności kokpitu flagą `<meta-data android:name="distractionOptimized" android:value="true" />`, wymaganą przez system AAOS do pracy ekranu głównego podczas jazdy (bez blokady *UX Restrictions*).
  - Konfiguracja filtrów intencji (`<intent-filter>`) jako natywnego systemowego launchera samochodowego: `android.intent.action.MAIN` z kategoriami `android.intent.category.HOME`, `android.intent.category.DEFAULT`, `android.intent.category.CAR_DOCK`, `android.intent.category.CAR_MODE` oraz `android.intent.category.LAUNCHER`, przy `android:launchMode="singleTask"` i `android:stateNotNeeded="true"`.
- **Potwierdzona decyzja 2 — Modułowy ekran główny z włączaniem/wyłączaniem każdego widżetu**: Wszystkie cztery moduły (Podzielony ekran nawigacji/radia/skrótów, Zegary telemetrii i DNA, Dolny/boczny pasek dokowania oraz Siatka wszystkich aplikacji z ulubionymi i wyszukiwarką) będą dostępne i indywidualnie przełączane (włącz/wyłącz) w konfiguratorze ekranu głównego.
- **Potwierdzona decyzja 3 — Obsługa gestów wielodotykowych i edycji układu**:
  - **Przesunięcie dwoma palcami (2-finger swipe)**: poziomo (lewo/prawo) zmienia utwór (`Poprzedni / Następny`), pionowo (góra/dół) płynnie reguluje głośność systemową z wyświetleniem nakładki HUD.
  - **Dwukrotne stuknięcie / gest trzema palcami**: szybkie przełączanie trybu jazdy **Alfa DNA** (`Dynamic` / `Natural` / `Advanced Efficiency`) oraz przełączanie trybu `Dzień / Noc`.
  - **Przytrzymanie i przeciągnij-upuść (Long-press & Drag-and-Drop)**: wejście w tryb edycji kokpitu umożliwiający zmianę kolejności, rozmiaru i widoczności kafelków na ekranie głównym.
- **Potwierdzona decyzja 4 — Trzy przełączalne style wizualne**:
  1. **Alfa Rosso Sport Carbon**: ciemny karbonowy splot, krwistoczerwone akcenty (`#D31125`), dynamiczna zmiana kolorystyki wraz z trybem DNA.
  2. **Minimalistyczny AAOS Material 3**: czysty, nowoczesny styl Android Automotive OS z wysokim kontrastem i adaptacyjnymi tonami.
  3. **Klasyczny Cannocchiale**: klasyczne podwójne tubusowe zegary Alfa Romeo ze złotawym/bursztynowym lub białym podświetleniem tarcz i wyraźnymi okrągłymi wskaźnikami.
- **Potwierdzona decyzja 5 — Integracje systemowe i zapis w pamięci urządzenia**:
  - Prawdziwa telemetria GPS (`LocationManager` / `SensorManager` dla prędkości km/h i kompasu) połączona z przełączalnym trybem **Safety Simulation / Demo** (symulacja obrotów RPM, ciśnienia doładowania Turbo, przeciążenia G-Force i prędkości podczas postoju).
  - Wbudowany kontroler multimediów (obsługa `AudioManager` i klawiszy multimedialnych) oraz pełny menedżer zainstalowanych aplikacji Androida (`PackageManager`) z uruchamianiem aplikacji i przypinaniem ulubionych.
  - Szybki panel sterowania klimatyzacją (**HVAC** — dwustrefowa temperatura, nawiew, podgrzewanie foteli/kierownicy, obieg powietrza) oraz ustawieniami kokpitu.
  - Edytor profili kierowcy (np. *Kierowca 1*, *Tor / Sport*, *Trasa Nocna*) zapisujący układ kafelków, aktywne widżety, wybrany motyw oraz nastawy HVAC w lokalnej bazie danych **Room**.

---

### 1. Overview & Core Concept

- **Co robi aplikacja**: Działa jako natywny launcher systemowy (`HOME` / `CAR_DOCK` / `CAR_MODE`) dla jednostek centralnych **Android Automotive OS (AAOS)** oraz systemów pokładowych Android Auto. Zapewnia pełną optymalizację pod kątem prowadzenia pojazdu (`distractionOptimized="true"`), oferując podzielony ekran roboczy, wskaźniki telemetryczne `Cannocchiale`, obsługę gestami wielodotykowymi bez odrywania wzroku od drogi oraz trwałe profile kierowców.
- **Grupa docelowa / Kontekst użycia**: Jednostki główne samochodów z systemem Android Automotive OS / Android Auto (zarówno ekrany panoramiczne *Landscape*, jak i konsole pionowe *Portrait*), w których kierowca oczekuje bezpośredniej instalacji jako domyślny ekran główny pojazdu.
- **Główna wartość**: Natywna integracja z architekturą AAOS (rejestracja jako systemowy Car Launcher z obsługą przycisku Home w pojeździe), obsługa gestów 2 i 3 palców, natychmiastowe przełączanie charakterystyki **Alfa DNA** oraz pełna personalizacja widżetów.

---

### 2. User Experience & Visual Design

- **Główne ścieżki użytkownika (Key User Flows)**:
  1. **Uruchomienie w systemie AAOS jako domyślny Car Launcher**: Po zainstalowaniu w systemie Android Automotive OS naciśnięcie systemowego przycisku *Home* na ekranie jednostki centralnej lub kierownicy przenosi bezpośrednio do kokpitu Alfa Launcher (bez zamykania aktywności podczas jazdy dzięki `distractionOptimized="true"` oraz `launchMode="singleTask"`).
  2. **Ekran główny (Kokpit Dashboard)**: Kierowca widzi spersonalizowany układ aktywnych modułów (karta Nawigacji/Kompasu GPS, Zegary Cannocchiale z trybem DNA, Odtwarzacz Multimediów, Kafelki szybkich skrótów oraz Siatka aplikacji) wraz z paskiem dokowania i panelem HVAC.
  3. **Wykonywanie gestów na ekranie głównym**:
     - Przesunięcie 2 palcami w górę/dół wywołuje półprzezroczysty wskaźnik HUD głośności i zmienia poziom `STREAM_MUSIC`.
     - Przesunięcie 2 palcami w lewo/prawo przełącza utwór i wyświetla animowany komunikat HUD z tytułem utworu.
     - Gest 3 palcami (lub dwukrotne stuknięcie w nagłówek telemetryczny) przełącza tryb **Alfa DNA** (`D` — czerwony sportowy z boostem i G-meterem, `N` — zbalansowany błękitny, `A` — zielony eko) lub tryb Dzień/Noc.
  4. **Personalizacja ekranu głównego (Tryb Edycji & Włączanie/Wyłączanie widżetów)**:
     - Długie przytrzymanie dowolnego kafelka lub kliknięcie przycisku *Personalizuj Kokpit* otwiera tryb edycji: kafelki zyskują uchwyty przeciągania (zmiana kolejności), przełączniki rozmiaru (Kompaktowy / Szeroki) oraz przełączniki widoczności (Włączony / Wyłączony) dla każdego modułu.
  5. **Szuflada wszystkich aplikacji AAOS (App Drawer)**:
     - Pobiera zainstalowane w systemie aplikacje samochodowe i użytkowe (`PackageManager`), oferuje szybkie filtrowanie po nazwie, przypinanie do ulubionych skrótów na pulpicie oraz natychmiastowe uruchamianie przez `Intent`.
  6. **Panel HVAC i Profile Kierowcy**:
     - Dolny pasek zawiera szybkie regulatory temperatury lewej/prawej strefy; dotknięcie rozwija pełny arkusz klimatyzacji (siła nawiewu, kierunek nawiewu, podgrzewanie foteli, A/C, MAX Defrost).
     - Przełącznik profilu kierowcy w górnym pasku statusu pozwala jednym dotknięciem zmienić profil (np. *Alfa Sport*, *Komfort*, *Własny*) lub utworzyć nowy profil, natychmiast wczytując zapisany w bazie Room zestaw widżetów, motyw i ustawienia.

- **Tożsamość wizualna i motywy (Visual Identity & Theme)**:
  - *Trzy pełne systemy kolorystyczne i stylistyczne*:
    - **Motyw 1: Alfa Rosso Sport Carbon** — Tło głębokiego antracytu i włókna węglowego (`#0B0D10` / `#14171C`), akcenty *Alfa Rosso* (`#D31125`), stalowe obramowania (`#2A2F37`) oraz dynamiczny poblask zależny od trybu DNA (`#D31125` dla Dynamic, `#0077FF` dla Natural, `#00C853` dla Advanced).
    - **Motyw 2: Nowoczesny Minimalistyczny AAOS (Material 3)** — Gładkie matowe powierzchnie samochodowe (`#101418` w trybie nocnym / `#F4F6F9` w trybie dziennym), błękitno-szmaragdowe akcenty systemowe (`#4FC3F7`), zaokrąglone karty `24.dp` o podwyższonym kontraście.
    - **Motyw 3: Klasyczny Cannocchiale** — Wzorowany na kultowych, głęboko osadzonych zegarach tubusowych Alfa Romeo: ciemne tarcze z satynowymi pierścieniami, precyzyjna podziałka kątowa, bursztynowo-czerwone wskazówki i klasyczna typografia zegarów włoskich.
  - *Typografia*:
    - Fonty instalowane lokalnie przez `font-util`: **Orbitron** / **Rajdhani** dla cyfrowych prędkościomierzy, obrotomierzy i oznaczeń trybu DNA oraz **Space Grotesk** / **JetBrains Mono** dla czytelnych etykiet kokpitu, nazw ulic, temperatury HVAC i danych telemetrycznych.
  - *Układ adaptacyjny (Responsive Automotive Layout)*:
    - Automatyczne dostosowanie do szerokich ekranów poziomych AAOS (boczny pasek dokowania + wielokolumnowa siatka kokpitu) oraz ekranów pionowych (dolny pasek dokowania + przewijana kolumna modułów), z zachowaniem minimalnych celów dotykowych `48.dp–64.dp` wymaganych podczas jazdy.

---

### 3. Key Product Decisions & Trade-Offs

- **Decyzja 1: Konfiguracja Manifestu dla Android Automotive OS (AAOS) i Android Auto**
  - *Wybrane podejście*: W `AndroidManifest.xml` definiujemy cechy systemowe `android.hardware.type.automotive`, metadane `com.google.android.gms.car.application` wskazujące na deskryptor `res/xml/automotive_app_desc.xml` (`<automotiveApp><uses name="template" /><uses name="media" /></automotiveApp>`), flagę `distractionOptimized="true"` oraz pełny zestaw kategorii launchera samochodowego (`HOME`, `DEFAULT`, `CAR_DOCK`, `CAR_MODE`, `LAUNCHER`).
  - *Dlaczego*: Dzięki temu pakiet APK jest natychmiast rozpoznawany przez Android Automotive OS jako pełnoprawny ekran główny pojazdu (Car Launcher), nie jest blokowany przez restrykcje bezpieczeństwa podczas ruchu pojazdu, a jednocześnie może być uruchamiany w środowisku podglądu i na jednostkach head-unit.
- **Decyzja 2: Hybrydowa Telemetria (Prawdziwy GPS + Tryb Symulacji Demo)**
  - *Wybrane podejście*: Wykorzystanie natywnego `LocationManager` (GPS) oraz czujników urządzenia do pomiaru rzeczywistej prędkości (km/h), kursu kompasu i wysokości, z wbudowanym przełącznikiem **Tryb Symulacji Demo** dostępnym z poziomu kokpitu.
  - *Dlaczego*: W prawdziwym samochodzie kierowca otrzymuje realną prędkość GPS i kompas, natomiast na postoju może włączyć realistyczną symulację pracy silnika (obroty RPM, bieg 1–8, ciśnienie Turbo bar, przeciążenie G-Force i prędkość) reagującą na wybrany tryb DNA.
- **Decyzja 3: Trwały zapis układów i profili w bazie Room Database**
  - *Wybrane podejście*: Zastosowanie lokalnej bazy danych **Room** (z KSP i reaktywnym `Flow`) do przechowywania profili kierowców, konfiguracji widoczności i kolejności widżetów, przypiętych aplikacji oraz nastaw klimatyzacji HVAC.
  - *Dlaczego*: Jednostka samochodowa AAOS musi uruchamiać się natychmiastowo po przekręceniu zapłonu bez dostępu do sieci i zawsze przywracać ostatnio wybrany profil kierowcy wraz z dokładnym układem kafelków.
- **Decyzja 4: Własny detektor gestów wielodotykowych na warstwie kokpitu**
  - *Wybrane podejście*: Dedykowany modyfikator `pointerInput` analizujący liczbę aktywnych punktów dotyku (2 palce vs 3 palce vs 1 palec) przed przekazaniem zdarzenia do podrzędnych list i kart.
  - *Dlaczego*: Zapobiega konfliktom między standardowym przewijaniem jednym palcem (lub przeciąganiem kafelków po długim przytrzymaniu) a globalnymi gestami zmiany utworu/głośności (2 palce) i zmiany trybu DNA/Dzień-Noc (3 palce).

---

### 4. Technical Architecture & Data Strategy *(Technical Reference)*

#### Architecture & Component Diagram

```
┌───────────────────────────────────────────────────────────────────────────────┐
│              ANDROID AUTOMOTIVE OS (AAOS) / ANDROID AUTO MANIFEST             │
│  - <uses-feature android:name="android.hardware.type.automotive" />           │
│  - <meta-data android:name="com.google.android.gms.car.application"           │
│               android:resource="@xml/automotive_app_desc" />                  │
│  - <meta-data android:name="distractionOptimized" android:value="true" />     │
│  - Categories: HOME | DEFAULT | CAR_DOCK | CAR_MODE | LAUNCHER (singleTask)   │
└───────────────────────────────────────┬───────────────────────────────────────┘
                                        ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│                   ALFA LAUNCHER AAOS UI (Jetpack Compose)                     │
│                                                                               │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │ Top Status Bar: Zegar | GPS Status | Tryb DNA | Profil | Dzień/Noc      │  │
│  └─────────────────────────────────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │ Multi-Touch Gesture Overlay (2-Finger Audio/Vol, 3-Finger DNA, DoubleTap)│  │
│  │  ┌──────────────────────┐  ┌──────────────────────┐  ┌───────────────┐  │  │
│  │  │ Moduł Zegarów        │  │ Moduł Nawigacji /    │  │ Moduł Mediów  │  │  │
│  │  │ Cannocchiale & DNA   │  │ Kompasu GPS & Trasy  │  │ & Wizualizera │  │  │
│  │  └──────────────────────┘  └──────────────────────┘  └───────────────┘  │  │
│  │  ┌──────────────────────────────────┐  ┌─────────────────────────────┐  │  │
│  │  │ Moduł Skrótów & Ulubionych Appek │  │ Moduł Siatki Wszystkich App │  │  │
│  │  └──────────────────────────────────┘  └─────────────────────────────┘  │  │
│  │         (Drag & Drop Reordering + Włączanie/Wyłączanie Modułów)         │  │
│  └─────────────────────────────────────────────────────────────────────────┘  │
│  ┌─────────────────────────────────────────────────────────────────────────┐  │
│  │ Dock & HVAC Bar: Home | Aplikacje | L/P Temp HVAC | Edycja | Ustawienia │  │
│  └─────────────────────────────────────────────────────────────────────────┘  │
└───────────────────────┬───────────────────────────────────┬───────────────────┘
                        │ StateFlow / UI Intents            │
                        ▼                                   ▼
┌───────────────────────────────────────────┐ ┌─────────────────────────────────┐
│         LauncherViewModel (MVVM)          │ │   System Services & Hardware    │
│ - Aktywny profil, motyw i tryb DNA        │ │ - LocationManager (GPS km/h)    │
│ - Lista modułów (kolejność, widoczność)   │ │ - SensorManager (Kompas/G-Force)│
│ - Stan odtwarzacza i nakładki HUD gestów  │ │ - AudioManager (Głośność/Media) │
│ - Stan klimatyzacji HVAC (L/P, nawiewy)   │ │ - PackageManager (Lista Appek)  │
└───────────────────────┬───────────────────┘ └─────────────────────────────────┘
                        │
                        ▼
┌───────────────────────────────────────────────────────────────────────────────┐
│              LauncherRepository & Room Database (Local Persistence)           │
│  - DriverProfileEntity (id, nazwa, motyw, trybDNA, isDarkMode, hvacStateJson) │
│  - WidgetConfigEntity (profileId, widgetType, isVisible, positionOrder, span) │
│  - FavoriteAppEntity (profileId, packageName, customLabel, sortOrder)         │
└───────────────────────────────────────────────────────────────────────────────┘
```

#### Data Model & State

- **`DriverProfileEntity` (Room)**: Przechowuje profil kierowcy (`id`, `name`, `visualTheme`: `ALFA_CARBON` / `AAOS_MINIMAL` / `CANNOCCHIALE_CLASSIC`, `dnaMode`: `DYNAMIC` / `NATURAL` / `ADVANCED`, `dayNightMode`: `NIGHT` / `DAY`, parametry klimatyzacji `leftTemp`, `rightTemp`, `fanSpeed`, `seatHeatLeft`, `seatHeatRight`, `acEnabled`).
- **`WidgetConfigEntity` (Room)**: Definiuje każdy moduł ekranu głównego przypisany do profilu (`widgetId`, `profileId`, `widgetType`: `SPLIT_NAV_COMPASS`, `TELEMETRY_GAUGES`, `MEDIA_CONTROLLER`, `SHORTCUT_TILES`, `APP_GRID_WIDGET`, `DOCK_BAR`, `isEnabled`: Boolean, `orderIndex`: Int, `isWide`: Boolean).
- **`FavoriteAppEntity` (Room)**: Przechowuje przypięte aplikacje Androida (`packageName`, `label`, `profileId`, `orderIndex`).
- **`TelemetryUiState`**: Bieżąca prędkość (km/h), obroty RPM, aktualny bieg, ciśnienie doładowania (bar), wektor przeciążenia G-Force, azymut kompasu (stopnie + kierunek N/NE/E...), źródło danych (`REAL_GPS` vs `DEMO_SIMULATION`).

#### Interactive Component & State Mapping

- **Integracja z Android Automotive OS (AAOS Home & Car Mode)**:
  - Rejestracja `MainActivity` jako domyślnego ekranu `HOME` / `CAR_DOCK` z `distractionOptimized="true"`. Obsługa przycisku `Back` na głównym ekranie launchera pozostaje w kokpicie (lub zamyka otwarte arkusze `AppDrawer` / `HVAC` / `Tryb Edycji`), zgodnie z zachowaniem systemowego launchera AAOS.
- **Obsługa gestów wielodotykowych (`MultiTouchGestureBox`)**:
  - Rozpoznaje przesunięcie 2 palcami w pionie → wywołuje `viewModel.adjustVolumeByGesture(delta)` → zmienia głośność systemową przez `AudioManager` i wyświetla animowany baner HUD głośności przez 1.8 s.
  - Rozpoznaje przesunięcie 2 palcami w poziomie → wywołuje `viewModel.skipTrackByGesture(next/prev)` → wysyła zdarzenie klawisza multimedialnego (`KEYCODE_MEDIA_NEXT` / `PREVIOUS`), aktualizuje stan wbudowanego kontrolera multimediów i pokazuje baner HUD utworu.
  - Rozpoznaje gest 3 palcami lub dwukrotne stuknięcie w pasek trybu → wywołuje `viewModel.cycleDnaMode()` lub `viewModel.toggleDayNightMode()` z animacją przejścia koloru przewodniego i wibracją haptyczną.
- **Edycja układu i przełączanie widżetów (`WidgetCustomizerSheet` & tryb `isEditMode`)**:
  - Przytrzymanie kafelka na ekranie głównym aktywuje `isEditMode = true`. Użytkownik może przesuwać kafelki (zmieniając ich `orderIndex`), przełączać ich szerokość (`1x` / `2x`) lub całkowicie wyłączać/włączać dowolny moduł (w tym pasek dokowania, zegary, mapę/kompas, media, skróty i siatkę aplikacji). Każda zmiana jest automatycznie zapisywana w bazie Room dla aktywnego profilu.
- **Szuflada aplikacji i skróty (`AppDrawerSheet` & `ShortcutTilesWidget`)**:
  - Pobiera listę zainstalowanych aplikacji z `PackageManager` (z uwzględnieniem `<queries>` w `AndroidManifest.xml`). Kliknięcie ikony uruchamia aplikację (`startActivity`), a ikona gwiazdki dodaje/usuwa aplikację z kafelków szybkich skrótów.
- **Panel Klimatyzacji (`HvacControlSheet`) i Edytor Profili (`DriverProfileDialog`)**:
  - Pozwala płynnie regulować temperaturę kierowcy i pasażera (16.0°C – 28.0°C), funkcję `SYNC`, prędkość wentylatora (1–7), podgrzewanie foteli (3 stopnie) oraz przełączać i tworzyć nowe profile kierowców.
