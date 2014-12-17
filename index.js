function createSocket(url1,token){
    url0 = "";
    if(!url1.match(/:(\d){2,5}/g)){
       url0 = url1.concat(":25564");
    }
    var socket1 = new WebSocket(url0);
    socket1.onmessage= function(event){
        console.log(event.data);
        switch(event.data) {
            case"rx:token":
                $('#tokenModal').modal('hide');
                $('#tokenModalProgressBarDiv').addClass('progress-bar-success');
                $('#tokenModalProgressBar').removeClass('progress-bar-danger');
                break;
            case "rx:ok":
                break;
            case "rx:err:badtoken":
                $('.tokenForm').addClass('has-error');
                $('#tokenInput').focus();
                $('#tokenModalProgressBarDiv').removeClass('active');
                $('#tokenModalProgressBar').addClass('progress-bar-danger');
                break;
            default:
                break;
        }
    }
    socket1.onopen = function(event){
        socket1.send(token);
    }
    socket1.onclose = function(event){
        alert("opps it closed");    
    }
}
//var socket1 = new WebSocket("ws://127.0.0.1:25564/");


function send(hh){
    socket1.send(hh);
}