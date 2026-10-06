# Colors and formatting

## Text formats

Prefixes, suffixes, tags, hovers and join/quit messages understand both formats, also mixed:

| Format | Example |
|---|---|
| Legacy color codes | `&4Admin &7» &c` |
| [MiniMessage](https://docs.papermc.io/adventure/minimessage/format/) | `<dark_red>Admin <gray>» <red>` |
| Hex colors | `<#ff5555>` |
| Gradients and effects | `<gradient:#ff5555:#ffaa00>Admin</gradient>`, `<rainbow>VIP</rainbow>`, `<shadow:gold:0.6>Glow</shadow>` |

Legacy codes are converted to MiniMessage when a group is loaded, so the file shows MiniMessage after the first start.
The `%` placeholders of EasyPrefix and PlaceholderAPI work in all of them, see [Placeholders](placeholders.md).

## Chat colors

The chat color is the color of a player's messages. Every group has a default one (`chat-color` in `groups.yml`),
players can choose another one with `/color` or `/ep settings`. `/color reset` goes back to the group's color.

The available colors are defined in `config.yml` under `chat.colors`:

```yaml
chat:
  colors:
    red:
      display-name: "Red"
      hex: "#ff5555"
      code: "c"
      default: true
```

| Key | Description |
|---|---|
| *(name)* | the key, used in commands and permissions (`/color set red`, `EasyPrefix.color.red`) |
| `display-name` | name shown in menus and messages |
| `hex` | the color |
| `code` | optional legacy code: `&c` in texts is turned into this color |
| `default` | `true`: every player can use it without a permission |
| `tag` | an effect instead of a color, see below |

You can add as many colors as you like and remove the ones you don't need. This section is never touched by
automatic config updates. Keep the 16 Minecraft colors with their `code` if you use `&` codes anywhere - a removed
color's code is no longer converted.

### Effects

Instead of `hex`, a color can be a single MiniMessage tag that wraps the whole message - gradients, rainbow,
transitions, pride flags:

```yaml
    sunset:
      display-name: "Sunset"
      tag: "<gradient:#ff5555:#ffaa00>"
    rainbow:
      display-name: "Rainbow"
      tag: "<rainbow>"
```

Players only get this exact effect (`EasyPrefix.color.sunset`), not gradients in general. `hex` is optional for
effects, it only colors the icon in the menu (default: the first color of the tag).

### Color icons

`chat.color-icon` sets the item shown for each color in `/color`:

| Value | Item |
|---|---|
| `wool`, `glass`, `dye` | the closest of the 16 Minecraft colors |
| `leather`, `firework_star` | the exact hex color |
| `book` | no color |

It can also be changed in `/ep setup` → *Settings* → *Color Icons*.

## Formattings

Formattings are added on top of the chat color, e.g. bold or a shadow. They are defined under `chat.decorations`:

```yaml
chat:
  decorations:
    bold:
      display-name: "Bold"
    glow:
      display-name: "Glow"
      tag: "<shadow:gold:0.6>"
```

The built-in names are `bold`, `underlined`, `italic`, `strikethrough` and `obfuscated`. Everything else needs a `tag`,
e.g. `<shadow:color:opacity>` (opacity 0-1) or `<!shadow>` for no shadow. Permissions and `default` work like for
colors: `EasyPrefix.color.bold`.

## Custom prefixes and suffixes

Players with `EasyPrefix.custom.prefix` / `EasyPrefix.custom.suffix` can type in their own prefix or suffix with
`/prefix` and `/suffix` (or *My Layout* in `/ep settings`, with `EasyPrefix.custom.gui`). It replaces the prefix or
suffix of their group.

Settings in `config.yml` under `user.custom-layout`:

```yaml
user:
  custom-layout:
    enabled: true
    cooldown: 0.5       # hours between changes, bypass: EasyPrefix.custom.bypass
    blacklist:          # bypass: EasyPrefix.custom.blacklist
      - "Admin"
      - "Owner"
      - "&4"
    alias:
      prefix: /prefix   # empty = no command
      suffix: /suffix
```

What players can write:

- colors and formattings they have the permission for, as `<red>`, `&c` or `<bold>` - a permitted color also as hex
  (`<#ff5555>`)
- any hex color with `EasyPrefix.custom.hex`
- `<gradient>`, `<transition>`, `<pride>` with `EasyPrefix.custom.gradient`
- shadows with `EasyPrefix.custom.shadow`

All other tags (click events, hovers, fonts, ...) and placeholders are shown as plain text, so players can't abuse
them. The blacklist is checked against the typed text, the visible text and the colors - a blocked `&4` also blocks
`<dark_red>` and `<#aa0000>`.

Admins can set a custom prefix for any player with `/ep user <name> setprefix <prefix>`.
