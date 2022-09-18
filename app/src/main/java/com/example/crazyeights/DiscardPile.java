package com.example.crazyeights;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;

import java.util.Iterator;

public class DiscardPile extends Deck {


    public DiscardPile( Context application, CrazyEightsView view ) {
        super( application, view );
    }

    @Override
    public void addCard( Card card ) {
        super.addCard( card );
    }

    @Override
    protected void buildCards() {
    }

    @Override
    protected void shuffleCards() {
    }

    @Override
    public void draw( Canvas canvas ) {

        this.top = canvas.getHeight()/2-100;
        this.left = canvas.getWidth()/2+60;

        Iterator i = getIterator();
        int startPosition = this.left;
        while ( i.hasNext() ) {
            Card card = ( Card ) i.next();
            LogUtil.i( String.format( "DECK DRAWING CARD: %S", card ) );
            card.draw( canvas, startPosition, this.top );
            startPosition += 1;
            // We set the width and height of the discard pile to that of one of its cards..
            width = (int)card.getWidth();
            height = (int)card.getHeight();
        }
    }

    @Override
    public boolean cardIsInBounds( Card card ) {
        int top = card.getTop();
        int left = card.getLeft();
        if ( left >= getLeft() && left < getWidth()+getLeft() && top >= getTop() && top <= getHeight()+getTop() )
            return true;
        int right = ( int ) ( left + card.getWidth() );
        if ( right >= getLeft() && right < getWidth()+getLeft() && top >= getTop() && top <= getHeight()+getTop() )
            return true;
        return false;
    }

    @Override
    public void onMotionDown( MotionEvent event ) {
        // Make sure to override this method since cards will never be dragged from the discard
        // pile..
    }

    @Override
    public void onMotionMove( MotionEvent event ) {
        LogUtil.i( "DISCARD PILE HAS RECEIVED A MOTION MOVE EVENT.." );
        Card cardBeingDragged = view.getDeck().getSelectedCard();
        if ( cardBeingDragged == null )
            cardBeingDragged = view.getCurrentPlayer().getSelectedCard();
        if ( cardBeingDragged == null )
            return;
        if ( cardIsInBounds( cardBeingDragged ) ) {
            if ( cardShouldBeAdded( cardBeingDragged ) ) {
                addCard( cardBeingDragged );
                notifyCurrentPlayerIfTheCardIsAnEight( cardBeingDragged );
                view.removeListener( cardBeingDragged );
                // The current player has made a valid move.
                view.getCurrentPlayer().moveIsValid();
                view.getCurrentPlayer().clearCache();
                view.getDeck().clearCache();
            }
        }
    }

    public void notifyCurrentPlayerIfTheCardIsAnEight( Card card ) {
        if ( card.getRank() == 8 )
            view.getCurrentPlayer().playedAnEight();
    }

    private boolean cardShouldBeAdded( Card card ) {
        if ( card.getRank() == 8 || card.getSuit() == getValidSuit() )
            return true;
        if ( getValidRank() != null && card.getRank() == getValidRank() )
            return true;
        return false;
    }
}
