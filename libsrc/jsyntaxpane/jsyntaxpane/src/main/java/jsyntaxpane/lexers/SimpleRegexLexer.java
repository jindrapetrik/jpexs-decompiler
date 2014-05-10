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
package jsyntaxpane.lexers;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.text.Segment;
import jsyntaxpane.Lexer;
import jsyntaxpane.Token;
import jsyntaxpane.TokenComparators;
import jsyntaxpane.TokenType;

/**
 * This is a "dynamic" Lexer that will use Regex patterns to parse any document,
 * It is NOT as fast as other JFLex generated lexers.
 *
 * The current implementation is about 20x slower than a JFLex lexer
 * (5000 lines in 100ms, vs 5ms for JFlex lexer)
 *
 * This is still usable for a few 100 lines.  500 lines parse in about 10ms.
 *
 * It also depends on how complex the Regexp and how many of them will actually
 * provide a match.
 *
 * Since KEYWORD TokenType is by order less than IDENTIFIER, the higher
 * precedence of KEYWORD token will be used, even if the same regex matches
 * an IDENTIFIER.  This is a neat side-effect of the ordering of the TokenTypes.
 * We now just need to add any non-overlapping matches.  And since longer matches
 * are found first, we will properly match the longer identifiers which start with
 * a keyword.
 *
 * This behaviour can easily be modified by overriding the {@link compareTo} method
 *
 * @author Ayman Al-Sairafi
 */
public class SimpleRegexLexer implements Lexer {

    public SimpleRegexLexer(Map props) {
        putPatterns(props);
    }

    public SimpleRegexLexer(String propsLocation) throws IOException {
        Properties props = new Properties();
        props.load(new FileReader(propsLocation));
        putPatterns(props);
    }

    @Override
    public void parse(Segment segment, int ofst, List<Token> tokens) {
        TreeSet<Token> allMatches = new TreeSet<Token>(TokenComparators.LONGEST_FIRST);
        // add to ourset all the matches by all our regexes
        for (Map.Entry<TokenType, Pattern> e : patterns.entrySet()) {
            Matcher m = e.getValue().matcher(segment);
            while (m.find()) {
                Token t = new Token(e.getKey(), m.start() + ofst, m.end() - m.start());
                allMatches.add(t);
            }
        }
        int end = -1;
        for (Token t : allMatches) {
            if (t.start > end) {
                tokens.add(t);
                end = t.end();
            }
        }
    }
    Map<TokenType, Pattern> patterns = new HashMap<TokenType, Pattern>();

    public SimpleRegexLexer putPattern(TokenType type, String regex) {
        patterns.put(type, Pattern.compile(regex));
        return this;
    }

    public SimpleRegexLexer putPatterns(Map props) {
        for (Object key : props.keySet()) {
            TokenType t = TokenType.valueOf(key.toString());
            patterns.put(t, Pattern.compile(props.get(key).toString()));
        }
        return this;
    }
}
