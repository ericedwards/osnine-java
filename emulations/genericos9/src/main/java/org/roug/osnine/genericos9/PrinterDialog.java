package org.roug.osnine.genericos9;

import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.IndexColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrinterDialog {

    private static final Logger LOGGER =
                    LoggerFactory.getLogger(PrinterDialog.class);

    private String newline = "\n";
    private JDialog printerDialog;
    private JTextPane textPane;

    private StringBuilder textBuffer = new StringBuilder(80);

    public PrinterDialog(JFrame parent) {
        printerDialog = new JDialog(parent, "Printer", false);
        printerDialog.setLayout(new BorderLayout());

        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout());

        printerDialog.add(buttonPane, BorderLayout.PAGE_START);

        JButton button = new JButton("Print");
        button.addActionListener(new PrintAction());
        buttonPane.add(button);

        button = new JButton("Clear");
        button.addActionListener(new ClearAction());
        buttonPane.add(button);

        button = new JButton("Close");
        button.addActionListener(new CloseAction());
        buttonPane.add(button);

        textPane = createTextPane();
        JScrollPane paneScrollPane = new JScrollPane(textPane);
        paneScrollPane.setVerticalScrollBarPolicy(
                        JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        paneScrollPane.setPreferredSize(new Dimension(600, 700));
        paneScrollPane.setMinimumSize(new Dimension(100, 100));

        printerDialog.add(paneScrollPane, BorderLayout.CENTER);

        printerDialog.pack();

    }

    private JTextPane createTextPane() {

        JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        Font currFont = textPane.getFont();
        textPane.setFont(new Font("monospaced", currFont.getStyle(), currFont.getSize()));

        StyledDocument doc = textPane.getStyledDocument();
        addStylesToDocument(doc);

        return textPane;
    }

    private void addStylesToDocument(StyledDocument doc) {
        //Initialize some styles.
        Style def = StyleContext.getDefaultStyleContext().
                        getStyle(StyleContext.DEFAULT_STYLE);

        Style monospace = doc.addStyle("monospace", def);
        //StyleConstants.setFontFamily(monospace, "monospaced");

    }

    private void printSegment(String segment) {
        StyledDocument doc = textPane.getStyledDocument();
        try {
            doc.insertString(doc.getLength(), segment, null);
        } catch (BadLocationException ble) {
            LOGGER.error("Couldn't insert text into text pane.", ble);
        }
        textPane.repaint();
    }


    /**
     * Print a byte to the text pane. Also executes printer control sequences.
     *
     * @param value the character received from the PIA.
     */
    public void printOneByte(int value) {
        if (value == 0x0A) {
            textBuffer.append('\n');
            printSegment(textBuffer.toString());
            textBuffer = new StringBuilder(80);
        } else {
            textBuffer.append((char) (value & 0x7F)); // Convert to char
        }
    }

    public void setVisible(boolean visible) {
        printerDialog.setVisible(visible);
    }

    private class PrintAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            Paper paper = new Paper();
            paper.setSize(595.0, 842.0); // A4
            paper.setImageableArea(9.0, 48.0, 576.0, 746.0);
            PageFormat pageFormat = new PageFormat();
            pageFormat.setPaper(paper);
            PrinterJob pj = PrinterJob.getPrinterJob();
            pj.setPrintable(textPane.getPrintable(null, null), pageFormat);
            PageFormat pf = pj.pageDialog(pageFormat);
//          if (pj.printDialog()) {
                try {
                    pj.print();
                } catch (Exception pex) {
                    JOptionPane.showMessageDialog(null, "Error while printing");
                    LOGGER.error("Couldn't print text pane", pex);
                }
//          }

/*
            try {
                textPane.print();
            } catch (Exception pex) {
                JOptionPane.showMessageDialog(null, "Error while printing");
                LOGGER.error("Couldn't print text pane", pex);
            }
*/
        }
    }

    private class ClearAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                StyledDocument doc = textPane.getStyledDocument();
                doc.remove(0, doc.getLength());
            } catch (BadLocationException ble) {
                LOGGER.error("Couldn't clear text pane", ble);
            }
        }
    }

    private class CloseAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            setVisible(false);
        }
    }

}