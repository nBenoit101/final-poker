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
    
    private enum GameState{DEAL, DEALDECISIONS, FLOP, TURN, RIVER};
    private GameState currentState = GameState.DEAL;
    private GameState nextState = GameState.DEAL;
    private boolean hasPlayerGone;
    
    //game fields
    private ArrayList<Card> playerCards;
    private ArrayList<Card> dealerCards;
    private ArrayList<Card> tableCards;
    
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
                            out.close();
                            in.close();
                            objectIn.close();
                            socket.close();
                            break;
                        }
                        else if(currentState == GameState.DEALDECISIONS){
                            String serverMessage = in.readLine();
                            
                            if(serverMessage.startsWith("winner:")){
                                handleWinnerMessage(serverMessage);
                                currentState = GameState.DEAL;
                                nextState = GameState.DEAL;
                               
                            }
                            
                            if(serverMessage.equals("playersTurn")){
                                SinglePlayerWin.getWindow().changeDealersChoice(serverMessage);
                                SinglePlayerWin.getWindow().handleGui("check");
                            }
                            if(serverMessage.equals("check")){
                                System.out.println("[Client]Recieved check");
                                SinglePlayerWin.getWindow().changeDealersChoice(serverMessage);
                                    if(!hasPlayerGone){
                                        SinglePlayerWin.getWindow().handleGui("check");
                                    }
                            }
                            if(serverMessage.equals("nextState")){
                                currentState = nextState;
                                System.out.println("[Client]Recieved next state");
                            }
                            if (serverMessage.equals("call")) {
                                SinglePlayerWin.getWindow().changeDealersChoice("call");
                                out.println("nextState");
                                currentState = nextState;
                                continue;
                            }
                            out.println(messageToSend); 
                            
                        }else{
                            handleTableCardStates();
                        }
                        synchronized(this){
                            messageToSend = "donothing";   
                        }    
                       
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }.start();
        
        
    }
    
    private void handleTableCardStates() {
        try {
            if (currentState == GameState.DEAL) {
                resetGame();
                SinglePlayerWin.getWindow().quitBtnState(true);
                out.println("dealState"); 
                System.out.println("[Client] sent out deal state");
                try {
                    playerCards = (ArrayList<Card>) objectIn.readObject(); 
                    System.out.println("[Player] Received cards: " + playerCards);
                    SinglePlayerWin.getWindow().showInitialCards();
                    out.println("cardsRecieved"); 
                    System.out.println("[Client] sent out initail state");
                    currentState = GameState.DEALDECISIONS;
                    nextState = GameState.FLOP;

                } catch (Exception e) {
                    e.printStackTrace();
                    messageToSend = "donothing";
                    System.out.println("[Client] did not reciee cards");
                }
            } else if(currentState == GameState.FLOP) {
                SinglePlayerWin.getWindow().quitBtnState(false);
                hasPlayerGone = false;
                out.println("flopState");
                tableCards = (ArrayList<Card>) objectIn.readObject();
                SinglePlayerWin.getWindow().showFlopCards();
                out.println("cardsRecieved");
                currentState = GameState.DEALDECISIONS;
                nextState = GameState.TURN;

            } else if (currentState == GameState.TURN) {
                hasPlayerGone = false;
                out.println("turnState");
                tableCards = (ArrayList<Card>) objectIn.readObject();
                SinglePlayerWin.getWindow().showTurnCard();
                out.println("cardsRecieved");
                currentState = GameState.DEALDECISIONS;
                nextState = GameState.RIVER;

            } else if (currentState == GameState.RIVER) {
                hasPlayerGone = false;
                out.println("riverState");
                tableCards = (ArrayList<Card>) objectIn.readObject();
                SinglePlayerWin.getWindow().showRiverCard();
                out.println("cardsRecieved");
                currentState = GameState.DEALDECISIONS;
                nextState = GameState.DEAL;
            }
        } catch (Exception e) {
            e.printStackTrace();
            messageToSend = "donothing"; // recover from error
        }
    }
    
    
    // Handles the message sent from the server
    private void handleWinnerMessage(String message){
        
        //Refernecd from chatGPT to help parse data 
        //Message is split into a list by the character :
        String[] parts = message.split(":");
        String result = parts[1];
        int pot = parts.length > 2 ? Integer.parseInt(parts[2]) : 20;
        
        if(result.equals("player")){
            changeBalance(pot);
            SinglePlayerWin.getWindow().showPopup("You win the round");
            
        }else if (result.equals("dealer")){
            SinglePlayerWin.getWindow().showPopup("Dealer wins the round");
        }else{
            changeBalance(pot / 2);
            SinglePlayerWin.getWindow().showPopup("Tie Game");
        }
        
        SinglePlayerWin.getWindow().updateBalanceText();
    }
    
    
    
    private void resetGame(){
        playerCards = new ArrayList();
        dealerCards = new ArrayList();
        SinglePlayerWin.getWindow().cardState(false);
        messageToSend = "donothing";
        hasPlayerGone = false;
    }
    
    //PUBLIC METHODS
    
    //observer methods
    public void addObservers(Observer o){
       observer.add(o);
   }
    
    //Ends Connection to Server
    public void endConnection(){
        try{
            out.close();
            in.close();
            objectIn.close();
            socket.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        
    }

    //Player Methods 
    //Important methods Changes the field message to send
    //This message is sent to the server depending on the players interaction with the gui
    public void playerActions(String s){
       synchronized(this){
            messageToSend = s;
       }
    }
    
    public void changeHasPlayerGone(boolean b){
        hasPlayerGone = b;
    }
    
    public String getName(){
        return name;
    }
    
    public int getBalance(){
        return balance;
    }
    
    public ArrayList<Card> getPlayerCards(){
        return playerCards;
    }
    
    public ArrayList<Card> getTableCards(){
        return tableCards;
    }
    
    public boolean changeBalance(int amount){
        balance += amount;
        if (balance > 0){
            return true;
      }    
        balance = 0;
        return false;
    }
    
   
}