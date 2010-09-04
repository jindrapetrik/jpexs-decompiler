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

import javax.swing.RootPaneContainer;

/**
 * This interface is used by dialogs that will need to listen to ESC key.
 * When the ESC key is pressed, escapePressed is called.
 * @author Ayman Al-Sairafi
 */
public interface EscapeListener extends RootPaneContainer {

	/**
	 * This method will be called when ESC key is pressed.
	 */
	public void escapePressed();

}
