# DutSchedule

A unofficial Android app to provide better UI from [sv.dut.udn.vn](http://sv.dut.udn.vn).

# Version

- Release version: [![https://github.com/ZoeMeow1027/DutSchedule](https://img.shields.io/github/v/release/ZoeMeow1027/DutSchedule)](https://github.com/ZoeMeow1027/DutSchedule/releases)
- Pre-release version: [![https://github.com/ZoeMeow1027/DutSchedule/tree/draft](https://img.shields.io/github/v/tag/ZoeMeow1027/DutSchedule?label=pre-release%20tag)](https://github.com/ZoeMeow1027/DutSchedule/tree/draft)
- Badge provided by [https://shields.io/](https://shields.io/).

# Features & Screenshots?

- These screenshot will get you to app summary. Just navigate to [screenshot](SCREENSHOT.md) and open images to view details.

# Downloads

- Navigate to release (at right of this README) or click [here](https://github.com/ZoeMeow1027/DutSchedule/releases) to download app.

# Build app yourself

- Required Gradle: 8.5
  - Older version of Gradle will be failed while building.
- If you open project with Android Studio, make sure your IDE support Gradle [Gradle](https://gradle.org/releases/) above, which can be fixed by upgrading your IDE. After that, just build and run app normally as you do with another Android project.
- If you want to build app without IDE, just type command as you build another gradle project (note that you need to [Gradle](https://gradle.org/releases/) installed first):

```
Build: gradlew build
For Powershell: ./gradlew build
```

# FAQ

### Where can I found app changelog?

If you want to:
- View major changes: [Click here](CHANGELOG.md).
- View entire source code changes, [click here](https://github.com/ZoeMeow1027/DutSchedule/commits).
  - You will need to change branch if you want to view changelog for stable/draft version.

### Why some news in application is different from sv.dut.udn.vn?

- This app is only crawl data from sv.dut.udn.vn (web) and modify to friendly view. To make sure you can read news cache when you are offline, app will need save current news and compare to web. So, if news from web deleted, news in app will still here.

### I need to clear old news. What should I do?

- You just need to refresh news and this will clear old and get latest one automatically.

### I'm got issue with this app. Which place can I reproduce issue for you?

- You can report issue via [Issue tab](https://github.com/ZoeMeow1027/DutSchedule/issues) on this repository.

# Known issues

If you found a issue, you can report this via [issue tab](https://github.com/ZoeMeow1027/DutSchedule/issues) on this repository.
- Global news and subject news were shown not correctly.
  - You just need to refresh news and this will clear old and get latest one automatically.
- Can't get current wallpaper as my app background wallpaper.
  - On Android 14, Google is restricted for getting current wallpaper on Android 14 or later. This issue will be delayed very loong until a posible fix. You can [see why here](https://github.com/ZoeMeow1027/DutSchedule/issues/19).

# Credits, changelog and license?
- [Changelog](CHANGELOG.md)
- [Credit](CREDIT.md)
- License: [MIT](LICENSE)
