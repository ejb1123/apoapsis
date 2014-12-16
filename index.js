function createSocket(url0,token){
    var socket1 = new WebSocket(url0);
    socket1.onmessage= function(event){
        console.log(event.data);
        switch(event.data) {
            case "rx:ok":
                $('#tokenModal').modal('hide');
                break;
            default:
                break;
        }
    }
    socket1.onopen = function(event){
        socket1.send(token);
    }
}
//var socket1 = new WebSocket("ws://127.0.0.1:25564/");


function send(hh){
    socket1.send(hh);
}