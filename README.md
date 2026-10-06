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

Häufige SQL Queries

Performance-Tipps

Backup Strategie

Lizenz & Nutzungsbedingungen

Installation
1. Voraussetzungen
Paper Server: 1.21.1+

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
            username = System.getenv("USERNAME") // GitHub-Nutzername
            password = System.getenv("TOKEN")    // GitHub Token (ghp_...)
        }
    }
}

dependencies {
    implementation "de.grimlock.nitromc:nitro-core:1.0.0"
}
Maven
XML
<repositories>
    <repository>
        <id>github</id>
        <url>https://maven.pkg.github.com/nitromc-minecraft/nitro-core</url>
    </repository>
</repositories>

<dependencies>
    <dependency>
        <groupId>de.grimlock.nitromc</groupId>
        <artifactId>nitro-core</artifactId>
        <version>1.0.0</version>
    </dependency>
</dependencies>
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
    private final UUID uuid;
    private final int kills;
    private final int deaths;

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

@AutoTable // Erstellt und registriert die Tabelle automatisch beim Start!
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
