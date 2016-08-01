package org.roug.osnine;

/**
 * Register interface.
 */
public interface Register {

    public static final int WIDTH = 8;

    int intValue();

    public int get();

    public void set(int newValue);

    public int getSigned();

    /**
     * Bit test.
     */
    int btst(int n);

    void bset(int n);

    void bclr(int n);

}

