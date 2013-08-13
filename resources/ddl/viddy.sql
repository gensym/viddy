-- This is all denormalized for now because we don't know how the Divvy-provided API will handle changes to, e.g., station location.
-- e.g., If the station at California and Milwaukee is moved a dozen feet, changing its latitude and longitude, is it defined as a new station? Should it be? Not a question I currently want to answer.

CREATE TABLE station_update_executions (
       id serial primary key,
       execution_time timestamp with time zone,
       insertion_time timestamp with time zone
);

CREATE FUNCTION set_insertion_time_timestamp() RETURNS TRIGGER AS $$
BEGIN  
       NEW.insertion_time = NOW();
       RETURN NEW;
END;
$$ LANGUAGE 'plpgsql';

CREATE TRIGGER station_update_executions_insertion_trigger 
BEFORE INSERT ON station_update_executions FOR EACH ROW
EXECUTE PROCEDURE set_insertion_time_timestamp();


CREATE INDEX station_update_executions_insertion_time 
ON station_update_executions(insertion_time);

CREATE TABLE station_updates (
       id serial primary key,
       execution_id int references station_update_executions,
       station_id int,
       station_name varchar(255),
       bikes int,
       docks int,
       longitude float,
       latitude float,
       status varchar(255));

CREATE INDEX station_updates_station_id ON station_updates (station_id);




