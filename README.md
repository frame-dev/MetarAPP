# ✈️ MetarAPP

**MetarAPP** is a desktop application that retrieves and displays METAR (Meteorological Aerodrome Report) data — ideal for flight simulator enthusiasts and aviation weather fans.

> ⚠️ **Disclaimer:**  
> This app is intended for **flight simulation use only**.  
> **Do not use it for real-world aviation or operational decisions.**

---

## 📌 Information

- The application relies on two APIs:
  - 🌐 **MetarAPI** – *Required*
  - 🌐 **AirportData API** – *Optional*

> If the **AirportData API** is temporarily unavailable, an error may appear.  
> The application will still function normally using only the **MetarAPI**.

---

## 🧩 API

Here is a guide for the API:  
👉 [Click here](https://github.com/frame-dev/MetarAPP/tree/master/documents)

---

## 🔌 Plugin Support

You can create custom plugins to extend MetarAPP’s functionality!

📦 Example plugin repository:  
👉 [MetarAPP Plugin Example](https://github.com/frame-dev/MetarAPP-Plugin-Example)

---

## 📥 Installation

Download the latest version here:  
👉 [Download MetarAPP](https://framedev.ch/others/metarapp.php)

> ✅ **Java 11 or later is required**

---

## 🛠️ Build from Source

**Requirements:**  
- Java 11+  
- Maven

### Steps:

```bash
mvn clean package
```
After the build completes, navigate to the target folder. You’ll find:
```
MetarAPP-VERSION.jar
```
Extract this JAR into a separate folder. Then, in a terminal opened in that folder, run:
```
java -jar MetarAPP-VERSION.jar
```
## 📫 Feedback & Issues

Found a bug or have a suggestion?  
Feel free to [open an issue](https://github.com/frame-dev/MetarAPP/issues) or contribute via pull request!

---

## 🛡️ License

© FrameDev — All rights reserved.  
Please do not modify or redistribute without permission.