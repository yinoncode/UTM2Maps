# UTM2Maps

UTM2Maps is a native Android app written in Kotlin and Jetpack Compose. It reads an image with shortened UTM coordinates, extracts text with ML Kit Text Recognition, converts the selected coordinate to WGS84 latitude/longitude offline, and builds a Google Maps link.

## Default settings

- UTM Zone Number: `36`
- Latitude Band: `R`
- Hemisphere: `North`
- Northing Prefix: `3`
- Interface Language: `Hebrew`
- Auto open Google Maps: `false`
- Copy link automatically: `true`

The latitude band is stored for context. The actual conversion uses zone number and hemisphere, so the default `36R` is converted as UTM Zone `36N`.


## Interface languages

The app supports Hebrew and English from the Settings screen. Hebrew is the default and uses RTL layout direction; English uses LTR layout direction. All screen labels, buttons, result messages, copy/share/open actions, Settings groups, the splash screen, and the About section use the selected interface language.

On launch, UTM2Maps shows a short splash/About screen with the app name and creator credit for Yinon Cohen before opening the main scanner screen.


## Manual text extraction

In addition to image OCR, the main screen includes a manual text area. Paste or type free text such as `נצ הנחתה 625854/439328`, `נ.צ. 625854 / 439328`, a multiline coordinate, or a compact 12-digit coordinate, then tap the extract button to open the same result flow used by OCR.

The Result screen also lets you copy the full recognized OCR text or re-run coordinate extraction on that recognized text.

## Supported OCR coordinate formats

Examples accepted by the parser:

```text
629073:431750
629073 431750
629073,431750
629073-431750
629073
431750
629073431750
```

For `629073:431750` with prefix `3`, the app builds the full northing by string concatenation: `3` + `431750` = `3431750`.


## Download an installable APK

The debug build is installable on a phone and is signed with Android's standard debug key.

### Option 1: GitHub Actions artifact

1. Open the repository on GitHub.
2. Go to **Actions** → **Build Android APK**.
3. Run the workflow manually with **Run workflow** or open the latest successful run.
4. Download the artifact named **UTM2Maps-debug-apk**.
5. Extract the ZIP and install `app-debug.apk` on the Android phone.

### Option 2: Local build

```bash
gradle assembleDebug
```

The APK will be created at:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Build and test

```bash
gradle test
gradle assembleDebug
```

You can also open the project in Android Studio and run the `app` configuration on a device or emulator.

## Manual sample checks

Use Settings:

- Zone = `36`
- Hemisphere = `North`
- Northing Prefix = `3`

Then scan or paste images containing these values:

| Input | Full UTM | Approx output |
| --- | --- | --- |
| `629073:431750` | Easting `629073`, Northing `3431750`, Zone `36N` | `31.012301, 34.352145` |
| `630299:429076` | Easting `630299`, Northing `3429076`, Zone `36N` | `30.988045, 34.364641` |
| `629377:431566` | Easting `629377`, Northing `3431566`, Zone `36N` | `31.010608, 34.355305` |
