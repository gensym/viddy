DROP MATERIALIZED VIEW current_stations;

CREATE MATERIALIZED VIEW current_stations AS
       SELECT s.id,
              s.station_id, 
              s.station_name,
              s.bikes,
              s.docks,
              s.longitude,
              s.latitude,
              s.status,
              e.execution_time, 
              a.addition_time
       FROM station_updates s, station_update_executions e, station_additions a 
       WHERE s.execution_id = e.id
       AND a.station_id = s.station_id
       AND e.insertion_time = (
           SELECT MAX(e2.insertion_time) FROM station_update_executions e2);


