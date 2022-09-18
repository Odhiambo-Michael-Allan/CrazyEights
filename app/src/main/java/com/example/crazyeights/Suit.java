package com.example.crazyeights;

// Typesafe Enum..

public final class Suit {

    // Statically define all valid values of Suit
    public static final Suit DIAMONDS = new Suit( 0, "diamonds" );
    public static final Suit HEARTS = new Suit( 1, "hearts" );
    public static final Suit SPADES = new Suit( 2, "spades" );
    public static final Suit CLUBS = new Suit( 3, "clubs" );

    // Helps to iterate over the enum values..
    public static final Suit[] SUITS = { DIAMONDS, HEARTS, SPADES, CLUBS };

    private final int suit;
    private final String name;

    // Do not allow instantiation by outside objects..
    private Suit( int suitValue, String name ) {
        this.suit = suitValue;
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    public int getSuit() {
        return suit;
    }
}
