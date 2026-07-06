# ChefSocial

Социальная сеть для поваров — Android-приложение на Kotlin + Jetpack Compose + Room.

## Возможности

- **Лента** — свежие рецепты от поваров сообщества
- **Профиль** — ваш профиль, статистика, редактирование, список рецептов
- **Поиск** — по рецептам, ингредиентам и поварам
- **Публикация** — добавление рецептов с ингредиентами и шагами
- **Фото** — съёмка камерой или выбор из галереи
- **Комментарии** — обсуждение рецептов
- **Лайки и подписки** — отметки и подписки на поваров
- **Backend-синхронизация** — Ktor REST API (`:server` модуль)

## Стек

- Kotlin 2.0, Jetpack Compose, Material 3
- Room (SQLite) — локальное хранение
- Retrofit + Ktor — синхронизация с сервером
- Coil — загрузка фото (URL и локальные файлы)
- MVVM

## Сборка APK

### Локально

```bash
cd /Users/admin/Projects/ChefSocial
./gradlew assembleDebug
```

APK: `app/build/outputs/apk/debug/app-debug.apk`

### GitHub (CI)

При каждом push в `main` GitHub Actions собирает APK автоматически:

1. **Releases** — готовый файл `app-debug.apk` в [Releases](../../releases/latest)
2. **Artifacts** — вкладка Actions → последний workflow → Artifacts → `ChefSocial-debug-apk`

Скачать последний APK:
```bash
gh release download --repo arkadiyilyushin-arch/ChefSocial --pattern "*.apk"
```

## Запуск сервера (backend)

```bash
./gradlew :server:run
```

Сервер слушает `http://0.0.0.0:8080`. В приложении:
- **Эмулятор**: URL по умолчанию `http://10.0.2.2:8080/`
- **Реальное устройство**: укажите `http://<IP-компьютера>:8080/` в профиле

Нажмите кнопку синхронизации (↻) в ленте или в профиле.

## Установка

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell monkey -p com.chefsocial -c android.intent.category.LAUNCHER 1
```

## Структура

```
ChefSocial/
├── app/              # Android-приложение
├── server/           # Ktor REST backend
└── README.md
```
