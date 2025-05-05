/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.location;

import com.mycompany.finalpoker.Card;
import com.mycompany.finalpoker.Deck;
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
 
    
    private enum GameState{DEAL,DEALDECISIONS,FLOP,TURN,RIVER}

    private enum ButtonPos{DEALER, PLAYER}
    private GameState currentState;
    private GameState nextState;
    private ButtonPos currentButton;
    private Deck deck;
    private ArrayList<Card> dealerCards;
    private ArrayList<Card> playerCards;
    private ArrayList<Card> tableCards;
    private int pot;
    private boolean playerRaised;
    
    private static SinglePlayerServer server;
    
    private SinglePlayerServer(){
        port = 1234;
        currentState = GameState.DEAL;
        nextState = GameState.DEAL;
        currentButton = ButtonPos.PLAYER;
        run();
        
        
    }
   
   //public Methods
   public static SinglePlayerServer server(){
        if(server == null){
            server = new SinglePlayerServer();
        }
        return server;
        
    }
   
   
   //private Methods 
   
   //Runs The Game Server When Called
 
   private void run() {
    try {
        socket = new ServerSocket(port);
        System.out.println("[Server] Waiting on Client Connection.....");
        client = socket.accept();
        objectOut = new ObjectOutputStream(client.getOutputStream());
        out = new PrintWriter(client.getOutputStream(), true);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));                
        System.out.println("[Server] Client Connected Successfully");

        try {
            while (true) {
                if (currentState == GameState.DEALDECISIONS) {
                    String request = in.readLine();
                    playerRaised = false;

                    if (request.equals("cardsRecieved")) {
                        if (buttonPosToSend().equals("dealersTurn")) {
                            String dealerMove = dealersChoice();
                            out.println(dealerMove);
                            System.out.println("[Server] Dealer chooses to: " + dealerMove);
                        } else {
                            out.println(buttonPosToSend());
                        }
                    }

                    if (request.equals("check")) {
                        System.out.println("[Server] Received Check State");
                        out.println("nextState");
                        currentState = nextState;
                    }

                    if (request.startsWith("raise:")) {
                        int raiseAmount = Integer.parseInt(request.split(":")[1]);
                        pot += raiseAmount * 2;
                        playerRaised = true;
                        out.println("nextState");
                        currentState = nextState;
                    }

                    if (request.equals("fold")) {
                        System.out.println("[Server] Player folded.");
                        out.println("winner:dealer:" + pot);
                        nextState = GameState.DEAL;
                        currentState = GameState.DEAL;
                    }

                    if (request.equals("donothing")) {
                        out.println("doNothing");
                    }

                } else {
                    handleTableCardStates();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    } catch (Exception e) {
        e.printStackTrace();
    }
}

        
        
        

    
    //Handles which Cards to send out based on which state of the game it is
    private void handleTableCardStates() {
       try {
           if(currentState == GameState.DEAL){
               String request = in.readLine();
               if(request.equals("dealState")){
                   newRound();
                   dealCards();
                   objectOut.reset();
                   objectOut.writeObject(playerCards);
                   objectOut.flush();
                   System.out.println(currentButton);
                   System.out.println("[Server has dealt the cards]");
                   currentState = GameState.DEALDECISIONS;
                   nextState = GameState.FLOP;
               }
           }else if (currentState == GameState.FLOP) {
               String request = in.readLine();
               if (request.equals("flopState")) {
                   dealCards();
                   System.out.println("[Server] Received Flop State");
                   System.out.println("[Server] Flop Cards: " + tableCards);
                   objectOut.reset();
                   objectOut.writeObject(tableCards);
                   objectOut.flush();
                   System.out.println(currentButton);

                   currentState = GameState.DEALDECISIONS;
                   nextState = GameState.TURN;
               }
           } else if (currentState == GameState.TURN) {
               String request = in.readLine();
               if (request.equals("turnState")) {
                   dealCards();
                   System.out.println("[Server] Received Turn State");
                   System.out.println("[Server] Turn Cards: " + tableCards);
                   objectOut.reset();
                   objectOut.writeObject(tableCards);
                   objectOut.flush();
                   System.out.println(currentButton);
                   System.out.println("[Server has dealt the cards]");

                   currentState = GameState.DEALDECISIONS;
                   nextState = GameState.RIVER;
               }
           } else if (currentState == GameState.RIVER) {
               String request = in.readLine();
               if (request.equals("riverState")) {
                   System.out.println("[Server] Received River State");
                   dealCards();
                   System.out.println("[Server] River Cards: " + tableCards);
                   objectOut.reset();
                   objectOut.writeObject(tableCards);
                   objectOut.flush();
                   System.out.println(currentButton);
                   System.out.println("[Server has dealt the cards]");

                   ArrayList<Card> playerFullHand = new ArrayList<>(playerCards);
                   ArrayList<Card> dealerFullHand = new ArrayList<>(dealerCards);
                   
                   //Combines table and player Cardds
                   playerFullHand.addAll(tableCards);
                   //Combines table and dealer Cardds
                   dealerFullHand.addAll(tableCards);

                   PokerHandEvaluator evaluator = new PokerHandEvaluator();
                   
                   //Returns the Value of a Players Hand(The best pair or High Card of your best 5 cards)
                   HandResult playerResult = evaluator.evaluate(playerFullHand);
                   HandResult dealerResult = evaluator.evaluate(dealerFullHand);

                   //returns which hand has won the game
                   int winner = evaluator.compareHands(playerResult, dealerResult);

                   if (winner > 0) {
                       out.println("winner:player:" + pot);
                       System.out.println("[Server] Player wins.");
                   } else if (winner < 0) {
                       out.println("winner:dealer:" + pot);
                       System.out.println("[Server] Dealer wins.");
                   } else {
                       out.println("winner:draw:" + pot);
                       System.out.println("[Server] It's a draw.");
                   }

                   currentState = GameState.DEAL;
                   nextState = GameState.DEAL;
               }
           }
       } catch (Exception e) {
           e.printStackTrace();
       }
   }

    
    //Dealers Choice in the game
    private String dealersChoice() {
        return "check";
    }

    
    //Current Button Position of the game (If the dealer or player gets their cardd first)
    private String buttonPosToSend(){
        if(currentButton == ButtonPos.DEALER){
            return "playersTurn";
        }
        return "dealersTurn";
    }
    
    
    //Deals out the cards of the game based on the current state
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
    
    //Resets necessary fields and changes button positions
    private void newRound(){
        currentState = GameState.DEAL;
        playerRaised = false;
        deck = new Deck();
        dealerCards = new ArrayList();
        playerCards = new ArrayList();
        tableCards = new ArrayList();
        pot = 0;
        if(currentButton == ButtonPos.PLAYER){
            currentButton = ButtonPos.DEALER;
        }else{
            currentButton = ButtonPos.PLAYER;
        }
    }
    
    //Main Method
    public static void main(String[] args){
        SinglePlayerServer.server();
    }
    
    
    
}
