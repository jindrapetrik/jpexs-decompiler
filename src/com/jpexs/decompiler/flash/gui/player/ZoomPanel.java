/*
 * Copyright (C) 2024 JPEXS
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.decompiler.flash.gui.player;

import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * @author JPEXS
 */
public class ZoomPanel extends JPanel implements MediaDisplayListener {

    private MediaDisplay display;
    private JButton zoomFitButton;
    private final JLabel percentLabel = new JLabel("100%");
    private boolean zoomToFit = false;
    private double realZoom = 1.0;
    private final double MAX_ZOOM = 1.0e6; //in larger zooms, flash viewer stops working  

    public ZoomPanel(MediaDisplay display) {
        this.display = display;
        JButton zoomInButton = new JButton(View.getIcon("zoomin16"));
        zoomInButton.addActionListener(this::zoomInButtonActionPerformed);
        zoomInButton.setToolTipText(AppStrings.translate("button.zoomin.hint"));

        JButton zoomOutButton = new JButton(View.getIcon("zoomout16"));
        zoomOutButton.addActionListener(this::zoomOutButtonActionPerformed);
        zoomOutButton.setToolTipText(AppStrings.translate("button.zoomout.hint"));

        zoomFitButton = new JButton(View.getIcon("zoomfit16"));
        zoomFitButton.addActionListener(this::zoomFitButtonActionPerformed);
        zoomFitButton.setToolTipText(AppStrings.translate("button.zoomfit.hint"));

        JButton zoomNoneButton = new JButton(View.getIcon("zoomnone16"));
        zoomNoneButton.addActionListener(this::zoomNoneButtonActionPerformed);
        zoomNoneButton.setToolTipText(AppStrings.translate("button.zoomnone.hint"));

        setLayout(new FlowLayout());
        add(percentLabel);
        add(zoomInButton);
        add(zoomOutButton);
        add(zoomNoneButton);
        add(zoomFitButton);

        display.addEventListener(this);
    }

    private void zoomInButtonActionPerformed(ActionEvent evt) {
        double currentRealZoom = getRealZoom();
        if (currentRealZoom >= MAX_ZOOM) {
            return;
        }
        realZoom = currentRealZoom * PlayerControls.ZOOM_MULTIPLIER;
        zoomToFit = false;
        updateZoom();
    }

    private void zoomOutButtonActionPerformed(ActionEvent evt) {
        realZoom = getRealZoom() / PlayerControls.ZOOM_MULTIPLIER;
        zoomToFit = false;
        updateZoom();
    }

    private void zoomNoneButtonActionPerformed(ActionEvent evt) {
        realZoom = 1.0;
        zoomToFit = false;
        updateZoom();
    }

    private void zoomFitButtonActionPerformed(ActionEvent evt) {
        realZoom = 1.0;
        zoomToFit = true;
        updateZoom();
    }

    private double getRealZoom() {
        if (zoomToFit) {
            return display.getZoomToFit();
        }

        return realZoom;
    }

    private void updateZoom() {
        updateZoomDisplay();
        double pctzoom = roundZoom(getRealZoom() * 100, 3);
        double zoom = pctzoom / 100.0;
        Zoom zoomObj = new Zoom();
        zoomObj.value = zoom;
        zoomObj.fit = zoomToFit;
        display.zoom(zoomObj);
    }

    private void updateZoomDisplay() {
        double pctzoom = roundZoom(getRealZoom() * 100, 3);
        String r = Double.toString(pctzoom);
        if (r.endsWith(".0")) {
            r = r.substring(0, r.length() - 2);
        }

        r += "%";

        if (zoomToFit) {
            percentLabel.setText(AppStrings.translate("fit") + " (" + r + ")");
        } else {
            percentLabel.setText(r);
        }
    }

    private static double roundZoom(double realZoom, int mantissa) {
        double l10 = Math.log10(realZoom);
        int lg = (int) (-Math.floor(l10) + mantissa - 1);
        if (lg < 0) {
            lg = 0;
        }
        BigDecimal bd = new BigDecimal(String.valueOf(realZoom)).setScale(lg, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    public void update() {
        if (!display.isLoaded()) {
            return;
        }

        View.execInEventDispatchLater(() -> {
            Zoom zoom = display.getZoom();
            zoomFitButton.setVisible(zoom != null);
            percentLabel.setVisible(zoom != null);
            Zoom currentZoom = new Zoom();
            currentZoom.fit = zoomToFit;
            currentZoom.value = realZoom;
            if (zoom != null && !Objects.equals(zoom, currentZoom)) {
                zoomToFit = zoom.fit;
                realZoom = zoom.value;
                updateZoomDisplay();
            }
        });
    }

    public void setMedia(MediaDisplay media) {
        if (this.display != null) {
            this.display.removeEventListener(this);
        }

        this.display = media;
        this.display.addEventListener(this);

        update();
    }

    @Override
    public void mediaDisplayStateChanged(MediaDisplay source) {
        if (display != source) {
            return;
        }

        update();
    }

    @Override
    public void playingFinished(MediaDisplay source) {
    }

    @Override
    public void statusChanged(String status) {
    }
}
