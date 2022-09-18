package com.example.crazyeights;

import android.graphics.Canvas;

import java.util.Iterator;

public class Hand {

    private java.util.ArrayList<Card> cards = new java.util.ArrayList<>();

    public Hand() {}

    public void addCard( Card card ) {
        cards.add( card );
    }

    //TODO: Revisit this method. It's ugly..
    public void draw( Canvas canvas, int x, int y ) {
        Iterator i = getIterator();
        int startXPosition = x;
        int startYPosition = y;
        int updatedXPos = startXPosition;
        int updatedYPos = startYPosition;
        int cardNumber = 0;
        while ( i.hasNext() ) {
            Card card = ( Card ) i.next();
            if ( cardNumber % 7 == 0 ) {
                updatedXPos = startXPosition;
                updatedYPos += 100;
            }
            card.draw( canvas, updatedXPos, updatedYPos );
            updatedXPos += 100;
            cardNumber++;
        }
    }

    public Iterator getIterator() {
        return cards.iterator();
    }

    protected int size() {
        return cards.size();
    }

    public Card getCard( int position ) {
        return cards.get( position );
    }

    public void removeCard( Card card ) {
        cards.remove( card );
    }
}
