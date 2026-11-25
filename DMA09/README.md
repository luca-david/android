# DMA09 — Saving data locally: SharedPreferences, files and resources

Companion app for slides 1–30 of *EN_DMA082 Saving Data Locally* (everything before SQLite). Java only, no storage permissions, `minSdk 33`.

Three tabs, one per section of the lecture:

| Tab | Slides | What to show |
|---|---|---|
| **Preferences** | 7–17 | launch counter that survives a process kill; `apply()` vs `commit()`; live change listener; dark-mode switch; the real `settings.xml`; the Settings screen (`androidx.preference`); a *bad* plain-text password |
| **Files** | 18–26 | save / append / read / list / delete in `getFilesDir()`; try-with-resources everywhere; cache write, size, clear; export to external app-specific storage; all paths |
| **Shared** | 27–30 | file picker (`CreateDocument` / `OpenDocument`); `res/raw` vs `assets`; sharing one file with `FileProvider` |

## Code map

```
DmaApp                 launch counter, night mode, app-wide preference listener
data/PrefsManager      typed wrapper over SharedPreferences (file "settings")
data/FileStore         every file operation (internal, cache, external, raw, assets, share)
ui/PrefsFragment       preferences tab
ui/FilesFragment       files tab
ui/SharedFragment      SAF launchers + FileProvider
ui/SettingsActivity    PreferenceFragmentCompat bound to the same settings.xml
util/Logs              one logging helper, adds the thread name
res/xml/preferences.xml, res/xml/file_paths.xml
res/raw/help.txt, assets/license.txt
```

## Logcat

Filter with `tag:DMA09` (all) or one area:

| Tag | What is logged |
|---|---|
| `DMA09/APP` | process start and launch number, night mode, tab changes, all storage paths |
| `DMA09/PREFS` | `apply()` / `commit()` result, every changed key and value, listener register / unregister, XML file reads, a **WARN** when a password is stored in plain text |
| `DMA09/FILES` | every save / append / read / delete with path and byte count, cache size and clearing, external storage state, **ERROR** with stack trace on `IOException` |
| `DMA09/SAF` | picker launched / cancelled, the `content://` Uri received, FileProvider Uri, errors |

Every line starts with `[thread]`. All the work here is on `main`, which is fine for tiny files.

## Demo script

1. Open the app, type a name, **Save (commit)**, kill it from Recents, reopen: counter +1, name still there (`DMA09/APP: process started, launch #N`).
2. **Show XML file**: the file on disk. Tap **Save (apply)** and **Show XML** quickly: the file may lag behind. After commit it never does.
3. Flip **Dark mode**: the app-wide listener changes the theme (`DMA09/PREFS: changed: dark_mode = true`).
4. **Save password (bad idea)**: it appears in the XML, and Logcat shows a WARN.
5. **Settings screen**: same file, same values.
6. **Files** tab: Save, Append, Read, List, Paths. Delete, then Read to see the `FileNotFoundException` handled (WARN, no crash).
7. Write cache, Cache size, Clear cache. Export to the external folder; in Device Explorer look in `Android/data/ro.ase.ie.dma09/files`.
8. **Shared** tab: pick a place to save, import it back, read `res/raw` and `assets`, share through the chooser.

## Notes

- UI messages are plain strings in the code to keep the demo readable; labels are in `strings.xml`.
- `SettingsFragment` calls `setSharedPreferencesName("settings")` so it uses our file, not the default `<package>_preferences.xml`.
- For real secrets use the Android Keystore; `EncryptedSharedPreferences` (security-crypto) is deprecated.
- Tests (`src/androidTest`): `FileStoreTest`, `PrefsManagerTest`. Run on a device or emulator.
