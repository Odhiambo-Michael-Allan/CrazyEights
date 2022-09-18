package com.example.crazyeights;

import android.content.Context;
import android.graphics.Canvas;
import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;

abstract class Player
        implements CrazyEightsView.ViewListener {

    private String playerName;
    protected Hand hand;
    protected Context application;
    protected int validSuitChoice;
    protected ArrayList<PlayerListener> listeners = new ArrayList<>();
    protected CrazyEightsView view;
    protected boolean madeAValidMove;
    protected Card selectedCard;

    public Player( String playerName, Context application, CrazyEightsView view ) {
        this.playerName = playerName;
        this.application = application;
        this.view = view;
        hand = new Hand();
    }

    public Card getSelectedCard() {
        return selectedCard;
    }


    protected void setCardSelected( Card cardSelected ) {
        selectedCard = cardSelected;
    }

    public void dealCard( Card card ) {
        hand.addCard( card );
    }

    /**
     * Used by the discard pile to inform the player he made a valid move..
     */
    public void moveIsValid() {
        madeAValidMove = true;
    }

    /**
     * Subclasses will override this method to specify how their hand should be drawn..
     */
    abstract public void drawHand( Canvas canvas, int x, int y );

    public String getName() {
        return this.playerName;
    }

    public abstract Card getCardTouched( int x, int y );

    public void removeCard( Card card ) {
        hand.removeCard( card );
    }


    public boolean hasAPlayableCard( Card topCard ) {
        Iterator i = hand.getIterator();
        while ( i.hasNext() ) {
            Card card = ( Card ) i.next();
            if ( card.getSuit() == topCard.getSuit() || card.getRank() == topCard.getRank()
                    || card.getRank() == 8 )
                return true;
        }
        return false;
    }

    /**
     * This method will be used by the computer player to get a card to play. Null will be
     * returned if no card in the hand is playable...
     */
    public Card getCardToPlayBasedOn( int validSuit, Integer validRank ) {
        Iterator i = hand.getIterator();
        while ( i.hasNext() ) {
            Card card = ( Card ) i.next();
            if ( card.getRank() == 8 ||  card.getSuit() == validSuit  )
                return card;
            if ( validRank == null )
                continue;  // Don't check the rank..
            if ( card.getRank() == validRank )
                return card;
        }
        return null;
    }

    public void clearCache() {
        selectedCard = null;
    }

    public abstract void playedAnEight();

    public int getValidSuitChoice() {
        return validSuitChoice;
    }

    public void setValidSuitChoice() {
        validSuitChoice = 0;
    }

    public void register( PlayerListener listener ) {
        listeners.add( listener );
    }

    public void donePlaying() {
        LogUtil.i( "CURRENT PLAYER IS DONE PLAYING.." );
        notifyListenersDonePlaying();
    }

    protected void notifyListenersDonePlaying() {
        Iterator i = listeners.iterator();
        while ( i.hasNext() ) {
            PlayerListener listener = ( PlayerListener ) i.next();
            listener.donePlaying();
        }
    }

    /**
     * Subclasses will override this method to specify how they play..
     */
    public abstract void play();

    public interface PlayerListener {
        void cardIsSelected();
        void donePlaying();
    }
}
