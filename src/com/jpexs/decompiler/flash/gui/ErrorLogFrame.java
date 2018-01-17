/*
 *  Copyright (C) 2010-2018 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.helpers.Helper;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

/**
 *
 * @author JPEXS
 */
public class ErrorLogFrame extends AppFrame {

    private static ErrorLogFrame instance;

    private final JPanel logView = new JPanel();

    private final JPanel logViewInner = new JPanel();

    private final Handler handler;

    private final ImageIcon expandIcon;

    private final ImageIcon collapseIcon;

    private ErrorState errorState = ErrorState.NO_ERROR;

    private static final int MAX_LOG_ITEM_COUNT = 100;

    private final AtomicInteger logItemCount = new AtomicInteger();

    public Handler getHandler() {
        return handler;
    }

    public static boolean hasInstance() {
        return instance != null;
    }

    public static ErrorLogFrame getInstance() {
        if (instance == null) {
            instance = new ErrorLogFrame();
            Logger logger = Logger.getLogger("");
            logger.addHandler(instance.getHandler());
        }

        return instance;
    }

    public static ErrorLogFrame createNewInstance() {
        if (instance != null) {
            Logger logger = Logger.getLogger("");
            logger.removeHandler(instance.getHandler());
            instance.setVisible(false);
            instance.dispose();
            instance = null;
        }

        return getInstance();
    }

    public ErrorState getErrorState() {
        return errorState;
    }

    public ErrorLogFrame() {
        setTitle(translate("dialog.title"));
        setSize(700, 400);
        setBackground(Color.white);
        View.centerScreen(this);
        View.setWindowIcon(this);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
        Container cnt = getContentPane();
        cnt.setLayout(new BorderLayout());
        logView.setBackground(Color.white);
        logView.setLayout(new BorderLayout());
        cnt.setBackground(Color.white);

        logViewInner.setLayout(new BoxLayout(logViewInner, BoxLayout.Y_AXIS));
        logView.add(logViewInner, BorderLayout.NORTH);
        JPanel buttonsPanel = new JPanel(new FlowLayout());
        JButton clearButton = new JButton(translate("clear"));
        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                clearLog();
            }
        });
        buttonsPanel.add(clearButton);

        expandIcon = View.getIcon("expand16");
        collapseIcon = View.getIcon("collapse16");
        cnt.add(buttonsPanel, BorderLayout.SOUTH);

        cnt.add(new JScrollPane(logView), BorderLayout.CENTER);
        handler = new Handler() {
            SimpleFormatter formatter = new SimpleFormatter();

            @Override
            public void publish(LogRecord record) {
                if (getLevel().intValue() <= record.getLevel().intValue()) {
                    log(record.getLevel(), formatter.formatMessage(record), record.getThrown());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() throws SecurityException {
            }
        };
        handler.setLevel(Level.WARNING);
    }

    public void clearLog() {
        logViewInner.removeAll();
        logItemCount.set(0);
        Main.clearLogFile();
        revalidate();
        repaint();
    }

    public void clearErrorState() {
        errorState = ErrorState.NO_ERROR;
        MainFrame mainFrame = Main.getMainFrame();
        if (mainFrame != null) {
            mainFrame.getPanel().setErrorState(errorState);
        }
    }

    private void notifyMainFrame(Level level) {
        boolean stateChanged = false;
        if (level.intValue() >= Level.SEVERE.intValue()) {
            if (errorState != ErrorState.ERROR) {
                errorState = ErrorState.ERROR;
                stateChanged = true;
            }
        } else if (level.intValue() >= Level.WARNING.intValue()) {
            if (errorState != ErrorState.ERROR && errorState != ErrorState.WARNING) {
                errorState = ErrorState.WARNING;
                stateChanged = true;
            }
        } else if (level.intValue() >= Level.INFO.intValue()) {
            if (errorState == ErrorState.NO_ERROR) {
                errorState = ErrorState.INFO;
                stateChanged = true;
            }
        }
        if (stateChanged) {
            MainFrame mainFrame = Main.getMainFrame();
            if (mainFrame != null) {
                mainFrame.getPanel().setErrorState(errorState);
            }
        }
    }

    private void log(final Level level, final String msg, final String detail) {
        if (logItemCount.getAndIncrement() < MAX_LOG_ITEM_COUNT) {
            View.execInEventDispatchLater(() -> {
                notifyMainFrame(level);

                JPanel pan = new JPanel();
                pan.setBackground(Color.white);
                pan.setLayout(new BoxLayout(pan, BoxLayout.Y_AXIS));

                JComponent detailComponent;
                if (detail == null) {
                    detailComponent = null;
                } else {
                    final JTextArea detailTextArea = new JTextArea(detail);
                    detailTextArea.setEditable(false);
                    detailTextArea.setOpaque(false);
                    detailTextArea.setFont(new JLabel().getFont());
                    detailTextArea.setBackground(Color.white);
                    detailComponent = detailTextArea;
                }
                JPanel header = new JPanel();
                header.setLayout(new BoxLayout(header, BoxLayout.X_AXIS));
                header.setBackground(Color.white);

                SimpleDateFormat format = new SimpleDateFormat("dd/MM/YYYY HH:mm:ss");
                final String dateStr = format.format(new Date());

                JToggleButton copyButton = new JToggleButton(View.getIcon("copy16"));
                copyButton.setFocusPainted(false);
                copyButton.setBorderPainted(false);
                copyButton.setFocusable(false);
                copyButton.setContentAreaFilled(false);
                copyButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                copyButton.setMargin(new Insets(2, 2, 2, 2));
                copyButton.setToolTipText(translate("copy"));
                copyButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                        StringSelection stringSelection = new StringSelection(dateStr + " " + level.toString() + " " + msg + "\r\n" + detail);
                        clipboard.setContents(stringSelection, null);
                    }
                });

                final JToggleButton expandButton = new JToggleButton(collapseIcon);
                expandButton.setFocusPainted(false);
                expandButton.setBorderPainted(false);
                expandButton.setFocusable(false);
                expandButton.setContentAreaFilled(false);
                expandButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                expandButton.setMargin(new Insets(2, 2, 2, 2));
                expandButton.setToolTipText(translate("details"));

                final JScrollPane scrollPane;
                if (detailComponent != null) {
                    scrollPane = new JScrollPane(detailComponent);
                    scrollPane.setAlignmentX(0f);
                } else {
                    scrollPane = null;
                }

                if (detailComponent != null) {
                    expandButton.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            expandButton.setIcon(expandButton.isSelected() ? expandIcon : collapseIcon);
                            scrollPane.setVisible(expandButton.isSelected());
                            revalidate();
                            repaint();
                        }
                    });
                }

                if (detailComponent != null) {
                    header.add(expandButton);
                }
                JLabel dateLabel = new JLabel(dateStr);
                dateLabel.setPreferredSize(new Dimension(200, (int) dateLabel.getPreferredSize().getHeight()));
                dateLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                header.add(dateLabel);

                JLabel levelLabel = new JLabel(level.getName());
                levelLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                header.add(levelLabel);
                JTextArea msgLabel = new JTextArea(msg);
                msgLabel.setEditable(false);
                msgLabel.setOpaque(false);
                msgLabel.setFont(levelLabel.getFont());

                msgLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                header.add(msgLabel);
                header.setAlignmentX(0f);

                header.add(copyButton);
                pan.add(header);
                if (detailComponent != null) {
                    pan.add(scrollPane);
                    scrollPane.setVisible(false);
                }
                pan.setAlignmentX(0f);
                logViewInner.add(pan);
                revalidate();
                repaint();
            });
        }
    }

    public void log(Level level, String msg) {
        log(level, msg, (String) null);
    }

    public void log(Level level, String msg, Throwable ex) {
        StringWriter sw = new StringWriter();
        if (ex != null) {
            ex.printStackTrace(new PrintWriter(sw));
        }
        log(level, msg, sw.toString());
    }

    @Override
    public void dispose() {
        removeAll();
        Helper.emptyObject(this);
        super.dispose();
    }
}
