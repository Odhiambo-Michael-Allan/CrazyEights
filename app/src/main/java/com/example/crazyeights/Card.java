package com.example.crazyeights;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.os.Parcelable;
import android.view.MotionEvent;

public class Card implements CrazyEightsView.ViewListener {

    private Suit suit;
    private Rank rank;
    private Bitmap cardImage;
    private Bitmap scaledCardImage;
    private int scaledCardWidth, scaledCardHeight;
    private int resourceID;
    private float scale;
    private int top, left;
    protected Context application;


    public Card( Context application, Suit suit, Rank rank, int resourceID ) {
        this.application = application;
        this.resourceID = resourceID;
        this.suit = suit;
        this.rank = rank;
        scale = application.getResources().getDisplayMetrics().density;
        constructCardImage();
    }

    private void constructCardImage() {
        cardImage = BitmapFactory.decodeResource( this.application.getResources(), resourceID );
        scaledCardWidth = cardImage.getWidth() / 8;
        scaledCardHeight = cardImage.getHeight() / 8;
        scaledCardImage = Bitmap.createScaledBitmap( cardImage, scaledCardWidth, scaledCardHeight,
                false );
    }

    public void draw( Canvas drawingArea, int x, int y ) {
        setTop( y );
        setLeft( x );
        drawingArea.drawBitmap( scaledCardImage, x, y, null );
    }

    public int getTop() {
        return this.top;
    }

    public int getLeft() {
        return this.left;
    }

    public double getWidth() {
        return scaledCardWidth;
    }

    public double getHeight() {
        return scaledCardHeight;
    }

    public boolean isTouched( int x, int y ) {
        if ( x >= getLeft() && x < getWidth()+getLeft() && y >= getTop() && y <= getHeight()+getTop() )
            return true;
        return false;
    }

    public String toString() {
        return rank.toString() + suit.toString();
    }

    public void setTop( int top ) {
        this.top = top;
    }

    public void setLeft( int left ) {
        this.left = left;
    }

    public int getSuit() {
        return suit.getSuit();
    }

    public int getRank() {
        return rank.getRank();
    }

    /**
     * This method is called when this card is being dragged..
     */
    @Override
    public void onDraw( Canvas canvas ) {
        draw( canvas, this.left, this.top );
        LogUtil.i( String.format( "DRAGGED CARD \"%S\" RECEIVED A NOTIFICATION TO DRAW ITSELF..", this ) );
    }

    @Override
    public void onMotionDown( MotionEvent event ) {
        // Do nothing here..
    }

    @Override
    public void onMotionMove( MotionEvent event ) {
        // If this card is being dragged across the screen, then update it coordinates..
        int left = ( int ) event.getX();
        int top = ( int ) event.getY();
        setTop( top-200 );
        setLeft( left-200 );
    }

    @Override
    public void onMotionUp( MotionEvent event ) {
    }

}
