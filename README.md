# Banking workspace

This outer folder is **not** the Gradle project root.

The actual Spring Boot / Gradle project is in:

- `C:\Users\lenovo\Downloads\banking\banking`

## Correct project root

Open or run commands from:

```powershell
C:\Users\lenovo\Downloads\banking\banking
```

## Build commands

### Quickest option from the outer folder

Use the helper script in this folder:

```powershell
Set-Location "C:\Users\lenovo\Downloads\banking"
.\build.ps1
```

It defaults to `build`, and you can forward other Gradle tasks or flags too:

```powershell
.\build.ps1 tasks
.\build.ps1 test
.\build.ps1 bootRun
.\build.ps1 --version
```

### Option 1: change into the real project folder first

```powershell
Set-Location "C:\Users\lenovo\Downloads\banking\banking"
.\gradlew.bat build
```

### Option 2: stay in the outer folder and target the nested project

```powershell
Set-Location "C:\Users\lenovo\Downloads\banking"
.\banking\gradlew.bat -p .\banking build
```

## Other useful commands

```powershell
Set-Location "C:\Users\lenovo\Downloads\banking\banking"
.\gradlew.bat tasks
.\gradlew.bat test
.\gradlew.bat bootRun
```

## IDE tip

If possible, open `C:\Users\lenovo\Downloads\banking\banking` as the project root in your IDE to avoid running Gradle from the wrong folder.

