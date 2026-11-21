# MySQL and multiple servers

By default EasyPrefix stores groups and tags in `groups.yml` and player data in `storage.db` (SQLite) - nothing to set
up. With MySQL several servers (e.g. behind Velocity or BungeeCord) share the same groups, tags, players and
settings, and changes show up on all servers within about a second.

EasyPrefix uses the MySQL driver that comes with Paper, no extra plugin is needed.

## Setup

1. Create a database and a user for EasyPrefix (not root, see [A user of its own](#a-mysql-user-of-its-own)).
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

## Keep the password out of config.yml

`config.yml` is often shared - in a support chat, with an AI assistant or in git. Any value in it can reference an
environment variable as `${NAME}`, which EasyPrefix replaces when it reads the config:

```yaml
sql:
  enabled: true
  host: 'db.example.com'
  username: 'easyprefix'
  password: "${EP_MYSQL_PASSWORD}"
```

The file keeps `${EP_MYSQL_PASSWORD}`, also when EasyPrefix saves it. Values without `${...}` work as before. The
other connection values can come from variables as well (`host: "${EP_MYSQL_HOST}"`, `port: "${EP_MYSQL_PORT}"`). If
a variable isn't set, the console warns once with its name and the value is empty. Set the variable where the server
is started:

**Start script (Linux)** - `start.sh`:

```bash
export EP_MYSQL_PASSWORD='your password'
java -Xmx4G -jar paper.jar --nogui
```

**Start script (Windows)** - `start.bat`:

```bat
set "EP_MYSQL_PASSWORD=your password"
java -Xmx4G -jar paper.jar --nogui
```

**Docker** - `docker run -e EP_MYSQL_PASSWORD='your password' ...`, or in `docker-compose.yml`:

```yaml
services:
  minecraft:
    environment:
      EP_MYSQL_PASSWORD: "your password"
```

**Pterodactyl / Pelican** - the panel only passes the variables of the egg to the server. Add a variable
`EP_MYSQL_PASSWORD` to the egg (admin area: *Nests* → egg → *Variables*; untick "Users Can View" if they shouldn't
see it), then set its value on the server's *Startup* page.

A start script still holds the password, but it is rarely shared - keep it out of git (or put the `export` into a file
next to it that is not committed).

### A MySQL user of its own

Don't let EasyPrefix log in as `root`. Create a user that may only use its database, so a leaked password can't harm
anything else:

```sql
CREATE DATABASE EasyPrefix;
CREATE USER 'easyprefix'@'%' IDENTIFIED BY 'a long random password';
GRANT SELECT, INSERT, UPDATE, DELETE, CREATE, ALTER, INDEX, REFERENCES ON EasyPrefix.* TO 'easyprefix'@'%';
```

Replace `%` with the IP of the Minecraft server if it always connects from the same address.

## Moving existing data

The first server only starts with the default groups in MySQL. To take over the data you already have:

| Command | What it does |
|---|---|
| `/ep database upload` | copies `groups.yml` and `storage.db` of this server into MySQL |
| `/ep database migrate` | copies the data from MySQL back into `groups.yml` and `storage.db` (your local files are backed up first) |

Both commands need `sql.enabled: true`. To go back to local storage, run `/ep database migrate`, set
`sql.enabled: false` and restart.

## When MySQL is not reachable

If the database can't be reached on start or `/ep reload`, EasyPrefix keeps running on the local storage of this
server (`groups.yml` and `storage.db`) and warns in the console; `/ep debug` shows `Storage: local (MySQL not reachable)`.
Run `/ep reload` or restart once MySQL is back to switch to it again.

- The local storage is what was there before - run `/ep database migrate` from time to time, so it holds a current
  copy of your groups and players.
- Changes made meanwhile are only saved on this server and are **not** copied to MySQL later.
- `sql.fallback-to-local: false` disables the plugin instead (the behaviour of older versions).

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
  IP. The error is in the console on start. With `${NAME}` in `config.yml`, look for a warning that the environment
  variable isn't set (it is only passed to the server if it is set where the server is started).
- **Changes don't show up on other servers**: all servers need the same `table-prefix`. A server that could not
  reach the database for some minutes reloads everything once it is back.
