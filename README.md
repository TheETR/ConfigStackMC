# ConfigStackMC

<p align="center">
  <img src="https://img.shields.io/github/v/release/TheETR/ConfigStackMC?style=for-the-badge&logo=github&label=Latest%20Release" alt="Latest GitHub release">
  <img src="https://img.shields.io/github/downloads/TheETR/ConfigStackMC/total?style=for-the-badge&logo=github&label=Downloads" alt="GitHub release downloads">
  <img src="https://img.shields.io/github/actions/workflow/status/TheETR/ConfigStackMC/release.yml?branch=main&style=for-the-badge&logo=githubactions&label=Release%20Build" alt="Release workflow status">
  <img src="https://img.shields.io/badge/Minecraft-versioned%20releases-62B47A?style=for-the-badge&logo=minecraft&logoColor=white" alt="Minecraft versioned releases">
  <img src="https://img.shields.io/badge/Paper-server%20plugin-2E7DDE?style=for-the-badge" alt="Paper server plugin">
  <img src="https://img.shields.io/badge/Fabric-singleplayer%20%2B%20server%20mod-DB8A00?style=for-the-badge" alt="Fabric singleplayer and server mod">
  <img src="https://img.shields.io/badge/Java-25-B07219?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java 25">
</p>

ConfigStackMC is a small Minecraft Java project that lets you change the stack size of specific items from a config file.

Want ender pearls to stack to 64? Totems to stack to 16? Buckets to stack to 16? Add the item id to the config, set the number, reload, and you are done.

By default, ConfigStackMC uses vanilla values, so installing it does **not** change gameplay until you edit the config.

---

## Download

Download the latest build from the **Releases** page:

**https://github.com/TheETR/ConfigStackMC/releases/latest**

Each release contains separate files:

| File | Use it for | Where it goes |
| --- | --- | --- |
| `ConfigStackMC-...-paper.jar` | Paper servers | `plugins/` |
| `ConfigStackMC-...-fabric.jar` | Fabric singleplayer or Fabric multiplayer servers | `mods/` |
| `SHA256SUMS.txt` | Optional checksum verification | Nowhere; only for checking downloads |

Do **not** download the source code zip unless you are a developer. Normal users only need one of the jar files above.

---

## Which file should I use?

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

## Compatibility

Releases are built for a specific Minecraft version. Always check the release title and release notes before downloading.

Example release title:

```text
ConfigStackMC v1.0.0-mc26.1.2
```

The release workflow can automatically find the latest Minecraft version that has:

- a stable Paper build
- a matching Fabric API artifact
- a successful ConfigStackMC build

This means new Minecraft targets can be released without manually editing hardcoded version numbers every time.

Build success means the jars compile against that Minecraft/Paper/Fabric target. For main servers, test the jar on a small test world first.

---

## Paper installation

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

That is it. Vanilla clients can join normally.

---

## Fabric installation: singleplayer

1. Install Fabric Loader for the Minecraft version shown in the release notes.
2. Install Fabric API for the same Minecraft version.
3. Download the `ConfigStackMC-...-fabric.jar` file from Releases.
4. Put it into your local `mods/` folder.
5. Start the game once.
6. Open the generated config:

```text
config/configstackmc.yml
```

7. Edit the stack sizes you want.
8. Run:

```text
/stackconfig reload
```

---

## Fabric installation: multiplayer server

1. Install Fabric Loader on the server for the Minecraft version shown in the release notes.
2. Install Fabric API on the server for the same Minecraft version.
3. Download the `ConfigStackMC-...-fabric.jar` file from Releases.
4. Put it into the server's `mods/` folder.
5. Start or restart the server once.
6. Open the generated config:

```text
config/configstackmc.yml
```

7. Edit the stack sizes you want.
8. Run:

```text
/stackconfig reload
```

The Fabric jar is for both Fabric singleplayer and Fabric multiplayer servers. Paper servers still need the Paper jar instead.

---

## Config example

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

---

## Commands

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

---

## What ConfigStackMC changes

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

## Notes about special items

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

## Troubleshooting

### Paper says the jar has no `plugin.yml` or `paper-plugin.yml`

You probably placed the wrong file in `plugins/`.

Use the file with `paper` in its name:

```text
ConfigStackMC-...-paper.jar
```

Do not use the Fabric jar on Paper.

### Fabric does not load the mod

Check that you installed:

- the Minecraft version shown in the release notes
- Fabric Loader for that version
- Fabric API for that version
- the `ConfigStackMC-...-fabric.jar` file

### Nothing changed after editing the config

Run:

```text
/stackconfig reload
```

Then check an item:

```text
/stackconfig info ender_pearl
```

Also make sure the item is listed under `items:` and that the number is valid.

---

## For developers

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

## Manual test checklist

Before marking a new Minecraft version as fully tested:

- Start a Paper server with the paper jar.
- Join with a vanilla client.
- Run `/stackconfig info ender_pearl`.
- Edit `ender_pearl: 64` and run `/stackconfig reload`.
- Verify ender pearls stack to 64.
- Edit `totem_of_undying: 16` and verify compatible totems stack.
- Verify unconfigured wood/planks remain vanilla.
- Add an invalid item name and confirm the server/game logs a warning but still starts.
- Test the Fabric jar in singleplayer.
- Test the Fabric jar on a Fabric multiplayer server.

---

## License

All rights reserved unless a separate license file says otherwise.
