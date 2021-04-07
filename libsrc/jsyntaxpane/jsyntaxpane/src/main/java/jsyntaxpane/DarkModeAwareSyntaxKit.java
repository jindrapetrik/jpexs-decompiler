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
package jsyntaxpane;

import java.awt.Color;
import javax.swing.UIManager;
import jsyntaxpane.util.Configuration;

public class DarkModeAwareSyntaxKit extends DefaultSyntaxKit {

    public DarkModeAwareSyntaxKit(Lexer lexer) {
        super(lexer);
    }

    private int cut(double val) {
        int ival = (int) Math.round(val);
        if (ival < 0) {
            return 0;
        }
        if (ival > 255) {
            ival = 255;
        }
        return ival;
    }
    @Override
    public Configuration getConfig() {
        Configuration cnf = super.getConfig();
        Color editorBackground = UIManager.getColor("EditorPane.background");
        int light = (editorBackground.getRed() + editorBackground.getGreen() + editorBackground.getBlue()) / 3;
        if (light < 128) {
            cnf.put("Style.DEFAULT", "0xffffff, 0");
            cnf.put("Style.IDENTIFIER", "0xffffff, 0");
            cnf.put("Style.TYPE", "0xffffff, 2");
            cnf.put("Style.TYPE2", "0xffffff, 1");
            cnf.put("Style.TYPE3", "0xffffff, 3");
            cnf.put("Style.OPERATOR", "0xffffff, 0");
            cnf.put("Style.DELIMITER", "0xffffff, 1");
            cnf.put("Style.KEYWORD", "0x8888ff, 0");
            cnf.put("Style.KEYWORD2", "0x448888, 3");
            cnf.put("Style.COMMENT", "0x88ff88, 3");
            cnf.put("Style.COMMENT2", "0x88ff88, 3");

            cnf.put("CaretColor", "0xffffff");
        }
        return cnf;
    }

}
