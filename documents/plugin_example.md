# 📘 Note

You can use the API to do more.  
👉 [Click here](https://github.com/frame-dev/MetarAPP/blob/master/documents/api_explanation.md)

---

# 📚 Events

List of all events available in the MetarAPP Plugin API:

- [CallRefreshEvent](#callrefreshevent)
- [DatabaseChangeEvent](#databasechangeevent)
- [DatabaseErrorEvent](#databaseerrorevent)
- [DatabaseSendEvent](#databasesendevent)
- [DisplayMetarEvent](#displaymetarevent)
- [DownloadedFileEvent](#downloadedfileevent)
- [LoginEvent](#loginevent)
- [LogoutEvent](#logoutevent)
- [SendIcaoEvent](#sendicaoevent)

---

## 🔄 CallRefreshEvent

Triggered when the user changes windows (e.g., from login to main).

**Properties:**

| Property | Type   | Getter       | Setter       | Notes     |
|----------|--------|--------------|--------------|-----------|
| `from`   | String | ✅ `getFrom()` | ❌            | Read-only |
| `to`     | String | ✅ `getTo()`   | ❌            | Read-only |

---

## 🗃️ DatabaseChangeEvent

Fired when the user changes the selected database in the Settings GUI.

**Properties:**

| Property       | Type   | Getter             | Setter             | Notes         |
|----------------|--------|--------------------|--------------------|---------------|
| `databaseType` | String | ✅ `getDatabaseType()` | ✅ `setDatabaseType(String)` | Modifiable |

---

## ❌ DatabaseErrorEvent

Fired when an error occurs in the database.

**Properties:**

| Property       | Type      | Getter              | Setter | Notes     |
|----------------|-----------|---------------------|--------|-----------|
| `error`        | Throwable | ✅ `getError()`      | ❌      | Read-only |
| `errorMessage` | String    | ✅ `getErrorMessage()`| ❌      | Read-only |
| `tableName`    | String    | ✅ `getDatabaseName()`| ❌      | Read-only |

---

## 📤 DatabaseSendEvent

Fired when an SQL command is sent to MySQL or SQLite.

**Properties:**

| Property    | Type   | Getter             | Setter | Notes     |
|-------------|--------|--------------------|--------|-----------|
| `tableName` | String | ✅ `getDatabaseName()` | ❌      | Read-only |
| `query`     | String | ✅ `getQuery()`     | ❌      | Read-only |

---

## 🌐 DisplayMetarEvent

Fired when the user clicks the "FullAsJson" button.

**Properties:**

| Property | Type   | Getter      | Setter | Notes     |
|----------|--------|-------------|--------|-----------|
| `icao`   | String | ✅ `getIcao()` | ❌      | Read-only |
| `data`   | String | ✅ `getData()` | ❌      | Read-only |

---

## 📁 DownloadedFileEvent

Fired when the user clicks "Download as JSON".

**Properties:**

| Property   | Type   | Getter          | Setter | Notes     |
|------------|--------|-----------------|--------|-----------|
| `filePath` | String | ✅ `getFilePath()` | ❌      | Read-only |

---

## 🔐 LoginEvent

Fired on login attempt.

**Properties:**

| Property    | Type     | Getter             | Setter | Notes     |
|-------------|----------|--------------------|--------|-----------|
| `username`  | String   | ✅ `getUsername()`  | ❌      | Read-only |
| `rememberMe`| boolean  | ✅ `isRememberMe()` | ❌      | Read-only |
| `success`   | boolean  | ✅ `isSuccess()`    | ❌      | Read-only |

---

## 🚪 LogoutEvent

Fired when the user logs out.

**Properties:**

| Property  | Type   | Getter         | Setter            | Notes               |
|-----------|--------|----------------|-------------------|---------------------|
| `username`| String | ✅ `getUsername()` | ❌               | Read-only           |
| `message` | String | ✅ `getMessage()`  | ✅ `setMessage(String)` | Modifiable |

---

## 🛩️ SendIcaoEvent

Fired when searching for ICAO METAR data.

**Properties:**

| Property | Type   | Getter      | Setter             | Notes       |
|----------|--------|-------------|--------------------|-------------|
| `icao`   | String | ✅ `getIcao()` | ✅ `setIcao(String)` | Modifiable |