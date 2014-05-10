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
package jsyntaxpane.actions;

import javax.swing.Action;
import jsyntaxpane.util.Configuration;

/**
 * All JSyntaxPane Keyboard related actions implement this class.  These
 * classes are created dynamically, and then registered to the SyntaxKit.
 *
 * A class may have multiple TextActions that may be related.  Each EditorKit
 * that is installed will have only one instance of each class, even if more
 * than one action is specified.
 *
 * The key value pairs in the COnfiguration are of the form:
 *
 * [EditorKit.]Action.NAME = class, keyboard key
 *
 * @author Ayman Al-Sairafi
 */
public interface SyntaxAction extends Action {

    /**
     * Configure the actions in this class
     * @param config
     * @param name Name of the action, (prefixed by Action.)
     * will be obtained from the property Key as the
     * text following the Action.
     */
    public void config(Configuration config, String name);

}
