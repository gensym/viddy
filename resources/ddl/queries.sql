SELECT a.addition_time, a.station_id, s.station_name 
FROM station_additions a
LEFT JOIN current_stations s ON s.station_id = a.station_id
ORDER BY a.addition_time DESC 
LIMIT 30;

SELECT a.addition_time, s.station_id, s.station_name 
FROM current_stations s LEFT JOIN
     (SELECT s1.station_id AS station_id, MIN(e.execution_time) AS addition_time
      FROM station_updates s1, station_update_executions e
      WHERE s1.execution_id = e.id 
      GROUP BY s1.station_id) a
ON a.station_id = s.station_id;
ORDER BY a.addition_time DESC 
LIMIT 30;
