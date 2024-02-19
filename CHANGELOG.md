# DutSchedule - CHANGELOG

- This will save changelog for application. Feel free to report issues, fork or contribute this project :))
- You can also view file changed at [https://github.com/ZoeMeow1027/DutSchedule/commits](https://github.com/ZoeMeow1027/DutSchedule/commits).

## Known issues:
- `Your current wallpaper` option in app background settings will be disabled on Android 14. You can check why in `Issue` tab in repository.

## 2.0-draft15 (971)
- Others:
  - NewsBackgroundUpdateService will now start if phone is booted.
  - Make a solution to fix issue #18.
  - Move all views in all activities to `ui\view`.
  - Merge OpenLink in `utils` into BaseActivity.

## 2.0-draft14 (954)
- NewsBackgroundUpdateService:
  - Fixed a issue cause news subject parsed as news global.

## 2.0-draft13 (951)
- General:
  - Updated dependencies to latest version.
  - Updated dutwrapper to fix a issue cause resultT4 and resultT10 returned wrong values.
  - Updated LargeTopAppBar container color to transparent again due to recent dependency.
  - Fixed MainViewModel is not initialized (temporary).
  
- AccountActivity:
  - Reworked subject result list UI in account training status.

- NewsActivity:
  - Changed news type and search method to SegmentedButton.
  - Merge NewsActivity and NewsDetailActivity into one.
  
- SettingsActivity:
  - Moved "New parse method on notification" to "Experiment Settings" because of very experiment.
  - "New parse method on notification" settings will be turned off at default.
  - News Background Update Service will show subject name in title by default (for subject news).

## 2.0-draft12 (933)
- **Important note**:
  - `Your current wallpaper` option in app background settings will be disabled on Android 14. You can check why in `Issue` tab in repository.

- General:
  - Optimize codes.
  - Updated dependencies to latest version.

- Account Session:
  - Increase expired duration to 10 minutes.

- Account Activity:
  - Made School Year combobox selection fixed location in Subject Result.

- Settings Activity:
  - Add option to edit "School year" variable settings in expriment settings.
  - Move all dialog to `ui.components`.

- NewsUpdateService:
  - Optimize `NewsUpdateService` to prevent too many request when MainActivity is paused or stopped.

- News Activity:
  - Rework Search function in News Search.
    - Two option, `Search Method` and `News Type` is now in a row instead of IconButton.
	- Add news search history for quick search.

## 2.0-draft11 (886)
- Account Activity:
  - Redesign items in Subject Information (previous named Subject Schedule) and Subject Fee.
  - Add a visibility icon for showing password for Password OutlinedTextField.
  - Search bar for search a SubjectResult.
  - School year filter in SubjectResult.

- [New] Help Activity:
  - This will explain and provide external link related for DUT school.

- News Activity & News Detail Activity:
  - News Search will split variables to NewsSearchViewModel.
  - In news details - subject, content description will show entire content whenever new can parse to news subject.

- Main Activity:
  - Redesign components for optimize view.
    - Lessons today
    - Date and time (add current school year and week)
    - School news

- MainViewModel:
  - Various changes for easier for manage them.

- [New] NewsUpdateService:
  - This service ensure notify you new announcements in sv.dut.udn.vn.

- App Settings & Settings Activity
  - [New] News Subject with new parse
    - This will apply a new parse for news subject if supported.
    - Access them in App Settings.
  - [View only] Background opacity and Component opacity
    - This will change background color and component opacity.
    - Will shown only when BackgroundImageOption isn't equal BackgroundImageOption.None.
  - Redesign in News Filter Settings.
  - Reorder some options to correct group.
  - Added and modified some properties. You might need to delete app or clear app data before updating. We will update a migrations in future.

- Miscellaneous:
  - Updated dependencies to latest.
  - Add new permission: SCHEDULE_EXACT_ALARM, FOREGROUND_SERVICE.
  - Modified app icon: Got from old project - DUTApp_API (this may be changed in future).
  - Rework BaseActivity.kt: No more built-in Scaffold, transparent status bar.
  - App will have black background in SplashScreen if system dark mode have enabled.
  - Fix a issue cause news subject page automatically refresh.
  - Make transparent for component if BackgroundImageOption isn't equal BackgroundImageOption.None.
  - Improve performance and optimize codes.
  - Merge extension function into one.

## 2.0-draft10 (701)
- [New] Account training details (all subjects result), but not done yet.
- [Changed] Implement dependencies to latest version.
- [Changed] Optimized code usages.
- [Fixed] Request permissions now update its status.

## 2.0-draft9 (692)
- [New] News search (however, this's new feature, so maybe improve later).

## 2.0-draft8 (683)
- [New] Account training status.
- [Changed] Implement dependencies to latest version.
- [Changed] Optimized code usages.

## 2.0-draft7 (672)
- [New] Fetch news background duration (just a option, not working yet).
- [New] News filter settings (just a option, not working yet).
- [Changed] Add refresh button in Subject Schedule, Subject Fee and Account Information (in Account activity).
- [Changed] Implement dependencies to latest version.
- [Changed] Optimized code usages and reduce code weight.

## 2.0-draft6 (660)
- [New] Background Image from file you chose (note: this option will access only image you choosed).
- [Changed] Removed unnecessary repositories and update to latest.
- [Changed] Now preload data from MainViewModel instead of activities.
- [Changed] Changed some enum name in Model. This might require you to CLEAR APP DATA if current is crashed.
- [Changed] News service is now News Background Update Service (this will run task on background if app isn't run on foreground).

## 2.0-draft5 (651)
- [New] News filter settings (not functionally yet).
- [Changed] News: Handle end of list for load next page.
- [Changed] Settings: Added a option to quickly navigate to Android notification settings.
- [Changed] Imporved app performance.

## 2.0-draft4 (640)
- News activity works (but not fully functionally yet).
- News service (just test, not ready yet).
- Improved app performance.

## 2.0-draft3 (604)
- Improve app performance.

## 2.0-draft2 (600)
- Rollback Account feature, app theme, and more in settings.
- Update dependencies to latest.

## 2.0-draft1 (540)
- A experiment UI and re-coding this app were applied, but not working yet.
- Renamed app package to "io.zoemeow.dutschedule".

## 1.0-beta11 (532)

### What's changed:
- Updated implementations to latest.

### Fixed:
- Fixed issue in #12

## 1.0-beta10 (528)

### Important notes

- Renamed app package to "io.zoemeow.subjectnotifier".

### What's changed (general)
- Just rename package and optimize codes.

### What's changed (technically information)
- androidx.compose.material3:material3 fixed in 1.0.0-beta3.
- Moved from SmallTopAppBar (as deprecated in 1.0.0-beta3) to TopAppBar.
- Addressed a issue cause logged in account won't preload in "Account" tab.
- Created BaseBroadcastReceiver.
- Renamed AccountServiceCode to ServiceCode.
- Updated implements.
- Fully converted to receive news data with NewsBroadcastServer.
- Deleted NewsDataStore and Old news service.
- Deleted old news service.
- Adjust algorithms for better news loading performance.
- [Android 13] Added app language support in Android Settings.
- Enabled in build.gradle: shrinkResources and minifyEnabled.
- MainActivityTheme is now directly load drawable (without MainViewModel).
- Renamed ServiceCode to ServiceBroadcastOptions.
- AppBroadcastReceiver2 will replace AppBroadcastReceiver. However, this won't ready yet.
- Implements all activity to using BaseActivity.

## 1.0-beta9 (518)

### Important notes

- Renamed app to **Subject Notifier**.

### What's new

- Added back: [Google Dagger/Hilt]
- NewsFilterSettings ViewModel for NewsFilterSettingsActivity.

### What's changed

- Implemented in Vietnamese strings.
- Merge some files in Utils into a class.
- Preload subject schedule, subject fee and account information.
- Move reloadAppBackground to getCurrentWallpaperBackground in AppUtils.
- Implemented LoginState performance in NewsFilterSettingsActivity.
- Implemented with AccountBroadcastReceiver for only receive specific targets.

### Fixed

- [In 1.0-beta7] Update NewsService.kt for line 309: String.format error.

## 1.0-beta8 (501)

### What's new

- AccountBroadcastReceiver and AccountService. This will replace AccountDataStore in ViewModel.
- AccountCache. However, this isn't working yet.

### What's changed

- Gradle updated to 7.5.1.
- Implements with Vietnamese strings.
- Included a fix for [#8](https://github.com/ZoeMeow5466/DUTNotify/issues/8)
- Fixed no loading in subject fee activity.
- Fixed request permission in background image when permission is already granded.

## 1.0-beta7 (492)

- Note: This build is still marked as beta, but you can use this one as daily application.

### What's changed

- This build is nearly full translated Vietnamese language.
- Added this change log.

### Known issues

- Background Image is unavailable for Android 13.

## 1.0-beta6 (490)

- This is hotfix only for 1.0-beta5, so don't expected for many new or changed features.

### What's changed

- Week in Account Dashboard will now filter week that subject being affected by.

### Fixes

- A issue cause stopped working app when enable NewsService in App Settings. This has been fixed in 1.0-beta6.

### Known issues

- Background Image is unavailable for Android 13.

## 1.0-beta5 (472)

- ~~This build will still mark as 'beta', but you can use this version as daily for now.~~ We are aware a issue cause stopped working app when enable NewsService. This will be fixed in 1.0-beta6.

### What's new

- Simple small notification icon. App notification will now use this instead of app launcher icon.
- Getting news in background will show current status. However, this's still in beta.
- News subject details have a new design now, replacing old design due to have an issue with very looooong title.

### What's changed

- Updated implementations used in project.
- Optimized codes about requiring permissions.
- Enable news in background will now requiring POST_NOTIFICATIONS permission (required by Android 13).
- Moved all functions from DateUtils.kt to DUTDateUtils.kt.
- AccountSubWeekList will now show current date and current school year. You can adjust by arrow to view another week. However, this's still in beta due to not filtered subject by week yet.
- Optimized codes about AccountSubWeekList.kt.
- Re-login account will now use SchoolYear settings as you set in App Settings.

### Fixes

- Revert back androidx.compose.material3:material3 to 1.0.0-beta1 due to issue about transparent. [You can track issue here](https://issuetracker.google.com/issues/245626686).
- Fixed issue cause wrong date list in account dashboard.

### Known issues

- Background Image is unavailable for Android 13.

### New Additional Requests

- New permission: POST_NOTIFICATIONS (required by Android 13).

## 1.0-beta4 (423)

### What's new

- News subject filter for receiving your subject news only.

### What's changed

- Merge AppSettingsCode.kt into AppSettings.kt
- News Service is now using filter as you set up in news filter.
- Optimize codes and performance.

### Known issues

- Background Image is unavailable for Android 13.

## 1.0-beta3 (402) - Hotfix 2

### Fixes

- Wrong notification in news subject about in room showing affected lessons instead. Now solved this issue.

## 1.0-beta3 (402) - Hotfix 1

### Fixes

- Commit issue for AccountDetailsActivity.kt. Now this is resolved.

## 1.0-beta3 (402)

**NOTE:** You need to uninstall old app because of conflict settings.json.

### What's new

- Load news in background can be controlled to enable or disable them (default is now disabled).
- News subject notifications is now notify you to make up or leaving (with affected class, date, room).
- Clicked a subject in account dashboard will now displaying subject inforomation.

### What's changed

- Optimize code in Account area.
- Updated implementations.
- More translated string in vi-VN.

### Fixes

- Fixed issue cause notifications can't be shown full.

## 1.0-beta2 (352)

### What's changed:

- Merge subject schedule and subject fee into once.
- Account Information was functionally, but not done yet.

## 1.0-beta1 (347)

**Note:** App package has been renamed from io.zoemeow.dutapp.android to io.zoemeow.dutnotify.

### What's new:

- Added README.md and its screenshot.

### What's changed:

- Moved news and app in MainViewModel to two data store class.
- Redesign Subject Schedule (Account).

## 0.10-alpha2 (252)

### What's new

- External activity for clicking a notification.
- CustomClock for adjust refresh news time range.
- Refresh news settings will be applied when next trigger refresh ran (this can edit in Settings panel).
- Sorted strings.xml and adjsuted some issues.

### What's changed

- Updated build.gradle (:app) to latest implementations.
- Small optimize MainActivity.
- New settings in AppSettings.kt (Model).
- Changed default refresh news interval to 3 minutes.
- Deleted MainActivityTheme (just change to minimal parameters).

### Fixes

- Fixed a issue cause date selector in datetime showing unsorted date (you will see when you logged in).
- Notifications details is now showing correctly news details when you click to open them.

## 0.10-alpha1 (243)

### What's new:

- App icon (from internet).
- News notifications is now worked properly.

### What's changed:

- Removed Google Dagger/Android Hilt.
- Moved all news check to NewsModule.
- Moved all module to another folder named module.
- Removed all file repository (its function will moved to module).

## 0.9-alpha2 (132)

### What's new:

- PermissionRequestActivity for replacement (replace from onRequestPermissionsResult, which deprecated on java).
- dut_week.json, however, not being used yet.

### What's changed:

- Optimize implementation in build.gradle app.
- Remove trigger in uiStatus.

### Fixes

- Fixed press back twice to close app when logged in and viewing account page.
- Subject schedule view items will now show room of current day.

## 0.9-alpha1 (125)

### What's new:

- Subject schedule by day of week if you logged in.
- More strings for translation.

### What's changed:

- "Main" page has deleted. "Account" page will be default (temporary).
- "Built-in browser" has been deleted. Now, custom tabs for web will be default.
- Removed service notifications (it created for background service before).
- Merge 4 ViewModels into once.
- Optimized codes.
- Merge files which processing for date time into one file.
- "CalculateDayAgo" function will be calcuated for news.
- Merge news global and news subject into once per type.

## 0.8-alpha4 (89)

SDK was updated to 33

### What's new

- New service: RefreshNewsService. This will check latest news in 15 minutes or time you specificed. However, this isn't completed yet.
- Introduced news cache for offline reading and notifications.
- Added app cache viewmodel for news.
- Add new page: Account Information. However, this isn't done yet.

### What's changed

- Deleted account query screen (not need).
- Main bottom navigation bar will use title by string id in values.xml instead of provided strings (to easier translation).

### Fixes

- Fixed fatal error in account view model when relogin (you can checkout this issue in previous version by close and re-open app).

### Known issues

- Notification Channel creation is still exist in MainActivity.kt. This will replaced with another notification id for register.

## 0.8-alpha3 (77)

### What's changed

- Optimize code in account view model and account file repository to avoid UI hang when starting app.
- Optimize code in dark/light theme.
- Fully ran optimize code in Android Studio.

## 0.8-alpha2 (65)

Note: You need to uninstall or delete old app data before continue using application.

### What's changed

- Move all status variables to UIStatus.

### Fixes

- Fixed missing permission request for needed permissions.

## 0.8-alpha1 (52)

### What's new

- Added built-in browser. However, this isn't completed yet.
- Separated news list by date for easier find them.

### What's changed

- Updated gradle and libraries to latest version.
- Removed activities, show in compose instead for subject schedule and subject fee.

### Fixes

- Fixed unreadable text in all app layout.

### Known issues

- My laptop is low-end, so in gradle.properties I will temporary config for optimize it. You can delete 5 bottom lines in that file to speed up building.

## 0.7-alpha (37)

### What's new

- Black background for AMOLED display, better battery backup.

### Fixes

- Background Image, dark/light mode and dynamic color will be updated immediately after settings changed.
- Fixed unreadable in News Details.

### Known issues

- Still unreadable text in news list when background image is set.

## 0.6-alpha (32)

### What's new

- This app is now using dagger/hilt.
- Dark/light mode and changes app theme layout immediately.
- Settings is being saved to json for reload on open app.

### Fixes

- Fix an invalid layout cause action in news top app bar is unreadable.

### New dependencies

- Google Dagger: Hilt

## 0.5-alpha (25)

### What's new

- Testing app transparent.

### What's changed

- Changed variable names in view model.
