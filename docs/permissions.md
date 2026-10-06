# Permissions

Permissions are not case-sensitive (`EasyPrefix.group.Admin` = `easyprefix.group.admin`). EasyPrefix works with every
permission plugin, e.g. [LuckPerms](https://luckperms.net).

## Groups and tags

| Permission | Description |
|---|---|
| `EasyPrefix.group.<group>` | the player gets this group (e.g. `EasyPrefix.group.Admin`) |
| `EasyPrefix.tag.<tag>` | the player can select this tag (`EasyPrefix.subgroup.<tag>` works as well) |
| `EasyPrefix.tags.switch` | select a tag with `/tags select <tag>` |

- Players without any group permission get the `default` group, it has no permission.
- With the permissions of several groups the one with the highest `priority` is used. Players can switch between
  their groups in `/ep settings`.
- `/ep user <name> setgroup <group>` sets a group without a permission.

Example with LuckPerms - every player in the LuckPerms group `vip` gets the EasyPrefix group `Vip` and the tag `Vip`:

```
/lp group vip permission set EasyPrefix.group.Vip true
/lp group vip permission set EasyPrefix.tag.Vip true
```

## Colors and formattings

| Permission | Description |
|---|---|
| `EasyPrefix.color.<color>` | use the chat color, e.g. `EasyPrefix.color.red`, `EasyPrefix.color.rainbow` |
| `EasyPrefix.color.<formatting>` | use the formatting, e.g. `EasyPrefix.color.bold`, `EasyPrefix.color.glow` |

The names are the keys in `config.yml` (`chat.colors`, `chat.decorations`). Colors and formattings with
`default: true` can be used by everyone. `EasyPrefix.color.*` allows all of them, if your permission plugin supports
wildcards.

## Custom prefixes and suffixes

| Permission | Description |
|---|---|
| `EasyPrefix.custom.prefix` | set an own prefix (`/prefix`) |
| `EasyPrefix.custom.suffix` | set an own suffix (`/suffix`) |
| `EasyPrefix.custom.gui` | show the *My Layout* button in `/ep settings` |
| `EasyPrefix.custom.bypass` | no cooldown between changes |
| `EasyPrefix.custom.blacklist` | the blacklist does not apply |
| `EasyPrefix.custom.hex` | any hex color (`<#ff00aa>`), otherwise only the permitted colors |
| `EasyPrefix.custom.gradient` | `<gradient>`, `<transition>` and `<pride>` |
| `EasyPrefix.custom.shadow` | text shadows (`<shadow:...>`) |

Without `custom.hex`, `custom.gradient` and `custom.shadow`, players can only use the colors and formattings they have
the `EasyPrefix.color.<name>` permission for. See [Colors and formatting](colors-and-formatting.md#custom-prefixes-and-suffixes).

## General

| Permission | Description |
|---|---|
| `EasyPrefix.settings` | `/ep settings` and `/ep mentions` |
| `EasyPrefix.admin` | all admin commands, `/ep setup`, setting every color, update notifications |
