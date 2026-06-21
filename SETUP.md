# SAFAD Project Setup

## Main Class

Run the project with:

```text
Main
```

## Project Type

This project is a plain Java/IntelliJ desktop project. It does not use Maven or Gradle, so the JavaFX SDK must be supplied through your local environment.

## Recommended Setup

Install OpenJFX that matches your JDK version, then set:

```powershell
$env:JAVAFX_HOME = "C:\path\to\javafx-sdk"
```

Run:

```powershell
.\run.ps1
```

The script compiles all sources and launches `Main`.

## Database

The app uses:

```text
src/db/DBConnection.java
```

No database schema, table names, column names, or DAO SQL are changed by the UI structure.
