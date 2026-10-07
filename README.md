<div align="center">
⚡ NITROCORE
Ein professionelles, asynchrones Paper-Plugin-Framework für Minecraft

# NITROCORE
📑 Inhaltsverzeichnis
Installation

**Ein professionelles, asynchrones Minecraft Paper Plugin Framework**
Dependency Integration

![Version](https://img.shields.io/badge/Version-1.0.0-6C5CE7?style=flat-square)
![Java](https://img.shields.io/badge/Java-21+-F39C12?style=flat-square&logo=openjdk&logoColor=white)
![Paper](https://img.shields.io/badge/Paper-1.21.1-00BDFF?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat-square&logo=mysql&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-10.5+-003545?style=flat-square&logo=mariadb&logoColor=white)
![Build](https://img.shields.io/badge/Build-Passing-00B894?style=flat-square)
Quick Start — Neue Tabelle in 2 Minuten

</div>
Real-World Beispiel

---
Core Features

## 📑 Inhaltsverzeichnis
Database ORM System

- [Installation](#installation)
- [Als Dependency einbinden](#als-dependency-einbinden)
- [Quick Start - Neue Tabelle in 2 Minuten](#quick-start---neue-tabelle-in-2-minuten)
- [Neue Tabelle im Real-World Beispiel](#neue-tabelle-im-real-world-beispiel)
- [So einfach ist es!](#so-einfach-ist-es)
- [Features](#features)
  - [Database ORM System](#-database-orm-system)
  - [Dependency Injection](#-dependency-injection)
  - [Async Database](#-async-database)
  - [Cache System](#-cache-system)
  - [LuckPerms Integration](#-luckperms-integration)
  - [Config Service](#-config-service)
  - [ItemBuilder](#-itembuilder)
  - [Utilities](#-utilities)
  - [Player Management](#-player-management)
  - [Core Architecture](#-core-architecture)
- [Database Schema & Management](#database-schema--management)
  - [Automatically Created Tables](#automatically-created-tables)
  - [Häufige Queries](#häufige-queries)
  - [Query Performance Tipps](#query-performance-tipps)
  - [Backups](#backups)
  - [Design Patterns](#design-patterns)
  - [Troubleshooting](#troubleshooting)
- [License](#license)
Dependency Injection

---
Async Database & Reliability

## Installation
Cache System

### 1. Voraussetzungen
LuckPerms Integration

- **Paper Server** 1.21.1+
- **Java 21+**
- **MySQL 8.0+** oder **MariaDB 10.5+**
- **LuckPerms** für Permission-Features
Config Service

### 2. Plugin installieren
ItemBuilder

```bash
# Build
./gradlew build
Utilities & Helpers

# JAR in plugins/ kopieren
cp build/libs/NitroCore-*.jar /path/to/server/plugins/
```
Player Management

### 3. Datenbank konfigurieren
Database Schema & Management

Beim ersten Start erstellt das Plugin automatisch `plugins/NitroCore/mysql.yml`:
Automatisch erstellte Tabellen

```yaml
host: "localhost"
port: 3306
database: "nitrocore"
username: "nitrocore"
password: "dein_passwort"
```
⚡ NITROCORE
Ein professionelles, asynchrones Paper-Plugin-Framework für Minecraft

📑 Inhaltsverzeichnis
Installation

Dependency Integration

Quick Start — Neue Tabelle in 2 Minuten

Real-World Beispiel

Core Features

Database ORM System

Dependency Injection

Async Database & Reliability

Cache System

LuckPerms Integration

Config Service

ItemBuilder

Utilities & Helpers

Player Management

Database Schema & Management

Automatisch erstellte Tabellen


> **✅ Das Plugin erstellt automatisch alle notwendigen Tabellen beim Startup.**
Java: 21+

---
Datenbank: MySQL 8.0+ oder MariaDB 10.5+

## Als Dependency einbinden
Plugin: LuckPerms (für Permission-Features)

NitroCore ist als Paket auf GitHub Packages verfügbar und kann direkt in andere Plugins oder Projekte eingebunden werden.
Java: 21+

Datenbank: MySQL 8.0+ oder MariaDB 10.5+

Plugin: LuckPerms (für Permission-Features)

2. Plugin bauen & platzieren
Bash
# Plugin kompilieren
./gradlew build

# JAR in den Server-Ordner kopieren
cp build/libs/NitroCore-1.0.0.jar /path/to/server/plugins/
3. Datenbank einrichten
Beim ersten Start wird die Konfigurationsdatei plugins/NitroCore/mysql.yml generiert:

```groovy
YAML
host: "localhost"
port: 3306
database: "nitrocore"
username: "nitrocore"
password: "dein_sicheres_passwort"
💡 Einmaliges MySQL Setup:

SQL
CREATE DATABASE nitrocore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'nitrocore'@'localhost' IDENTIFIED BY 'dein_sicheres_passwort';
GRANT ALL PRIVILEGES ON nitrocore.* TO 'nitrocore'@'localhost';
FLUSH PRIVILEGES;
4. Server starten
Bash
java -Xmx1024M -Xms1024M -jar paper-1.21.1.jar nogui
Alle Kern-Tabellen werden beim Startup automatisch migriert und angelegt.

Dependency Integration
NitroCore ist über GitHub Packages als Abhängigkeit verfügbar.

Gradle (Groovy DSL)
Groovy
repositories {
mavenCentral()
maven {
name = "GitHubPackages"
url = uri("https://maven.pkg.github.com/nitromc-minecraft/nitro-core")
credentials {
            username = System.getenv("USERNAME") // Dein GitHub-Nutzername
            password = System.getenv("TOKEN")    // Dein ghp_ Classic Token
            username = System.getenv("USERNAME") // GitHub-Nutzername
            password = System.getenv("TOKEN")    // GitHub Token (ghp_...)
}
}
    mavenCentral()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/nitromc-minecraft/nitro-core")
        credentials {
            username = System.getenv("USERNAME") // GitHub-Nutzername
            password = System.getenv("TOKEN")    // GitHub Token (ghp_...)
        }
    }
}

dependencies {
implementation "de.grimlock.nitromc:nitro-core:1.0.0"
}
```

### Maven

```xml
Maven
XML
<repositories>
<repository>
   <id>github</id>
@@ -134,37 +116,29 @@ dependencies {
   <version>1.0.0</version>
</dependency>
</dependencies>
```

> **⚠️ Wichtig für Maven-Nutzer:** Damit Maven auf das GitHub Package zugreifen kann, müssen die Credentials lokal in `~/.m2/settings.xml` hinterlegt sein:
>
> ```xml
> <settings>
>   <servers>
>     <server>
>       <id>github</id>
>       <username>DEIN_GITHUB_NUTZERNAME</username>
>       <password>DEIN_GHP_TOKEN</password>
>     </server>
>   </servers>
> </settings>
> ```

> **💡 Token erstellen:** GitHub → Settings → Developer settings → Personal access tokens → Classic → `read:packages` Berechtigung auswählen.

---

## Quick Start - Neue Tabelle in 2 Minuten

### Schritt 1: Entity-Klasse (Plain POJO)

```java
🔑 Authentifizierung für Maven:

Hinterlege das Personal Access Token (read:packages) in der ~/.m2/settings.xml:

XML
<settings>
  <servers>
    <server>
      <id>github</id>
      <username>DEIN_GITHUB_USERNAME</username>
      <password>DEIN_GHP_TOKEN</password>
    </server>
  </servers>
</settings>
Quick Start — Neue Tabelle in 2 Minuten
1. Plain POJO erstellen
Java
import java.util.UUID;

public class PlayerStats {
    private UUID uuid;
    private int kills;
    private int deaths;
    private final UUID uuid;
    private final int kills;
    private final int deaths;

public PlayerStats(UUID uuid, int kills, int deaths) {
   this.uuid = uuid;
@@ -176,19 +150,16 @@ public class PlayerStats {
public int getKills() { return kills; }
public int getDeaths() { return deaths; }
}
```

### Schritt 2: Tabellen-Klasse mit @AutoTable (Das ist alles!)

```java
    public PlayerStats(UUID uuid, int kills, int deaths) {
        this.uuid = uuid;
        this.kills = kills;
        this.deaths = deaths;
    }

    public UUID getUuid() { return uuid; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
}
2. Tabellen-Klasse mit @AutoTable annotieren
Java
import de.grimlock.nitromc.database.*;
import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@AutoTable  // ← Diese Annotation genügt!
@AutoTable // Erstellt und registriert die Tabelle automatisch beim Start!
public class PlayerStatsTable extends ManagedTable<PlayerStats> {

@Inject
@@ -223,857 +194,3 @@ public class PlayerStatsTable extends ManagedTable<PlayerStats> {
   );
}
}
