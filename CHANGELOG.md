# DUTNotify ChangeLog

- This will save add developer log for this application. Feel free to report issues, fork or contribute this project :))
- You can also view file changed at [https://github.com/ZoeMeow5466/DUTNotify/commits](https://github.com/ZoeMeow5466/DUTNotify/commits).

## 1.0-beta8 (501)

### What's new
- AccountBroadcastReceiver and AccountService. This will replace AccountDataStore in ViewModel.
- AccountCache. However, this isn't working yet.

### What's changed
- Gradle updated to 7.5.1.
- Implements with Vietnamese strings.
-
### Fixed
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