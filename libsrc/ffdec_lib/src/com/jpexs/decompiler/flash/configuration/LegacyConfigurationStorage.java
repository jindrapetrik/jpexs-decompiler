/*
 *  Copyright (C) 2010-2025 JPEXS, All rights reserved.
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.configuration;

import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 * Legacy (binary file) configuration storage.
 * @author JPEXS
 */
public class LegacyConfigurationStorage implements ConfigurationStorage {

    @Override
    public String getConfigName() {
        return "config.bin";
    }

    @Override
    public Map<String, Object> loadFromFile(String file) {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {

            @SuppressWarnings("unchecked")
            Map<String, Object> cfg = (HashMap<String, Object>) ois.readObject();
            return cfg;
        } catch (ClassNotFoundException | IOException ex) {
            // ignore
        }

        return new HashMap<>();
    }

    @Override
    public void saveToFile(String file) {
        Map<String, Object> config = new HashMap<>();
        for (Map.Entry<String, Field> entry : Configuration.getConfigurationFields(false, true).entrySet()) {
            try {
                String name = entry.getKey();
                Field field = entry.getValue();
                ConfigurationItem item = (ConfigurationItem) field.get(null);
                if (item.hasValue) {
                    config.put(name, item.get());
                }
            } catch (IllegalArgumentException | IllegalAccessException ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try (ObjectOutputStream oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(file)))) {
            oos.writeObject(config);
        } catch (IOException ex) {
            //TODO: move this to GUI
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "Cannot save configuration.", "Error", JOptionPane.ERROR_MESSAGE);
            Logger.getLogger(Configuration.class.getName()).severe("Configuration directory is read only.");
        }
    }
}
