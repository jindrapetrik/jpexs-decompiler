/*
 *  Copyright (C) 2010-2015 JPEXS
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
package com.jpexs.decompiler.flash.gui.controls;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.SwingUtilities;

/**
 *
 * @author JPEXS
 */
public class JRepeatButton extends JButton {

    final ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);

    private int repeatCount;

    public JRepeatButton(String text, ImageIcon icon) {
        super(text, icon);
        addMouseListener(new MouseAdapter() {
            ScheduledFuture<?> future;

            @Override
            public void mousePressed(MouseEvent e) {
                repeatCount = 0;
                Runnable runnable = new Runnable() {
                    private int cnt = 0;

                    @Override
                    public void run() {
                        SwingUtilities.invokeLater(() -> {
                            cnt++;
                            JRepeatButton button = JRepeatButton.this;
                            repeatCount = cnt;
                            fireActionPerformed(new ActionEvent(button, ActionEvent.ACTION_PERFORMED, button.getActionCommand(), e.getWhen(), e.getModifiers()));
                        });
                    }
                };

                future = executor.scheduleAtFixedRate(runnable, 200, 200, TimeUnit.MILLISECONDS);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (future != null) {
                    future.cancel(true);
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (future != null) {
                    future.cancel(true);
                }
            }

        });
    }

    public int getRepeatCount() {
        return repeatCount;
    }
}
