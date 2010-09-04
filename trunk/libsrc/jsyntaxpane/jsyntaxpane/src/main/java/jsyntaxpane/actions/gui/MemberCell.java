/*
 * Copyright 2008 Ayman Al-Sairafi ayman.alsairafi@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License
 *       at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jsyntaxpane.actions.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ImageIcon;
import javax.swing.JList;
import javax.swing.JPanel;
import jsyntaxpane.SyntaxView;
import jsyntaxpane.actions.ActionUtils;
import jsyntaxpane.util.ReflectUtils;

/**
 * This class will render a Member.  There are Method, Field and Constructor subclasses
 * @author Ayman Al-Sairafi
 */
abstract class MemberCell extends JPanel {

    private final JList list;
    private final boolean isSelected;
    private final Color backColor;
    private final Member member;
    private final Class theClass;

    public MemberCell(JList list, boolean isSelected, Color backColor, Member member, Class clazz) {
        super();
        this.list = list;
        this.isSelected = isSelected;
        this.backColor = backColor;
        this.member = member;
        this.theClass = clazz;
    }

    @Override
    public void paintComponent(Graphics g) {
        SyntaxView.setRenderingHits((Graphics2D) g);
        g.setFont(list.getFont());
        super.paintComponent(g);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(isSelected ? list.getSelectionBackground() : backColor);
        g.fillRect(0, 0, getWidth(), getHeight());
        g.setColor(isSelected ? list.getSelectionForeground() : list.getForeground());
        g.drawImage(getIcon(), 2, 0, null);
        int x = 6 + getIcon().getWidth(this);
        int y = fm.getHeight();
        if (member.getDeclaringClass().equals(theClass)) {
            Font bold = list.getFont().deriveFont(Font.BOLD);
            g.setFont(bold);
        }
        x = drawString(getMemberName(), x, y, g);
        g.setFont(list.getFont());
        x = drawString(getArguments(), x, y, g);
        String right = getReturnType();
        int rw = fm.stringWidth(right);
        g.drawString(right, getWidth() - rw - 4, fm.getAscent());
    }

    @Override
    public Dimension getPreferredSize() {
        Font font = list.getFont();
        Graphics g = getGraphics();
        FontMetrics fm = g.getFontMetrics(font);
        // total text for this component:
        String total = getMemberName() + getArguments() + getReturnType() + "  ";
        return new Dimension(fm.stringWidth(total) + 20, Math.max(fm.getHeight(), 16));
    }

    private int drawString(String string, int x, int y, Graphics g) {
        if(ActionUtils.isEmptyOrBlanks(string)) {
            return x;
        }
        int w = g.getFontMetrics().stringWidth(string);
        g.drawString(string, x, y);
        return x + w;
    }

    /**
     * Read all relevant icons and returns the Map.  The loc should contain the
     * fully qualified URL for the icons.  The icon names read will have the words
     * _private, protected, _static, _static_private and _static_protected and the
     * extension ".png" appended.
     * @param loc root for icon locations
     * @return Map (can be used directly with getModifiers & 0xf)
     */
    Map<Integer, Image> readIcons(String loc) {
        Map<Integer, Image> icons = new HashMap<Integer, Image>();
        icons.put(Modifier.PUBLIC, readImage(loc, ""));
        icons.put(Modifier.PRIVATE, readImage(loc, "_private"));
        icons.put(Modifier.PROTECTED, readImage(loc, "_protected"));
        icons.put(Modifier.STATIC | Modifier.PUBLIC, readImage(loc, "_static"));
        icons.put(Modifier.STATIC | Modifier.PRIVATE, readImage(loc, "_static_private"));
        icons.put(Modifier.STATIC | Modifier.PROTECTED, readImage(loc, "_static_protected"));
        return icons;
    }

    private Image readImage(String iconLoc, String kind) {
        String fullPath = iconLoc + kind + ".png";
        URL loc = this.getClass().getResource(fullPath);
        if (loc == null) {
            return null;
        } else {
            Image i = new ImageIcon(loc).getImage();
            return i;
        }
    }

    protected String getMemberName() {
        return member.getName();
    }

    abstract protected String getArguments();

    abstract protected String getReturnType();

    abstract protected Image getIcon();
}

/**
 * Renders a Method
 * @author Ayman Al-Sairafi
 */
class MethodCell extends MemberCell {

    private final Method method;

    public MethodCell(JList list, boolean isSelected, Color backColor, Method method, Class clazz) {
        super(list, isSelected, backColor, method, clazz);
        this.method = method;
    }

    @Override
    protected String getArguments() {
        return ReflectUtils.getParamsString(method.getParameterTypes());
    }

    @Override
    protected String getReturnType() {
        return method.getReturnType().getSimpleName();
    }

    @Override
    protected Image getIcon() {
        int type = method.getModifiers() & 0xf; // only get public/private/protected/static
        if (icons == null) {
            icons = readIcons(METHOD_ICON_LOC);
        }
        return icons.get(type);
    }
    private static Map<Integer, Image> icons = null;
    public static final String METHOD_ICON_LOC = "/META-INF/images/completions/method";
}

/**
 * Renders a Field
 * @author Ayman Al-Sairafi
 */
class FieldCell extends MemberCell {

    private final Field field;

    public FieldCell(JList list, boolean isSelected, Color backColor, Field field, Class clazz) {
        super(list, isSelected, backColor, field, clazz);
        this.field = field;
    }

    @Override
    protected String getArguments() {
        return "";
    }

    @Override
    protected String getReturnType() {
        return field.getType().getSimpleName();
    }

    @Override
    protected Image getIcon() {
        int type = field.getModifiers() & 0xf; // only get public/private/protected/static
        if (icons == null) {
            icons = readIcons(FIELD_ICON_LOC);
        }
        if (icons.get(type) == null) {
            System.err.println("Unable to get icon for type: " + field.getModifiers());
        }
        return icons.get(type);
    }
    private static Map<Integer, Image> icons = null;
    public static final String FIELD_ICON_LOC = "/META-INF/images/completions/field";
}

/**
 * Renders a Field
 * @author Ayman Al-Sairafi
 */
class ConstructorCell extends MemberCell {

    private final Constructor cons;

    public ConstructorCell(JList list, boolean isSelected, Color backColor, Constructor cons, Class clazz) {
        super(list, isSelected, backColor, cons, clazz);
        this.cons = cons;
    }

    @Override
    protected String getMemberName() {
        return cons.getDeclaringClass().getSimpleName();
    }

    @Override
    protected String getArguments() {
        return ReflectUtils.getParamsString(cons.getParameterTypes());
    }

    @Override
    protected String getReturnType() {
        return cons.getDeclaringClass().getSimpleName();
    }

    @Override
    protected Image getIcon() {
        int type = cons.getModifiers() & 0x7; // only get public/private/protected, mask out static
        if (icons == null) {
            icons = readIcons(FIELD_ICON_LOC);
        }
        if (icons.get(type) == null) {
            System.out.println("Unable to get icon for type: " + cons.getModifiers());
        }
        return icons.get(type);
    }

    private static Map<Integer, Image> icons = null;
    public static final String FIELD_ICON_LOC = "/META-INF/images/completions/constructor";
}