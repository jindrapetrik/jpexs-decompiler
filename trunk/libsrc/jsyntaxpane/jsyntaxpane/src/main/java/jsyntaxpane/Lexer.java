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

import java.util.List;
import javax.swing.text.Segment;

/**
 * Lexers must implement these methods.  These are used in the Tokenizer 
 * 
 * A Lexer should be tied to one document.
 * 
 * @author Ayman Al-Sairafi
 */
public interface Lexer {
    /**
     * This is the only method a Lexer needs to implement.  It will be passed
     * a Reader, and it should return non-overlapping Tokens for each recognized token
     * in the stream.
     * @param segment Text to parse.
     * @param ofst offset to add to start of each token (useful for nesting)
     * @param tokens List of Tokens to be added.  This is done so that the caller creates the
     * appropriate List implementation and size.  The parse method just adds to the list
     */
    public void parse(Segment segment, int ofst, List<Token> tokens);
}
