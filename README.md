# Nudgi

## System Intent

In Nudgi, players race to be the first to escape with their treasures out of a treacherous labyrinth, bending the labyrinth to their advantage to compete for a spot on the leaderboards.

## Development cycle information

- Current cycle: 1
- Cycle Intent: Players can join a game where they must race to the other corner of the board while shifting the board.


## [General planning (link)](https://docs.google.com/document/d/1OdQo34TRY2Hrxp1SSklVcMOJ-Jqr4Vcy3ZOWPZyE4w0/edit?tab=t.0 "Planning document")

## [Current status report (link)](https://docs.google.com/document/d/1xwpr5O2NyeQS4m0Wzhfy5U7ARfyJxWCm/edit#heading=h.sikzabrle87h "Current Status Report")
- Past status reports:
    - [Cycle 1 Report #1](https://docs.google.com/document/d/10v7Pbj6k7JzzrXYRHgn1d2ci_lSR2HV-/edit#heading=h.sikzabrle87h "Cycle 1 Status Report 1")
    - [Cycle 1 Report #2](https://docs.google.com/document/d/1Z5WFBWtPt5FA6oTMBvIoCHYWB1SKYJKe/edit "Cycle 1 Status Report 2")

## Design Docs - [Template (link)](https://docs.google.com/document/d/15QqPoigbQFneIwy-A7bhc5gTs5FZ6y1akalJa--ZtyA/edit?tab=t.0 "Design Doc Template (copy to use)")

### Active

- [Player Movement](https://docs.google.com/document/d/1xKP9NifVkKyYLq4-aHesP8PpuBHuFJWMUUbH6KvO2oY/edit?tab=t.0 "Player Movement")
- [Board Shifting](https://docs.google.com/document/d/1OEnIwxenCAzS8PM1t6CFdrwAKfLscrAUecjFLZhto10/edit?tab=t.0 "Board Shifting")
- [Networking](https://docs.google.com/document/d/17Nrr1fzNYtPUSSX8ve5Je1b2VpxK9WhOguunlntGuw4/edit?tab=t.0 "Networking")

### Completed

- [Board Generation](https://docs.google.com/document/d/1zmD4rErnJfma34ZQIkqu5GK1MK8kTl2Em-iosYw3BMw/edit?tab=t.0 "Board Generation")

### Upcoming

- [Turn Management](https://docs.google.com/document/d/1NOCoOTdKG_9spo3uMet2lnwfgNiNHnXw6t-LcNTdTH8/edit?tab=t.0 "Turn Management")
- [Board Objects](https://docs.google.com/document/d/1zg5x2khFJc299fgYcZNbo1OLq5wmPSmK8lM4O7iFAVQ/edit?tab=t.0 "Board Objects")

## Meeting Summaries - [Template (link)](https://docs.google.com/document/d/1YBH1sPN3Tn1IL58iR_LdvnfkF7CgVUHUkOxGYOes0Q4/edit?tab=t.0 "Meeting Summary Template (copy to use)")

- 

## LibGDX project information

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

### Platforms

- `core`: Main module with the application logic shared by all platforms.
- `lwjgl3`: Primary desktop platform using LWJGL3; was called 'desktop' in older docs.

### Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `headless:run`: starts the headless application. Note: if headless sources were not modified - and the application still creates `ApplicationListener` from `core` project - this task might fail due to no graphics support.
- `idea`: generates IntelliJ project data.
- `lwjgl3:jar`: builds application's runnable jar, which can be found at `lwjgl3/build/libs`.
- `lwjgl3:run`: starts the application.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.
