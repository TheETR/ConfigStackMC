# ConfigStackMC

<p align="center">
  <a href="https://github.com/TheETR/ConfigStackMC/releases/latest">
    <img src="https://img.shields.io/github/v/release/TheETR/ConfigStackMC?style=for-the-badge&logo=github&label=Latest%20Release" alt="Latest GitHub release">
  </a>
  <a href="https://github.com/TheETR/ConfigStackMC/releases">
    <img src="https://img.shields.io/github/downloads/TheETR/ConfigStackMC/total?style=for-the-badge&logo=github&label=Downloads" alt="GitHub release downloads">
  </a>
  <a href="https://github.com/TheETR/ConfigStackMC/actions/workflows/release.yml">
    <img src="https://img.shields.io/github/actions/workflow/status/TheETR/ConfigStackMC/release.yml?branch=main&style=for-the-badge&logo=githubactions&label=Release%20Build" alt="Release workflow status">
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Paper-server%20plugin-2E7DDE?style=for-the-badge" alt="Paper server plugin">
  <img src="https://img.shields.io/badge/Fabric-singleplayer%20%2B%20server%20mod-DB8A00?style=for-the-badge" alt="Fabric singleplayer and server mod">
  <img src="https://img.shields.io/badge/Java-25-B07219?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25">
</p>

<p align="center">
  <strong>Change Minecraft item stack sizes with a simple config file.</strong>
</p>

---

ConfigStackMC lets you change stack sizes for vanilla items and Fabric mod items from one simple config file.

It works on Paper servers, Fabric singleplayer worlds, and Fabric multiplayer servers. Items you leave out of the config keep their normal stack size.

By default, the included config uses vanilla values, so installing ConfigStackMC does **not** change gameplay until you edit it.

<p align="center">
  <a href="#-demo">Demo</a> •
  <a href="#-download">Download</a> •
  <a href="#-which-file-should-i-use">Which file?</a> •
  <a href="#-configuration">Configuration</a> •
  <a href="#-commands">Commands</a> •
  <a href="#-troubleshooting">Troubleshooting</a>
</p>

---

## 🎬 Demo

<p align="center">
  <img src="assets/ConfigStackMC-demo.gif" alt="ConfigStackMC item stack size demo">
</p>

---

## 📦 Download

Download the latest build from the **Releases** page:

**https://github.com/TheETR/ConfigStackMC/releases/latest**

Each release contains separate files:

| File | Use it for | Where it goes |
| --- | --- | --- |
| `ConfigStackMC-...-paper.jar` | Paper servers | `plugins/` |
| `ConfigStackMC-...-fabric.jar` | Fabric singleplayer or Fabric multiplayer servers | `mods/` |
| `SHA256SUMS.txt` | Optional checksum verification | Nowhere; only for checking downloads |

> You only need the jar that matches your setup. The source code archives are there for developers who want to build the project themselves.

---

## 🧩 Which file should I use?

### I run a Paper server

Use the **paper jar**.

Players do **not** need to install anything. Only the server needs the plugin.

### I want this in singleplayer

Use the **fabric jar** in your local `mods/` folder.

Singleplayer does not load Paper plugins, so the Fabric version is the singleplayer option.

### I run a Fabric multiplayer server

Use the **fabric jar** on the Fabric server.

ConfigStackMC is designed to work on the logical server side. If your server setup allows vanilla or unmodded clients, players should not need ConfigStackMC just for this mod. If your modpack/server already requires Fabric clients for other mods, keep using the matching Fabric setup for that pack.

---

## ✅ Compatibility

Each release targets a specific Minecraft version. Check the release title before downloading and use the jar that matches your game or server version.

Example release title:

```text
ConfigStackMC v1.0.0-mc26.1.2
```

Paper and Fabric builds are provided separately for the same target version.

A successful build means the project compiled for that target. For an important server or world, test the new jar once before replacing your current setup.

---

## 🛠️ Paper installation

1. Download the `ConfigStackMC-...-paper.jar` file from Releases.
2. Put it into your Paper server's `plugins/` folder.
3. Start or restart the server.
4. Open the generated config:

```text
plugins/ConfigStackMC/config.yml
```

5. Edit the stack sizes you want.
6. Run this command in game or console:

```text
/stackconfig reload
```

---

## 🧵 Fabric installation

Install Fabric Loader and Fabric API for the Minecraft version shown in the release notes, then download the `ConfigStackMC-...-fabric.jar` file.

### Singleplayer

1. Put the Fabric jar into your local `mods/` folder.
2. Start the game once.
3. Open the generated config:

```text
config/configstackmc.yml
```

4. Edit the stack sizes you want.
5. Run:

```text
/stackconfig reload
```

### Multiplayer server

1. Put the same Fabric jar into the server's `mods/` folder.
2. Start or restart the server once.
3. Open the generated config:

```text
config/configstackmc.yml
```

4. Edit the stack sizes you want.
5. Run:

```text
/stackconfig reload
```

---

## ⚙️ Configuration

Paper config path:

```text
plugins/ConfigStackMC/config.yml
```

Fabric config path:

```text
config/configstackmc.yml
```

Default config uses normal vanilla values. It is safe to install without changing anything.

```yaml
max-allowed-stack-size: 99
warn-dangerous-items: true

items:
  dried_ghast: 64
  bell: 64
  ender_pearl: 16
  snowball: 16
  egg: 16
  totem_of_undying: 1
  saddle: 1
  minecart: 1
  water_bucket: 1
  lava_bucket: 1
  milk_bucket: 1
```

Example edited config:

```yaml
max-allowed-stack-size: 99
warn-dangerous-items: true

items:
  ender_pearl: 64
  snowball: 64
  egg: 64
  totem_of_undying: 16
  water_bucket: 16
  lava_bucket: 16
  milk_bucket: 16
```

Item id rules:

- `ender_pearl` is treated as `minecraft:ender_pearl`.
- `minecraft:totem_of_undying` also works.
- Items not listed in the config stay completely vanilla.
- Invalid item names are ignored with a warning instead of crashing the server/game.
- Stack sizes must be from `1` to the safe API limit, currently `99`.

### Modded items

On Fabric, modded items should work as long as you use their full item id:

```yaml
items:
  create:brass_ingot: 64
  farmersdelight:tomato: 32
  another_mod:custom_item: 16
```

The part before `:` is the mod id. Leaving it out makes ConfigStackMC treat the item as a normal `minecraft:` item.

Paper does not have true Fabric/Forge mod items. Custom items added by Paper plugins are usually based on a vanilla material, so ConfigStackMC may only see the base item instead of that custom item by itself.

---

## ⌨️ Commands

| Command | Permission | What it does |
| --- | --- | --- |
| `/stackconfig reload` | `stackconfig.reload` | Reloads the config and reapplies rules |
| `/stackconfig info <item>` | `stackconfig.info` | Shows configured and vanilla stack size info |
| `/stacks` | same permissions | Short alias for `/stackconfig` on Paper |

Examples:

```text
/stackconfig reload
/stackconfig info ender_pearl
/stackconfig info minecraft:totem_of_undying
```

> [!IMPORTANT]
> After using `/stackconfig reload` or `/stacks reload`, some existing stacks may not update instantly. Reopen the inventory, reconnect, or restart the server/singleplayer world if needed.

---

## 🎒 What ConfigStackMC changes

ConfigStackMC only changes the max stack size of configured items.

It does **not**:

- rename items
- change lore
- add enchantments
- repair or damage items
- merge different items together
- touch items that are not listed in the config

Minecraft still decides whether two item stacks are compatible. For example, two items with different custom data usually will not stack together even if their max stack size is higher.

---

## ⚠️ Notes about special items

Be careful when increasing stack sizes for items that can store special data, such as:

- shulker boxes
- bundles
- written books
- filled maps
- damaged tools
- armor
- weapons

ConfigStackMC does not block them, but stacking data-heavy items can have gameplay consequences. Keep `warn-dangerous-items: true` enabled if you want warnings in the log.

---

## 🧯 Troubleshooting

Most problems are caused by using the wrong jar or mixing versions.

- **Paper server:** use the jar with `paper` in its name and place it in `plugins/`.
- **Fabric:** use the jar with `fabric` in its name, place it in `mods/`, and make sure Fabric Loader and Fabric API match the Minecraft version shown in the release.
- **Config changes are not showing:** run `/stackconfig reload`, then reopen the inventory or reconnect. Existing stacks may need a server or world restart.
- **Still not loading:** check the first ConfigStackMC warning or error in the server/game log. It usually points directly to the problem.

Still stuck or found a bug? [Open an issue](https://github.com/TheETR/ConfigStackMC/issues/new) and include your Minecraft version, platform, ConfigStackMC version, and the relevant log or error message.

---

## 👨‍💻 For developers

Most users do not need to build this project. Download a jar from Releases instead.

Default local build:

```bash
./gradlew build
```

Build for a specific target manually:

```bash
./gradlew :common:test :paper:jar :fabric:jar --no-daemon --stacktrace \
  -PminecraftVersion=26.1.2 \
  -PpaperApiVersion=26.1.2.build.69-stable \
  -PpaperApiVersionShort=26.1 \
  -PfabricLoaderVersion=0.18.6 \
  -PfabricApiVersion=0.152.1+26.1.2
```

Artifacts are separate:

- `paper/build/libs/ConfigStackMC-1.0.0-paper.jar`
- `fabric/build/libs/ConfigStackMC-1.0.0-fabric.jar`

The release workflow supports:

- manual releases through **Actions > Release > Run workflow**
- `minecraft_version: auto`
- automatic daily checks for new compatible targets
- release tags like `v1.0.0-mc26.1.2`

---

## 📜 License

All rights reserved unless a separate license file says otherwise.
