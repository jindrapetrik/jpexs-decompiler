/*
 * Copyright (c) 2005 JGoodies Karsten Lentzsch. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 
 *  o Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer. 
 *     
 *  o Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution. 
 *     
 *  o Neither the name of JGoodies Karsten Lentzsch nor the names of 
 *    its contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission. 
 *     
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, 
 * THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR 
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, 
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; 
 * OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, 
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */
package org.pushingpixels.substance.internal.contrib.jgoodies.looks.common;

import java.awt.Component;
import java.awt.Window;

import javax.swing.*;

import org.pushingpixels.lafwidget.utils.LookUtils;
import org.pushingpixels.substance.internal.contrib.jgoodies.looks.Options;

/**
 * The JGoodies Looks implementation of <code>PopupFactory</code>. Adds a drop
 * shadow border to all popups except ComboBox popups. It is installed by the
 * JGoodies Plastic L&amp;F, as well as by the JGoodies Windows L&amp;F during
 * the Look&amp;Feel initialization, see
 * {@link com.jgoodies.looks.plastic.PlasticLookAndFeel#initialize} and
 * {@link com.jgoodies.looks.windows.WindowsLookAndFeel#initialize}.
 * <p>
 *
 * This factory shall not be used on platforms that provide native drop shadows,
 * such as the Mac OS X. Therefore the invocation of the {@link #install()}
 * method will have no effect on such platforms.
 * <p>
 *
 * <strong>Note:</strong> To be used in a sandbox environment, this PopupFactory
 * requires two AWT permissions: <code>createRobot</code> and
 * <code>readDisplayPixels</code>. The reason for it is, that in the case of the
 * heavy weight popups this PopupFactory uses a Robot to snapshot the screen
 * background to simulate the drop shadow effect.
 *
 * @author JPEXS
 *
 * @see java.awt.AWTPermission
 * @see java.awt.Robot
 * @see javax.swing.Popup
 * @see LookAndFeel#initialize
 * @see LookAndFeel#uninitialize
 */
public final class FixedShadowPopupFactory extends PopupFactory {

    /**
     * In the case of heavy weight popups, snapshots of the screen background
     * will be stored as client properties of the popup contents' parent. These
     * snapshots will be used by the popup border to simulate the drop shadow
     * effect. The two following constants define the names of these client
     * properties.
     *
     * @see com.jgoodies.looks.common.ShadowPopupBorder
     */
    static final String PROP_HORIZONTAL_BACKGROUND = "jgoodies.hShadowBg";
    static final String PROP_VERTICAL_BACKGROUND = "jgoodies.vShadowBg";

    /**
     * The PopupFactory used before this PopupFactory has been installed in
     * <code>#install</code>. Used to restored the original state in
     * <code>#uninstall</code>.
     */
    private final PopupFactory storedFactory;

    // Instance Creation ******************************************************
    public FixedShadowPopupFactory(PopupFactory storedFactory) {
        this.storedFactory = storedFactory;
    }

    // API ********************************************************************
    /**
     * Installs the FixedShadowPopupFactory as the shared popup factory on non-Mac
     * platforms. Also stores the previously set factory, so that it can be
     * restored in <code>#uninstall</code>.
     * <p>
     *
     * In some Mac Java environments the popup factory throws a
     * NullPointerException when we call <code>#getPopup</code>.
     * <p>
     *
     * TODO: The Mac case shows that we may have problems replacing non
     * PopupFactory instances. Therefore we should consider replacing only
     * instances of PopupFactory.
     *
     * @see #uninstall()
     */
    public static void install() {
        if (LookUtils.IS_OS_MAC) {
            return;
        }

        PopupFactory factory = PopupFactory.getSharedInstance();        
        PopupFactory.setSharedInstance(new FixedShadowPopupFactory(factory));
    }

    /**
     * Uninstalls the ShadowPopupFactory and restores the original popup factory
     * as the new shared popup factory.
     *
     * @see #install()
     */
    public static void uninstall() {
        PopupFactory factory = PopupFactory.getSharedInstance();
        if (!(factory instanceof FixedShadowPopupFactory)) {
            return;
        }

        PopupFactory stored = ((FixedShadowPopupFactory) factory).storedFactory;
        PopupFactory.setSharedInstance(stored);
    }

    /**
     * Creates a <code>Popup</code> for the Component <code>owner</code>
     * containing the Component <code>contents</code>. In addition to the
     * superclass behavior, we try to return a Popup that has a drop shadow, if
     * popup drop shadows are active - as returned by
     * <code>Options#isPopupDropShadowActive</code>.
     * <p>
     *
     * <code>owner</code> is used to determine which <code>Window</code> the new
     * <code>Popup</code> will parent the <code>Component</code> the
     * <code>Popup</code> creates to. A null <code>owner</code> implies there is
     * no valid parent. <code>x</code> and <code>y</code> specify the preferred
     * initial location to place the <code>Popup</code> at. Based on screen
     * size, or other paramaters, the <code>Popup</code> may not display at
     * <code>x</code> and <code>y</code>.
     * <p>
     *
     * We invoke the super <code>#getPopup</code>, not the one in the stored
     * factory, because the popup type is set in this instance, not in the
     * stored one.
     *
     * @param owner Component mouse coordinates are relative to, may be null
     * @param contents Contents of the Popup
     * @param x Initial x screen coordinate
     * @param y Initial y screen coordinate
     * @return Popup containing Contents
     * @throws IllegalArgumentException if contents is null
     *
     * @see Options#isPopupDropShadowActive()
     */
    @Override
    public Popup getPopup(Component owner, Component contents, int x, int y)
            throws IllegalArgumentException {
        Window ownerWindow = owner == null
                ? null
                : SwingUtilities.getWindowAncestor(owner);

        JWindow window = ownerWindow == null
                ? new JWindow()
                : new JWindow(ownerWindow);

        window.setType(Window.Type.POPUP);
        window.setFocusableWindowState(false);
        window.getContentPane().add(contents);
        window.pack();
        window.setLocation(x, y);

        Popup popup = new Popup() {
            @Override
            public void show() {
                window.setVisible(true);
            }

            @Override
            public void hide() {
                window.setVisible(false);
                window.getContentPane().remove(contents);
                window.dispose();
            }
        };

        return Options.isPopupDropShadowActive() ? ShadowPopup.getInstance(
                owner, contents, x, y, popup) : popup;
    }

}
