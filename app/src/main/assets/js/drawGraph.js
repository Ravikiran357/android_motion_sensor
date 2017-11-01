function showGraph(running, walking, eating) {

    var data = [];
    // clean up the dom element
    var node = document.getElementById('container');
    while (node.hasChildNodes()) {
        node.removeChild(node.lastChild);
    }

    // Plot the graph
    function unpack(rows, key) {
      return rows.map(function(row)
      { return row[key]; });
    }

    function isEmpty(str) {
        return (!str || (0 === str.length));
    }

    if (!isEmpty(running)) {
        var trace1 = {
          x:unpack(running, 0),  y: unpack(running, 1), z: unpack(running, 2),
          mode: 'lines+markers',
          symbol: 'circle',
          name: 'running',
          line: {
              width: 6,
              colorscale: "Blue"},
          marker: {
            size: 6,
            line: {
              color: 'rgb(217, 217, 217)',
              width: 0.5
            },
            opacity: 0.6
          },
          type: 'scatter3d'
        };
        data.push(trace1);
    }

    if (!isEmpty(walking)) {
        var trace2 = {
          x:unpack(walking, 0),  y: unpack(walking, 1), z: unpack(walking, 2),
          mode: 'lines+markers',
          name: 'walking',
          line: {
              width: 6,
              colorscale: "Red"},
          marker: {
            color: 'rgb(127, 0, 0)',
            size: 6,
            symbol: 'rect',
            line: {
              color: 'rgb(255, 0, 0)',
              width: 0.7
            },
            opacity: 0.8
          },
          type: 'scatter3d'
        };
        data.push(trace2);
    }

    if (!isEmpty(eating)) {
        var trace3 = {
          x:unpack(eating, 0),  y: unpack(eating, 1), z: unpack(eating, 2),
          mode: 'lines+markers',
          name: 'eating',
          line: {
              width: 6,
              colorscale: "Green"},
          marker: {
            size: 6,
            symbol: 'square',
            line: {
              color: 'rgb(0, 128, 0)',
              width: 0.7
            },
            opacity: 1
          },
          type: 'scatter3d'
        };
        data.push(trace3);
    }

    var layout = {margin: {
        l: 0,
        r: 0,
        b: 0,
        t: 0
      },
      showlegend: false
    };
    Plotly.newPlot('container', data, layout);
}