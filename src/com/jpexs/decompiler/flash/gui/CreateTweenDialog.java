/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui;

import com.jpexs.decompiler.flash.SWF;
import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.tags.DefineSpriteTag;
import com.jpexs.decompiler.flash.tags.PlaceObject2Tag;
import com.jpexs.decompiler.flash.tags.ShowFrameTag;
import com.jpexs.decompiler.flash.types.MATRIX;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * Motion tween options with an animated preview.
 */
public class CreateTweenDialog extends AppDialog {

    private final SWF swf;
    private final int characterId;
    private final int frameCount;
    private final MATRIX startMatrix;
    private final MATRIX endMatrix;
    private final ImagePanel previewPanel = new ImagePanel();
    private final JSpinner easingSpinner = new JSpinner(new SpinnerNumberModel(0, -100, 100, 1));
    private final JLabel easingDirectionLabel = new JLabel();
    private final JTabbedPane easingTabs = new JTabbedPane();
    private final CustomEasePanel customEasePanel;
    private final Timer previewUpdateTimer = new Timer(75, e -> updatePreview());
    private int previewEasingValue;
    private int result = CANCEL_OPTION;

    public CreateTweenDialog(Window owner, SWF swf, int characterId, int frameCount,
            MATRIX startMatrix, MATRIX endMatrix) {
        super(owner);
        this.swf = swf;
        this.characterId = characterId;
        this.frameCount = frameCount;
        this.startMatrix = new MATRIX(startMatrix);
        this.endMatrix = new MATRIX(endMatrix);

        setTitle(translate("dialog.title"));
        setLayout(new BorderLayout(8, 8));

        previewPanel.setPreferredSize(new Dimension(520, 320));
        add(previewPanel, BorderLayout.CENTER);

        JPanel options = new JPanel(new BorderLayout(8, 8));
        JPanel standardPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        standardPanel.add(new JLabel(translate("easing.value")));
        standardPanel.add(easingSpinner);
        standardPanel.add(easingDirectionLabel);
        easingTabs.addTab(translate("easing.standard"), standardPanel);

        customEasePanel = new CustomEasePanel(frameCount, translate("graph.frames"),
                translate("graph.tween"), translate("graph.frame"));
        customEasePanel.setToolTipText(translate("graph.hint"));
        easingTabs.addTab(translate("easing.custom"), customEasePanel);
        options.add(easingTabs, BorderLayout.CENTER);

        previewEasingValue = Math.max(-100, Math.min(100, Configuration.lastTweenEasingValue.get()));
        easingSpinner.setValue(previewEasingValue);
        customEasePanel.setCurveData(Configuration.lastTweenCustomEase.get());
        easingTabs.setSelectedIndex(Math.max(0, Math.min(1, Configuration.lastTweenEasingMode.get())));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton okButton = new JButton(translate("button.ok"));
        okButton.addActionListener(e -> {
            saveConfiguration();
            result = OK_OPTION;
            setVisible(false);
        });
        JButton cancelButton = new JButton(translate("button.cancel"));
        cancelButton.addActionListener(e -> setVisible(false));
        buttons.add(okButton);
        buttons.add(cancelButton);
        options.add(buttons, BorderLayout.SOUTH);
        add(options, BorderLayout.SOUTH);

        easingSpinner.addChangeListener(e -> {
            previewEasingValue = (Integer) easingSpinner.getValue();
            updateEasingDirection();
            schedulePreviewUpdate();
        });
        JFormattedTextField easingEditor = ((JSpinner.DefaultEditor) easingSpinner.getEditor()).getTextField();
        easingEditor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent event) {
                easingTextChanged(easingEditor.getText());
            }

            @Override
            public void removeUpdate(DocumentEvent event) {
                easingTextChanged(easingEditor.getText());
            }

            @Override
            public void changedUpdate(DocumentEvent event) {
                easingTextChanged(easingEditor.getText());
            }
        });
        easingTabs.addChangeListener(e -> schedulePreviewUpdate());
        customEasePanel.addChangeListener(this::schedulePreviewUpdate);
        previewUpdateTimer.setRepeats(false);
        updateEasingDirection();
        updatePreview();

        setModal(true);
        pack();
        View.centerScreen(this);
        View.setWindowIcon(this, "tween");
    }

    private void updateEasingDirection() {
        easingDirectionLabel.setText(previewEasingValue < 0 ? translate("easing.in")
                : previewEasingValue > 0 ? translate("easing.out") : translate("easing.linear"));
    }

    private void easingTextChanged(String text) {
        try {
            int value = Integer.parseInt(text.trim());
            if (value < -100 || value > 100) {
                return;
            }
            previewEasingValue = value;
            updateEasingDirection();
            schedulePreviewUpdate();
        } catch (NumberFormatException ex) {
            // Keep the last valid preview while the user is typing, for
            // example while the editor temporarily contains only "-".
        }
    }

    private void schedulePreviewUpdate() {
        previewUpdateTimer.restart();
    }

    private void saveConfiguration() {
        Configuration.lastTweenEasingMode.set(easingTabs.getSelectedIndex());
        Configuration.lastTweenEasingValue.set(previewEasingValue);
        Configuration.lastTweenCustomEase.set(customEasePanel.getCurveData());
    }

    private void updatePreview() {
        DefineSpriteTag preview = new DefineSpriteTag(swf);
        preview.frameCount = frameCount;
        for (int frame = 0; frame < frameCount; frame++) {
            double progress = frameCount == 1 ? 1 : (double) frame / (frameCount - 1);
            double eased = getEasedProgress(progress);
            PlaceObject2Tag place = new PlaceObject2Tag(swf, frame > 0, 1,
                    frame == 0 ? characterId : -1,
                    TweenEasing.interpolate(startMatrix, endMatrix, eased), null, -1, null, -1, null);
            place.setTimelined(preview);
            preview.addTag(place);
            ShowFrameTag showFrame = new ShowFrameTag(swf);
            showFrame.setTimelined(preview);
            preview.addTag(showFrame);
        }
        preview.resetTimeline();
        previewPanel.setLoop(true);
        // Frame -1 selects timeline playback. A non-negative frame would make
        // ImagePanel render a still frame even when autoPlay is enabled.
        previewPanel.setTimelined(preview, swf, -1, false, true, false, true, true, false, true, true, false);
    }

    public double getEasedProgress(double progress) {
        if (easingTabs.getSelectedIndex() == 1) {
            return customEasePanel.getValue(progress);
        }
        return TweenEasing.ease(progress, previewEasingValue);
    }

    public int showDialog() {
        setVisible(true);
        previewUpdateTimer.stop();
        previewPanel.stop();
        return result;
    }
}
