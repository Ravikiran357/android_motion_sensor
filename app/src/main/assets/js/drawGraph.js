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

//    if (!isEmpty(running)) {
        var trace1 = {
          x:unpack(running, 0),  y: unpack(running, 1), z: unpack(running, 2),
          mode: 'lines+markers',
          symbol: 'circle',
          name: 'running',
          line: {
              width: 5,
              colorscale: "Blue"},
          marker: {
            size: 5,
            opacity: 0.5
          },
          type: 'scatter3d'
        };
        data.push(trace1);
//    }

    var trace2 = {
      x:unpack(walking, 0),  y: unpack(walking, 1), z: unpack(walking, 2),
      mode: 'lines+markers',
      symbol: 'square',
      name: 'walking',
      line: {
          width: 7,
          colorscale: "Red"},
      marker: {
        size: 7,
        symbol: 'square',
        colorscale: 'Red',
        opacity: 0.7
      },
      type: 'scatter3d'
    };
    data.push(trace2);

    var trace3 = {
      x:unpack(eating, 0),  y: unpack(eating, 1), z: unpack(eating, 2),
      mode: 'lines+markers',
      symbol: 'rectangle',
      name: 'eating',
      line: {
          width: 8,
          colorscale: "Green"},
      marker: {
        size: 8,
        opacity: 0.8
      },
      type: 'scatter3d'
    };
    data.push(trace3);

    var layout = {
        margin: {
            l: 0,
            r: 0,
            b: 0,
            t: 0
        },
        showlegend: false
    };
    Plotly.newPlot('container', data, layout);
}