# MySQL and multiple servers

By default EasyPrefix stores groups and tags in `groups.yml` and player data in `storage.db` (SQLite) - nothing to set
up. With MySQL several servers (e.g. behind Velocity or BungeeCord) share the same groups, tags, players and
settings, and changes show up on all servers within about a second.

EasyPrefix uses the MySQL driver that comes with Paper, no extra plugin is needed.

## Setup

1. Create a database and a user for EasyPrefix.
2. Enter the connection in `config.yml` on **every** server:
   ```yaml
   sql:
     enabled: true
     host: 'localhost'
     port: 3306
     database: 'EasyPrefix'
     username: 'easyprefix'
     password: 'secret'
     table-prefix: 'ep'
   ```
3. Restart the servers. EasyPrefix creates its tables (`ep_groups`, `ep_users`, ...) and keeps them up to date.

Servers with the same database **and** the same `table-prefix` share their data. Use different prefixes to put several
networks into one database.

## Moving existing data

The first server only starts with the default groups in MySQL. To take over the data you already have:

| Command | What it does |
|---|---|
| `/ep database upload` | copies `groups.yml` and `storage.db` of this server into MySQL |
| `/ep database migrate` | copies the data from MySQL back into `groups.yml` and `storage.db` (your local files are backed up first) |

Both commands need `sql.enabled: true`. To go back to local storage, run `/ep database migrate`, set
`sql.enabled: false` and restart.

## What is shared

Groups, tags and all player data (group, tag, colors, custom prefixes, mention setting) are stored in MySQL.

These settings of `config.yml` are shared as well - the database wins on start, changes in `/ep setup` and
`/ep reload` are uploaded to the other servers:

- `chat.colors`, `chat.decorations`, `chat.color-icon`
- `chat.name-hover`, `chat.name-click`, `chat.date-format`, `chat.mentions`
- `user.custom-layout.enabled`, `.cooldown`, `.blacklist`
- `display.tab-list-layout`, `display.name-tag-prefix`, `display.name-tag-suffix`

Everything else stays per server: which features are on (`handle-chat`, `tab-list`, `name-tags`, `tags`,
join/quit messages), the command aliases, `excluded-worlds`, `text-input` and the `sql` connection itself. So one
server can show prefixes in the tab list while another leaves it to a minigame plugin.

`messages.yml` is not shared - copy it to every server if you change it.

## Troubleshooting

- **The server can't connect**: check host, port and login, and that the MySQL user may connect from the server's
  IP. The error is in the console on start.
- **Changes don't show up on other servers**: all servers need the same `table-prefix`. A server that could not
  reach the database for some minutes reloads everything once it is back.
