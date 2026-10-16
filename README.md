# Pantry Manager

An offline Android app that keeps track of what's in your kitchen and tells you what you can cook with it.

You record the items in your pantry (name, quantity, unit and an optional expiry date). The app then checks that stock against a built-in recipe book and suggests the recipes you can make **right now**, using only ingredients you actually have in sufficient quantity.

## Features

- **Pantry list** – add, edit and delete items. Each item has a name, quantity, unit (`g`, `kg`, `ml`, `l`, `tsp`, `tbsp`, `cup`, `pcs`) and an optional expiry date (`yyyy-MM-dd`).
- **Expiry warnings** – items are flagged `EXPIRED` or `expires soon` (3 days or less). Warnings can be switched off in Settings.
- **Suggested recipes** – lists every recipe whose ingredients are all covered by your pantry. Tap one to see the ingredients and method.
- **Smart matching**
  - Units are converted within the same dimension, so 1 kg of flour satisfies a recipe that needs 200 g.
  - Names are normalised, so `Eggs` matches `egg` and `Tomatoes` matches `tomato`.
  - Quantities of the same item are added together across entries.
  - Expired items are ignored when matching recipes.
- **Settings** – toggle expiry warnings and clear the whole pantry.
- **15 seeded recipes** (e.g. Pancakes, Tomato Pasta, Fried Rice, Greek Salad) loaded on first launch.

## Database choice: SQLite (Android `SQLiteOpenHelper`)

I chose **SQLite**, accessed through Android's built-in `SQLiteOpenHelper` (`DbHelper.java`), for these reasons:

1. **The data is relational.** A recipe has many ingredients, so the schema uses three tables (`pantry`, `recipe`, `recipe_ingredient`) linked by a foreign key.
2. **It works fully offline.** A pantry app is used in the kitchen or at the supermarket, often with poor signal. SQLite stores everything on the device with no server and no account.
3. **Zero extra dependencies.** SQLite ships with Android, which keeps the APK small and the build simple. No ORM or annotation processing is needed for a schema this size.
4. **Right-sized for the job.** Data volume is tiny (dozens of items, a handful of recipes), so a hosted database such as Firebase or a REST backend would add cost, latency and privacy concerns without any benefit. Room would also be a reasonable choice, but plain SQLite keeps the data layer transparent and easy to follow.
5. **Simple seeding and persistence.** The recipe book is inserted in `onCreate`, and the pantry persists across app restarts.

Simple user preferences (the expiry-warning toggle) are stored separately in `SharedPreferences`, since a single boolean doesn't warrant a table.

### Schema

| Table | Columns |
|---|---|
| `pantry` | `id` (PK, autoincrement), `name`, `quantity`, `unit`, `expiry` |
| `recipe` | `id` (PK, autoincrement), `name`, `instructions` |
| `recipe_ingredient` | `id` (PK, autoincrement), `recipe_id` (FK → `recipe.id`), `name`, `quantity`, `unit` |

The database file is `pantry.db` in the app's private storage. Its version is `1`, and `onUpgrade` currently drops and recreates the tables, so bumping the version resets stored data.

## Tech stack

- Java 11 source level, Android SDK (`minSdk 24`, `compileSdk`/`targetSdk 37`)
- Gradle (Kotlin DSL) with the Gradle wrapper (Gradle 9.6.0) and Android Gradle Plugin 9.4.1
- Plain `Activity` classes and programmatic/XML layouts; no network access or permissions required
- JUnit 4 unit tests for the matching and unit-conversion logic

## Project structure

```
app/src/main/java/com/nats/pantrymanager/
├── MainActivity.java         Pantry list (home screen)
├── ItemActivity.java         Add / edit / delete an item
├── SuggestedActivity.java    Recipes you can make now
├── RecipeDetailActivity.java Ingredients and method for one recipe
├── SettingsActivity.java     Expiry-warning toggle, clear pantry
├── BackActivity.java         Base class: up arrow and edge-to-edge padding
├── DbHelper.java             SQLite schema, seed data and queries
├── Matcher.java              Decides whether a recipe can be made
├── Units.java                Unit conversion, name normalisation, date helpers
└── Models.java               Item, Ingredient and Recipe data classes
app/src/test/                 Unit tests (UnitsTest, MatcherTest)
```

## Setup and run instructions

### Prerequisites

- **Android Studio** (a recent stable release that supports Android Gradle Plugin 9.x). It bundles the JDK and Android SDK tooling.
- **Android SDK Platform 37**, installed via *Tools → SDK Manager*.
- An **Android emulator** (API 24 or higher) or a **physical Android device** with USB debugging enabled.
- An internet connection on the first build, so Gradle can download the wrapper, dependencies and the JDK toolchain.

### Option A: Android Studio (recommended)

1. Clone or unzip the repository.
2. In Android Studio choose **File → Open** and select the `PantryManager` folder (the one containing `settings.gradle.kts`).
3. Wait for the Gradle sync to finish. If prompted, install any missing SDK components, and accept the suggestion to use the JDK Gradle requests.
4. Pick a device in the toolbar: create one under *Tools → Device Manager*, or plug in a phone.
5. Click **Run ▶** (or press `Shift+F10`). The app installs and launches as "Smart Pantry Manager".

`local.properties` (which points to your local Android SDK) is machine-specific and git-ignored. Android Studio regenerates it automatically. If you build from the command line, create it yourself with `sdk.dir=/path/to/Android/sdk`, or set the `ANDROID_HOME` environment variable.

### Option B: Command line

From the project root:

```bash
# Build a debug APK
./gradlew assembleDebug          # Windows: gradlew.bat assembleDebug

# Install it on a connected device or running emulator
./gradlew installDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`. The repository's Gradle config requests a specific JDK toolchain and can download it automatically on first run.

### Running the tests

```bash
./gradlew test                   # JVM unit tests (Matcher, Units)
./gradlew connectedAndroidTest   # instrumented tests (needs a device/emulator)
```

### Using the app

1. Tap **Add** and enter an item, for example `Egg`, quantity `3`, unit `pcs`. Add an expiry date if you like.
2. Add a few more ingredients (try `Butter 10 g` and `Salt 1 tsp`).
3. Tap **Suggest** to see which recipes you can cook. With the items above, *Scrambled Eggs* appears.
4. Tap a recipe to read its ingredients and method.
5. Tap an item in the pantry list to edit or delete it. Use **Settings** to turn expiry warnings on or off or to clear the pantry.

## Troubleshooting

- **"SDK location not found"** – create `local.properties` containing `sdk.dir=...`, or set `ANDROID_HOME`.
- **Gradle sync fails on the JDK** – let Gradle download the requested toolchain (needs internet), or set *Settings → Build Tools → Gradle → Gradle JDK* to the JDK Android Studio suggests.
- **Missing platform 37** – install *Android SDK Platform 37* from the SDK Manager.
- **Stale data after a schema change** – uninstall the app from the device/emulator, or clear its storage, so `pantry.db` is recreated and re-seeded.
