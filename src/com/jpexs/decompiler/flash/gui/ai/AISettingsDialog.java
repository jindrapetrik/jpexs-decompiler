/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui.ai;

import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.io.IOException;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.WindowConstants;

public class AISettingsDialog extends AppDialog {

    private final JPasswordField storePasswordField = new JPasswordField(24);
    private final JPasswordField openRouterKeyField = new JPasswordField(32);
    private final JPasswordField openCodeKeyField = new JPasswordField(32);
    private final JComboBox<PlanOption> openCodePlan = new JComboBox<>(new PlanOption[]{
        new PlanOption(OpenCodeModelsProvider.PLAN_PAY_AS_YOU_GO,
                text("settings.plan.payg")),
        new PlanOption(OpenCodeModelsProvider.PLAN_GO, text("settings.plan.go"))
    });
    private final JLabel statusLabel = new JLabel(" ");
    private final JButton saveOpenRouterButton = new JButton(text("settings.saveTest"));
    private final JButton saveOpenCodeButton = new JButton(text("settings.saveTest"));
    private final JButton clearOpenRouterButton = new JButton(text("settings.remove"));
    private final JButton clearOpenCodeButton = new JButton(text("settings.remove"));
    private final JButton unlockButton = new JButton(text("settings.unlock"));

    public AISettingsDialog(Window owner) {
        super(owner);
        setTitle(text("settings.title"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(true);
        setResizable(true);
        setMinimumSize(new Dimension(560, 390));
        initComponents();
        View.centerScreen(this);
        pack();
    }

    private void initComponents() {
        JPanel content = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 3;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(8, 10, 4, 10);
        content.add(new JLabel("<html>" + text("settings.secureStore") + "</html>"), c);

        c.gridy++;
        c.gridwidth = 1;
        c.weightx = 0;
        content.add(new JLabel(text("settings.storePassword")), c);
        c.gridx = 1;
        c.weightx = 1;
        content.add(storePasswordField, c);
        c.gridx = 2;
        c.weightx = 0;
        content.add(unlockButton, c);

        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 3;
        c.weightx = 1;
        content.add(providerPanel(text("settings.openRouter"), openRouterKeyField,
                saveOpenRouterButton, clearOpenRouterButton), c);

        c.gridy++;
        content.add(providerPanel(text("settings.openCode"), openCodeKeyField,
                saveOpenCodeButton, clearOpenCodeButton), c);

        c.gridy++;
        c.insets = new Insets(8, 10, 8, 10);
        content.add(statusLabel, c);

        saveOpenRouterButton.addActionListener(e -> saveAndTest(
                new OpenRouterProvider(), openRouterKeyField));
        saveOpenCodeButton.addActionListener(e -> saveAndTest(
                new OpenCodeModelsProvider(), openCodeKeyField));
        clearOpenRouterButton.addActionListener(e -> removeCredential(
                OpenRouterProvider.ID, openRouterKeyField));
        clearOpenCodeButton.addActionListener(e -> removeCredential(
                OpenCodeModelsProvider.ID, openCodeKeyField));
        unlockButton.addActionListener(e -> unlockCredentialStore());

        openCodePlan.setSelectedItem(OpenCodeModelsProvider.PLAN_GO.equals(
                com.jpexs.decompiler.flash.configuration.Configuration.aiOpenCodePlan.get())
                ? openCodePlan.getItemAt(1) : openCodePlan.getItemAt(0));
        openCodePlan.addActionListener(e -> {
            PlanOption option = (PlanOption) openCodePlan.getSelectedItem();
            if (option != null && !option.id.equals(
                    com.jpexs.decompiler.flash.configuration.Configuration.aiOpenCodePlan.get())) {
                com.jpexs.decompiler.flash.configuration.Configuration.aiOpenCodePlan.set(option.id);
                com.jpexs.decompiler.flash.configuration.Configuration.saveConfig();
            }
        });

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton(text("button.close"));
        closeButton.addActionListener(e -> dispose());
        buttons.add(closeButton);

        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(content, BorderLayout.CENTER);
        getContentPane().add(buttons, BorderLayout.SOUTH);
    }

    private JPanel providerPanel(String title, JPasswordField keyField,
            JButton saveButton, JButton removeButton) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(javax.swing.BorderFactory.createTitledBorder(title));
        GridBagConstraints c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        c.insets = new Insets(4, 5, 4, 5);
        panel.add(new JLabel(text("settings.apiKey")), c);
        c.gridy++;
        panel.add(keyField, c);
        if (title.equals(text("settings.openCode"))) {
            c.gridy++;
            c.gridwidth = 1;
            c.weightx = 0;
            panel.add(new JLabel(text("settings.accessPlan")), c);
            c.gridx = 1;
            c.weightx = 1;
            panel.add(openCodePlan, c);
        }
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actions.add(removeButton);
        actions.add(saveButton);
        c.gridx = 0;
        c.gridy++;
        c.gridwidth = 2;
        c.weightx = 1;
        panel.add(actions, c);
        return panel;
    }

    private void saveAndTest(AIProvider provider, JPasswordField keyField) {
        char[] apiKey = keyField.getPassword();
        char[] storePassword = storePasswordField.getPassword();
        setBusy(true);
        statusLabel.setText(text("settings.testing"));
        String plan = getPlan();
        new javax.swing.SwingWorker<List<AIModel>, Void>() {
            @Override
            protected List<AIModel> doInBackground() throws Exception {
                unlockStore(storePassword);
                provider.testConnection(new String(apiKey), plan);
                AICredentialManager.saveCredential(provider.getId(), apiKey);
                return provider.loadModels(new String(apiKey), plan);
            }

            @Override
            protected void done() {
                java.util.Arrays.fill(apiKey, '\0');
                java.util.Arrays.fill(storePassword, '\0');
                keyField.setText("");
                storePasswordField.setText("");
                setBusy(false);
                try {
                    List<AIModel> models = get();
                    statusLabel.setText(text("settings.connected") + " (" + models.size() + ")");
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    statusLabel.setText(text("settings.failed") + ": " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void unlockStore(char[] password) throws IOException {
        if (!AICredentialManager.isUnlocked()) {
            if (password.length == 0) {
                throw new IOException(text("settings.passwordRequired"));
            }
            AICredentialManager.unlock(password);
        } else if (password.length > 0) {
            throw new IOException(text("settings.unlockAlready"));
        }
    }

    private void removeCredential(String providerId, JPasswordField keyField) {
        try {
            if (!AICredentialManager.isUnlocked()) {
                char[] password = storePasswordField.getPassword();
                if (password.length == 0) {
                    throw new IOException(text("settings.passwordRequired"));
                }
                AICredentialManager.unlock(password);
                java.util.Arrays.fill(password, '\0');
            }
            AICredentialManager.removeCredential(providerId);
            keyField.setText("");
            statusLabel.setText(text("settings.removed"));
            storePasswordField.setText("");
        } catch (IOException ex) {
            statusLabel.setText(text("settings.failed") + ": " + ex.getMessage());
        }
    }

    private void unlockCredentialStore() {
        char[] password = storePasswordField.getPassword();
        try {
            AICredentialManager.unlock(password);
            statusLabel.setText(text("settings.unlocked"));
            storePasswordField.setText("");
        } catch (IOException ex) {
            statusLabel.setText(text("settings.failed") + ": " + ex.getMessage());
        } finally {
            java.util.Arrays.fill(password, '\0');
        }
    }

    private void setBusy(boolean busy) {
        saveOpenRouterButton.setEnabled(!busy);
        saveOpenCodeButton.setEnabled(!busy);
        clearOpenRouterButton.setEnabled(!busy);
        clearOpenCodeButton.setEnabled(!busy);
        unlockButton.setEnabled(!busy);
    }

    private String getPlan() {
        PlanOption option = (PlanOption) openCodePlan.getSelectedItem();
        return option == null ? OpenCodeModelsProvider.PLAN_PAY_AS_YOU_GO : option.id;
    }

    private static String text(String key) {
        return AppStrings.translate(AIStudioDialog.class, key);
    }

    private static class PlanOption {

        private final String id;
        private final String label;

        PlanOption(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
