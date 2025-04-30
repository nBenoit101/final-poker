/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.finalpoker;

import java.util.ArrayList;
import java.util.Collections;

/**
 *
 * @author nickbenoit
 */
public class Deck {
    private ArrayList<Card> deck;
    private int currentCard;
    
    public Deck(){
        deck = new ArrayList();
        currentCard = 0;
        
        //Creates a deck of 52 cards 
        for(int i=0; i<4; i++){
            
            String suit = "";
            if(i==0){
                suit = "C";
            }else if(i==1){
                suit = "S";
            }else if(i==2){
                suit = "H";
            }else{
                suit = "D";
            }
            for(int j=1; j<14; j++){
                if(j==1){
                   deck.add(new Card(suit, j,"A"));
                }else if(j<10){
                   deck.add(new Card(suit, j, j+"")); 
                }else if (j==10){
                    deck.add(new Card(suit, j, 0+""));
                }else if(j==11){
                   deck.add(new Card(suit, j, "J")); 
                }else if(j==12){
                   deck.add(new Card(suit, j, "Q")); 
                }else{
                   deck.add(new Card(suit, j, "K")); 
                }  
            }
        }
        shuffleDeck();
    }
    
    
    public void printDeck(){
        for(int i=0; i<deck.size(); i++)
        System.out.println(deck.get(i));
    }
    
    private void shuffleDeck(){
        Collections.shuffle(deck);
    }
    
    public Card getCard(){
        currentCard++;
        return deck.get(currentCard-1);
    }
    
        
    
}