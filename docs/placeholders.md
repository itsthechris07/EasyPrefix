# Placeholders and integrations

## PlaceholderAPI

With [PlaceholderAPI](https://modrinth.com/plugin/placeholderapi) installed, EasyPrefix registers its placeholders
automatically (no `/papi ecloud download` needed). They can be used in every plugin that supports PlaceholderAPI,
e.g. scoreboards, tab list plugins or other chat plugins.

| Placeholder | Value |
|---|---|
| `%ep_user_prefix%` (or `%ep_prefix%`) | prefix of the player (custom prefix or the one of the group) |
| `%ep_user_suffix%` (or `%ep_suffix%`) | suffix of the player |
| `%ep_user_group%` | name of the group |
| `%ep_user_tag%` (or `%ep_user_subgroup%`) | name of the tag, empty without a tag |
| `%ep_tag_prefix%` (or `%ep_user_subgroup_prefix%`) | prefix of the tag |
| `%ep_tag_suffix%` (or `%ep_user_subgroup_suffix%`) | suffix of the tag |
| `%ep_user_color%` | name of the chat color (`red`), empty if the group's color is used |
| `%ep_user_chatcolor%` | chat color as MiniMessage tag (`<red>`), empty if the group's color is used |
| `%ep_user_formatting%` | name of the chat formatting (`bold`), empty if there is none |

Prefixes and suffixes are returned with legacy colors (`§`) and all placeholders inside them already replaced, so
every plugin can show them. Hex colors work in all plugins that support the Bukkit hex format (`§x§r§r§g§g§b§b`).

The other way around, placeholders of other plugins can be used in prefixes, suffixes, tags, hovers, join/quit
messages and the tab list layout, e.g. `%luckperms_prefix%` or `%vault_eco_balance%`.

### Without PlaceholderAPI

These placeholders also work without PlaceholderAPI in prefixes, suffixes, join/quit messages and layouts:
`%player%`, `%ep_user_prefix%`, `%ep_user_suffix%`, `%ep_user_group%`, `%ep_user_tag%`, `%ep_tag_prefix%`,
`%ep_tag_suffix%`.

The chat hover has more placeholders of its own, see [Groups and tags](groups-and-tags.md#chat-hover).

## Vault

With [Vault](https://www.spigotmc.org/resources/vault.34315/) (or its fork VaultUnlocked) installed, EasyPrefix
registers itself as the chat provider. Plugins that read prefixes through Vault (EssentialsChat, scoreboards, ...) get
the prefixes and suffixes of EasyPrefix.

- `getPlayerPrefix` / `getPlayerSuffix` return the prefix of the player, `getGroupPrefix` the one of an EasyPrefix
  group.
- `setPlayerPrefix` sets a custom prefix - it is only shown if the player has `EasyPrefix.custom.prefix`.
- Group membership comes from your permission plugin if there is one, otherwise from EasyPrefix.
- EasyPrefix has no per-world prefixes, the world is ignored.

## Other chat plugins

If another plugin formats the chat (EssentialsChat, VentureChat, ChatControl, ...), set `chat.handle-chat: false` in
`config.yml`. EasyPrefix then leaves the chat alone, and the other plugin shows the prefixes through Vault or the
placeholders above. EssentialsChat example:

```yaml
chat:
  format: '{PREFIX}{DISPLAYNAME}{SUFFIX} {MESSAGE}'
```

Chat colors, the name hover and mentions are part of the chat formatting of EasyPrefix and are not available then.

## Other tab list plugins

Plugins like TAB manage the tab list and name tags themselves. Set `display.tab-list: false` and
`display.name-tags: false` and use `%ep_user_prefix%` / `%ep_user_suffix%` in their config. See
[Tab list and name tags](tab-list-and-name-tags.md#other-plugins).

## Nickname plugins

The chat, tab list and hover use the display name of the player, so nicknames (e.g. of Essentials) are shown. The
hover can show the real name with `%ep_user_real_name%`.
