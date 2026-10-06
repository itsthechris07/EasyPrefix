# FAQ and troubleshooting

### A player doesn't get their group

- Check the permission: `EasyPrefix.group.<group>` with the exact group name. `/ep user <name> info` shows the group
  EasyPrefix uses.
- With several group permissions the highest `priority` wins - check the priorities in `groups.yml`.
- The group is checked when the player joins. After changing permissions, the player has to rejoin (or run
  `/ep reload`).
- A player can choose one of their groups in `/ep settings` - that choice is kept as long as they have its
  permission.

### The prefix doesn't show up in the chat

- Is another chat plugin installed (EssentialsChat, VentureChat, ...)? Either remove its chat format or set
  `chat.handle-chat: false` and use EasyPrefix's prefixes in that plugin, see
  [Other chat plugins](placeholders.md#other-chat-plugins).
- `/color show` shows how the chat of a player should look.

### The tab list or name tags flicker or are overwritten

Another plugin (TAB, a scoreboard or minigame plugin) manages them as well. Turn the feature off in EasyPrefix
(`display.tab-list`, `display.name-tags`) or exclude the worlds of the minigame with `display.excluded-worlds`. See
[Tab list and name tags](tab-list-and-name-tags.md).

### A placeholder is shown as text

- Placeholders of other plugins need [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi) and the expansion
  of that plugin (`/papi ecloud download <name>`).
- Players can't use placeholders in their custom prefix on purpose.

### Players can't see the text input window

Dialogs need a 1.21.6+ client. If players join with older versions (ViaBackwards), set `text-input: chat` in
`config.yml` - they type the text into the chat then.

### Colors in other plugins look wrong

EasyPrefix gives other plugins legacy colors (`§c`, hex as `§x§r§r§g§g§b§b`). Plugins that don't support hex colors
show the closest color or none - use the 16 Minecraft colors in prefixes that other plugins display.

### `/tags` doesn't exist

Tags are turned off (`tags.enabled: false`) or were turned on without a restart. The command is registered on start.

### My changes to `config.yml` are gone (MySQL)

With MySQL, some settings are shared and the database wins on start. Change them in `/ep setup` or run `/ep reload`
after editing the file - both upload them. See [What is shared](mysql.md#what-is-shared).

### Does EasyPrefix work on Spigot or older versions?

No. This version needs Paper or Folia 1.21.7 or newer (it uses the Paper chat and dialog api). Older versions of
EasyPrefix for Spigot are still on [SpigotMC](https://www.spigotmc.org/resources/easyprefix-prefix-chat-color-tab-list-gui.44580/history),
but they are not supported anymore.

### Does EasyPrefix work on Folia?

Yes, the same jar runs on Paper and Folia. Name tags are not available on Folia because it has no scoreboards.

## Reporting a bug

Open an [issue on GitHub](https://github.com/itsthechris07/EasyPrefix/issues) using the template and include:

- the output of `/ep debug`
- the errors in the console (`logs/latest.log`)
- what you did, what you expected and what happened
