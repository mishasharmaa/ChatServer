let ws;
window.onload = function() {
    // Your code here
    newRoom();
};
function newRoom(){
    // calling the ChatServlet to retrieve a new room ID
    let callURL= "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet";
    fetch(callURL, {
        method: 'GET',
        headers: {
            'Accept': 'text/plain',
        },
    })
        .then(response => response.text())
        .then(response => enterRoom(response)); // enter the room with the code
}

function enterRoom(code) {
    // refresh the list of rooms
    // create the web socket
    ws = new WebSocket("ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/" + code);

    let newRow = "<tr><td>" + code + "</td></tr>" ;
    document.querySelector("#roomCode tbody").innerHTML = newRow;

    ws.onopen = function (event) {
        let request = {"type": "chatRooms", "msg" : ""};
        ws.send(JSON.stringify(request));
    }

    // parse messages received from the server and update the UI accordingly
    ws.onmessage = function (event) {
        console.log(event.data);
        let message = JSON.parse(event.data);
        console.log(message.message);
        if(message.type === "chatRooms") {
            document.getElementById("chatRoom").value = "";
            document.getElementById("chatRoom").value += message.message;

        }
        else
            document.getElementById("log").value += "[" + timestamp() + "] " + message.message + "\n";
    }
}
function changeRoom() {
    // refresh the list of rooms
    // create the web socket\
    let code = document.getElementById("changeRoomCode").value;
    const pattern = /[A-Z0-9]{5}/
    let isRoom = pattern.test(code)
    if (isRoom === false) {
        alert("Please enter a valid room code")
        return
    }
    let temp = "ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/" + code;
    if(temp === ws.url)
    {
        document.getElementById("log").value += "[" + timestamp() + "] " + "(Server): You are already in the Chat Room \n";
    }
    else
    {
        ws.close();
        ws = new WebSocket("ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/" + code);
        // parse messages received from the server and update the UI accordingly
        ws.onmessage = function (event) {
            console.log(event.data);
            let message = JSON.parse(event.data);
            if(message.type === "chatRooms") {
                document.getElementById("chatRoom").value = "";
                document.getElementById("chatRoom").value += message.message;
            }
            else
                document.getElementById("log").value += "[" + timestamp() + "] " +message.message+ "\n";
        }
    }
}

function refreshRooms() {
    let request = {"type": "chatRooms", "msg" : ""};
    ws.send(JSON.stringify(request));
}
document.getElementById("input").addEventListener("keyup", function (event) {
    if (event.key === "Enter" && !(event.target.value === "")) {
        let request = {"type": "chat", "msg": event.target.value};
        ws.send(JSON.stringify(request));
        event.target.value = "";
    }
});


function timestamp() {
    let d = new Date(), minutes = d.getMinutes();
    if (minutes < 10) minutes = '0' + minutes;
    return d.getHours() + ':' + minutes;
}
window.onbeforeunload = function(event)
{
    console.log("hello");
    ws.close();
}