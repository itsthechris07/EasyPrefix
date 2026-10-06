# Groups and tags

Groups and tags are stored in `plugins/EasyPrefix/groups.yml` (with MySQL in the database). Edit them in game with
`/ep setup`, or edit the file and run `/ep reload`.

## Groups

Every player has exactly one group. It decides the prefix, suffix, chat color and the join/quit messages.

```yaml
groups:
  Admin:
    priority: 90
    prefix: '<dark_red>Admin <gray>| <dark_red>'
    suffix: '<white>:'
    chat-color: gray
    chat-formatting: bold
    join-msg: '&8» %ep_user_prefix%%player% &7joined the game'
    quit-msg: '&8« %ep_user_prefix%%player% &7left the game'
    hover:
      - '&4&l%ep_user_display_name%'
      - '&7Rank: %ep_user_group_color%%ep_user_group%'
```

| Key | Description |
|---|---|
| `priority` | players with the permissions of several groups get the one with the highest priority; the tab list is sorted by it (higher first). Default: `0` for `default`, `1` for all others |
| `prefix` | text in front of the name |
| `suffix` | text after the name, usually the separator to the message (`:`) |
| `chat-color` | color of the messages, the name of a color in `config.yml` (`gray`, `rainbow`, ...) - `&7` works as well |
| `chat-formatting` | optional formatting of the messages (`bold`, `glow`, ...) |
| `join-msg`, `quit-msg` | join and quit message, see below |
| `hover` | lines shown when hovering over the name in the chat, see below |

A chat line looks like `<prefix><name><suffix> <message>`. Prefix and suffix support colors, MiniMessage and
placeholders, see [Colors and formatting](colors-and-formatting.md#text-formats).

### Assigning groups

- A player gets a group with the permission `EasyPrefix.group.<group>`.
- With several group permissions the highest `priority` wins. Players can switch to another of their groups in
  `/ep settings` → *My Prefixes*.
- `/ep user <name> setgroup <group>` sets a group without a permission.
- Everyone else is in the `default` group.

### The default group

The `default` group is the fallback for players without a group. **Don't remove it.** Groups without their own hover,
join message or quit message use the ones of the default group.

### Join and quit messages

Enabled with `join-quit-messages.enabled` in `config.yml`. `join-quit-messages.hide-messages: true` hides them
completely.

Placeholders: `%player%` (display name), `%ep_user_prefix%`, `%ep_user_suffix%`, `%ep_user_group%`, `%ep_tag_prefix%`,
`%ep_tag_suffix%`, `%ep_user_tag%` and all PlaceholderAPI placeholders.

### Chat hover

Hovering over the name of a player in the chat shows the `hover` of their group (turn it off with `chat.name-hover` in
`config.yml`). Clicking the name runs `chat.name-click` (default: writes `/msg <name> ` into the chat box).

The hover supports colors, MiniMessage, PlaceholderAPI (e.g. `%player_ping%`, `%vault_eco_balance%`) and these
placeholders, which only work in the hover:

| Placeholder | Value |
|---|---|
| `%ep_user_display_name%` | display name (e.g. a nickname) |
| `%ep_user_name%` | account name |
| `%ep_user_real_name%` | account name, only if the display name is different |
| `%ep_user_group%`, `%ep_user_group_prefix%`, `%ep_user_group_color%` | group, its prefix and its color |
| `%ep_user_tag%`, `%ep_user_tag_prefix%`, `%ep_user_tag_color%` | tag, its prefix and its color |
| `%ep_user_playtime%` | play time |
| `%ep_user_first_join%` | first join, formatted with `chat.date-format` |
| `%ep_user_world%` | current world |

A line is left out if one of these placeholders is empty - e.g. the tag line for players without a tag, or the
`real_name` line for players without a nickname.

The color of a group (`%ep_user_group_color%`) is the first color in its prefix.

## Tags

Tags (called `subgroups` in the file) are additional titles players can switch between with `/tags`, independent of
their group. Tags are enabled with `tags.enabled` in `config.yml`.

```yaml
subgroups:
  Vip:
    prefix: '&6VIP'
    suffix: ''
```

A tag does not show up by itself: put `%ep_tag_prefix%` or `%ep_tag_suffix%` into the prefix or suffix of a group, or
into the [tab list layout](tab-list-and-name-tags.md). Players without a tag get an empty text there.

```yaml
groups:
  Vip:
    prefix: '%ep_tag_prefix% &7| &e'
```

- A player can use a tag with `EasyPrefix.tag.<tag>`.
- `/tags` opens a menu with all tags of the player, `/tags list` lists them in the chat.
- Admins set tags with `/tags set <user> <tag>` or `/ep user <user> settag <tag>`.
- The `/tags` command is only registered when tags are enabled - after turning them on, restart the server.
