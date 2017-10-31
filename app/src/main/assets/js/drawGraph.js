function showGraph(running, walking, eating) {
    function unpack(rows, key) {
      return rows.map(function(row)
      { return row[key]; });
    }
    var trace1 = {
      x:unpack(running, 0),  y: unpack(running, 1), z: unpack(running, 2),
      mode: 'markers',
      symbol: 'circle',
      name: 'running',
      marker: {
        size: 8,
        line: {
          color: 'rgba(217, 217, 217, 0.14)',
          width: 0.5
        },
        opacity: 1
      },
      type: 'scatter3d'
    };
    var trace2 = {
      x:unpack(walking, 0),  y: unpack(walking, 1), z: unpack(walking, 2),
      mode: 'markers',
      name: 'walking',
      marker: {
        color: 'rgb(127, 127, 127)',
        size: 8,
        symbol: 'square',
        line: {
          color: 'rgb(204, 204, 204)',
          width: 1
        },
        opacity: 1
      },
      type: 'scatter3d'
    };
    var trace3 = {
      x:unpack(eating, 0),  y: unpack(eating, 1), z: unpack(eating, 2),
      mode: 'markers',
      name: 'eating',
      symbol: 'circle',
      marker: {
        size: 8,
        line: {
          color: 'rgba(217, 200, 200, 0.74)',
          width: 0.5
        },
        opacity: 1
      },
      type: 'scatter3d'
    };
    var data = [trace1, trace2, trace3];
    var layout = {margin: {
        l: 0,
        r: 0,
        b: 0,
        t: 0
      },
      showlegend: false};
    Plotly.newPlot('container', data, layout);

}