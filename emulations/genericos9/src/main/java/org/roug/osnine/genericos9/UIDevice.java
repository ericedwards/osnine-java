package org.roug.osnine.genericos9;

import java.io.IOException;
import javax.swing.text.BadLocationException;

interface UIDevice {

    void sendToUI(int val) throws BadLocationException, IOException;

}