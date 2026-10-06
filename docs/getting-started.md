# Getting started

## Installation

1. Make sure your server runs **Paper or Folia 1.21.7 - 26.x**.
2. Download EasyPrefix from [Modrinth](https://modrinth.com/plugin/easyprefix-christian34),
   [Hangar](https://hangar.papermc.io/Christian34/EasyPrefixGUI),
   [SpigotMC](https://www.spigotmc.org/resources/easyprefix-prefix-chat-color-tab-list-gui.44580/) or the
   [GitHub releases](https://github.com/itsthechris07/EasyPrefix/releases).
3. Put the jar into `plugins/` and start the server.

EasyPrefix creates `plugins/EasyPrefix/` with `config.yml`, `groups.yml` and `messages.yml` and comes with a few
example groups (Owner, Admin, Developer, Moderator, Supporter, Builder, Premium, Vip) and a tag (Vip).

Optional plugins:

- [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi) - use the placeholders of EasyPrefix in other plugins
  and placeholders of other plugins in prefixes, hovers and join messages
- [Vault](https://www.spigotmc.org/resources/vault.34315/) or VaultUnlocked - other plugins can read the prefixes of
  EasyPrefix
- a permission plugin like [LuckPerms](https://luckperms.net) - EasyPrefix assigns groups by permissions

## Your first group

Groups are assigned by permission: a player with `EasyPrefix.group.<group>` gets that group. If a player has the
permission of several groups, the one with the highest `priority` wins. Players without any group permission get the
`default` group.

1. Create the group - either in game with `/ep setup` → *Groups* → *Create*, or in `groups.yml`:
   ```yaml
   groups:
     Admin:
       priority: 90
       prefix: '<dark_red>Admin <gray>| <dark_red>'
       suffix: '<white>:'
       chat-color: gray
   ```
   After editing the file, run `/ep reload` (or restart the server).
2. Give the permission to your admins, e.g. with LuckPerms:
   ```
   /lp group admin permission set EasyPrefix.group.Admin true
   ```
3. Write something in the chat - it shows `Admin | Name: message`. Check it with `/color show` or
   `/ep user <name> info`.

You can also set a group directly without a permission plugin: `/ep user <name> setgroup <group>`.

## What players can do

| Command | What it does | Permission |
|---|---|---|
| `/color` | pick a chat color and formatting | `EasyPrefix.color.<color>` per color |
| `/tags` | pick a tag | `EasyPrefix.tag.<tag>` per tag |
| `/ep settings` | settings menu: colors, tags, own prefix/suffix, mentions | `EasyPrefix.settings` |
| `/prefix`, `/suffix` | type in an own prefix or suffix | `EasyPrefix.custom.prefix` / `.suffix` |

Colors with `default: true` in `config.yml` can be used by everyone. All permissions: [Permissions](permissions.md).

## The setup GUI

`/ep setup` (permission `EasyPrefix.admin`) edits everything in game:

- **Groups** - create, delete, prefix, suffix, chat color, formatting, priority, hover, join and quit messages, with a
  rendered preview of the chat line
- **Tags** - create, delete, prefix and suffix
- **Settings** - turn features on and off, the color icon, the blacklist for custom prefixes

Texts are entered in a dialog window with the current value and a preview. If your players use clients that can't
show dialogs (e.g. older versions via ViaBackwards), set `text-input: chat` in `config.yml`.

## Updating

Replace the jar in `plugins/` and restart. New options are added to `config.yml` and `messages.yml` automatically,
the database is migrated on start. EasyPrefix tells admins on join when a new version is available.

`/reload` does **not** reload plugins on Paper 26. `/ep reload` reloads the files of EasyPrefix, but after an update a
restart is required.

## Next steps

- Design your groups: [Groups and tags](groups-and-tags.md)
- Add gradients and effects: [Colors and formatting](colors-and-formatting.md)
- Already use a chat plugin like EssentialsChat? See [Placeholders and integrations](placeholders.md)
