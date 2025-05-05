/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.location;

/**
 *
 * @author nickbenoit
 */
public class HandResult {
    private boolean hasPair;
    private int pairValue;
    private int highCard;
    
    public HandResult(boolean hasPair, int pairValue, int highCard){
        this.hasPair = hasPair;
        this.pairValue = pairValue;
        this.highCard = highCard;
    }
    
    public boolean hasPair(){
        return hasPair;
    }
    
    public int getPairValue(){
        return pairValue;
    }
    
    public int getHighCard(){
        return highCard;
    }
}
