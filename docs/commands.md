# Commands

`<argument>` is required, `[argument]` optional. Arguments are tab-completed. Prefixes and suffixes with spaces can be
put in quotes (`"&4Admin | "`). `/ep` is short for `/easyprefix`.

Every command listed here shows up in `/ep help` - the help only lists the commands you have the permission for.

## Players

| Command | Description | Permission |
|---|---|---|
| `/ep` | plugin version | - |
| `/ep help [query]` | command overview | - |
| `/ep settings` | settings menu: group, tag, chat color, own prefix/suffix, mentions | `EasyPrefix.settings` |
| `/ep mentions` | turn pings by `@mentions` of your name on or off | `EasyPrefix.settings` |
| `/color` | menu to pick your chat color and formatting | - |
| `/color set <color>` | set your chat color (alias `/setcolor <color>`) | `EasyPrefix.color.<color>` |
| `/color format <formatting>` | set your chat formatting, e.g. `bold` | `EasyPrefix.color.<formatting>` |
| `/color format none` | remove your chat formatting | - |
| `/color reset` | back to the color and formatting of your group | - |
| `/color show` | preview of your chat messages | - |
| `/tags` | menu to pick a tag | - |
| `/tags list` | list your tags, click one to select it | - |
| `/tags select <tag>` | select a tag | `EasyPrefix.tags.switch` and `EasyPrefix.tag.<tag>` |
| `/prefix` | type in your own prefix | `EasyPrefix.custom.prefix` |
| `/suffix` | type in your own suffix | `EasyPrefix.custom.suffix` |

`/tags` only exists if tags are enabled (`tags.enabled` in `config.yml`). The names of `/prefix` and `/suffix` are set
in `config.yml` (`user.custom-layout.alias`), an empty value removes the command. Colors and formattings with
`default: true` don't need a permission.

## Admins

All admin commands need `EasyPrefix.admin`.

### Setup

| Command | Description |
|---|---|
| `/ep setup` | setup menu: groups, tags, plugin settings, color icons, blacklist |
| `/ep reload` | reloads `config.yml`, `groups.yml` and `messages.yml` (a restart is safer) |
| `/ep debug` | version, server, storage and other information for bug reports |
| `/ep debug stop` | disables EasyPrefix until the next restart |

### Groups

| Command | Description |
|---|---|
| `/ep group <group> info` | prefix, suffix, color and settings of a group |
| `/ep group <group> setprefix <prefix>` | set the prefix of a group |
| `/ep group <group> setsuffix <suffix>` | set the suffix of a group |
| `/ep group <group> setpriority <priority>` | set the priority (the permitted group with the highest one wins, also the tab list order) |

Groups and tags are created and deleted in `/ep setup` or in `groups.yml`.

### Players

`<user>` can also be a player who is offline.

| Command | Description |
|---|---|
| `/ep user <user> info` | group, tag, colors and custom prefix of a player |
| `/ep user <user> setgroup <group>` | set the group of a player, also without the group permission |
| `/ep user <user> setsubgroup <tag>` | set the tag of a player |
| `/ep user <user> settag <tag>` | set the tag of a player |
| `/ep user <user> setprefix <prefix>` | set a custom prefix for a player |
| `/ep user <user> setsuffix <suffix>` | set a custom suffix for a player |
| `/ep user <user> setcolor <color>` | set the chat color |
| `/ep user <user> setformat <formatting>` | set the chat formatting |
| `/ep user <user> setformat none` | remove the chat formatting |
| `/ep user <user> resetcolor` | back to the color and formatting of the group |
| `/color <player> set <color>` | set the chat color of an online player |
| `/color <player> format <formatting>` | set the chat formatting of an online player |
| `/color <player> format none` | remove the chat formatting of an online player |
| `/color <player> reset` | back to the color and formatting of the group |
| `/color <player> show` | preview of the chat messages of a player |
| `/tags set <user> <tag>` | set the tag of a player |
| `/tags clear <user>` | remove the tag of a player |

Admins can set every color and formatting, also the ones the player has no permission for.

### Database

Both commands need `sql.enabled: true`, see [MySQL and multiple servers](mysql.md).

| Command | Description |
|---|---|
| `/ep database upload` | copies groups, tags and players of the local storage (`groups.yml`, `storage.db`) to MySQL |
| `/ep database migrate` | copies groups, tags and players from MySQL into the local storage |
