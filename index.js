var socket1 = new WebSocket("ws://127.0.0.1:25564/");
socket1.onmessage= function(event){
    console.log(event.data);
}
socket1.onopen = function(event){
    socket1.send("token:UNDEFINED");
    socket1.send("token:UNDEFINED");
}
function send(hh){
    socket1.send(hh);
}