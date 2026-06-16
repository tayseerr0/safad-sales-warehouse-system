# SAFAD JavaFX UI

JavaFX is the default SAFAD desktop UI. The older Swing UI is still kept as a legacy fallback.

## Main Class

Run the project normally with:

```text
Main
```

`Main` launches:

```text
ui.fx.FxMainApp
```

The old Swing interface remains available through:

```text
LegacySwingMain
```

## Project Type

This project is a plain Java/IntelliJ project. It does not use Maven or Gradle, so JavaFX must be supplied with a JavaFX SDK or runtime jar.

## Recommended Setup

Install OpenJFX that matches your JDK version, then set:

```powershell
$env:JAVAFX_HOME = "C:\path\to\javafx-sdk"
```

Run:

```powershell
.\run-javafx.ps1
```

The script compiles all sources and launches `ui.fx.FxMainApp`. It also supports the local legacy JavaFX 8 runtime jar if it is available on this machine.

## Database

The JavaFX UI uses the existing:

```text
src/db/DBConnection.java
```

No database schema, table names, column names, or DAO SQL were changed for the JavaFX UI.
