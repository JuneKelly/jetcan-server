# jetcan-server

![jetcan-logo](resources/images/jetcan_transparent_200x200.png)

A snippet-saving app in Clojure, PostgreSQL and AngularJS.


# Dependencies

To run jetcan-server, you will need to have a PostgreSQL 9.3 instance available.
Create the `jetcan` database like so:
```
CREATE DATABASE jetcan
  WITH OWNER = {{USERNAME}}
       ENCODING = 'UTF8'
       TABLESPACE = pg_default
       LC_COLLATE = 'C'
       LC_CTYPE = 'C'
       CONNECTION LIMIT = -1
       TEMPLATE template0;
```



System Dependencies:

- ensure you have nodejs and npm installed
- ensure you have ruby and the ```sass``` rubygem installed:
  ```gem install sass```
- use npm to install grunt: ```npm install -g grunt-cli``` (may require sudo)


# Environments

jetcan uses leiningen profiles and the lein-environ plugin to manage
different run-time environments. The three important environments
are `dev`, `testing` and `production`. Variables which differ by environment
can either be set in the appropriate section of project.clj, or set as shell
environment variables before starting the server:
- `DB_URI` : the uri of the postgres database, ex: `//localhost/my_jetcan`
- `DB_USER`: username to use to connect to the database
- `DB_PASSWORD`: password to use to connect to the database
- `SECRET`: a string to use as the secret when generating secure web tokens,
  in production this should be a unique, random string and kept secret.


# Getting started

To start the jetcan server:

- run ```npm install``` to install the required node packages
- run ```bower install``` to install front-end libraries
- Either edit the project.clj file and fill in the environment settings for
  the 'dev' environment, or export the appropriate values for
  `DB_USER`, `DB_PASSWORD` and `DB_URI` in your shell.
- run `lein with-profile dev migratus migrate` to set up
  the `jetcan` database tables.
- run ```grunt server``` to fire up the development server,
  with coffeescript compilation and live-reloading.


# Migrations

This project uses the [migratus](https://github.com/pjstadig/migratus)
library to handle database migrations. To create a new set of migrations,
use the `migrate:new` grunt task:
```
$ grunt migrate:new:add-some-table
```
This will create timestamped files in `resources/sql/migrations`,
ending in `add-some-table.up.sql` and `add-some-table.down.sql`

To run migrations for the local dev environment, do the following:
```
$ lein with-profile dev migratus migrate
```


# Testing

First, set up a `jetcan_test` database,
and run `lein with-profile testing migratus migrate` to set up the tables.

Then, to run the backend tests: `grunt test:backend`

