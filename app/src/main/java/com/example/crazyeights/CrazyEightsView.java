package com.example.crazyeights;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;



import android.os.Handler;
import android.os.Message;
import android.os.Looper;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;


import java.util.ArrayList;
import java.util.Iterator;


public class CrazyEightsView extends View {

    private Context application;
    private Player humanPlayer, computer;
    private Deck deck, tempDeck, discardPile;
    private float scale;
    private Canvas canvas;
    private Player currentPlayer;
    private Paint paint = new Paint();
    private final ArrayList<ViewListener> listeners = new ArrayList<>();
    private boolean boardIsDisabled;

    private Card cardBeingMovedFromTheDiscardPileToTheDeck;


    public CrazyEightsView( Context context ) {
        super( context );
        this.application = context;
        scale = application.getResources().getDisplayMetrics().density;
        setBackgroundColor( Color.WHITE );
        initializePaint();
        initializeDeck();
    }


    public void endHand() {
        boardIsDisabled = true;
        showEndHandDialog();
    }

    private void showEndHandDialog() {
        final Dialog endHandDialog = new Dialog( application );
        endHandDialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
        endHandDialog.setContentView( R.layout.endhanddialog );
        endHandDialog.setCanceledOnTouchOutside( false );
        TextView resultsTextView = endHandDialog.findViewById( R.id.resultTextView );
        if ( currentPlayer.equals( humanPlayer ) )
            resultsTextView.setText( "YOU WON!!!" );
        else
            resultsTextView.setText( "SORRY, YOU LOST" );
        endHandDialog.show();
    }

    public void addListener( ViewListener listener ) {
        listeners.add( listener );
        LogUtil.i( "ADDING VIEW LISTENER. SIZE: " + listeners.size() );
    }

    public String getSuitNameBasedOn( int value ) {
        switch ( value ) {
            case 0 :
                return "DIAMONDS";
            case 1 :
                return "HEARTS";
            case 2 :
                return "SPADES";
            case 3 :
                return "CLUBS";
        }
        return null;
    }

    public Player getCurrentPlayer() {
        return currentPlayer;
    }

    public Card getTopCard() {
        return discardPile.getTopCard();
    }

    public Deck getDiscardPile() {
        return discardPile;
    }

    public Deck getDeck() {
        return deck;
    }

    private void initializePaint() {
        paint.setAntiAlias( true );
        paint.setColor( Color.BLACK );
        paint.setStyle( Paint.Style.FILL );
        paint.setTextSize( scale*20 );
        paint.setTextAlign( Paint.Align.CENTER );
    }

    public void removeListener( ViewListener listener ) {
        listeners.remove( listener );
        LogUtil.i( "REMOVING VIEW LISTENER. SIZE: " + listeners.size() );
    }


    /**
     * Initialization of the deck takes some time so i decided to do it on a separate thread..
     */
    public void initializeDeck() {
        Runnable runnable = () -> {
            tempDeck = new Deck( application, this );
            LogUtil.i( "Deck is ready.." );
            deckInitializationHandler.sendEmptyMessage( 0 );
        };
        new Thread( runnable ).start();
    }


    protected void onDraw( Canvas canvas ) {
        super.onDraw( canvas );
        this.canvas = canvas;
        if ( deck == null ) {
            LogUtil.i( "CANVAS WIDTH: %d, CANVAS HEIGHT: %d", canvas.getWidth(), canvas.getHeight() );
            displayWaitingScreen();
            return;
        }
        notifyListenersCanvasIsBeingDrawn( canvas );
    }

    private void displayWaitingScreen() {
        Bitmap cardImage = BitmapFactory.decodeResource( application.getResources(),
                R.drawable.loadingscreenlogo );
        int scaledCW = cardImage.getWidth() / 8 ;
        int scaledCH = cardImage.getHeight() / 8;
        cardImage = Bitmap.createScaledBitmap( cardImage, scaledCW, scaledCH, false );
        canvas.drawBitmap( cardImage, 150, 800, null );
        canvas.drawText( "Loading..", 550, 1400, paint );
    }

    private void notifyListenersCanvasIsBeingDrawn( Canvas canvas ) {
        LogUtil.i( "NOTIFYING LISTENERS CANVAS BEING REDRAWN. " );
        ViewListener[] viewListeners = new ViewListener[ listeners.size() ];
        listeners.toArray( viewListeners );
        for ( int i = 0; i < viewListeners.length; i++ ) {
            viewListeners[i].onDraw( canvas );
        }
    }


    private void initializePlayers() {
        humanPlayer = new HumanPlayer( "Michael", application, this );
        addListener( humanPlayer );
        computer = new ComputerPlayer( "Dell Optiplex", application, this );
        addListener( computer );
        currentPlayer = humanPlayer;
        registerListenersWithBothPlayers();
    }

    private void registerListenersWithBothPlayers() {
        humanPlayer.register( new Player.PlayerListener() {
            @Override
            public void cardIsSelected() {
                LogUtil.i( "CARD SELECTED NOTIFICATION.." );
                Card cardSelected = humanPlayer.getSelectedCard();
                addListener( cardSelected );
            }
            @Override
            public void donePlaying() {
                switchTurns();
            }
        } );
        computer.register( new Player.PlayerListener() {
            @Override
            public void cardIsSelected(){
                // Do nothing. Computer will never select a card..
            }

            @Override
            public void donePlaying() {
                switchTurns();
            }
        } );
    }

    private void switchTurns() {
        LogUtil.i( "SWITCHING TURNS.." );
        if ( currentPlayer.equals( humanPlayer ) ) {
            boardIsDisabled = true;
            currentPlayer = computer;
        }
        else {
            boardIsDisabled = false;
            currentPlayer = humanPlayer;
        }
        currentPlayer.play();
    }

    private void dealCards() {
        dealCardsToBothPlayers();
        initializeTheDiscardPile();
    }

    private void dealCardsToBothPlayers() {
        // Deal seven cards to each player...
        for ( int i = 0; i < 7; i++ ) {
            humanPlayer.dealCard( deck.getCard( i ) );
            deck.removeCard( i );
            computer.dealCard( deck.getCard( i ) );
            deck.removeCard( i );
        }
    }

    // ---------------------------------------------------------------------------------
    public void deckIsEmpty() {
        boardIsDisabled = true;
        moveCardsFromTheDiscardPileToTheDeck();
    }

    private void moveCardsFromTheDiscardPileToTheDeck() {
        if ( discardPile.isEmpty() ) {
            if ( currentPlayer.equals( humanPlayer ) )
                boardIsDisabled = false;
            deck.shuffleCards();  // We're done moving the cards..
            initializeTheDiscardPile();
            this.invalidate();
            currentPlayer.play();
            return;
        }
        cardBeingMovedFromTheDiscardPileToTheDeck = discardPile.getTopCard();
        discardPile.removeCard( cardBeingMovedFromTheDiscardPileToTheDeck );
        addListener( cardBeingMovedFromTheDiscardPileToTheDeck );
        animateCard( cardBeingMovedFromTheDiscardPileToTheDeck );
    }

    private void animateCard( Card card ) {
        Runnable runnable = () -> {
            int startX = card.getLeft();
            int destX = deck.getLeft();
            while ( startX > destX ) {
                startX -= 10;
                card.setLeft( startX );
                this.invalidate();
                sleep();
            }
            doneMovingCardToDeckHandler.sendEmptyMessage( 0 );
        };
        new Thread( runnable ).start();
    }

    private void sleep() {
        try {
            Thread.sleep( 10 );
        } catch ( InterruptedException e ) {}
    }

    private Handler doneMovingCardToDeckHandler = new Handler( Looper.getMainLooper() ) {
        @Override
        public void handleMessage( Message message ) {
            removeListener( cardBeingMovedFromTheDiscardPileToTheDeck );
            deck.addCard( cardBeingMovedFromTheDiscardPileToTheDeck );
            cardBeingMovedFromTheDiscardPileToTheDeck = null;
            moveCardsFromTheDiscardPileToTheDeck();
        }
    };
    // ---------------------------------------------------------------------------------

    private void initializeTheDiscardPile() {
        // Add the top card on the deck to the discard pile. If the top card is an eight,
        // return it into the deck and shuffle it..
        Card topCard = deck.getCard( 0 );
        while ( topCard.getRank() == 8 ) {
            deck.shuffleCards();
            topCard = deck.getCard( 0 );
        }
        deck.removeCard( topCard );
        discardPile.addCard( topCard );
    }

    public boolean onTouchEvent( MotionEvent event ) {
        if (boardIsDisabled)
            return true;
        int eventAction = event.getAction();
        switch ( eventAction ) {
            case MotionEvent.ACTION_DOWN :
                notifyListenersOfActionDownEvent( event );
                break;
            case MotionEvent.ACTION_MOVE :
                notifyListenerOfActionMoveEvent( event );
                invalidate();
                break;
            case MotionEvent.ACTION_UP :
                // If a card was being dragged, it should be placed into the human's hand.
                notifyListenersOfActionUpEvent( event );
                invalidate();
                break;
        }
        invalidate();
        return true;
    }

    private void notifyListenersOfActionDownEvent( MotionEvent event ) {
        LogUtil.i( "NOTIFYING LISTENERS OF ACTION DOWN EVENT: ", event.getAction() );
        // This is done to avoid java.util.ConcurrentModificationException..
        ViewListener[] viewListeners = new ViewListener[ listeners.size() ];
        listeners.toArray( viewListeners );
        for ( int i = 0; i < viewListeners.length; i++ ) {
            viewListeners[i].onMotionDown( event );
        }
    }

    private void notifyListenerOfActionMoveEvent( MotionEvent event ) {
        LogUtil.i( "NOTIFYING LISTENERS OF ACTION MOVE EVENT: ", event.getAction() );
        ViewListener[] viewListeners = new ViewListener[ listeners.size() ];
        listeners.toArray( viewListeners );
        for ( int i = 0; i < viewListeners.length; i++ ) {
            viewListeners[i].onMotionMove( event );
        }
    }

    private void notifyListenersOfActionUpEvent( MotionEvent event ) {
        LogUtil.i( "NOTIFYING LISTENERS OF ACTION UP EVENT: ", event.getAction() );
        // This is done to avoid java.util.ConcurrentModificationException..
        ViewListener[] viewListeners = new ViewListener[ listeners.size() ];
        listeners.toArray( viewListeners );
        for ( int i = 0; i < viewListeners.length; i++ ) {
            viewListeners[i].onMotionUp( event );
        }
    }


    /**
     * This handler will listen for messages from the initialization thread.
     * After the initialization thread is done, it will notify this handler
     * which will then do the remaining initialization tasks..
     */
    Handler deckInitializationHandler = new Handler( Looper.getMainLooper() ) {
        @Override
        public void handleMessage( Message message ) {
            deck = tempDeck;
            addListener( deck );
            discardPile = new DiscardPile( application, CrazyEightsView.this );
            addListener( discardPile );
            initializePlayers();
            dealCards();
            invalidate();
            Toast.makeText( application, "Its your turn", Toast.LENGTH_SHORT ).show();
        }
    };

    Handler discardToDeckHandler = new Handler( Looper.getMainLooper() ) {
        @Override
        public void handleMessage( Message message ) {
            Toast.makeText( application, "Done moving cards from the discard " +
                    "pile to the deck. You can now move cards", Toast.LENGTH_LONG ).show();
            deck.shuffleCards();
            Card card = deck.getTopCard();
            discardPile.addCard( card );
            deck.removeCard( card );
            invalidate();
        }
    };


    /**
     * Components that are interested in events generated by the view will implement this
     * interface..
     */
    public interface ViewListener {
        void onDraw( Canvas canvas );
        void onMotionDown( MotionEvent event );
        void onMotionMove( MotionEvent event );
        void onMotionUp( MotionEvent event );
    }

}
