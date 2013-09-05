function update_available_bikes_graph(graph_selector, station_id ) {

  $.ajax("/available_bikes/" + station_id + ".edn").success(
    function(e) {
      var updates = jsedn.toJS(jsedn.parse(e));
      var table = _.map(updates, function(update) {
        return [update[":execution-time"].getTime(), 
                update[":available-bikes"]];
      });
      
      var options = {
        xaxis: {
          mode: "time",
        },
        yaxis: {
          ticks: function(axis) {
            return _.range(axis.max);
          }
        }
      };
      $.plot(graph_selector, [table], options);
    });
}


function update_available_docks_graph(graph_selector, station_id ) {

  $.ajax("/available_docks/" + station_id + ".edn").success(
    function(e) {
      var updates = jsedn.toJS(jsedn.parse(e));
      var table = _.map(updates, function(update) {
        return [update[":execution-time"].getTime(), 
                update[":available-docks"]];
      });

      var options = {
        xaxis: {
          mode: "time",
        },
        yaxis: {
          ticks: function(axis) {
            return _.range(axis.max);
          }
        }
      };
      $.plot(graph_selector, [table], options);
    });
}
