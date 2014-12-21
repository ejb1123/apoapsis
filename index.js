var socket1;
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
            $("#serverList").append("<tr class = 'danger' id='" + eventArray[2] + "'" + "><td>" + globalName + "</td><td>" + eventArray[2] + "</td><td id='serverStatus'>Not in implamented yet</td><td><span class='glyphicon glyphicon-off'></span>  <span class='glyphicon glyphicon-pencil'></span>      <span class='glyphicon glyphicon-cog'></span>  <span class='glyphicon glyphicon-trash'></span></td></tr>");
            $('#newServerModal').modal('hide');
            $('#serverName').val("");
            break;
        case "rx:list-servers:[" + eventServersArray[1]: //case does not work. dont now how to detect the rx:list-serer:[] response
            servers = JSON.parse("[" + eventServersArray[1]);
            for (var server = 0; server < servers.length; server++) {
                console.log(servers[server]);
                color = "";
                switch (servers[server].status) {
                case "NotRunning":
                    color = "danger";
                    break;
                case "Init":
                    color = "warning";
                    break;
                case "Running":
                    color = "success";
                    break;
                case "DeInit":
                    color = "warning";
                    break;
                }
                $("#serverList").append("<tr class='" + color + "' id='" + servers[server].uuid + "'" + "><td>" + servers[server].name + "</td><td>" + servers[server].uuid + "</td><td id='serverStatus'>" + "Not in implamented yet" + '</td><td><a href="javascript:startServer(\'' + servers[server].uuid  +  "\');\"><span class='glyphicon glyphicon-off'></span></a>  <span class='glyphicon glyphicon-pencil'></span>      <span class='glyphicon glyphicon-cog'></span>  <span class='glyphicon glyphicon-trash'></span></td></tr>");
            }
            break;
            case "status:" + eventArray[1] + ":" + eventArray[2]:
                switch (eventArray[2]){
                case "init":
                    $('#' + eventArray[1]).removeClass();
                    $('#' + eventArray[1]).addClass('warning');
                    break;
                case "deinit":
                    $('#' + eventArray[1]).removeClass();
                    $('#' + eventArray[1]).addClass('warning');
                    break;
                case "running":
                    $('#' + eventArray[1]).removeClass();
                    $('#' + eventArray[1]).addClass('success');
                    break;
                case "notrunning":
                    $('#' + eventArray[1]).removeClass();
                    $('#' + eventArray[1]).addClass('danger');
                    break;
                }
                break;
           /* case "message:" + eventArray[1] + ":" + eventArray[2]:
                switch (eventArray[2]){
                    case"Initialized run":
                        $('#' + eventArray[1]).removeClass();
                        $('#' + eventArray[1]).addClass('warning');
                        break;
                }
                break;*/
        default:
            break;
        }
    };
    socket1.onopen = function (event) {
        socket1.send(token);
    };
    socket1.onclose = function (event) {
        $('#tokenModal').modal('show');
        $('#serverList').empty();
    };
}

function newServer(name, version) {
    globalName = name;
    alert(name + " " + version);
    send("create:" + JSON.stringify({
        name: name,
        version: {
            name: version,
            base: "https://s3.amazonaws.com/Minecraft.Download/versions"
        },
        location: "./Servers/"+name
    }));
}
//var socket1 = new WebSocket("ws://127.0.0.1:25564/");

function startServer(uuid){
    if($('#' + uuid).hasClass("danger")){
        socket1.send("select:"+uuid);
        socket1.send("start");
    }
    else if($('#' + uuid).hasClass("success")){
        socket1.send("select:"+uuid);
        socket1.send("stop");
    }
}

function send(hh) {
    socket1.send(hh);
}