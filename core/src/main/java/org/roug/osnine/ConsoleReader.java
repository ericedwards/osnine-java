package org.roug.osnine;

import java.io.InputStream;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Thread to listen to incoming data.
 */
class ConsoleReader implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsoleReader.class);

    private Acia acia;

    ConsoleReader(Acia acia) {
        this.acia = acia;
    }

    public void run() {
        LOGGER.debug("Reader thread started");
        while (true) {
            try {
                int receiveData = System.in.read();
                if (receiveData == -1) receiveData = 0x1B;
                if (receiveData == 10) receiveData = 13;
                LOGGER.debug("Received {}", receiveData);
                while (acia.isReceiveRegisterFull()) {
                    Thread.yield();
                }
                acia.dataReceived(receiveData);
            } catch (Exception e) {
                System.exit(2);
            }
        }
    }
}

