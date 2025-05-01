/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.finalpoker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

/**
 *
 * @author nickbenoit
 */
public class SinglePlayerServer {
    //Server Fields
    private int port;
    private ServerSocket socket;
    //Freezes program until client connects
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private ObjectOutputStream objectOut;
    
    //Observer fields
    private ArrayList<Observer> observers;
    
    
    //Game Fields 
    public enum GameState{
        DEAL(0),
        FLOP(1),
        TURN(2),
        RIVER(3);
        private final int value;
        private GameState(int value){
            this.value = value;
        }
        public int getValue(){
            return value;
        }
    }

    private enum ButtonPos{DEALER, PLAYER}
    private GameState currentState;
    private ButtonPos currentButton;
    private Deck deck;
    private ArrayList<Card> dealerCards;
    private ArrayList<Card> playerCards;
    private ArrayList<Card> tableCards;
    private int pot;
    private int playerBet;
    private int dealerBet;
    private int dealerBalance;
    
    
    private static SinglePlayerServer server;
    
    private SinglePlayerServer(){
        port = 1234;
        dealerBalance = 0;
        observers = new ArrayList();
        currentState = GameState.DEAL;
        currentButton = ButtonPos.DEALER;
        handleGameServer();
        
    }
   
   //public Methods
   public static SinglePlayerServer server(){
        if(server == null){
            server = new SinglePlayerServer();
        }
        return server;
        
    }
   
   //Add Observers to server
   public void addObservers(Observer o){
       observers.add(o);
   }
   
   public ArrayList<Card> getDealerCards(){
       return dealerCards;
   } 
   
   public ArrayList<Card> getPlayerCards(){
       return playerCards;
   }
   
   public ArrayList<Card> getTableCards(){
       return tableCards;
   }
   
   public GameState getCurrentState(){
       return currentState;
   }
   
   //private Methods 
    private void handleGameServer(){
            
        new Thread() {
           public void run() {
            try{
                socket = new ServerSocket(port);
                System.out.println("[Server] Waiting on Client Connection.....");
                client = socket.accept();
                out = new PrintWriter(client.getOutputStream(),true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                objectOut = new ObjectOutputStream(client.getOutputStream());
                System.out.println("[Server] Client Connected Succesfully");
                try{
                    while(true){
                        String request = in.readLine();
                        if(request.equals("dealState")){
                            newRound();
                            handleGame(request);
                            objectOut.writeObject(playerCards);
                            objectOut.flush();
                            System.out.println(currentButton);
                            System.out.println("[Server has dealt the cards]");
                           
                        }else if(request.equals("initalCardsRecieved")){
//                            if(buttonPosToSend().equals("dealers turn")){
//                                out.println("players turn");
//                            
//                            }else{
//                                out.println("dealers turn");
//                            }
                            out.println("dealersTurn");
                        }else{
                        
                        }
                    }
                }catch(Exception e){
                
                }
            }catch(Exception e){
        
            }
          }
        }.start();
        
        
    }
    
    private String dealersChoice(){
       //can return check 
       //call
       //raise
        return "Dealer calls";
    }
    
    private void handleGame(String action){
        if(action.equals("deal")){
            System.out.println(dealerCards);
            System.out.println(playerCards);
        }
    }
    
    private String buttonPosToSend(){
        if(currentButton == ButtonPos.DEALER){
            return "players turn";
        }
        return "dealers turn";
    }
    
    private void dealCards(){
       if(currentState==GameState.DEAL){
           if(currentButton == ButtonPos.PLAYER){
               dealerCards.add(deck.getCard());
               playerCards.add(deck.getCard());
               dealerCards.add(deck.getCard());
               playerCards.add(deck.getCard());
           }else{
               playerCards.add(deck.getCard());
               dealerCards.add(deck.getCard());
               playerCards.add(deck.getCard());
               dealerCards.add(deck.getCard());
               
           }
       }else if(currentState == GameState.FLOP){
           tableCards.add(deck.getCard());
           tableCards.add(deck.getCard());
           tableCards.add(deck.getCard());
       }else if(currentState == GameState.TURN){
           tableCards.add(deck.getCard());
       }else{
           tableCards.add(deck.getCard());
       }
       
    }
    
    private void newRound(){
        currentState = GameState.DEAL;
        deck = new Deck();
        dealerCards = new ArrayList();
        playerCards = new ArrayList();
        tableCards = new ArrayList();
        pot = 0;
        playerBet = 0;
        dealerBet = 0;
        dealCards();
        if(currentButton == ButtonPos.PLAYER){
            currentButton = ButtonPos.DEALER;
        }else{
            currentButton = ButtonPos.PLAYER;
        }
    }
    
    
}
