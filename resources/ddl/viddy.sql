-- This is all denormalized for now because we don't know how the Divvy-provided API will handle changes to, e.g., station location.
-- e.g., If the station at California and Milwaukee is moved a dozen feet, changing its latitude and longitude, is it defined as a new station? Should it be? Not a question I currently want to answer.

CREATE TABLE station_updates (
       id int unique,
       station_id int,
       station_name varchar(255),
       t timestamp with time zone,
       station int,
       bikes int,
       docks int,
       longitude float,
       latitude float,
       status varchar(255));

CREATE INDEX station_updates_station_id ON station_updates (station_id);
CREATE INDEX station_updates_timesampe ON station_updates (t);
