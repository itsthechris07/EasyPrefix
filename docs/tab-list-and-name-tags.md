# Tab list and name tags

EasyPrefix shows prefixes and suffixes outside the chat as well: in the tab list and above the heads of players. All
settings are in `config.yml` under `display`.

```yaml
display:
  tab-list: true
  sort-tab-list: true
  name-tags: true
  tab-list-layout: "{prefix}{name}"
  name-tag-prefix: "{prefix}"
  name-tag-suffix: ""
  update-interval: 5
  excluded-worlds: [
    "ctb_*"
  ]
```

## Layouts

| Option | Used for |
|---|---|
| `tab-list-layout` | the whole entry in the tab list |
| `name-tag-prefix` | text in front of the name above the head |
| `name-tag-suffix` | text after the name above the head (the name itself can't be changed) |

Variables: `{prefix}`, `{name}` and `{suffix}` of the player. Colors, MiniMessage and placeholders work as well:

```yaml
tab-list-layout: "%ep_tag_prefix% {prefix}{name} <gray>%player_ping%ms"
```

The suffix is left out by default because it usually ends with the separator of the chat (`:`).

## Sorting

With `sort-tab-list: true` the tab list is sorted by the `priority` of the groups, the highest first. Players of the
same group are sorted by name, numbers by their value (`Player2` before `Player10`).

## Updates

The tab list and name tags are updated when a player's group, tag, prefix or suffix changes. Placeholders of other
plugins (e.g. the ping) can change anytime, so everything is refreshed every `update-interval` seconds as well. `0`
only updates on changes.

## Excluded worlds

In the worlds of `excluded-worlds` EasyPrefix does not touch the tab list or name tags, e.g. for minigames that color
the tab list themselves. Names are case-insensitive, `*` is a wildcard (`ctb_*` matches `ctb_arena1`).

When a player enters such a world, EasyPrefix only resets what it set itself - names set by other plugins are kept.
When the player leaves the world again, the prefix comes back right away.

## Other plugins

A disabled feature is not touched at all, so other plugins can manage it:

- **TAB, other tab list plugins**: set `tab-list: false` and `name-tags: false`, then use the
  [placeholders](placeholders.md) of EasyPrefix in their layouts (`%ep_user_prefix%`).
- **Plugins with own scoreboard teams**: name tags use teams called `ep_<name>` on the main scoreboard. If another
  plugin manages name tags, set `name-tags: false`.

On **Folia** name tags are not available (Folia has no scoreboards), the tab list works.
