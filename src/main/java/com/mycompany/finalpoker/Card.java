/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mycompany.finalpoker;

/**
 *
 * @author nickbenoit
 */
public class Card {
    private String suit;
    private int value;
    private String strVal;

    public Card(String suit, int value, String strVal){
        this.suit = suit;
        this.strVal = strVal;
        this.value = value;

    }

    public String getSuit(){
        return suit;
    }

    public int getValue(){
        return value;
    }

    public String getStrVal(){
        return strVal;
    }


    @Override
    public String toString(){
        return strVal + "" + suit;
    }
}