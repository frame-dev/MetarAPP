# 🧩 MetarAPPApi Documentation

## 🌐 API Availability Checks

### `boolean isMetarAPIOnline()` {#isMetarAPIOnline}

Checks if the MetarAPI is reachable and responding successfully.

### `boolean isAirportAPIOnline()` {#isAirportAPIOnline}

Checks if the AirportData API is available.

---

## 📦 Version & Changelog Access

### `List<String> getAllVersions()` {#getAllVersions}

Returns all available released versions.

### `List<String> getAllPreReleaseVersions()` {#getAllPreReleaseVersions}

Returns all available pre-release versions.

### `List<List<String>> getAllChangelogs()` {#getAllChangelogs}

Returns changelogs (features, fixes, performance changes) for all versions.

### `List<List<String>> getAllPreReleaseChangelogs()` {#getAllPreReleaseChangelogs}

Same as above but for pre-release versions.

### `String getMetarAPPVersion()` {#getMetarAPPVersion}

Returns the current running application version.

### `String getLatestVersion()` {#getLatestVersion}

Returns the latest stable version.

### `String getLatestPreReleaseVersion()` {#getLatestPreReleaseVersion}

Returns the latest pre-release version.

---

## 🔗 Download Links

### `String getDownloadString(String version)` {#getDownloadString}

Returns OS-specific download link for a given version.

### `String[] getDownloadStringAllTypes(String version)` {#getDownloadStringAllTypes}

Returns both Windows .exe and Unix .jar links for a version.

### `boolean isVersionAvailable(String version)` {#isVersionAvailable}

Checks if a version is downloadable.

### `String getLatestDownloadLink()` {#getLatestDownloadLink}

Returns download link for the latest stable version.

### `String getLatestPreReleaseDownloadLink()` {#getLatestPreReleaseDownloadLink}

Returns download link for the latest pre-release version.

---

## 🌤️ METAR & Airport Data

### `MetarRequest getMetarRequest(String icao)` {#getMetarRequest}

Returns the full request object for a given ICAO.

### `MetarData getMetarData(String icao)` {#getMetarData}

Returns parsed METAR weather data.

### `AirportData getAirportData(String icao)` {#getAirportData}

Returns parsed airport information.

### `AirportRequest getAirportRequest(String icao)` {#getAirportRequest}

Returns the raw request object for the airport.

---

## 🕹️ User Activity

### `List<String> getLastSearchList()` {#getLastSearchList}

Returns the last searched ICAOs for the current user session.

### `List<String> getFavouriteIcaos()` {#getFavouriteIcaos}

Reads and returns the user’s saved favorite ICAOs from file.

---

## 🧪 Integration Methods

### `void sendIcaoToMetarAPP(String icao)` {#sendIcaoToMetarAPP}

Injects the ICAO code into the GUI field and triggers a search.

### `void openFullAsJson(String icao)` {#openFullAsJson}

Opens a new GUI window displaying full METAR data as JSON.

---

## 📋 Notes

* Many methods may throw `IOException`. Always handle them properly.
* Uses `UpdateHandler` internally for version checking (e.g. `getLatestVersion()`).
* This API is singleton-based and reflects the current application state.
