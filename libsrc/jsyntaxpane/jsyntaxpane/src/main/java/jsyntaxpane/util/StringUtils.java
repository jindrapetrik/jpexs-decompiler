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
package jsyntaxpane.util;

import java.io.Serializable;
import java.util.Comparator;

/**
 * Don't we all need one of those?
 *
 * @author Ayman Al-Sairafi
 */
public class StringUtils {

    /**
     * Perfrom a String startsWith match with support for CamelCase.
     * @param word full word
     * @param abbr abbreviated word
     * @return true if the word startsWith abbr, or if any uppercase char in abbr
     * matches the next uppercase char in word
     *
     * FIXME: not so efficient as it creates a StringBuilder, but works
     * FIXME: add {@code Comparator<String, String>}
     */
    public static boolean camelCaseMatch(String word, String abbr) {
        StringBuilder sb = new StringBuilder();
        sb.append(word.charAt(0));
        for (int i = 1; i < word.length(); i++) {
            char c = word.charAt(i);
            if (Character.isUpperCase(c)) {
                sb.append(c);
            }
        }
        String cc = sb.toString();
        if (cc.startsWith(abbr)) {
            return true;
        } else {
            return word.startsWith(abbr);
        }
    }

    static class CamelCaseCompare implements Comparator<String>, Serializable {

        @Override
        public int compare(String o1, String o2) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
