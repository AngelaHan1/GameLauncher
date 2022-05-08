package com.example.networkdemo;

import java.io.*;
import java.net.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.util.*;


public class Main extends Application {

    ObjectOutputStream toServer = null;
    ObjectInputStream fromServer = null;
    private String clientName;
    RoomList roomList = new RoomList();


    @Override
    public void start(Stage primaryStage) {


        BorderPane mainPane = new BorderPane();
        // Text area to display contents
        TextArea ta = new TextArea();
        mainPane.setCenter(new ScrollPane(ta));

        // Create a scene and place it in the stage
        Scene scene = new Scene(mainPane, 450, 200);
        primaryStage.setTitle("Game Launcher"); // Set the stage title
        primaryStage.setScene(scene); // Place the scene in the stage
        primaryStage.show(); // Display the stage

        try {
            // Create a socket to connect to the server
            Socket socket = new Socket("localhost", 8000);
            // Socket socket = new Socket("130.254.204.36", 8000);
            // Socket socket = new Socket("drake.Armstrong.edu", 8000);

            // Create an output stream to send data to the server
            toServer = new ObjectOutputStream(socket.getOutputStream());

            // Create an input stream to receive data from the server
            fromServer = new ObjectInputStream(socket.getInputStream());


        } catch (IOException ex) {
            ta.appendText(ex.toString() + '\n');
        }

        Thread sendMessage = new Thread( () -> {
            while (true) {
                try {
                    // read the message sent to this client
                    Object msg = fromServer.readObject();

                    // Downcast message from Object
                    Message messageReceived = (Message)msg;
                    // Downcast humanTypes from Typess
                    HumanTypes messageType = (HumanTypes) messageReceived.getType();

                    // display the text area
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            // Display to the text area
                            ta.appendText(messageReceived.getType().getDescription() + "\n");
                        }
                    });


                    switch (messageType) {

                        case NEW_CLIENT:
                            Message newMsg = new Message(roomList, HumanTypes.SEND_GAMECHANNEL);
                            toServer.writeObject(newMsg);
                            toServer.reset();
                            break;

                        case SEND_NAME :
                            // get username and saved in this.clientName
                            clientName = (String) messageReceived.getData();
                            break;

                        case CREATE_MULTIGAME :
                            // create a new game room and assign creator to room 1 with token 'X'
                            GameRoom room = new GameRoom(clientName, "multi");
                            System.out.println("Adding a new room to the room list: " + room.getRoomID());
                            roomList.addToList(room);
                            // send multigame created
                            toServer.writeObject(new Message(room, HumanTypes.MULTIGAME_CREATED));
                            toServer.reset();

                            System.out.println(roomList.size());
                            //client.out.writeObject(new Message(room.getRoomID(), HumanTypes.ROOM_ADDED));
                            Message newMessage = new Message(roomList, HumanTypes.ROOM_ADDED);
                             toServer.writeObject(newMessage);
                             toServer.reset();
                            break;

                        case JOIN_GAME:  // this message was sent with the room_id player wanna join
                            String room_id = (String) messageReceived.getData();

                            for (int i = 0; i < roomList.size(); i++) {
                                GameRoom currentRoom = roomList.getGameRoomList().get(i);
                                // if room is found
                                if (currentRoom.getRoomID().equals(room_id)) {
                                    // if room is not full (player 2 hasn't joined)
                                    if (currentRoom.getPlayer2().getUserName().equals("")) {
                                        currentRoom.setPlayer2(clientName);
                                        //roomList.updatePlayer2InList(currentRoom);
                                        System.out.println("Adding client " + currentRoom.getPlayer2().getUserName() + " to " + currentRoom.getRoomID());
                                        System.out.println("Player 1: " + currentRoom.getPlayer1().getUserName());
                                        System.out.println("Player 2: " + currentRoom.getPlayer2().getUserName());
                                        toServer.reset();
                                        toServer.writeObject(new Message(currentRoom, HumanTypes.JOIN_SUCCESS));
                                    }
                                    else  // else if the room is full
                                        toServer.writeObject(new Message("full", HumanTypes.JOIN_FAIL));
                                }
                            }
                            break;
                    }
                } catch (IOException ex) {
                    System.out.println("Invalid input");
                    break;
                }
                catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });
        sendMessage.start();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
