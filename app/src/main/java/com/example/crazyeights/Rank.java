package com.example.crazyeights;

public final class Rank {

    public static final Rank TWO = new Rank( 2, "two" );
    public static final Rank THREE = new Rank( 3, "three" );
    public static final Rank FOUR = new Rank( 4, "four" );
    public static final Rank FIVE = new Rank( 5, "five" );
    public static final Rank SIX = new Rank( 6, "six" );
    public static final Rank SEVEN = new Rank( 7, "seven" );
    public static final Rank EIGHT = new Rank( 8, "eight" );
    public static final Rank NINE = new Rank( 9, "nine" );
    public static final Rank TEN = new Rank( 10, "ten" );
    public static final Rank JACK = new Rank( 11, "jack" );
    public static final Rank QUEEN = new Rank( 12, "queen" );
    public static final Rank KING = new Rank( 13, "king" );
    public static final Rank ACE = new Rank( 14, "ace" );

    public static final Rank[] RANKS = { TWO, THREE, FOUR, FIVE, SIX, SEVEN,
            EIGHT, NINE, TEN, JACK, QUEEN, KING, ACE };

    private final int rank;
    private final String name;

    private Rank( int rank, String name ) {
        this.rank = rank;
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public int getRank() {
        return this.rank;
    }

    public String toString() {
        return name;
    }
}
