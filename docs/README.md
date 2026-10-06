# EasyPrefix documentation

EasyPrefix is a chat formatting plugin for Paper and Folia 1.21.7 - 26.x. This documentation covers setup,
configuration and all features. Back to the [project page](../README.md).

## For server owners

1. [Getting started](getting-started.md) - installation, the first group, updating
2. [Commands](commands.md) - every command with its permission
3. [Permissions](permissions.md) - groups, tags, colors, custom prefixes
4. [Groups and tags](groups-and-tags.md) - `groups.yml`: prefix, suffix, priority, hover, join/quit messages
5. [Colors and formatting](colors-and-formatting.md) - chat colors, effects, text formats, custom prefixes
6. [Configuration](configuration.md) - every option of `config.yml`
7. [Tab list and name tags](tab-list-and-name-tags.md) - layouts, sorting, excluded worlds
8. [Placeholders and integrations](placeholders.md) - PlaceholderAPI, Vault, other chat plugins
9. [MySQL and multiple servers](mysql.md) - sharing groups, users and settings
10. [FAQ and troubleshooting](faq.md)

## For developers

- [Development](development.md) - building, testing and the code layout

## Files

All files are in `plugins/EasyPrefix/`:

| File | Content |
|---|---|
| `config.yml` | Features, chat, colors, tab list, MySQL - see [Configuration](configuration.md) |
| `groups.yml` | Groups and tags - see [Groups and tags](groups-and-tags.md) (not used with MySQL) |
| `messages.yml` | All texts of the plugin, translate them here (`%newline%` breaks a line) |
| `storage.db` | Player data (SQLite), not used with MySQL |

Missing options are added to `config.yml` and `messages.yml` automatically when the plugin starts, your values stay
untouched.
