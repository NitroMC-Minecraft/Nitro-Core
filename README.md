<div align="center">

# NITROCORE

**Ein professionelles, asynchrones Minecraft Paper Plugin Framework**

![Version](https://img.shields.io/badge/Version-1.0.0-6C5CE7?style=flat-square)
![Java](https://img.shields.io/badge/Java-21+-F39C12?style=flat-square&logo=openjdk&logoColor=white)
![Paper](https://img.shields.io/badge/Paper-1.21.1-00BDFF?style=flat-square)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=flat-square&logo=mysql&logoColor=white)
![MariaDB](https://img.shields.io/badge/MariaDB-10.5+-003545?style=flat-square&logo=mariadb&logoColor=white)
![Build](https://img.shields.io/badge/Build-Passing-00B894?style=flat-square)

</div>

---

## 📑 Inhaltsverzeichnis

- [Installation](#installation)
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

---

## Installation

### 1. Voraussetzungen

- **Paper Server** 1.21.1+
- **Java 21+**
- **MySQL 8.0+** oder **MariaDB 10.5+**
- **LuckPerms** für Permission-Features

### 2. Plugin installieren

```bash
# Build
./gradlew build

# JAR in plugins/ kopieren
cp build/libs/NitroCore-*.jar /path/to/server/plugins/
```

### 3. Datenbank konfigurieren

Beim ersten Start erstellt das Plugin automatisch `plugins/NitroCore/mysql.yml`:

```yaml
host: "localhost"
port: 3306
database: "nitrocore"
username: "nitrocore"
password: "dein_passwort"
```

> **💡 MySQL Setup** (einmaliger Setup):
> ```sql
> CREATE DATABASE nitrocore CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
> CREATE USER 'nitrocore'@'localhost' IDENTIFIED BY 'dein_passwort';
> GRANT ALL PRIVILEGES ON nitrocore.* TO 'nitrocore'@'localhost';
> FLUSH PRIVILEGES;
> ```

### 4. Server starten

```bash
java -Xmx1024M -Xms1024M -jar paper-1.21.1-xxx.jar nogui
```

> **✅ Das Plugin erstellt automatisch alle notwendigen Tabellen beim Startup.**

---

## Quick Start - Neue Tabelle in 2 Minuten

### Schritt 1: Entity-Klasse (Plain POJO)

```java
import java.util.UUID;

public class PlayerStats {
    private UUID uuid;
    private int kills;
    private int deaths;

    public PlayerStats(UUID uuid, int kills, int deaths) {
        this.uuid = uuid;
        this.kills = kills;
        this.deaths = deaths;
    }

    public UUID getUuid() { return uuid; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
}
```

### Schritt 2: Tabellen-Klasse mit @AutoTable (Das ist alles!)

```java
import de.grimlock.nitromc.database.*;
import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@AutoTable  // ← Diese Annotation genügt!
public class PlayerStatsTable extends ManagedTable<PlayerStats> {

    @Inject
    public PlayerStatsTable(DatabaseService databaseService) {
        super(databaseService);
    }

    @Override
    public TableSchema defineSchema() {
        return TableSchema.of("player_stats")
            .column(ColumnDef.of("uuid", ColumnType.VARCHAR, 36).primaryKey().notNull())
            .column(ColumnDef.of("kills", ColumnType.INT).defaultValue(0).notNull())
            .column(ColumnDef.of("deaths", ColumnType.INT).defaultValue(0).notNull())
            .foreignKey("uuid", "nitro_players", "uuid");
    }

    @Override
    public PlayerStats mapRow(ResultSet rs) throws SQLException {
        return new PlayerStats(
            UUID.fromString(rs.getString("uuid")),
            rs.getInt("kills"),
            rs.getInt("deaths")
        );
    }

    @Override
    protected Map<String, Object> toRow(PlayerStats entity) {
        return Map.of(
            "uuid", entity.getUuid().toString(),
            "kills", entity.getKills(),
            "deaths", entity.getDeaths()
        );
    }
}
```

> **✅ Fertig!** Die Tabelle wird automatisch beim Start erstellt und registriert.

### Schritt 3: Nutzen in deinem Code

```java
@Inject
private DatabaseManager databaseManager;

public void handlePlayer(Player player) {
    PlayerStatsTable table = databaseManager.get(PlayerStatsTable.class);
    UUID uuid = player.getUniqueId();

    // SELECT
    table.findById(uuid)
        .thenAccept(opt -> opt.ifPresent(stats -> {
            player.sendMessage("Kills: " + stats.getKills());
        }));

    // INSERT/UPSERT
    PlayerStats newStats = new PlayerStats(uuid, 10, 5);
    table.save(newStats)
        .thenRun(() -> player.sendMessage("Stats gespeichert!"));

    // UPDATE
    table.updateMultiple("uuid", uuid.toString(),
        Map.of("kills", 15, "deaths", 5)
    ).join();

    // DELETE
    table.deleteById(uuid).join();

    // Advanced Query
    table.query()
        .where("kills > ?", 100)
        .orderByDesc("kills")
        .limit(10)
        .mapToList(entity -> entity)
        .thenAccept(topPlayers -> {
            player.sendMessage("Top Killers: " + topPlayers.size());
        });
}
```

---

## Neue Tabelle im Real-World Beispiel

### Entity-Klasse (Plain POJO)

```java
import java.util.UUID;

public class CustomNPC {
    private int id;
    private UUID creatorUUID;
    private String npcName;
    private double x, y, z;
    private String world;
    private String skinBase64;

    public CustomNPC(int id, UUID creator, String name, double x, double y, double z, String world, String skin) {
        this.id = id;
        this.creatorUUID = creator;
        this.npcName = name;
        this.x = x;
        this.y = y;
        this.z = z;
        this.world = world;
        this.skinBase64 = skin;
    }

    public int getId() { return id; }
    public UUID getCreatorUUID() { return creatorUUID; }
    public String getNpcName() { return npcName; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getZ() { return z; }
    public String getWorld() { return world; }
    public String getSkinBase64() { return skinBase64; }
}
```

### Table-Klasse mit @AutoTable

```java
import de.grimlock.nitromc.database.*;
import javax.inject.Inject;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;

@AutoTable  // Auto-registered on startup!
public class CustomNPCTable extends ManagedTable<CustomNPC> {

    @Inject
    public CustomNPCTable(DatabaseService databaseService) {
        super(databaseService);
    }

    @Override
    public TableSchema defineSchema() {
        return TableSchema.of("custom_npcs")
            .column(ColumnDef.of("id", ColumnType.INT)
                .primaryKey()
                .autoIncrement()
                .notNull())
            .column(ColumnDef.of("creator_uuid", ColumnType.VARCHAR, 36).notNull())
            .column(ColumnDef.of("npc_name", ColumnType.VARCHAR, 255).notNull())
            .column(ColumnDef.of("x", ColumnType.DOUBLE).notNull())
            .column(ColumnDef.of("y", ColumnType.DOUBLE).notNull())
            .column(ColumnDef.of("z", ColumnType.DOUBLE).notNull())
            .column(ColumnDef.of("world", ColumnType.VARCHAR, 100).notNull())
            .column(ColumnDef.of("skin_base64", ColumnType.BLOB).nullable())
            .column(ColumnDef.of("created_at", ColumnType.TIMESTAMP).notNull())
            .foreignKey("creator_uuid", "nitro_players", "uuid");
    }

    @Override
    public CustomNPC mapRow(ResultSet rs) throws SQLException {
        return new CustomNPC(
            rs.getInt("id"),
            UUID.fromString(rs.getString("creator_uuid")),
            rs.getString("npc_name"),
            rs.getDouble("x"),
            rs.getDouble("y"),
            rs.getDouble("z"),
            rs.getString("world"),
            rs.getString("skin_base64")
        );
    }

    @Override
    protected Map<String, Object> toRow(CustomNPC entity) {
        return Map.of(
            "id", entity.getId(),
            "creator_uuid", entity.getCreatorUUID().toString(),
            "npc_name", entity.getNpcName(),
            "x", entity.getX(),
            "y", entity.getY(),
            "z", entity.getZ(),
            "world", entity.getWorld(),
            "skin_base64", entity.getSkinBase64(),
            "created_at", System.currentTimeMillis()
        );
    }
}
```

> **🚀 Das ist alles!** Kein registrieren, kein Main.java ändern. Fertig!

### Nutzen in deinem Code

```java
public class NPCManager {

    @Inject
    private DatabaseManager databaseManager;

    // CREATE - Neuen NPC erstellen
    public void createNPC(UUID creator, String name, Location loc, String skinBase64) {
        CustomNPCTable table = databaseManager.get(CustomNPCTable.class);

        CustomNPC npc = new CustomNPC(
            0,  // ID wird vom DB auto_increment gesetzt
            creator,
            name,
            loc.getX(),
            loc.getY(),
            loc.getZ(),
            loc.getWorld().getName(),
            skinBase64
        );

        table.save(npc)
            .thenRun(() -> System.out.println("NPC erstellt!"));
    }

    // READ - NPC laden
    public void loadNPC(int id) {
        CustomNPCTable table = databaseManager.get(CustomNPCTable.class);

        table.findById(id)
            .thenAccept(opt -> opt.ifPresent(npc -> {
                System.out.println("NPC: " + npc.getNpcName() + " at " + npc.getX());
            }));
    }

    // READ - Alle NPCs eines Creators
    public void loadCreatorNPCs(UUID creator) {
        CustomNPCTable table = databaseManager.get(CustomNPCTable.class);

        table.query()
            .where("creator_uuid = ?", creator.toString())
            .orderByDesc("id")
            .mapToList(npc -> npc)
            .thenAccept(npcs -> {
                System.out.println("Creator hat " + npcs.size() + " NPCs");
            });
    }

    // UPDATE - NPC Position ändern
    public void updateNPCLocation(int id, Location newLoc) {
        CustomNPCTable table = databaseManager.get(CustomNPCTable.class);

        table.updateMultiple(
            "id", id,
            Map.of(
                "x", newLoc.getX(),
                "y", newLoc.getY(),
                "z", newLoc.getZ(),
                "world", newLoc.getWorld().getName()
            )
        ).thenRun(() -> System.out.println("Location aktualisiert!"));
    }

    // DELETE - NPC löschen
    public void deleteNPC(int id) {
        CustomNPCTable table = databaseManager.get(CustomNPCTable.class);

        table.deleteById(id)
            .thenRun(() -> System.out.println("NPC gelöscht!"));
    }

    // Advanced Query - NPCs in einer Welt
    public void loadNPCsInWorld(String world) {
        CustomNPCTable table = databaseManager.get(CustomNPCTable.class);

        table.query()
            .where("world = ?", world)
            .orderBy("npc_name")
            .limit(50)
            .mapToList(npc -> npc)
            .thenAccept(npcs -> {
                System.out.println("Welt " + world + " hat " + npcs.size() + " NPCs");
            });
    }
}
```

---

## So einfach ist es!

### Nur 3 Schritte für eine neue Tabelle:

**1. Entity schreiben** — einfaches POJO, keine Interfaces
```java
public class User {
    private UUID uuid;
    private String name;
    // getter, constructor
}
```

**2. Table-Klasse schreiben** — mit `@AutoTable`
```java
@AutoTable
public class UserTable extends ManagedTable<User> {
    @Override public TableSchema defineSchema() { /* schema */ }
    @Override public User mapRow(ResultSet rs) { /* mapping */ }
    @Override protected Map<String, Object> toRow(User e) { /* to row */ }
}
```

**3. Nutzen!**
```java
UserTable table = databaseManager.get(UserTable.class);
table.findById(uuid).thenAccept(user -> { ... });
```

> **✅ Die Tabelle wird automatisch beim Server-Start erstellt und registriert.**

### Available ColumnTypes

| Type | SQL | Beschreibung |
|------|-----|-------------|
| `ColumnType.VARCHAR` | `VARCHAR(n)` | Variable Zeichenkette |
| `ColumnType.TEXT` | `TEXT` | Lange Zeichenkette |
| `ColumnType.INT` | `INT` | Ganzzahl (32-bit) |
| `ColumnType.BIGINT` | `BIGINT` | Große Ganzzahl (64-bit) |
| `ColumnType.DOUBLE` | `DOUBLE` | Fließkommazahl |
| `ColumnType.BOOLEAN` | `BOOLEAN` | Wahr/Falsch |
| `ColumnType.BLOB` | `BLOB` | Binärdaten (Base64) |
| `ColumnType.TIMESTAMP` | `TIMESTAMP` | Zeitstempel |
| `ColumnType.DATETIME` | `DATETIME` | Datum & Zeit |

### CRUD Operations

```java
UserTable table = databaseManager.get(UserTable.class);

// READ
table.findById(uuid)                           // Optional<User>
table.findAll()                                // List<User>
table.findWhere("name", "Alice")               // List<User>
table.query().where("kills > ?", 100).mapToList(...)  // Advanced

// CREATE/UPDATE
table.save(user)                               // UPSERT
table.batchSave(List<User> users)              // Batch UPSERT

// UPDATE only specific fields
table.updateMultiple("uuid", uuid,
    Map.of("name", "Bob", "kills", 50))

// DELETE
table.deleteById(uuid)
table.deleteWhere("name", "Bob")

// UTILITY
table.count()                                  // Long
table.exists("uuid", uuid)                     // Boolean
```

> **4. ColumnTypes verfügbar:**
> ```
> VARCHAR (braucht length)    TEXT
> INT                         BIGINT
> DOUBLE                      BOOLEAN
> BLOB                        TIMESTAMP
> DATETIME
> ```

> **5. Registrierung**
> ```java
> CustomNPCTable table = injector.getInstance(CustomNPCTable.class);
> databaseManager.register(CustomNPCTable.class, table);
> ```

> **6. CRUD Operationen**
> ```java
> table.findById(id)           // Optional<T>
> table.findAll()              // List<T>
> table.findWhere("col", val)  // List<T>
> table.save(entity)           // INSERT oder UPDATE
> table.deleteById(id)         // DELETE
> table.count()                // Long
> table.query()...             // Advanced Queries
> ```

---

## Features

### 🗄️ Database ORM System

Vollständiges Management für deine Datenbank-Tabellen.

**Table Definition:**
- `ManagedTable<T>` — Erbe davon, konfiguriere Schema, fertig
- `TableSchema` — Fluent API für Spalten-Definition
- `ColumnDef` — Spalten mit Constraints: `primaryKey()`, `notNull()`, `defaultValue()`, `autoIncrement()`
- `ColumnType` — `VARCHAR`, `TEXT`, `INT`, `BIGINT`, `DOUBLE`, `BOOLEAN`, `BLOB`, `TIMESTAMP`, `DATETIME`

**CRUD Operations:**
```java
table.findById(id)                          // Optional<T>
table.findAll()                             // List<T>
table.findWhere("column", value)            // List<T>
table.save(entity)                          // UPSERT
table.updateMultiple(where, Map.of(...))    // UPDATE
table.deleteById(id)                        // DELETE
table.batchSave(List<T>)                    // UPSERT Batch
table.count()                               // Long
table.exists("column", value)               // Boolean
```

**QueryBuilder - Komplexe Queries:**
```java
table.query()
    .where("kills > ?", 50)
    .orWhere("deaths < ?", 10)
    .join("other_table", "ON condition")
    .leftJoin("third_table", "ON condition")
    .orderBy("name")
    .orderByDesc("kills")
    .limit(10)
    .offset(5)
    .mapToList(mapper)
    .thenAccept(results -> { /* ... */ });
```

**Schema Migrations:**
```java
MigrationRunner migrations = new MigrationRunner(databaseService);
migrations
    .version(1, "Create player_stats", conn -> {
        conn.prepareStatement(
            "CREATE TABLE player_stats (uuid VARCHAR(36) PRIMARY KEY)"
        ).execute();
    })
    .version(2, "Add kills column", conn -> {
        conn.prepareStatement(
            "ALTER TABLE player_stats ADD COLUMN kills INT DEFAULT 0"
        ).execute();
    })
    .run()
    .thenAccept(count -> logger.info("Applied " + count + " migrations"));
```

---

### 🔌 Dependency Injection

```java
@Inject
private DatabaseService db;

@Inject
private ConfigService config;

@Inject
private LuckPermsService luckPerms;

@Inject
private NitroCache cache;
```

> **💡 Automatische Injection.** Services registrieren sich selbst. Zero Manual Wiring.

---

### ⚡ Async Database

- Priority Queue (`LOW`, `MEDIUM`, `HIGH`, `MONITOR`)
- Circuit Breaker mit Auto-Recovery (5 Fehler → 30s Recovery)
- Connection Pooling (HikariCP, max 10 connections)
- Performance Monitoring (slow query detection)

```java
table.findById(uuid)
    .thenAccept(result -> { /* async */ })
    .exceptionally(err -> { /* error handling */ });
```

---

### 💾 Cache System

Caffeine-basiert mit Custom TTL.

```java
@Inject
private NitroCache cache;

// Set with custom TTL
cache.set("player_data_" + uuid, data, 5, TimeUnit.SECONDS);

// Typed get
PlayerData data = cache.get("player_data_" + uuid, PlayerData.class);

// Cache-Aside Pattern
PlayerData data = cache.getOrLoad("player_" + uuid, uuid ->
    database.loadFromDB(uuid)
);

// Pub/Sub
cache.subscribe("updates", msg -> logger.info("Update: " + msg));
cache.publish("updates", "player joined");

// Cleanup
cache.unsubscribe("updates", subscriber);
cache.invalidateAll(keys);
```

---

### 🔐 LuckPerms Integration

Echte Permission Checks über die LuckPerms API.

```java
@Inject
private LuckPermsService luckPerms;

if (luckPerms.isAvailable()) {
    // Permission Check (echte LP API, nicht Bukkit Passthrough)
    boolean hasAdmin = luckPerms.hasPermission(player, "nitrocore.admin");

    // Gruppen
    List<String> groups = luckPerms.getAllGroups(player);

    // Chat Formatting
    String prefix = luckPerms.getPrefix(player);    // "[ADMIN] "
    String suffix = luckPerms.getSuffix(player);    // " ◆"

    // Meta Values
    Optional<String> customRank = luckPerms.getMetaValue(player, "rank");

    // Gruppen-Management (async)
    luckPerms.addToGroup(uuid, "vip")
        .thenRun(() -> logger.info("User added to VIP"));

    // Offline Spieler laden
    luckPerms.getUserAsync(uuid)
        .thenAccept(user -> user.ifPresent(u -> { /* ... */ }));
}
```

---

### ⚙️ Config Service

Thread-safe mit Defaults und Reload Listener.

```java
@Inject
private ConfigService config;

// Typed shortcuts
String message = config.getString("messages", "welcome");
int maxPlayers = config.getInt("server", "max-players");
boolean pvpEnabled = config.getBoolean("pvp", "enabled");
List<String> ranks = config.getStringList("modules/ranks", "vip.list");

// Reload Listener
config.addReloadListener(() -> {
    logger.info("Config reloaded!");
});
config.reloadConfigs();
```

> **💡 Auto-Defaults Merging:** Resource-Defaults werden automatisch mit User-Config gemergt.

---

### 🎨 ItemBuilder

Fluent Item Creation mit Adventure Components.

```java
ItemStack sword = new ItemBuilder(Material.DIAMOND_SWORD)
    .name(Component.text("Legendary Blade")
        .color(NamedTextColor.GOLD))
    .lore(
        Component.text("A legendary weapon"),
        Component.text("Damage: +5")
    )
    .glow()
    .unbreakable(true)
    .customModelData(1001)
    .enchant(Enchantment.SHARPNESS, 3)
    .flags(ItemFlag.HIDE_ENCHANTS)
    .persistentData(
        NamespacedKey.fromString("nitrocore:item_id"),
        PersistentDataType.STRING,
        "sword_001"
    )
    .build();

// Skull mit Custom Texture
ItemStack skull = new ItemBuilder(Material.PLAYER_HEAD)
    .skullTexture("eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHBzOi8v...")
    .name(Component.text("Custom Head"))
    .build();
```

---

### 🛠️ Utilities

**Text:**
```java
String colored = NitroUtils.colorize("&c&lHallo &e&oWelt");
Component component = NitroUtils.parseComponent("<gold>Willkommen!</gold>");
String plain = NitroUtils.stripColor("&c&lText");
```

**Formatting:**
```java
String duration = NitroUtils.formatDuration(5430000L);  // "1h 30m 30s"
String number = NitroUtils.formatNumber(1234567);       // "1,234,567"
```

**UUID:**
```java
if (NitroUtils.isUUID(input)) {
    UUID uuid = NitroUtils.toUUID(input).orElse(null);
}
```

**Pagination:**
```java
List<String> page = NitroUtils.paginate(allItems, 2, 10);  // Seite 2, 10 pro Seite
```

**Scheduling:**
```java
NitroUtils.runSync(plugin, () -> { /* main thread */ });
NitroUtils.runAsync(plugin, () -> { /* async */ });

NitroUtils.countdown(plugin, 10, remaining ->
    broadcastMessage(remaining + "s verbleibend!"),
    () -> broadcastMessage("Fertig!")
);
```

**Math:**
```java
int value = NitroUtils.clamp(100, 0, 50);           // 50
int random = NitroUtils.randomBetween(1, 10);       // 1-10
```

---

### 📊 Player Management

Auto-Persistierung in die Datenbank.

```java
@Inject
private NitroPlayerManager playerManager;

Optional<NitroPlayer> player = playerManager.getPlayer(uuid);

NitroPlayer nPlayer = player.get();
String name = nPlayer.getName();
long firstJoin = nPlayer.getFirstJoin();
long lastJoin = nPlayer.getLastJoin();
String primaryGroup = nPlayer.getPrimaryGroup();
boolean hasPermission = nPlayer.hasPermission("nitrocore.admin");
```

> **💡 Player-Daten werden beim Quit automatisch in die Datenbank gespeichert.**

---

### 🎯 Core Architecture

- **IService Pattern** — Alle Services implementieren `onEnable()`/`onDisable()`
- **Event Bus** — Loose Coupling zwischen Komponenten
- **Thread Pool** — Separate IO und Compute Executors
- **Performance Monitor** — SQL Query Tracking und Slow Query Detection

---

## Database Schema & Management

### Automatically Created Tables

Das Plugin erstellt diese Tabellen automatisch beim Startup:

#### `nitro_players` — Spieler-Metadaten

```sql
CREATE TABLE IF NOT EXISTS nitro_players (
    uuid CHAR(36) PRIMARY KEY,
    name VARCHAR(16) NOT NULL,
    first_join BIGINT NOT NULL,
    last_join BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

| Spalte | Typ | Beschreibung |
|--------|-----|-------------|
| `uuid` | `CHAR(36)` | Unique Player UUID (Primary Key) |
| `name` | `VARCHAR(16)` | Aktueller Spieler-Name |
| `first_join` | `BIGINT` | Timestamp des ersten Joins (ms seit Epoch) |
| `last_join` | `BIGINT` | Timestamp des letzten Logins |
| `created_at` | `TIMESTAMP` | Auto-gesetzt beim Erstellen |

#### `schema_migrations` — Migration Tracking

```sql
CREATE TABLE IF NOT EXISTS schema_migrations (
    version INT NOT NULL PRIMARY KEY,
    description VARCHAR(255) NOT NULL,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

> **💡 Wird automatisch von `MigrationRunner` erstellt und verwaltet.**

---

### Häufige Queries

**Spieler finden:**
```sql
-- Alle Spieler
SELECT uuid, name, first_join, last_join FROM nitro_players;

-- Mit Namen
SELECT * FROM nitro_players WHERE name = 'max';

-- Neue Spieler (letzte 7 Tage)
SELECT name FROM nitro_players
WHERE first_join > UNIX_TIMESTAMP() * 1000 - (7 * 24 * 60 * 60 * 1000);

-- Spieler die nie zurückkamen
SELECT name, first_join FROM nitro_players WHERE last_join = first_join;
```

**Performance überprüfen:**
```sql
-- Datenbank-Größe
SELECT ROUND(SUM(data_length + index_length) / 1024 / 1024, 2) as 'MB'
FROM information_schema.TABLES WHERE table_schema = 'nitrocore';

-- Per Tabelle
SELECT table_name, TABLE_ROWS,
       ROUND((data_length + index_length) / 1024 / 1024, 2) AS 'MB'
FROM information_schema.TABLES WHERE table_schema = 'nitrocore';

-- Slow Queries tracken
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;
```

**Indizes erstellen:**
```sql
CREATE INDEX idx_name ON nitro_players(name);
CREATE INDEX idx_last_join ON nitro_players(last_join DESC);
```

---

### Query Performance Tipps

> **🚀 Schnell** (< 10ms für 100k Zeilen):
> ```java
> table.findById(uuid)                    // Primary Key
> table.findWhere("uuid", value)          // Indexed column
> ```

> **⚡ Mittelmäßig** (100-500ms):
> ```java
> table.query().where("name LIKE ?", "%max%").mapToList(...)
> ```

> **🐢 Langsam** (> 1s):
> ```java
> table.query().where("name LIKE ?", "%a%").mapToList(...)  // Zu viele Matches
> ```

---

### Backups

Automatisches tägliches Backup (um 3 Uhr morgens):

```bash
#!/bin/bash
BACKUP_DIR="/backups/nitrocore"
DATE=$(date +%Y%m%d_%H%M%S)
mkdir -p $BACKUP_DIR

mysqldump -u nitrocore -p'password' nitrocore \
    > $BACKUP_DIR/nitrocore_$DATE.sql

# Alte Backups löschen (älter als 30 Tage)
find $BACKUP_DIR -name "*.sql" -mtime +30 -delete
```

Cron-Job: `0 3 * * * /path/to/backup.sh`

---

### Design Patterns

**Entity mit Saveable Interface:**
```java
public class PlayerStats implements Saveable {
    private UUID uuid;
    private int kills;

    @Override
    public Map<String, Object> toRow() {
        return Map.of("uuid", uuid.toString(), "kills", kills);
    }

    @Override
    public String getPrimaryKeyColumn() { return "uuid"; }

    @Override
    public Object getPrimaryKeyValue() { return uuid.toString(); }
}
```

**UPSERT (Insert or Update):**
```java
// Existiert der Eintrag? Update ihn. Sonst insert.
table.save(new PlayerStats(uuid, 100));

// SQL dahinter:
// INSERT INTO player_stats VALUES (?, ?)
// ON DUPLICATE KEY UPDATE kills=VALUES(kills)
```

**Batch Operations:**
```java
List<PlayerStats> stats = List.of(e1, e2, e3);
table.batchSave(stats).join();  // Alle auf einmal
```

**Caching für Performance:**
```java
// 30 Minuten cachen
cache.set("player_" + uuid, playerData, 30, TimeUnit.MINUTES);

// Später abrufen
PlayerData data = cache.get("player_" + uuid, PlayerData.class);

// Auto-Load wenn nicht im Cache
PlayerData data = cache.getOrLoad("player_" + uuid, uuid ->
    playerTable.findById(uuid).join().orElse(null)
);
```

---

### Troubleshooting

| Problem | Lösung |
|---------|--------|
| `Table doesn't exist` | Plugin erstellt es automatisch; notfalls manuell mit `CREATE TABLE` |
| `Connection refused` | `mysql.yml` prüfen (host, port, credentials) |
| `Access denied` | MySQL Benutzer Rechte: `GRANT ALL PRIVILEGES ON nitrocore.* TO 'nitrocore'@'localhost'` |
| `Circuit Breaker OPEN` | 5 DB-Fehler → Auto Recovery nach 30s; MySQL Status prüfen |
| UUID Format falsch | Format: `550e8400-e29b-41d4-a716-446655440000` (mit Bindestrichen) |

---

## License

Proprietary — Alle Rechte vorbehalten

<div align="center">

**Version:** 1.0.0 | **Paper:** 1.21.1 | **Java:** 21+

Made with ❤️ for Nitro Server Network

</div>
