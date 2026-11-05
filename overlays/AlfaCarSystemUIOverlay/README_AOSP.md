# Alfa Romeo 156 Ti - Car SystemUI Runtime Resource Overlay (RRO)

Ten moduł to oficjalny **Runtime Resource Overlay (RRO)** dla **Android Automotive OS (AAOS)**, modyfikujący górny pasek (`TopCarSystemBar`) i dolny pasek (`BottomCarSystemBar`) procesu `com.android.systemui` na poziomie całego systemu.

Dzięki niemu motyw Alfy Romeo (Alfa Rosso `#D11919`, carbon `#0E1013` / `#14171C`, ikony) jest utrzymany **natywnie w każdej aplikacji** (Mapy, Spotify, Telefon, Ustawienia).

---

## 1. Jak wpleść ten moduł do kompilacji AOSP (x86 / AAOS)

### Krok A: Skopiowanie folderu do drzewa źródeł AOSP
Skopiuj cały katalog `AlfaCarSystemUIOverlay` do swojego drzewa źródeł w jedno z poniższych miejsc (zależnie od struktury Twojego drzewa):

```bash
# Opcja rekomendowana (nakładki OEM dla samochodów):
packages/services/Car/car_product/overlay/AlfaCarSystemUIOverlay/

# Lub do katalogu Twojego targetu urządzenia x86:
device/your_vendor/your_x86_board/overlay/AlfaCarSystemUIOverlay/
```

### Krok B: Dodanie do `device.mk` lub `car.mk`
W pliku konfiguracyjnym Twojego produktu (np. `device/your_vendor/your_x86_board/device.mk` lub `packages/services/Car/car_product/build/car.mk`) dopisz paczkę do zmiennej `PRODUCT_PACKAGES`:

```make
PRODUCT_PACKAGES += \
    AlfaCarSystemUIOverlay
```

### Krok C: Automatyczne włączenie nakładki w systemie
Aby nakładka była domyślnie aktywna od pierwszego uruchomienia systemu (bez potrzeby ręcznego `cmd overlay enable`), upewnij się, że w `device.mk` znajduje się wpis:

```make
PRODUCT_ENFORCE_RRO_TARGETS := *
```
lub dodaj nazwę pakietu do `PRODUCT_PACKAGES_DEBUG` / `car_base.mk`.

Po wykonaniu `m AlfaCarSystemUIOverlay` lub pełnej kompilacji `m -j$(nproc)` obrazu, system wygeneruje i podpisze nakładkę kluczem platformy w partycji `/product/overlay/`.

---

## 2. Testowanie na żywo na emulatorze bez rekompilacji AOSP

Na emulatorze AAOS z dostępem roota możesz natychmiast aplikować i przełączać motyw za pomocą mechanizmu `cmd overlay fabricate`:

```bash
# Włączenie czerwonych akcentów Alfa Rosso na pasku systemowym:
adb shell cmd overlay fabricate --target com.android.systemui --name AlfaRossoSelected com.android.systemui:color/car_nav_icon_fill_color_selected 0x1c 0xffd11919
adb shell cmd overlay enable com.android.shell:AlfaRossoSelected

# Restart SystemUI, aby odświeżyć widok:
adb shell pkill -f com.android.systemui
```
