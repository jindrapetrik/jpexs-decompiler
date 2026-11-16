/*
 *  Copyright (C) 2010-2026 JPEXS
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 */
package com.jpexs.decompiler.flash.gui.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AIModel {

    private final String id;
    private final String name;
    private final String description;
    private final boolean free;
    private final double promptPrice;
    private final double completionPrice;
    private final String pricingDetails;
    private final List<String> inputModalities;
    private final List<String> outputModalities;

    public AIModel(String id, String name, String description, boolean free,
            double promptPrice, double completionPrice,
            String pricingDetails, List<String> inputModalities, List<String> outputModalities) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.free = free;
        this.promptPrice = promptPrice;
        this.completionPrice = completionPrice;
        this.pricingDetails = pricingDetails;
        this.inputModalities = Collections.unmodifiableList(new ArrayList<>(inputModalities));
        this.outputModalities = Collections.unmodifiableList(new ArrayList<>(outputModalities));
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public boolean isFree() {
        return free;
    }

    public double getPromptPrice() {
        return promptPrice;
    }

    public double getCompletionPrice() {
        return completionPrice;
    }

    public List<String> getInputModalities() {
        return inputModalities;
    }

    public List<String> getOutputModalities() {
        return outputModalities;
    }

    @Override
    public String toString() {
        String price;
        if (free) {
            price = "free";
        } else if (promptPrice >= 0 && completionPrice >= 0) {
            price = String.format("$%.2f input / $%.2f output per 1M tokens",
                    promptPrice * 1000000, completionPrice * 1000000)
                    + (pricingDetails.isEmpty() ? "" : " " + pricingDetails);
        } else {
            price = pricingDetails.isEmpty() ? "pricing not provided" : pricingDetails;
        }
        String capabilities = "input " + inputModalities + ", output " + outputModalities;
        return name + " (" + id + ") - " + price + " - " + capabilities;
    }

    public String getCapabilitySummary() {
        return "Input modalities: " + inputModalities + "; output modalities: " + outputModalities
                + (description.isEmpty() ? "" : "; " + description)
                + (pricingDetails.isEmpty() ? "" : "; pricing: " + pricingDetails);
    }
}
