/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.finalpoker;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * @author nickbenoit
 */
public class Player {
    private String name;
    private int balance;
    
    //Player networking fields
    private String ip;
    private int port;
    private BufferedReader in;
    private PrintWriter out;
    private Socket socket;
    private String messageToSend;
    
    //Static instance of player object
    private static Player player;
    
    private Player(String name){
        this.name = name;
        this.balance = 100;
        this.ip = "localhost";
        this.port = 1234;
        this.messageToSend = "deal";
         
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


                    while(true){
                        if(messageToSend.equals("quit")){
                            break;
                        }else if(messageToSend.equals("deal")){
                           out.println(messageToSend);
                           messageToSend = "donothing";
                        }else{
                            out.println("donothing");
                        }

                    }

                }catch(Exception e){
            
                }
            }
        }.start();
        
        
    }
    
    //Player Methods 
    public String getName(){
        return name;
    }
    
    public int getBalance(){
        return balance;
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
