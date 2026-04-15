# Entro Android - Калькулятор энтропии Шеннона

Это Android-версия приложения Entro, портированная с desktop-версии на Python.

## Функционал

Приложение позволяет рассчитывать энтропию Шеннона для различных типов каналов связи:

- **P(b|a)** - условная вероятность P(Bj|Ai)
- **P(a|b)** - условная вероятность P(Ai|Bj)  
- **P(a,b)** - совместная вероятность P(Ai,Bj)

### Возможности:

1. **Выбор размера алфавита**: 3x3, 4x4, 5x5
2. **Ввод данных вручную**: через поля ввода матрицы и вектора вероятностей
3. **Импорт из Excel**: загрузка данных из .xlsx файлов (формат аналогичен desktop-версии)
4. **Случайное заполнение**: генерация случайных значений для тестирования
5. **Расчет энтропий**:
   - H(A), H(B) - частные энтропии
   - H(A,B) - совместная энтропия
   - H(A|B), H(B|A) - условные энтропии
   - Частные условные энтропии H(ai), H(bj)

## Структура проекта

```
EntroAndroid/
├── app/
│   ├── src/main/
│   │   ├── java/com/entro/calculator/
│   │   │   ├── MainActivity.java          # Главная активность (UI + логика)
│   │   │   ├── EntropyCalculator.java     # Ядро расчета энтропии
│   │   │   ├── EntropyResult.java         # Модель данных результатов
│   │   │   └── MatrixAdapter.java         # Адаптер для матрицы
│   │   ├── res/
│   │   │   ├── layout/activity_main.xml   # Разметка главного экрана
│   │   │   ├── menu/menu_main.xml         # Меню приложения
│   │   │   └── values/strings.xml         # Строковые ресурсы
│   │   └── AndroidManifest.xml            # Манифест приложения
│   └── build.gradle                       # Конфигурация модуля app
├── build.gradle                           # Корневой build.gradle
├── settings.gradle                        # Настройки проекта
└── gradle/wrapper/                        # Gradle wrapper
```

## Сборка и запуск

### Требования:
- Android Studio Arctic Fox или новее
- JDK 8 или новее
- Android SDK API 34

### Шаги:

1. Откройте проект в Android Studio
2. Дождитесь синхронизации Gradle
3. Подключите Android-устройство или запустите эмулятор (API 24+)
4. Нажмите Run (Shift+F10)

### Сборка APK:
```bash
./gradlew assembleDebug
```
APK файл будет создан в: `app/build/outputs/apk/debug/app-debug.apk`

## Формат Excel файла

Для импорта данных создайте .xlsx файл со следующей структурой:

**Лист "Matrix"** (или первый лист):
- Квадратная матрица NxN (где N = 3, 4 или 5)
- Числовые значения вероятностей

**Лист "Vector"** (или второй лист, опционально):
- Вектор вероятностей длиной N
- Требуется для типов канала "b|a" и "a|b"

## Соответствие desktop-версии

| Desktop (Python) | Android (Java) |
|------------------|----------------|
| entro_gui.py | MainActivity.java |
| calculate_from_joint() | EntropyCalculator.calculateFromJoint() |
| calculate_uniform_conditional_entropy() | EntropyCalculator.calculateUniformConditionalEntropy() |
| normalize_vector() | EntropyCalculator.normalizeVector() |
| customtkinter UI | Android XML Layout |
| openpyxl | Apache POI |

## Лицензия

Порт создан для образовательных целей.
