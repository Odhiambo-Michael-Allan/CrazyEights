package com.example.crazyeights;

import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.Iterator;

public class Deck
        implements CrazyEightsView.ViewListener {

    private java.util.ArrayList<Card> deck = new java.util.ArrayList<>();
    private Context application;
    protected int top, left, width, height;
    protected CrazyEightsView view;  // View where this deck will be displayed..
    protected Card selectedCard;
    protected int validSuit, validRank;
    private boolean topCardIsAnEight;

    public Deck( Context application, CrazyEightsView view ) {
        this.application = application;
        this.view = view;
        buildCards();
        shuffleCards();
    }

    public void setValidSuit( int validSuit ) {
        this.validSuit = validSuit;
    }

    public int getValidSuit() {
        return validSuit;
    }

    public Integer getValidRank() {
        if ( topCardIsAnEight )
            return null;
        return validRank;
    }

    public void clearCache() {
        selectedCard = null;
    }

    protected void buildCards() {
        LogUtil.i( "Building cards.." );
        for ( int i = 0; i < Suit.SUITS.length; i++ ) {
            for ( int j = 0; j < Rank.RANKS.length; j++ ) {
                String cardResourceName = String.format( "%sof%s", Rank.RANKS[j].toString(),
                        Suit.SUITS[i].toString() );
                LogUtil.i( String.format( "Card resource name: %s", cardResourceName ) );
                int resourceID = application.getResources().getIdentifier( cardResourceName,
                        "drawable", application.getPackageName() );
                Card currentCard = new Card( this.application, Suit.SUITS[i], Rank.RANKS[j],
                        resourceID );
                addCard( currentCard );
            }
        }
    }

    protected void shuffleCards() {
        LogUtil.i( "Shuffling cards.." );
        java.util.Collections.shuffle( deck, new java.util.Random() );
    }

    public Card getCard( int position ) {
        return deck.get( position );
    }

    public void removeCard( int position ) {
        deck.remove( position );
    }

    public void removeCard( Card card ) {
        deck.remove( card );
    }

    public void draw( Canvas canvas ) {

        this.top = canvas.getHeight()/2-100;
        this.left = canvas.getWidth()/10;

        Iterator i = getIterator();
        int startPosition = this.left;
        int y = this.top;
        Card faceDownCard = new Card( application, Suit.CLUBS, Rank.ACE, R.drawable.facedowncard );
        while ( i.hasNext() ) {
            // DO NOT FORGET TO REMOVE THE CARD OTHERWISE THE LOOP WILL RUN FOREVER!!!
            Card currentCard = ( Card ) i.next();
            /**
             * The card's top and left position is set when it is first drawn and updated every time
             * it is drawn. In this instance however, the card that is drawn is the face down card
             * so the cards in the deck do not have a top and left position. We need to set them
             * manually..
             */
            currentCard.setTop( y );
            currentCard.setLeft( startPosition );
            faceDownCard.draw( canvas, startPosition, y );
            startPosition += 1;
        }
    }

    public boolean touchIsInBounds( int x, int y ) {
        Card card = getTopCard();
        LogUtil.i( "Checking bounds.." );
        return card != null && card.isTouched( x, y );
    }

    public Card getTopCard() {
        if ( deck.size() >= 1 )
            return deck.get( deck.size() - 1 );
        return null;
    }

    public Iterator getIterator() {
        return deck.iterator();
    }

    public void addCard( Card card ) {
        deck.add( card );
        // In the discard pile, when the first card is added, the valid suit and rank will be
        // initialized with the rank and suit of that card..
        validSuit = card.getSuit();
        validRank = card.getRank();
        if ( card.getRank() == 8 )
            topCardIsAnEight = true;
        else
            topCardIsAnEight = false;
        LogUtil.i( String.format( "ADDING CARD %S. DECK SIZE: %d", card, deck.size() ) );
        LogUtil.i( String.format( "VALID SUIT IS NOW %S. VALID RANK IS NOW: %S",
                card.getSuit(), card.getRank() ) );
    }

    public int getTop() {
        return top;
    }

    public int getLeft() {
        return left;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean cardIsInBounds( Card card ) {
        return false;
    }

    public Card getSelectedCard() {
        return selectedCard;
    }

    public boolean isEmpty() {
        return deck.size() == 0;
    }

    public int getSize() {
        return deck.size();
    }

    private boolean currentPlayerHasAPlayableCard() {
        Player currentPlayer = view.getCurrentPlayer();
        int validSuit = view.getDiscardPile().getValidSuit();
        Integer validRank = view.getDiscardPile().getValidRank();
        return currentPlayer.getCardToPlayBasedOn( validSuit, validRank ) != null;
    }

    @Override
    public void onDraw( Canvas canvas ) {
        draw( canvas );
    }

    @Override
    public void onMotionDown( MotionEvent event ) {
        if ( !touchIsInBounds( ( int ) event.getX(), ( int ) event.getY() ) )
            return;
        if ( currentPlayerHasAPlayableCard() ) {
            Toast.makeText( application, "You have a playable card", Toast.LENGTH_SHORT ).show();
            return;
        }
        selectedCard = getTopCard();
        removeCard( selectedCard );
        view.addListener( selectedCard );
    }

    @Override
    public void onMotionMove( MotionEvent event ) {
    }

    @Override
    public void onMotionUp( MotionEvent event ) {
        if ( selectedCard != null ) { // Add the card to the human player's hand..
            view.getCurrentPlayer().dealCard( selectedCard );
            view.removeListener( selectedCard );  // DO NOT FORGET TO REMOVE IT. CREATED A BUG THAT
                                                  // HAUNTED ME!!!!!!!!!!!!!
        }
        selectedCard = null;
        if ( isEmpty() )
            view.deckIsEmpty();
    }
}
