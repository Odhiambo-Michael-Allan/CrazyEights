package com.example.crazyeights;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.widget.Toast;

import java.util.Iterator;


public class ComputerPlayer extends Player
        implements CrazyEightsView.ViewListener  {

    private Card cardBeingAnimated, topCardInDeck, faceDownCard;

    public ComputerPlayer( String name, Context application, CrazyEightsView view ) {
        super( name, application, view );
    }

    /**
     * The computer player needs to override the drawhand to draw face down cards since we
     * are not supposed to see his hand..
     */
    @Override
    public void drawHand( Canvas canvas, int x, int y ) {
        Iterator i = hand.getIterator();
        Card faceDownCard = new Card( application, Suit.CLUBS, Rank.ACE, R.drawable.facedowncard );
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
            card.setTop( updatedYPos );
            card.setLeft( updatedXPos );
            LogUtil.i( String.format( "XPOSITION: %d, YPOSITION: %d", card.getLeft(), card.getTop() ) );
            faceDownCard.draw( canvas, updatedXPos, updatedYPos );
            updatedXPos += 100;
            cardNumber++;
        }
    }

    @Override
    public Card getCardTouched( int x, int y ) {
        return null;
    }

    @Override
    public void playedAnEight() {}

    @Override
    public void play() {
        int validSuit = view.getDiscardPile().getValidSuit();
        Integer validRank = view.getDiscardPile().getValidRank();
        cardBeingAnimated = getCardToPlayBasedOn( validSuit, validRank );
        if ( cardBeingAnimated == null ) {
            removeCardsFromDeck();
            return;
        }
        removeCard( cardBeingAnimated );
        view.addListener( cardBeingAnimated );  // To receive onDraw() events..
        animateMovingCardToTheDiscardPile( cardBeingAnimated );
    }

    private void removeCardsFromDeck() {
        if ( view.getDeck().isEmpty() ) {
            view.deckIsEmpty();
            return;
        }
        LogUtil.i( "REQUESTING CARDS FROM THE DECK.." );
        topCardInDeck = view.getDeck().getTopCard();
        view.getDeck().removeCard( topCardInDeck );
        Runnable runnable = () -> {
            faceDownCard = new Card( application, Suit.CLUBS, Rank.ACE, R.drawable.facedowncard );
            faceDownCard.setLeft( topCardInDeck.getLeft() );
            faceDownCard.setTop( topCardInDeck.getTop() );
            view.addListener( faceDownCard );
            int startX = topCardInDeck.getLeft();
            int startY = topCardInDeck.getTop();
            int destX = calculateDestX();
            int destY = calculateDestY();
            topCardInDeck.setLeft( destX );
            topCardInDeck.setTop( destY );
            while ( startX <= destX && startY > destY ) {
                startX += 10;
                startY -= 10;
                faceDownCard.setLeft( startX );
                faceDownCard.setTop( startY );
                view.invalidate();
                sleep();
            }

            while ( startX < destX ) {
                startX += 10;
                faceDownCard.setLeft( startX );
                view.invalidate();
                sleep();
            }

            while ( startY > destY ) {
                startY -= 10;
                faceDownCard.setTop( startY );
                view.invalidate();
                sleep();
            }
            doneRequestingCardHandler.sendEmptyMessage(0 );
        };
        new Thread( runnable ).start();
    }

    private int calculateDestX() {
        int numberOfCardsInHand = hand.size();
        if ( numberOfCardsInHand % 7 == 0 )
            return 100;
        return hand.getCard( hand.size() - 1 ).getLeft() + 100;
    }

    private int calculateDestY() {
        if ( hand.size() % 7 == 0 )
            return hand.getCard( hand.size() - 1 ).getTop() + 100;
        return hand.getCard( hand.size() - 1 ).getTop();
    }

    private void animateMovingCardToTheDiscardPile( Card card ) {
        Runnable runnable = () -> {
            int discardPileXPosition = view.getDiscardPile().getLeft();
            int discardPileYPosition = view.getDiscardPile().getTop();
            int cardXPosition = card.getLeft();
            int cardYPosition = card.getTop();
            while ( cardXPosition < discardPileXPosition && cardYPosition < discardPileYPosition ) {
                card.setLeft( cardXPosition + 5 );
                card.setTop( cardYPosition + 10 );
                cardXPosition += 5;
                cardYPosition += 10;
                view.invalidate();
                sleep();
            }
            while ( cardXPosition < discardPileXPosition ) {
                card.setLeft( cardXPosition + 5 );
                cardXPosition += 5;
                view.invalidate();
                sleep();
            }
            while ( cardYPosition < discardPileYPosition ) {
                card.setTop( cardYPosition + 5 );
                cardYPosition += 5;
                view.invalidate();
                sleep();
            }
            LogUtil.i( "DONE ANIMATING CARD.." );
            donePlayingHandler.sendEmptyMessage( 0 );
        };
        new Thread( runnable ).start();
    }

    private void sleep() {
        try {
            Thread.sleep( 15 );
        } catch ( InterruptedException e ) {

        }
    }

    @Override
    public void onDraw( Canvas canvas ) {
        drawHand( canvas, 100, 200 );

    }

    @Override
    public void onMotionDown( MotionEvent event ) {
    }

    @Override
    public void onMotionMove( MotionEvent event ) {
    }

    @Override
    public void onMotionUp( MotionEvent event ) {
    }

    Handler donePlayingHandler = new Handler( Looper.getMainLooper() ) {
        @Override
        public void handleMessage( Message message ) {
            view.getDiscardPile().addCard( cardBeingAnimated );
            view.removeListener( cardBeingAnimated );
            view.invalidate();
            if ( cardBeingAnimated.getRank() == 8 ) {
                int validSuit = getValidSuitChoice();
                view.getDiscardPile().setValidSuit( validSuit );
                Toast.makeText( application, String.format( "Computer " +
                        "played a wild eight. You must play: %s",
                        view.getSuitNameBasedOn( validSuit ) ), Toast.LENGTH_SHORT ).show();
            }
            else if ( hand.size() < 1 ) {
                LogUtil.i( "ENDING COMPUTER'S HAND.." );
                view.endHand();
            }
            donePlaying();
        }
    };

    Handler doneRequestingCardHandler = new Handler( Looper.getMainLooper() ) {
        @Override
        public void handleMessage( Message message ) {
            LogUtil.i( "DONE REMOVING CARD FROM DECK.." );
            dealCard( topCardInDeck );
            topCardInDeck = null;
            view.removeListener( faceDownCard );
            view.invalidate();
            play();
        }
    };
}
