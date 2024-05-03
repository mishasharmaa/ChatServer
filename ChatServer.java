package com.example.webchatserver;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


/**
 * This class represents a web socket server, a new connection is created and it receives a roomID as a parameter
 * **/
@ServerEndpoint(value="/ws/{roomID}")
public class ChatServer {

    // contains a static List of ChatRoom used to control the existing rooms and their users
    private static ArrayList<ChatRoom> chatRooms = new ArrayList();
    private static ChatRoom currentRoom;
    // you may add other attributes as you see fit

    @OnOpen
    public void open(@PathParam("roomID") String roomID,  Session session) throws IOException, EncodeException {
        if(!checkRooms(roomID))//if it is a new room
        {
            currentRoom = new ChatRoom(roomID, session.getId());
            chatRooms.add(currentRoom);
            session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): Welcome to " + roomID + " chat room. Please state your username to begin.\"}");
        }
        else {//if room is already made
            for(ChatRoom c :chatRooms)
            {
                if(c.getCode().equals(roomID))
                {
                    c.addUser(session.getId());
                    currentRoom = c;
                }
            }
            session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): Welcome to " + roomID + " chat room. Please state your username to begin.\"}");
        }
    }

    @OnClose
    public void close(Session session) throws IOException, EncodeException {
        String userId = session.getId();
        String userName = "";
        for (Session peer : session.getOpenSessions()){
            String user = peer.getId();
            if (currentRoom.inRoom(user) && (currentRoom.getCode().equals(currentRoom.getCode()))) {
                userName = currentRoom.getUserName(userId);
                currentRoom.removeUser(userId);
                if(userName.equals(""))
                {
                    userName = "anonymous";
                }
                peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): " + userName + " left the chat room. \"}");
            }
        }
    }

    @OnMessage
    public void handleMessage(String comm, Session session) throws IOException, EncodeException {
        String userID = session.getId();
        JSONObject jsonmsg = new JSONObject(comm);
        String type = (String) jsonmsg.get("type");
        String message = (String) jsonmsg.get("msg");
        if (type.equals("chat")) {
            if (currentRoom.first(userID)) {
                String username = currentRoom.getUserName(userID);
                System.out.println(username);
                for (Session peer : session.getOpenSessions()) {
                    String user = peer.getId();
                    for (ChatRoom c : chatRooms) {
                        if (c.inRoom(user) && (c.getCode().equals(currentRoom.getCode())))
                            peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(" + username + "): " + message + "\"}");
                    }
                }
            } else { //first message is their username
                if (currentRoom.checkUser(message)) {
                    session.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): " + message + " is already in use please use another name.\"}");//username already used
                    currentRoom.setUserName(userID, "");
                } else {
                    currentRoom.setUserName(userID, message);
                    for (Session peer : session.getOpenSessions()) {
                        String user = peer.getId();
                        if (currentRoom.inRoom(user))
                            peer.getBasicRemote().sendText("{\"type\": \"chat\", \"message\":\"(Server): Welcome, " + message + "!\"}");//Person Joined Room
                    }
                }
            }
        }
        else if(type.equals("chatRooms")) {
            message = "";
            for (ChatRoom c : chatRooms)
            {
                message += c.getCode() + " ";
            }
            session.getBasicRemote().sendText("{\"type\": \"chatRooms\", \"message\":\"" + message + "\"}");
        }
    }
    public boolean checkRooms(String roomID)
    {
        for(ChatRoom c :chatRooms)
        {
            if(c.getCode().equals(roomID))
                return true;
        }
        return false;
    }
}