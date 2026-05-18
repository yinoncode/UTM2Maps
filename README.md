# UTM2Maps

UTM2Maps is a native Android app written in Kotlin and Jetpack Compose. It reads an image with shortened UTM coordinates, extracts text with ML Kit Text Recognition, converts the selected coordinate to WGS84 latitude/longitude offline, and builds a Google Maps link.

## Default settings

- UTM Zone Number: `36`
- Latitude Band: `R`
- Hemisphere: `North`
- Northing Prefix: `3`
- Auto open Google Maps: `false`
- Copy link automatically: `true`

The latitude band is stored for context. The actual conversion uses zone number and hemisphere, so the default `36R` is converted as UTM Zone `36N`.

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
