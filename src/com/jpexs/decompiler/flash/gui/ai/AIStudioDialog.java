/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui.ai;

import com.jpexs.decompiler.flash.configuration.Configuration;
import com.jpexs.decompiler.flash.gui.AppDialog;
import com.jpexs.decompiler.flash.gui.AppStrings;
import com.jpexs.decompiler.flash.gui.View;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class AIStudioDialog extends AppDialog {

    private final JComboBox<ProviderOption> providerCombo = new JComboBox<>(new ProviderOption[]{
        new ProviderOption(OpenRouterProvider.ID, "OpenRouter"),
        new ProviderOption(OpenCodeModelsProvider.ID, "OpenCode Models")
    });
    private final JComboBox<OpenCodePlanOption> planCombo = new JComboBox<>(new OpenCodePlanOption[]{
        new OpenCodePlanOption(OpenCodeModelsProvider.PLAN_PAY_AS_YOU_GO, text("plan.payg")),
        new OpenCodePlanOption(OpenCodeModelsProvider.PLAN_GO, text("plan.go"))
    });
    private final JComboBox<AIModel> modelCombo = new JComboBox<>();
    private final JComboBox<FilterOption> filterCombo = new JComboBox<>(new FilterOption[]{
        new FilterOption(AIModelCatalog.Filter.ALL, text("filter.all")),
        new FilterOption(AIModelCatalog.Filter.FREE, text("filter.free")),
        new FilterOption(AIModelCatalog.Filter.PAID, text("filter.paid"))
    });
    private final JLabel statusLabel = new JLabel(" ");
    private final JTextArea conversation = new JTextArea();
    private final JTextArea prompt = new JTextArea(4, 60);
    private final JButton refreshButton = new JButton(text("button.refresh"));
    private final JButton settingsButton = new JButton(text("button.settings"));
    private final JButton sendButton = new JButton(text("button.send"));
    private final AIModelCatalog modelCatalog = new AIModelCatalog();
    private final AIChatService chatService = new AIChatService();
    private final List<AIChatMessage> history = new ArrayList<>();
    private List<AIModel> allModels = new ArrayList<>();
    private final String context;
    private final String sessionId = new AIChatService().newSessionId();
    private boolean initialized;

    public AIStudioDialog(Window owner, String swfTitle, String swfContext) {
        super(owner);
        setTitle(text("studio.title"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setModal(false);
        setResizable(true);
        setMinimumSize(new Dimension(700, 500));
        context = swfTitle == null || swfTitle.trim().isEmpty()
                ? text("context.noSwf")
                : text("context.swf") + ": " + swfTitle + "\n"
                + (swfContext == null ? "" : swfContext) + "\n"
                + text("context.limited");
        initComponents();
        View.centerScreen(this);
        setSize(900, 650);
        loadConfiguration();
        if (!swfTitleEmpty(swfTitle)) {
            conversation.append(text("context.currentSwf") + ": " + swfContext + "\n\n");
        }
    }

    private void initComponents() {
        providerCombo.addActionListener(e -> providerChanged());
        planCombo.addActionListener(e -> {
            if (!initialized) {
                return;
            }
            OpenCodePlanOption option = (OpenCodePlanOption) planCombo.getSelectedItem();
            if (option != null && !option.id.equals(Configuration.aiOpenCodePlan.get())) {
                Configuration.aiOpenCodePlan.set(option.id);
                Configuration.saveConfig();
                refreshModels();
            }
        });
        modelCombo.addActionListener(e -> modelSelected());
        filterCombo.addActionListener(e -> showFilteredModels());
        refreshButton.addActionListener(e -> refreshModels());
        settingsButton.addActionListener(e -> {
            new AISettingsDialog(this).setVisible(true);
            refreshModels();
        });
        sendButton.addActionListener(e -> sendMessage());

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT));
        toolbar.add(new JLabel(text("label.provider")));
        toolbar.add(providerCombo);
        toolbar.add(planCombo);
        toolbar.add(new JLabel(text("label.model")));
        modelCombo.setPreferredSize(new Dimension(400, 26));
        toolbar.add(modelCombo);
        toolbar.add(filterCombo);
        toolbar.add(refreshButton);
        toolbar.add(settingsButton);

        conversation.setEditable(false);
        conversation.setLineWrap(true);
        conversation.setWrapStyleWord(true);
        prompt.setLineWrap(true);
        prompt.setWrapStyleWord(true);
        JPanel bottom = new JPanel(new BorderLayout(6, 6));
        bottom.add(statusLabel, BorderLayout.NORTH);
        bottom.add(new JScrollPane(prompt), BorderLayout.CENTER);
        bottom.add(sendButton, BorderLayout.EAST);

        getContentPane().setLayout(new BorderLayout(5, 5));
        getContentPane().add(toolbar, BorderLayout.NORTH);
        getContentPane().add(new JScrollPane(conversation), BorderLayout.CENTER);
        getContentPane().add(bottom, BorderLayout.SOUTH);
    }

    private void loadConfiguration() {
        String providerId = Configuration.aiProvider.get();
        providerCombo.setSelectedIndex(OpenCodeModelsProvider.ID.equals(providerId) ? 1 : 0);
        String plan = Configuration.aiOpenCodePlan.get();
        planCombo.setSelectedIndex(OpenCodeModelsProvider.PLAN_GO.equals(plan) ? 1 : 0);
        filterCombo.setSelectedIndex(0);
        initialized = true;
        providerChanged();
    }

    private void providerChanged() {
        if (!initialized) {
            return;
        }
        ProviderOption option = (ProviderOption) providerCombo.getSelectedItem();
        if (option == null) {
            return;
        }
        if (!option.id.equals(Configuration.aiProvider.get())) {
            Configuration.aiProvider.set(option.id);
            Configuration.saveConfig();
        }
        boolean openCode = OpenCodeModelsProvider.ID.equals(option.id);
        planCombo.setVisible(openCode);
        filterCombo.setVisible(!openCode);
        filterCombo.setEnabled(!openCode);
        refreshModels();
    }

    private void modelSelected() {
        AIModel model = (AIModel) modelCombo.getSelectedItem();
        if (model == null) {
            modelCombo.setToolTipText(null);
            return;
        }
        modelCombo.setToolTipText(model.getCapabilitySummary());
        ProviderOption provider = (ProviderOption) providerCombo.getSelectedItem();
        if (provider == null) {
            return;
        }
        if (OpenRouterProvider.ID.equals(provider.id)) {
            Configuration.aiOpenRouterModel.set(model.getId());
        } else {
            Configuration.aiOpenCodeModel.set(model.getId());
        }
        Configuration.saveConfig();
    }

    private void refreshModels() {
        ProviderOption provider = (ProviderOption) providerCombo.getSelectedItem();
        if (provider == null) {
            return;
        }
        refreshButton.setEnabled(false);
        statusLabel.setText(text("status.loading"));
        String plan = getPlan();
        new javax.swing.SwingWorker<List<AIModel>, Void>() {
            @Override
            protected List<AIModel> doInBackground() throws Exception {
                return modelCatalog.load(provider.id, plan);
            }

            @Override
            protected void done() {
                refreshButton.setEnabled(true);
                try {
                    allModels = get();
                    showFilteredModels();
                    statusLabel.setText(text("status.models") + ": " + allModels.size());
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    modelCombo.removeAllItems();
                    statusLabel.setText(text("status.error") + ": " + cause.getMessage());
                }
            }
        }.execute();
    }

    private void showFilteredModels() {
        FilterOption filterOption = (FilterOption) filterCombo.getSelectedItem();
        AIModelCatalog.Filter filter = filterOption == null
                ? AIModelCatalog.Filter.ALL : filterOption.filter;
        String providerId = getProviderId();
        String savedModel = OpenCodeModelsProvider.ID.equals(providerId)
                ? Configuration.aiOpenCodeModel.get() : Configuration.aiOpenRouterModel.get();
        List<AIModel> models = OpenCodeModelsProvider.ID.equals(providerId)
                ? allModels : modelCatalog.filter(allModels, filter);
        modelCombo.removeAllItems();
        int selectedIndex = -1;
        for (int i = 0; i < models.size(); i++) {
            AIModel model = models.get(i);
            modelCombo.addItem(model);
            if (model.getId().equals(savedModel)) {
                selectedIndex = i;
            }
        }
        modelCombo.setSelectedIndex(selectedIndex);
    }

    private void sendMessage() {
        String userText = prompt.getText().trim();
        AIModel model = (AIModel) modelCombo.getSelectedItem();
        if (userText.isEmpty()) {
            return;
        }
        if (model == null) {
            statusLabel.setText(text("status.chooseModel"));
            return;
        }
        String providerId = getProviderId();
        String plan = getPlan();
        history.add(new AIChatMessage("user", userText));
        conversation.append(text("chat.you") + ": " + userText + "\n\n"
                + text("chat.assistant") + ": ");
        prompt.setText("");
        sendButton.setEnabled(false);
        refreshButton.setEnabled(false);
        statusLabel.setText(text("status.sending"));
        new javax.swing.SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return chatService.send(providerId, plan, model.getId(), context,
                        history, sessionId, chunk -> SwingUtilities.invokeLater(
                                () -> conversation.append(chunk)));
            }

            @Override
            protected void done() {
                sendButton.setEnabled(true);
                refreshButton.setEnabled(true);
                try {
                    String answer = get();
                    if (answer.isEmpty()) {
                        conversation.append("\n[" + text("status.emptyResponse") + "]");
                    }
                    conversation.append("\n\n");
                    history.add(new AIChatMessage("assistant", answer));
                    statusLabel.setText(text("status.ready"));
                } catch (Exception ex) {
                    Throwable cause = ex.getCause() == null ? ex : ex.getCause();
                    conversation.append("\n[" + text("status.error") + ": "
                            + cause.getMessage() + "]\n\n");
                    statusLabel.setText(text("status.failed"));
                }
            }
        }.execute();
    }

    private String getProviderId() {
        ProviderOption option = (ProviderOption) providerCombo.getSelectedItem();
        return option == null ? OpenRouterProvider.ID : option.id;
    }

    private String getPlan() {
        OpenCodePlanOption option = (OpenCodePlanOption) planCombo.getSelectedItem();
        return option == null ? OpenCodeModelsProvider.PLAN_PAY_AS_YOU_GO : option.id;
    }

    private static boolean swfTitleEmpty(String swfTitle) {
        return swfTitle == null || swfTitle.trim().isEmpty();
    }

    private static String text(String key) {
        return AppStrings.translate(AIStudioDialog.class, key);
    }

    private static class ProviderOption {

        private final String id;
        private final String name;

        ProviderOption(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }

    private static class OpenCodePlanOption {

        private final String id;
        private final String label;

        OpenCodePlanOption(String id, String label) {
            this.id = id;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }

    private static class FilterOption {

        private final AIModelCatalog.Filter filter;
        private final String label;

        FilterOption(AIModelCatalog.Filter filter, String label) {
            this.filter = filter;
            this.label = label;
        }

        @Override
        public String toString() {
            return label;
        }
    }
}
