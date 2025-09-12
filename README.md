# DutSchedule
A unofficial Android app to provide better UI from [sv.dut.udn.vn](http://sv.dut.udn.vn).

> [!NOTE]
> - I will decrepate this repository. My work will move to [dutschedule_flutter](https://github.com/ZoeMeow1027/dutschedule_flutter).
> - If you want to fork and add fix yourself, feel free to do that.

# Version
- Release version: [![https://github.com/ZoeMeow1027/DutSchedule](https://img.shields.io/github/v/release/ZoeMeow1027/DutSchedule)](https://github.com/ZoeMeow1027/DutSchedule/releases)
- Pre-release version: [![https://github.com/ZoeMeow1027/DutSchedule/tree/draft](https://img.shields.io/github/v/tag/ZoeMeow1027/DutSchedule?label=pre-release%20tag)](https://github.com/ZoeMeow1027/DutSchedule/tree/draft)
- Badge provided by [https://shields.io/](https://shields.io/).

# Features & Screenshots?
- These screenshot will get you to app summary. Just navigate to [screenshot](SCREENSHOT.md) and open images to view details.

# Downloads
- Navigate to release (at right of this README) or click [here](https://github.com/ZoeMeow1027/DutSchedule/releases) to download app.

# FAQ

### Where can I found app changelog?

If you want to:
- View major changes: [Click here](CHANGELOG.md).
- View entire source code changes: [Click here](https://github.com/ZoeMeow1027/DutSchedule/commits).
  - You will need to change branch if you want to view changelog for stable/draft version.

### Why some news in application is different from sv.dut.udn.vn?

- This app is only crawl data from sv.dut.udn.vn (web) and modify to friendly view. To make sure you can read news cache when you are offline, app will need save current news and compare to web. So, if news from web deleted, news in app will still here.

### I need to clear old news. What should I do?

- You just need to refresh news and this will clear old and get latest one automatically.

### I'm got issue with this app. Which place can I reproduce issue for you?

If you found a issue, you can report this via [issue tab](https://github.com/ZoeMeow1027/DutSchedule/issues) on this repository.
- Global news and subject news were shown not correctly.
  - You just need to refresh news and this will clear old and get latest one automatically.
- Can't get current wallpaper as my app background wallpaper.
  - On Android 14, Google is restricted for getting current wallpaper on Android 14 or later. This issue will be delayed very loong until a posible fix. You can [see why here](https://github.com/ZoeMeow1027/DutSchedule/issues/19).

# Developing
- Required Gradle: 8.9
  - Older version of Gradle might be failed while building.
- Build with Android Studio:
  - Make sure your IDE support [Gradle](https://gradle.org/releases/) above, which can be fixed by upgrading your IDE.
  - After that, just build and run app normally as you do with another Android project.
- Build with command line (without IDE):
  - Ensure you have installed [Gradle](https://gradle.org/releases/) and Java JDK 17 first.
  - Type command as you build another gradle project.
```
Build: gradlew build
In Powershell: ./gradlew build
```

# Credits and license?
- License: [**MIT**](LICENSE)
- DISCLAIMER:
  - This project - DutSchedule - is not affiliated with Da Nang University of Technology.
  - DUT, Da Nang University of Technology, web materials and web contents are trademarks and copyrights of Da Nang University of Technology school.
  - GitHub, GitHub mark and its icon are trademarks and copyrights of GitHub, Inc.
  - Google, Android and its icon are trademarks and copyrights of Google LLC.
  - Icons used by project is from Google Fonts.
- Used third-party dependencies:
  - [Google Accompanist](https://github.com/google/accompanist): Licensed under the [Apache License 2.0](https://github.com/google/accompanist/blob/main/LICENSE).
  - [Google Gson](https://github.com/google/gson): Licensed under the [Apache License 2.0](https://github.com/google/gson/blob/main/LICENSE).
  - [Jsoup](https://github.com/jhy/jsoup): Licensed under the [MIT license](https://github.com/jhy/jsoup/blob/master/LICENSE).
  - [timeago](https://github.com/marlonlom/timeago): Licensed under the [Apache License 2.0](https://github.com/marlonlom/timeago/blob/master/LICENSE).
