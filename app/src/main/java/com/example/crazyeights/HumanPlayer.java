package com.example.crazyeights;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import java.util.Iterator;

public class HumanPlayer extends Player
        implements CrazyEightsView.ViewListener {

    private int suitChoice;
    private boolean changingSuit;

    public HumanPlayer( String name, Context application, CrazyEightsView view ) {
        super( name, application, view );
    }

    /**
     * The HumanPlayer overrides the drawHand method to simply draw the player's hand..
     */
    @Override
    public void drawHand( Canvas canvas, int x, int y ) {
        hand.draw( canvas, x, y );
    }

    @Override
    public Card getCardTouched( int x, int y ) {
        int positionInCard = hand.size()-1;
        for ( int i = positionInCard; i >= 0; i-- ) {
            Card card = hand.getCard( i );
            if ( card.isTouched( x, y ) )
                return card;
        }
        return null;
    }

    @Override
    public void play() {
    }

    @Override
    public void onDraw( Canvas canvas ) {
        drawHand( canvas, 100, canvas.getHeight() - 700 );
    }

    @Override
    public void onMotionDown( MotionEvent event ) {
        int xPositionOfTouchLocation = ( int ) event.getX();
        int yPositionOfTouchLocation = ( int ) event.getY();
        selectedCard = getCardTouched( xPositionOfTouchLocation, yPositionOfTouchLocation );
        if ( selectedCard == null )
            return;
        LogUtil.i( "Card selected: " + selectedCard );
        setCardSelected( selectedCard );
        notifyListenersCardIsSelected();
        removeCard( selectedCard );
    }

    private void notifyListenersCardIsSelected() {
        Iterator i = listeners.iterator();
        while ( i.hasNext() ) {
            PlayerListener listener = ( PlayerListener ) i.next();
            listener.cardIsSelected();
        }
    }

    @Override
    public void playedAnEight() {
        showChangeSuitDialog();
        view.getDiscardPile().setValidSuit( suitChoice );
    }

    private void showChangeSuitDialog() {
        changingSuit = true;
        final Dialog changeSuitDialog = new Dialog( application );
        changeSuitDialog.requestWindowFeature( Window.FEATURE_NO_TITLE );
        changeSuitDialog.setContentView( R.layout.choose_suit_dialog );
        final Spinner spinner = changeSuitDialog.findViewById( R.id.suitSpinner );
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource( application,
                R.array.suits, android.R.layout.simple_spinner_item );
        adapter.setDropDownViewResource( android.R.layout.simple_spinner_dropdown_item );
        spinner.setAdapter( adapter );
        Button okButton = changeSuitDialog.findViewById( R.id.okButton );
        okButton.setOnClickListener( theView -> {
            suitChoice = ( spinner.getSelectedItemPosition() );
            LogUtil.i( "Selected item position: %d", suitChoice );
            changeSuitDialog.dismiss();
            Toast.makeText( application, String.format( "You've chosen: %s",
                            view.getSuitNameBasedOn( suitChoice ) ), Toast.LENGTH_SHORT )
                    .show();
            view.getDiscardPile().setValidSuit( suitChoice );
            cleanUp();
        } );
        changeSuitDialog.setCanceledOnTouchOutside( false );  // The dialog is only dismissed by
                                                              // the "ok" button..
        changeSuitDialog.show();
    }

    @Override
    public void onMotionMove( MotionEvent event ) {
    }

    @Override
    public void onMotionUp( MotionEvent event ) {
        if ( changingSuit )
            return;
        cleanUp();
    }

    private void cleanUp() {
        if ( madeAValidMove ) {
            if ( hand.size() < 1 ) {
                LogUtil.i( "ENDING HUMAN'S HAND.." );
                view.endHand();
            }
            else
                donePlaying();
        }
        else if ( selectedCard != null ){
            dealCard( selectedCard );
            view.removeListener( selectedCard );
        }
        madeAValidMove = false;
        selectedCard = null;
        changingSuit = false;
        LogUtil.i( String.format( "HUMAN HAND SIZE: " + hand.size() ) );
    }
}
