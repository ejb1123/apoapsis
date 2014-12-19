var socket1
uuid = new RegExp("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");

function createSocket(url0, token) {
    if (!url0.match(/:(\d){2,5}/g)) {
        url0 = url0.concat(":25564");
    }
    socket1 = new WebSocket(url0);
    socket1.onmessage = function (event) {
        eventArray = event.data.split(":");
        eventServersArray = event.data.split(":[");
        console.log(event.data);
        switch (event.data) {
        case "rx:token:ok":
            $('#tokenModal').modal('hide');
            $('#tokenModalProgressBarDiv').addClass('progress-bar-success');
            $('#tokenModalProgressBar').removeClass('progress-bar-danger');
            send("list:servers");
            break;
        case "rx:ok:select":
            break;
        case "rx:err:badtoken":
            $('.tokenForm').addClass('has-error');
            $('#tokenInput').focus();
            $('#tokenModalProgressBarDiv').removeClass('active');
            $('#tokenModalProgressBar').addClass('progress-bar-danger');
            break;
        case "rx:created:" + eventArray[2]:
            alert(eventArray[2]);
            $("#serverList").append("<tr " + "id='" + eventArray[2] + "'" + "><td>" + globalName + "</td><td>" + eventArray[2] + "</td><td id='serverStatus'>Not Started</td></tr>");
            break;
        case "rx:list-servers:[" + eventServersArray[1]: //case does not work. dont now how to detect the rx:list-serer:[] response
            alert("hey");
                servers= "[" + eventServersArray[1];
        default:
            break;
        }
    }
    socket1.onopen = function (event) {
        socket1.send(token);
    }
    socket1.onclose = function (event) {
        $('#tokenModal').modal('show');
    }
}

function newServer(name, version) {
    globalName = name;
    alert(name + " " + version);
    send("create:" + JSON.stringify({
        name: name,
        version: {
            name: version,
            base: "https:/\/\s3.amazonaws.com/Minecraft.Download/versions"
        },
        location: "./test"
    }));
}
//var socket1 = new WebSocket("ws://127.0.0.1:25564/");


function send(hh) {
    socket1.send(hh);
}