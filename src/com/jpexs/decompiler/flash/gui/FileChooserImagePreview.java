package com.jpexs.decompiler.flash.gui;

import javax.swing.*;
import java.beans.*;
import java.awt.*;
import java.io.File;
import javax.swing.border.Border;

public class FileChooserImagePreview extends JComponent
        implements PropertyChangeListener {

    ImageIcon thumbnail = null;
    File file = null;

    public static final int PREVIEW_SIZE = 150;

    public FileChooserImagePreview(JFileChooser fc) {
        setPreferredSize(new Dimension(PREVIEW_SIZE, 50));
        fc.addPropertyChangeListener(this);

        JLabel topLabel = new JLabel(AppStrings.translate("FileChooser.preview"), SwingConstants.CENTER);
        Border b = UIManager.getBorder("FileChooser.listViewBorder");
        topLabel.setBorder(b);
        JPanel previewPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                if (thumbnail == null) {
                    loadImage();
                }
                if (thumbnail != null) {
                    int x = getWidth() / 2 - thumbnail.getIconWidth() / 2;
                    int y = getHeight() / 2 - thumbnail.getIconHeight() / 2;

                    if (y < 0) {
                        y = 0;
                    }

                    if (x < 5) {
                        x = 5;
                    }
                    thumbnail.paintIcon(this, g, x, y);
                } else {
                    String str = AppStrings.translate("FileChooser.previewNotAvailable");
                    g.drawString(str, getWidth() / 2 - g.getFontMetrics().stringWidth(str) / 2, getHeight() / 2 + g.getFontMetrics().getHeight() / 2);
                }
            }
        };
        JPanel fullPanel = new JPanel(new BorderLayout());
        fullPanel.add(topLabel, BorderLayout.NORTH);
        fullPanel.add(previewPanel, BorderLayout.CENTER);
        setLayout(new BorderLayout());
        add(new JScrollPane(fullPanel), BorderLayout.CENTER);

    }

    public void loadImage() {
        if (file == null) {
            thumbnail = null;
            return;
        }

        ImageIcon tmpIcon = new ImageIcon(file.getPath());

        if (tmpIcon != null) {
            if (tmpIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
                if (tmpIcon.getIconWidth() > PREVIEW_SIZE) {
                    thumbnail = new ImageIcon(tmpIcon.getImage().getScaledInstance(PREVIEW_SIZE, -1, Image.SCALE_DEFAULT));
                } else { //no need to miniaturize
                    thumbnail = tmpIcon;
                }
            } else {
                thumbnail = null;
            }

        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent e) {
        boolean update = false;
        String prop = e.getPropertyName();

        //If the directory changed, don't show an image.
        if (JFileChooser.DIRECTORY_CHANGED_PROPERTY.equals(prop)) {
            file = null;
            update = true;

            //If a file became selected, find out which one.
        } else if (JFileChooser.SELECTED_FILE_CHANGED_PROPERTY.equals(prop)) {
            file = (File) e.getNewValue();
            update = true;
        }

        //Update the preview accordingly.
        if (update) {
            thumbnail = null;
            if (isShowing()) {
                loadImage();
                repaint();
            }
        }
    }

}
