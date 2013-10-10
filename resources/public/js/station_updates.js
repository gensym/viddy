
//Convert a 24-hour-time (e.g., "14:20") into minutes into the day"
function to_minutes(time_of_day) {
  var tod = time_of_day.split(":");
//  console.log(time_of_day);
  var ret = 60 * parseInt(tod[0]) + parseInt(tod[1]);
//  console.log(ret);
//  console.log(from_minutes(ret));
  return ret;
}

function from_minutes(minutes_from_start_of_day) {
  return Math.floor(minutes_from_start_of_day / 60) + 
    ":" + 
    (minutes_from_start_of_day % 60);
}

function to_data_series_percentiles(edn_percentiles) {
  var json_series = jsedn.toJS(jsedn.parse(edn_percentiles));
  return _.partial(_.extend, {}).
    apply(this, 
          _.map(json_series, function(v, k) {
            var r = {};
            r[k] = _.sortBy(_.map(v, function(v, k) { return [to_minutes(k), v]}), 
                            function(x) {return x[0];});
            
            return r; }));
}

function update_summary_bikes_graph(graph_selector, station_id) {

  $.ajax("/available_bikes/weekdays/" + station_id + ".edn").success(
    function(e) {
      var percentiles = to_data_series_percentiles(e);
      console.log(percentiles);
      
     var dataset = [

    { id: "m10%", data: percentiles["percentile-10"], 
      lines: { show: true, lineWidth: 0, fill: false}
      , color: "rgb(50,50,255)"},
    { id: "m15%", data: percentiles["percentile-15"], 
      lines: { show: true, lineWidth: 0, fill: 0.1}, 
      color: "rgb(50,50,255)", fillBetween: "m10%"},
    { id: "m25%", data: percentiles["percentile-25"], 
      lines: { show: true, lineWidth: 0.0, fill: 0.2}, 
      color: "rgb(50,50,255)", fillBetween: "m15%"},
    { id: "m50%", data: percentiles["percentile-50"], 
      lines: { show: true, lineWidth: 0.5, fill: 0.4}, 
      color: "rgb(50,50,255)", fillBetween: "m25%"},
    { id: "m75%", data: percentiles["percentile-75"], 
      lines: { show: true, lineWidth: 0, fill: 0.4}, color: "rgb(50,50,255)", fillBetween: "m50%"},
    { id: "m85%", data: percentiles["percentile-85"], 
      lines: { show: true, lineWidth: 0, fill: 0.2}, 
      color: "rgb(50,50,255)", fillBetween: "m75%"},
    { id: "m90%", data: percentiles["percentile-90"], 
      lines: { show: true, lineWidth: 0, fill: 0.1}, 
      color: "rgb(50,50,255)", fillBetween: "m85%"},
     ];
      $.plot($(graph_selector), dataset, {
        xaxis: {
          tickFormatter: from_minutes
        },
        yaxis: {}
      });
    });
}

function update_available_bikes_graph(graph_selector, station_id ) {

  $.ajax("/available_bikes/" + station_id + ".edn").success(
    function(e) {
      var updates = jsedn.toJS(jsedn.parse(e));
      var table = _.map(updates, function(update) {
        return [update[":execution-time"].getTime(), 
                update[":available-bikes"]];
      });
      
      var options = {
        series: {
          lines: {
            steps:true
          }
        },
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
        series: {
          lines: {
            steps:true
          }
        },
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
