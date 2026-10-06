# Configuration

All options of `plugins/EasyPrefix/config.yml`. Most of them can also be changed in game with `/ep setup` →
*Settings*. After editing the file, run `/ep reload` - a restart is needed for `sql.*`, the command aliases and
turning tags on or off.

New options are added to your file automatically on start, your values and comments stay.

## General

| Option | Default | Description |
|---|---|---|
| `enabled` | `true` | `false` disables the plugin |
| `text-input` | `dialog` | how players type in texts (custom prefix, setup): `dialog` opens a window with a text field, the current value and a preview; `chat` asks in the chat (`quit` cancels) - for clients that can't show dialogs, e.g. older versions via ViaBackwards |

## MySQL - `sql`

| Option | Default | Description |
|---|---|---|
| `sql.enabled` | `false` | store everything in MySQL instead of `groups.yml` and `storage.db` |
| `sql.host`, `sql.port`, `sql.database` | `localhost`, `3306`, `EasyPrefix` | connection |
| `sql.username`, `sql.password` | | login |
| `sql.table-prefix` | `ep` | prefix of the table names, e.g. for several networks in one database |

See [MySQL and multiple servers](mysql.md).

## Join and quit messages - `join-quit-messages`

| Option | Default | Description |
|---|---|---|
| `join-quit-messages.enabled` | `true` | use the join/quit messages of the groups (`groups.yml`) |
| `join-quit-messages.hide-messages` | `false` | hide join and quit messages completely |

## Custom prefixes - `user.custom-layout`

| Option | Default | Description |
|---|---|---|
| `enabled` | `true` | allow players to create their own prefix and suffix |
| `cooldown` | `0.5` | hours between two changes (bypass: `EasyPrefix.custom.bypass`) |
| `blacklist` | `Admin`, `Owner`, `&4` | texts that are not allowed (bypass: `EasyPrefix.custom.blacklist`) |
| `alias.prefix`, `alias.suffix` | `/prefix`, `/suffix` | command names, empty = no command |

See [Custom prefixes and suffixes](colors-and-formatting.md#custom-prefixes-and-suffixes).

## Tags - `tags`

| Option | Default | Description |
|---|---|---|
| `tags.enabled` | `true` | players can select tags with `/tags` (requires a restart) |

## Tab list and name tags - `display`

| Option | Default | Description |
|---|---|---|
| `tab-list` | `true` | prefix and suffix in the tab list |
| `sort-tab-list` | `true` | sort the tab list by group priority (higher first) |
| `name-tags` | `true` | prefix and suffix above the heads of players |
| `tab-list-layout` | `{prefix}{name}` | layout of the tab list entry |
| `name-tag-prefix` | `{prefix}` | text in front of the name above the head |
| `name-tag-suffix` | `""` | text after the name above the head |
| `update-interval` | `5` | seconds between updates (for placeholders of other plugins), `0` = only on changes |
| `excluded-worlds` | `["ctb_*"]` | worlds in which EasyPrefix leaves the tab list and name tags alone, `*` as wildcard |

See [Tab list and name tags](tab-list-and-name-tags.md).

## Chat - `chat`

| Option | Default | Description |
|---|---|---|
| `handle-chat` | `true` | EasyPrefix formats the chat. Set it to `false` if another plugin formats it, see [integrations](placeholders.md#other-chat-plugins) |
| `name-hover` | `true` | hovering over a name shows the `hover` of the group |
| `name-click` | `"/msg %player% "` | clicking a name writes this into the chat box, empty = nothing |
| `date-format` | `dd.MM.yyyy` | format of `%ep_user_first_join%` ([pattern](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/time/format/DateTimeFormatter.html#patterns)) |
| `color-icon` | `wool` | item per color in `/color`: `wool`, `glass`, `dye`, `leather`, `firework_star`, `book` |
| `colors` | | the chat colors, see [Colors and formatting](colors-and-formatting.md#chat-colors) |
| `decorations` | | the formattings, see [Colors and formatting](colors-and-formatting.md#formattings) |

### Mentions - `chat.mentions`

Players can ping each other with `@name` in the chat. Everyone can turn off being pinged in `/ep settings` or with
`/ep mentions`.

| Option | Default | Description |
|---|---|---|
| `enabled` | `true` | turn mentions on or off |
| `format` | `<aqua>@{name}</aqua>` | how a mention looks for everyone else |
| `highlight` | `<yellow><bold>@{name}</bold></yellow>` | how it looks for the mentioned player |
| `line-prefix` | `<gold><bold>»</bold></gold> ` | shown in front of the line for the mentioned player, `""` = nothing |
| `sound` | `entity.experience_orb.pickup` | sound for the mentioned player ([list](https://minecraft.wiki/w/Sounds.json)), `""` = no sound |
| `volume`, `pitch` | `1.0`, `1.0` | volume and pitch of the sound |

## messages.yml

All texts of the plugin are in `messages.yml` - translate or change them there. `%newline%` breaks a line, `%prefix%`
inserts `prefix_alt`. Keep a space after every `key:`, a single invalid line stops the whole file from loading.
