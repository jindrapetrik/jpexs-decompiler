/*
 *  Copyright (C) 2010-2025 JPEXS
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
package com.jpexs.decompiler.flash.gui.debugger;

/**
 * @author JPEXS
 */
public class DebugAdapter implements DebugListener {

    @Override
    public void onMessage(String clientId, String msg) {
    }

    @Override
    public void onLoaderURL(String clientId, String url) {
    }

    @Override
    public void onLoaderBytes(String clientId, byte[] data) {
    }

    @Override
    public void onDumpByteArray(String clientId, byte[] data) {
    }

    @Override
    public void onFinish(String clientId) {
    }

    @Override
    public byte[] onRequestBytes(String clientId) {
        return null;
    }

    @Override
    public void onLoaderURLInfo(String clientId, String url) {

    }

    @Override
    public void onLoaderModifyBytes(String clientId, byte[] inputData, String url, DebugLoaderDataModified modifiedListener) {
        modifiedListener.dataModified(inputData);
    }

    @Override
    public boolean isModifyBytesSupported() {
        return false;
    }
}
