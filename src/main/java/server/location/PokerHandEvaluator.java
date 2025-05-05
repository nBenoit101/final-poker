/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package server.location;

import com.mycompany.finalpoker.Card;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author nickbenoit
 */
public class PokerHandEvaluator {
    
    
    //Hand Evaluator algorithms were assisted by CHATGPT
    public HandResult evaluate(ArrayList<Card> cards){
        Map<Integer, Integer> valueCount = new HashMap<>();
        int highCard = 0;
        
        for(Card card: cards){
            int val = card.getValue();
            highCard = Math.max(highCard, val);
            valueCount.put(val, valueCount.getOrDefault(val, 0) + 1);
        }
        
        int bestPair = 0;
        for(Map.Entry<Integer, Integer> entry : valueCount.entrySet()){
            if(entry.getValue() >=2){
                bestPair = Math.max(bestPair, entry.getKey());
            }
        }
        boolean hasPair = bestPair > 0;
        return new HandResult(hasPair, bestPair, highCard);
    }
    
    public int compareHands(HandResult player, HandResult dealer){
        if(player.hasPair() && dealer.hasPair()){
            return Integer.compare(player.getPairValue(), dealer.getPairValue());
        }else if(player.hasPair()){
            return 1;
        }else if(dealer.hasPair()){
            return -1;
        }else{
            return Integer.compare(player.getHighCard(), dealer.getHighCard());
        }
    }
}
