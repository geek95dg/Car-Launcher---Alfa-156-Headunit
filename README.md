# 🍀 Alfa Launcher — Alfa Romeo 156 Headunit (AAOS)

Dedykowany systemowy interfejs i launcher dla stacji multimedialnej w **Alfa Romeo 156**, zbudowany w oparciu o **Android Automotive OS (AAOS)** i architekturę sprzętową **x86**.

Projekt łączy autorską aplikację ekranu głównego (Jetpack Compose) z natywną nakładką systemową **RRO (Runtime Resource Overlay)**, dzięki czemu stylistyka Alfa Romeo (Alfa Rosso `#D11919`, tło Carbon `#0E1013`, wykończenie szczotkowaną stalą) jest spójna i widoczna **w całym systemie operacyjnym oraz w każdej uruchomionej aplikacji** (Mapy Google, Spotify, Telefon, Ustawienia).

---

## 📐 Architektura Projektu

```
Car-Launcher---Alfa-156-Headunit/
├── app/                                # Główna aplikacja Launchera (Android / AAOS)
│   ├── src/main/java/com/example/
│   │   ├── MainActivity.kt             # Punkt wejścia z pełnym trybem Immersive
│   │   ├── data/                       # Baza Room: profile kierowców, konfiguracje widżetów
│   │   ├── ui/                         # Interfejs Jetpack Compose, LauncherViewModel
│   │   └── ui/components/              # Zegary Cannocchiale, siatka aplikacji, paski statusu
│   └── build.gradle.kts                # Konfiguracja modułu aplikacji
│
├── overlays/
│   └── AlfaCarSystemUIOverlay/         # Systemowy moduł RRO dla Android Automotive OS
│       ├── Android.bp                  # Plik budowania AOSP (Soong runtime_resource_overlay)
│       ├── AndroidManifest.xml         # Deklaracja nakładki na com.android.systemui
│       ├── res/values/colors.xml       # Systemowa paleta Alfa Rosso & Carbon
│       ├── res/xml/car_sysui_overlays.xml # Mapa zasobów dla pasków CarSystemBar
│       └── README_AOSP.md              # Instrukcja wdrożenia w drzewie źródeł AOSP
│
└── README.md                           # Główny opis projektu
```

---

## 🚗 Kluczowe Funkcjonalności

1. **Zegary Telemetrii Cannocchiale:**
   * Cyfrowe odwzorowanie klasycznych tubowych zegarów Alfy Romeo (prędkościomierz, obrotomierz, ciśnienie Turbo, G-Force, TPMS).
2. **Przełącznik Trybów Alfa D.N.A.:**
   * **D (Dynamic):** Czerwone podświetlenie Alfa Rosso, wyostrzona responsywność.
   * **N (Natural):** Subtelne niebieskie akcenty do codziennej jazdy.
   * **A (Advanced Efficiency):** Zielone akcenty zoptymalizowane pod ekonomię.
3. **Spójny Motyw Systemowy (RRO Overlay):**
   * Paski nawigacyjne i statusu AAOS (`TopCarSystemBar` i `BottomCarSystemBar`) renderują akcenty w kolorze **Alfa Rosso `#D11919`** we wszystkich aplikacjach systemu.
4. **Sterowanie Gestami Wielodotykowymi:**
   * Dwa palce góra/dół: głośność.
   * Dwa palce lewo/prawo: zmiana utworu.
   * Trzy palce: szybka zmiana trybu D.N.A. lub motywu dzień/noc.
5. **Profile Kierowcy:**
   * Możliwość zapisu niezależnych układów widżetów i preferencji stylistycznych dla każdego kierowcy.

---

## 🛠️ Wdrożenie do Kompilacji AOSP (Platforma x86)

Gdy kompilujesz własny obraz Android Automotive OS pod platformę x86:

### 1. Skopiowanie nakładki do drzewa AOSP
Skopiuj katalog `overlays/AlfaCarSystemUIOverlay/` do drzewa źródeł:
```bash
cp -r overlays/AlfaCarSystemUIOverlay/ <sciezka_do_aosp>/packages/services/Car/car_product/overlay/
```
*(lub do katalogu `device/<vendor>/<x86_board>/overlay/`)*

### 2. Rejestracja pakietu w konfiguracji obrazu
W pliku `device.mk` Twojej płyty bazowej x86 dopisz moduł:
```make
PRODUCT_PACKAGES += \
    AlfaCarSystemUIOverlay
```

### 3. Włączenie wymuszenia nakładek
Upewnij się, że w konfiguracji produktu znajduje się wpis:
```make
PRODUCT_ENFORCE_RRO_TARGETS := *
```

### 4. Kompilacja systemu
```bash
source build/envsetup.sh
lunch your_car_x86_target-userdebug
m -j$(nproc)
```
Po wgraniu obrazu na urządzenie x86, systemowe paski AAOS od pierwszego startu będą posiadać motyw Alfy Romeo.

---

## 💻 Testowanie na Emulatorze AAOS (Android Studio)

### Uruchomienie Launchera
Aplikacja kompiluje się standardowo przez Gradle:
```bash
./gradlew :app:assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

### Testowanie Systemowego Motywu RRO na żywo
Na uruchomionym emulatorze AAOS z uprawnieniami roota można błyskawicznie aplikować kolory za pomocą mechanizmu `cmd overlay fabricate`:

```bash
# Włączenie akcentu Alfa Rosso na zaznaczonych ikonach paska systemowego:
adb shell cmd overlay fabricate --target com.android.systemui --name AlfaRossoSelected com.android.systemui:color/car_nav_icon_fill_color_selected 0x1c 0xffd11919
adb shell cmd overlay enable com.android.shell:AlfaRossoSelected

# Włączenie ciemnego tła Carbon dla paska systemowego:
adb shell cmd overlay fabricate --target com.android.systemui --name AlfaBg com.android.systemui:color/car_background 0x1c 0xff0e1013
adb shell cmd overlay enable com.android.shell:AlfaBg

# Przeładowanie SystemUI w celu odświeżenia widoku:
adb shell pkill -f com.android.systemui
```
