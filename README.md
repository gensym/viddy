# Viddy

An application that fetches, analyzes, and displays Divvy usage data.

## Usage

### Prerequsites

* [Leinengen 2](https://github.com/technomancy/leiningen)
* [Posgresql 9.3](http://www.postgresql.org) (requires 9.3 because Viddy makes use of materialized views)

### Creating the database

This app depends on a Posgresql instance.The schema is defined at
https://github.com/gensym/viddy/blob/master/resources/ddl/viddy.sql

Here is one way you can create the database. Before taking these steps,
you should have a running Postgresql instance.:

    createdb viddy
    psql -f resources/ddl/viddy.sql viddy



### Environment variables

This app depends on the environment variable DATABASE_URL being set to
the URL of the running Postgresql instance. For example, my
development database URL  is set to "postgresql://localhost:5432/viddy"

You can also set the PORT environment variable to an open port for the
web application if you do not want to use the default of 8080.

### To run tests

    lein test

### To run the application (web app and data fetcher)

    lein run

### To run just the data fetcher
    
    lein run workerrun

### To run just the web application

    lein run webrun

## License

Copyright © 2013 David Altenburg

Distributed under the Eclipse Public License, the same as Clojure.
