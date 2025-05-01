/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.finalpoker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author nickbenoit
 */
public class Player {
    private String name;
    private int balance;
    
    private enum GameState{DEAL, DEALDECISIONS};
    private GameState currentState = GameState.DEAL;
    
    //game fields
    private ArrayList<Card> playerCards;
    private ArrayList<Card> dealerCards;
    
    //Player networking fields
    private String ip;
    private int port;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private ObjectInputStream objectIn;
    private String messageToSend;
    
    //Static instance of player object
    private static Player player;
    
    private ArrayList<Observer>observer;
    
    private Player(String name){
        this.name = name;
        this.balance = 100;
        this.observer = new ArrayList();
        this.ip = "localhost";
        this.port = 1234;
        this.messageToSend = "dealState";
         
    }
    
    public static Player getPlayer(String name){
       if (player == null){
           player = new Player(name);
       }
       return player;      
    }
    public static Player getPlayer(){
       if (player == null){
           player = new Player("Default");
       }
       return player;      
    }
    
    public void connectToGame(){
        new Thread() {
            public void run() {
                try{
                    socket = new Socket(ip, port);
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    out = new PrintWriter(socket.getOutputStream(), true);
                    objectIn = new ObjectInputStream(socket.getInputStream());
                    while(true){
                        if(messageToSend.equals("quit")){
                            break;
                        }else if(messageToSend.equals("dealState")){
                           out.println(messageToSend);
                           messageToSend = "donothing";
                        }else{
                            out.println("donothing");
                        }
                        
                        if (currentState == GameState.DEAL) {
                            try {
                                playerCards = (ArrayList<Card>) objectIn.readObject();
                                System.out.println("[Player] Received cards: " + playerCards);
                                notifyObserver(); // Don't forget this
//                                currentState = GameState.DEALDECISIONS;
                                out.println("initalCardsRecieved");
                            } catch (Exception e) {
                                
                            }
                        }
                        
                        if(currentState == GameState.DEALDECISIONS){
                            String serverMessage = in.readLine();
                            if(serverMessage.equals("playersTurn")){
                                SinglePlayerWin.getWindow().disableButton();
                            }else{
                              SinglePlayerWin.getWindow().disableSlider();  
                            }
                            messageToSend = "donothing";
                        }
                        currentState = GameState.DEALDECISIONS;

                    }

                }catch(Exception e){
            
                }
            }
        }.start();
        
        
    }
    
    
    //observer methods
    public void addObservers(Observer o){
       observer.add(o);
   }
    
    private void notifyObserver(){
        for(int i = 0; i< observer.size(); i++){
                observer.get(i).updateData();
            }
    }
    
    //Player Methods 
    public String getName(){
        return name;
    }
    
    public int getBalance(){
        return balance;
    }
    
    public ArrayList<Card> getPlayerCards(){
        return playerCards;
    }
    
    public boolean changeBalance(int amount){
        balance+= amount;
        if(balance>0){
            return  true;
        }
        balance = 0;
        return false;
    }
    
    //Methods to send messages to server 
    
    public void playerActions(String s){
        messageToSend = s;
    }
    
   
}
