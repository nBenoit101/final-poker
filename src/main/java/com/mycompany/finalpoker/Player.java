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
    
    private enum GameState{DEAL, DEALDECISIONS, FLOP};
    private GameState currentState = GameState.DEAL;
    private GameState nextState = GameState.DEAL;
    private boolean hasPlayerGone;
    
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
        this.hasPlayerGone = false;
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
                        }

                        if (currentState == GameState.DEAL) {
                            out.println("dealState"); 
                            try {
                                playerCards = (ArrayList<Card>) objectIn.readObject(); 
                                System.out.println("[Player] Received cards: " + playerCards);
                                SinglePlayerWin.getWindow().showInitialCards();
                                out.println("initalCardsRecieved"); 
                                nextState = GameState.DEALDECISIONS;

                            } catch (Exception e) {
                                e.printStackTrace();
                                messageToSend = "donothing";
                            }
                        }

                        if(currentState == GameState.DEALDECISIONS){
                            String serverMessage = in.readLine();
                            if(serverMessage.equals("playersTurn")){
                                SinglePlayerWin.getWindow().changeDealersChoice(serverMessage);
                                SinglePlayerWin.getWindow().handleGui("check");
                                
                                
                            }else{
                              SinglePlayerWin.getWindow().changeDealersChoice(serverMessage);
                              SinglePlayerWin.getWindow().handleGui(serverMessage);
                            }
                            
                            if(serverMessage.equals("check")){
                                System.out.println("[CLient]Recieved check");
                                SinglePlayerWin.getWindow().changeDealersChoice(serverMessage);
//                                if(hasPlayerGone){
//                                    SinglePlayerWin.getWindow().changeDealersChoice(serverMessage);
//                                }
                            }
                            if(serverMessage.equals("hi")){
                               System.out.println("[CLient]Recieved hi"); 
                            }
                            out.println(messageToSend);
                            messageToSend = "donothing";
                            
                            
                            
                        }
                                           
                        currentState = nextState; 



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
    
    public void changeHasPlayerGone(boolean b){
        hasPlayerGone = b;
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
