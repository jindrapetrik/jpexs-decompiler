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
import java.awt.Component;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

class MembersListRenderer extends DefaultListCellRenderer {

    static final Color evensColor = new Color(0xeeffee);
    private ReflectCompletionDialog dlg;

    public MembersListRenderer(ReflectCompletionDialog dlg) {
        this.dlg = dlg;
    }

    @Override
    public Component getListCellRendererComponent(final JList list, Object value, final int index,
            final boolean isSelected, boolean cellHasFocus) {
        Color back = (index % 2 == 1) ? list.getBackground() : evensColor;
        if (value instanceof Method) {
            final Method method = (Method) value;
            return new MethodCell(list, isSelected, back, method, dlg.getTheClass());
        } else if (value instanceof Field) {
            Field field = (Field) value;
            return new FieldCell(list, isSelected, back, field, dlg.getTheClass());
        } else if (value instanceof Constructor) {
            Constructor cons = (Constructor) value;
            return new ConstructorCell(list, isSelected, back, cons, dlg.getTheClass());
        } else {
            Component comp = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            comp.setBackground(back);
            return comp;
        }
    }
}
