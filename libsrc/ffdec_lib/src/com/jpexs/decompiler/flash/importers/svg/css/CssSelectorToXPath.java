/*
 *  Copyright (C) 2010-2024 JPEXS, All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3.0 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library.
 */
package com.jpexs.decompiler.flash.importers.svg.css;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Css selector to XPath converter.
 *
 * @author JPEXS
 * <p>
 * Based on implementation https://github.com/css2xpath/css2xpath
 */
public class CssSelectorToXPath {

    private String xpath_to_lower(String s) {
        return "translate("
                + (s == null ? "normalize-space()" : s)
                + ", 'ABCDEFGHJIKLMNOPQRSTUVWXYZ'"
                + ", 'abcdefghjiklmnopqrstuvwxyz')";
    }

    private String xpath_ends_with(String s1, String s2) {
        return "substring(" + s1 + ","
                + "string-length(" + s1 + ")-string-length(" + s2 + ")+1)=" + s2;
    }

    private String xpath_url(String s) {
        return "substring-before(concat(substring-after("
                + (s == null ? xpath_url_attrs : s) + ",\"://\"),\"?\"),\"?\")";
    }

    private String xpath_url_path(String s) {
        return "substring-after(" + (s == null || s.isEmpty() ? xpath_url_attrs : s) + ",\"/\")";
    }

    private String xpath_url_domain(String s) {
        return "substring-before(concat(substring-after("
                + (s == null || s.isEmpty() ? xpath_url_attrs : s) + ",\"://\"),\"/\"),\"/\")";
    }

    private final String xpath_url_attrs = "@href|@src";
    private final String xpath_lower_case = xpath_to_lower(null);
    private final String xpath_ns_uri = "ancestor-or-self::*[last()]/@url";
    private final String xpath_ns_path = xpath_url_path(xpath_url(xpath_ns_uri));
    private final String xpath_has_protocol = "(starts-with(" + xpath_url_attrs + ",\"http://\") or starts-with(" + xpath_url_attrs + ",\"https://\"))";
    private final String xpath_is_internal = "starts-with(" + xpath_url(null) + "," + xpath_url_domain(xpath_ns_uri) + ") or " + xpath_ends_with(xpath_url_domain(null), xpath_url_domain(xpath_ns_uri));
    private final String xpath_is_local = "(" + xpath_has_protocol + " and starts-with(" + xpath_url(null) + "," + xpath_url(xpath_ns_uri) + "))";
    private final String xpath_is_path = "starts-with(" + xpath_url_attrs + ",\"/\")";
    private final String xpath_is_local_path = "starts-with(" + xpath_url_path(null) + "," + xpath_ns_path + ")";
    private final String xpath_normalize_space = "normalize-space()";
    private final String xpath_internal = "[not(" + xpath_has_protocol + ") or " + xpath_is_internal + "]";
    private final String xpath_external = "[" + xpath_has_protocol + " and not(" + xpath_is_internal + ")]";
    private final char escape_literal = (char) 30;
    private final char escape_parens = (char) 31;
    private final String regex_string_literal = "(\"[^\"\\x1E]*\"|'[^'\\x1E]*'|=\\s*[^\\s\\]\\'\\\"]+)"; // /g
    private final String regex_escaped_literal = "['\"]?(\\x1E+)['\"]?"; // /g;
    private final String regex_css_wrap_pseudo = "(\\x1F\\)|[^\\)])\\:(first|limit|last|gt|lt|eq|nth)([^\\-]|$)"; // bez g
    private final String regex_specal_chars = "[\\x1C-\\x1F]+"; // /g;
    private final String regex_first_axis = "^([\\s\\(\\x1F]*)(\\.?[^\\.\\/\\(]{1,2}[a-z]*:*)";
    private final String regex_filter_prefix = "(^|\\/|\\:)\\["; // /g;
    private final String regex_attr_prefix = "([^\\(\\[\\/\\|\\s\\x1F])\\@"; // /g;
    private final String regex_nth_equation = "^([-0-9]*)n.*?([0-9]*)$"; //bez g
    private final String css_combinators_regex = "\\s*(!?[+>~,^ ])\\s*(\\.?\\/+|[a-z\\-]+::)?([a-z\\-]+\\()?((and\\s*|or\\s*|mod\\s*)?[^+>~,\\s'\"\\]\\|\\^\\$\\!\\<\\=\\x1C-\\x1F]+)?"; // /g;

    // Check if string is numeric
    private boolean isNumeric(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    private String css_combinators_callback(String match, String operator, String axis, String func, String literal, String exclude, int offset, String orig) {
        String prefix = ""; // If we can, we'll prefix a '.'

        // XPath operators can look like node-name selectors
        // Detect false positive for " and", " or", " mod"
        if (operator.equals(" ") && exclude != null) {
            return match;
        }

        if (axis == null) {
            // Only allow node-selecting XPath functions
            // Detect false positive for " + count(...)", " count(...)", " > position()", etc.
            if (func != null && (!func.equals("node(") && !func.equals("text(") && !func.equals("comment("))) {
                return null;
            } else if (literal == null) {
                literal = func;
            } // Handle case " + text()", " > comment()", etc. where "func" is our "literal"

            // XPath math operators match some CSS combinators
            // Detect false positive for " + 1", " > 1", etc.
            if (isNumeric(literal)) {
                return match;
            }

            if (orig.length() <= offset - 1) {
                prefix = ".";
            } else {
                char prevChar = orig.charAt(offset - 1);

                if (prevChar == '('
                        || prevChar == '|'
                        || prevChar == ':') {
                    prefix = ".";
                }
            }
        }

        // Return if we don't have a selector to follow the axis
        if (literal == null) {
            if (offset + match.length() == orig.length()) {
                literal = "*";
            } else {
                return match;
            }
        }

        switch (operator) {
            case " ":
                return "//" + literal;
            case ">":
                return "/" + literal;
            case "+":
                return prefix + "/following-sibling::*[1]/self::" + literal;
            case "~":
                return prefix + "/following-sibling::" + literal;
            case ",":
                axis = ".//";
                return "|" + axis + literal;
            case "^": // first child
                return "/child::*[1]/self::" + literal;
            case "!^": // last child
                return "/child::*[last()]/self::" + literal;
            case "! ": // ancestor-or-self
                return "/ancestor-or-self::" + literal;
            case "!>": // direct parent
                return "/parent::" + literal;
            case "!+": // adjacent preceding sibling
                return "/preceding-sibling::*[1]/self::" + literal;
            case "!~": // preceding sibling
                return "/preceding-sibling::" + literal;
            // case '~~'
            // return '/following-sibling::*/self::|'+selectorStart(orig, offset)+'/preceding-sibling::*/self::'+literal;
        }
        return null;
    }

    private final String css_attributes_regex = "\\[([^\\@\\|\\*\\=\\^\\~\\$\\!\\(\\/\\s\\x1C-\\x1F]+)\\s*(([\\|\\*\\~\\^\\$\\!]?)=?\\s*(\\x1E+))?\\]"; // /g;

    private String css_attributes_callback(String str, String attr, String comp, String op, String val, int offset, String orig) {
        String axis = "";
        //String prevChar = offset == 0 ? "" : "" + orig.charAt(offset - 1);

        /*
        if (prevChar === '/' || // found after an axis shortcut ("/", "//", etc.)
            prevChar === ':')   // found after an axis ("self::", "parent::", etc.)
            axis = '*';*/
        if (op == null) {
            op = "";
        }

        switch (op) {
            case "!":
                return axis + "[not(@" + attr + ") or @" + attr + "!=\"" + val + "\"]";
            case "$":
                return axis + "[substring(@" + attr + ",string-length(@" + attr + ")-(string-length(\"" + val + "'\")-1))=\"" + val + "\"]";
            case "^":
                return axis + "[starts-with(@" + attr + ",\"" + val + "\")]";
            case "~":
                return axis + "[contains(concat(\" \",normalize-space(@" + attr + "),\" \"),concat(\" \",\"" + val + "\",\" \"))]";
            case "*":
                return axis + "[contains(@" + attr + ",\"" + val + "\")]";
            case "|":
                return axis + "[@" + attr + "=\"" + val + "\" or starts-with(@" + attr + ",concat(\"" + val + "\",\"-\"))]";
            default:
                if (comp == null) {
                    if (attr.charAt(attr.length() - 1) == '(' || attr.matches("^[0-9]+$") || attr.indexOf(':') != -1) {
                        return str;
                    }
                    return axis + "[@" + attr + "]";
                } else {
                    return axis + "[@" + attr + "=\"" + val + "\"]";
                }
        }
    }

    private final String css_pseudo_classes_regex = ":([a-z\\-]+)(\\((\\x1F+)(([^\\x1F]+(\\3\\x1F+)?)*)(\\3\\)))?"; // /g;

    private String css_pseudo_classes_callback(String match, String name, String g1, String g2, String arg, String g3, String g4, String g5, int offset, String orig) {
        if ((offset - 2 >= 0) && orig.charAt(offset - 1) == ':' && orig.charAt(offset - 2) != ':') {
            // XPath "axis::node-name" will match
            // Detect false positive ":node-name"
            return match;
        }

        if ("odd".equals(name) || "even".equals(name)) {
            arg = name;
            name = "nth-of-type";
        }

        switch (name) { // name.toLowerCase()?
            case "after":
                return "[count(" + css2xpath("preceding::" + arg, true) + ") > 0]";
            case "after-sibling":
                return "[count(" + css2xpath("preceding-sibling::" + arg, true) + ") > 0]";
            case "before":
                return "[count(" + css2xpath("following::" + arg, true) + ") > 0]";
            case "before-sibling":
                return "[count(" + css2xpath("following-sibling::" + arg, true) + ") > 0]";
            case "checked":
                return "[@selected or @checked]";
            case "contains":
                return "[contains(" + xpath_normalize_space + "," + arg + ")]";
            case "icontains":
                return "[contains(" + xpath_lower_case + "," + xpath_to_lower(arg) + ")]";
            case "empty":
                return "[not(*) and not(normalize-space())]";
            case "enabled":
            case "disabled":
                return "[@" + name + "]";
            case "first-child":
                return "[not(preceding-sibling::*)]";
            case "first":
            case "limit":
            case "first-of-type":
                if (arg != null) {
                    return "[position()<=" + arg + "]";
                }
                return "[1]";
            case "gt":
                // Position starts at 0 for consistency with Sizzle selectors
                return "[position()>" + (Integer.parseInt(arg, 10) + 1) + "]";
            case "lt":
                // Position starts at 0 for consistency with Sizzle selectors
                return "[position()<" + (Integer.parseInt(arg, 10) + 1) + "]";
            case "last-child":
                return "[not(following-sibling::*)]";
            case "only-child":
                return "[not(preceding-sibling::*) and not(following-sibling::*)]";
            case "only-of-type":
                return "[not(preceding-sibling::*[name()=name(self::node())]) and not(following-sibling::*[name()=name(self::node())])]";
            case "nth-child":
                if (isNumeric(arg)) {
                    return "[(count(preceding-sibling::*)+1) = " + arg + "]";
                }
                switch (arg) {
                    case "even":
                        return "[(count(preceding-sibling::*)+1) mod 2=0]";
                    case "odd":
                        return "[(count(preceding-sibling::*)+1) mod 2=1]";
                    default:
                        String[] a = (arg == null || arg.isEmpty() ? "0" : arg).replaceAll(regex_nth_equation, "$1+$2").split("\\+");
                        String a0;
                        if (a.length < 1 || a[0].isEmpty()) {
                            a0 = "1";
                        } else {
                            a0 = a[0];
                        }
                        String a1;
                        if (a.length < 2 || a[1].isEmpty()) {
                            a1 = "0";
                        } else {
                            a1 = a[1];
                        }
                        return "[(count(preceding-sibling::*)+1)>=" + a1 + " and ((count(preceding-sibling::*)+1)-" + a1 + ") mod " + a0 + "=0]";
                }
            case "nth-of-type":
                if (isNumeric(arg)) {
                    return "[" + arg + "]";
                }
                switch (arg) {
                    case "odd":
                        return "[position() mod 2=1]";
                    case "even":
                        return "[position() mod 2=0 and position()>=0]";
                    default:
                        String[] a = (arg == null || arg.isEmpty() ? "0" : arg).replaceAll(regex_nth_equation, "$1+$2").split("\\+");
                        String a0;
                        if (a.length < 1 || a[0].isEmpty()) {
                            a0 = "1";
                        } else {
                            a0 = a[0];
                        }
                        String a1;
                        if (a.length < 2 || a[1].isEmpty()) {
                            a1 = "0";
                        } else {
                            a1 = a[1];
                        }
                        return "[position()>=" + a1 + " and (position()-" + a1 + ") mod " + a0 + "=0]";
                }
            case "eq":
            case "nth":
                // Position starts at 0 for consistency with Sizzle selectors
                if (isNumeric(arg)) {
                    return "[" + (Integer.parseInt(arg, 10) + 1) + "]";
                }

                return "[1]";
            case "text":
                return "[@type=\"text\"]";
            case "istarts-with":
                return "[starts-with(" + xpath_lower_case + "," + xpath_to_lower(arg) + ")]";
            case "starts-with":
                return "[starts-with(" + xpath_normalize_space + "," + arg + ")]";
            case "iends-with":
                return "[" + xpath_ends_with(xpath_lower_case, xpath_to_lower(arg)) + "]";
            case "ends-with":
                return "[" + xpath_ends_with(xpath_normalize_space, arg) + "]";
            case "has":
                String xpath1 = prependAxis(css2xpath(arg, true), ".//");

                return "[count(" + xpath1 + ") > 0]";
            case "has-sibling":
                String xpath2 = css2xpath("preceding-sibling::" + arg, true);

                return "[count(" + xpath2 + ") > 0 or count(following-sibling::" + xpath2.substring(19) + ") > 0]";
            case "has-parent":
                return "[count(" + css2xpath("parent::" + arg, true) + ") > 0]";
            case "has-ancestor":
                return "[count(" + css2xpath("ancestor::" + arg, true) + ") > 0]";
            case "last":
            case "last-of-type":
                if (arg != null) {
                    return "[position()>last()-" + arg + "]";
                }
                return "[last()]";
            case "selected": // Sizzle: "(option) elements that are currently selected"
                return "[local-name()=\"option\" and @selected]";
            case "skip":
            case "skip-first":
                return "[position()>" + arg + "]";
            case "skip-last":
                if (arg != null) {
                    return "[last()-position()>=" + arg + "]";
                }
                return "[position()<last()]";
            case "root":
                return "/ancestor::[last()]";
            case "range":
                String[] arr = arg.split(",");

                return "[" + arr[0] + "<=position() and position()<=" + arr[1] + "]";
            case "input": // Sizzle: "input, button, select, and textarea are all considered to be input elements."
                return "[local-name()=\"input\" or local-name()=\"button\" or local-name()=\"select\" or local-name()=\"textarea\"]";
            case "internal":
                return xpath_internal;
            case "external":
                return xpath_external;
            case "http":
            case "https":
            case "mailto":
            case "javascript":
                return "[starts-with(@href,concat(\"" + name + "\",\":\"))]";
            case "domain":
                return "[(string-length(" + xpath_url_domain(null) + ")=0 and contains(" + xpath_url_domain(xpath_ns_uri) + "," + arg + ")) or contains(" + xpath_url_domain(null) + "," + arg + ")]";
            case "path":
                return "[starts-with(" + xpath_url_path(null) + ",substring-after(\"" + arg + "\",\"/\"))]";
            case "not":
                String xpath3 = css2xpath(arg, true);

                if (xpath3.charAt(0) == '[') {
                    xpath3 = "self::node()" + xpath3;
                }
                return "[not(" + xpath3 + ")]";
            case "target":
                return "[starts-with(@href, \"#\")]";
            /*case "root":
          return "ancestor-or-self::*[last()]";*/ //FIXME?? Duplicated case label
            /* case 'active':
            case 'focus':
            case 'hover':
            case 'link':
            case 'visited':
                return '';*/
            case "lang":
                return "[@lang=\"" + arg + "\"]";
            case "read-only":
            case "read-write":
                return "[@" + name.replace("-", "") + "]";
            case "valid":
            case "required":
            case "in-range":
            case "out-of-range":
                return "[@" + name + "]";
            default:
                return "[@_pseudo_" + name + "]"; //JPEXS - to disable such notes
        }
    }

    private String css_ids_classes_regex = "(#|\\.)([^\\#\\@\\.\\/\\(\\[\\)\\]\\|\\:\\s\\+\\>\\<\\'\\\"\\x1D-\\x1F]+)";  // /g;

    private String css_ids_classes_callback(String str, String op, String val, int offset, String orig) {
        String axis = "";
        /* var prevChar = orig.charAt(offset-1);
        if (prevChar.length === 0 ||
            prevChar === '/' ||
            prevChar === '(')
            axis = '*';
        else if (prevChar === ':')
            axis = 'node()';*/
        if ("#".equals(op)) {
            return axis + "[@id=\"" + val + "\"]";
        }
        return axis + "[contains(concat(\" \",normalize-space(@class),\" \"),\" " + val + " \")]";
    }

    // Prepend descendant-or-self if no other axis is specified
    private String prependAxis(String s, String axis) {
        Pattern pat = Pattern.compile(regex_first_axis);
        StringBuffer buf = new StringBuffer();
        Matcher mat = pat.matcher(s);
        while (mat.find()) {
            String start = mat.group(1);
            String literal = mat.group(2);
            if (literal.length() >= 2 && "::".equals(literal.substring(literal.length() - 2))) {
                mat.appendReplacement(buf, mat.group());
            } else {
                if (literal.charAt(0) == '[') {
                    axis += "*";
                }
                mat.appendReplacement(buf, start + axis + literal);
            }

        }
        mat.appendTail(buf);
        return buf.toString();
    }

    // Find the beginning of the selector, starting at i and working backwards
    private int selectorStart(String s, int i) {
        int depth = 0;
        int offset = 0;

        while (i-- > 0) {
            switch (s.charAt(i)) {
                case ' ':
                case escape_parens:
                    offset++;
                    break;
                case '[':
                case '(':
                    depth--;

                    if (depth < 0) {
                        return ++i + offset;
                    }
                    break;
                case ']':
                case ')':
                    depth++;
                    break;
                case ',':
                case '|':
                    if (depth == 0) {
                        return ++i + offset;
                    }
                //fallthrough (?)
                default:
                    offset = 0;
            }
        }

        return 0;
    }

    // Append escape "char" to "open" or "close"
    private String escapeChar(String s, String open, String close, char chr) {
        Pattern pat = Pattern.compile("[\\" + open + "\\" + close + "]");
        StringBuffer buf = new StringBuffer();
        Matcher mat = pat.matcher(s);
        int depth = 0;
        while (mat.find()) {
            if (open.equals(mat.group())) {
                depth++;
            }
            if (open.equals(mat.group())) {
                mat.appendReplacement(buf, mat.group() + repeat("" + chr, depth));
            } else {
                mat.appendReplacement(buf, repeat("" + chr, depth--) + mat.group());
            }
        }
        mat.appendTail(buf);
        return buf.toString();
        /*int depth = 0;

    return s.replace(new RegExp('[\\' + open + '\\' + close + ']', 'g'), function (a) {
      if (a === open)            {
        depth++;
      }

      if (a === open) {
        return a + repeat(chr, depth);
      } else {
        return repeat(chr, depth--) + a;
      }
    })*/
    }

    private String repeat(String str, int num) {
        String result = "";

        while (true) {
            if ((num & 1) == 1) {
                result += str;
            }
            num >>>= 1;

            if (num <= 0) {
                break;
            }
            str += str;
        }

        return result;
    }

    private static interface ReplaceCallBack {

        public String run(Matcher mat, String s);
    }

    private String replace(String s, String regexp, ReplaceCallBack callback) {
        Pattern pat = Pattern.compile(regexp);
        StringBuffer sb = new StringBuffer();
        Matcher m = pat.matcher(s);
        while (m.find()) {
            m.appendReplacement(sb, callback.run(m, s));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private int search(String s, String regexp) {
        Pattern pat = Pattern.compile(regexp);
        Matcher m = pat.matcher(s);
        if (m.find()) {
            return m.start();
        }
        return -1;
    }

    /**
     * Convert CSS selector to XPath.
     *
     * @param s CSS selector
     * @return XPath
     */
    public String css2xpath(String s) {
        return css2xpath(s, false);
    }

    /**
     * Convert CSS selector to XPath.
     *
     * @param s CSS selector
     * @param nested true if nested
     * @return XPath
     */
    public String css2xpath(String s, boolean nested) {
        if (nested == true) {
            // Replace :pseudo-classes
            final String s1 = s;
            s = replace(s, css_pseudo_classes_regex, new ReplaceCallBack() {
                @Override
                public String run(Matcher mat, String s) {
                    return css_pseudo_classes_callback(mat.group(), mat.group(1), mat.group(1), mat.group(2), mat.group(3), mat.group(4), mat.group(5), mat.group(6), mat.start(), s);
                }
            });
            s = replace(s, css_pseudo_classes_regex, new ReplaceCallBack() {
                @Override
                public String run(Matcher mat, String s) {
                    return css_pseudo_classes_callback(mat.group(), mat.group(1), mat.group(2), mat.group(3), mat.group(4),
                            mat.group(5), mat.group(6), mat.group(7), mat.start(), s);
                }
            });

            // Replace #ids and .classes
            s = replace(s, css_ids_classes_regex, new ReplaceCallBack() {
                @Override
                public String run(Matcher mat, String s) {
                    return css_ids_classes_callback(mat.group(), mat.group(1), mat.group(2), mat.start(), s);
                }
            });

            return s;
        }

        // Tag open and close parenthesis pairs (for RegExp searches)
        s = escapeChar(s, "(", ")", escape_parens);

        // Remove and save any string literals
        List<String> literals = new ArrayList<>();

        s = replace(s, regex_string_literal, new ReplaceCallBack() {
            @Override
            public String run(Matcher mat, String fullS) {
                String s = mat.group();
                String a = mat.group(1);

                if (a.charAt(0) == '=') {
                    a = a.substring(1).trim();

                    if (isNumeric(a)) {
                        return s;
                    }
                } else {
                    a = a.substring(1, a.length() - 1);
                }

                literals.add(a);
                return repeat("" + escape_literal, literals.size());

            }
        });

        // Replace CSS combinators (" ", "+", ">", "~", ",") and reverse combinators ("!", "!+", "!>", "!~")
        s = replace(s, css_combinators_regex, new ReplaceCallBack() {
            @Override
            public String run(Matcher mat, String s) {
                return css_combinators_callback(mat.group(), mat.group(1), mat.group(2), mat.group(3), mat.group(4), mat.group(5), mat.start(), s);
            }
        });

        // Replace CSS attribute filters
        s = replace(s, css_attributes_regex, new ReplaceCallBack() {
            @Override
            public String run(Matcher mat, String s) {
                return css_attributes_callback(mat.group(), mat.group(1), mat.group(2), mat.group(3), mat.group(4), mat.start(), s);
            }
        });

        // Wrap certain :pseudo-classes in parens (to collect node-sets)
        while (true) {
            int index = search(s, regex_css_wrap_pseudo);

            if (index == -1) {
                break;
            }
            index = s.indexOf(':', index);
            int start = selectorStart(s, index);

            s = s.substring(0, start)
                    + '(' + s.substring(start, index) + ')'
                    + s.substring(index);
        }

        // Replace :pseudo-classes
        s = replace(s, css_pseudo_classes_regex, new ReplaceCallBack() {
            @Override
            public String run(Matcher mat, String s) {
                return css_pseudo_classes_callback(mat.group(), mat.group(1), mat.group(2), mat.group(3), mat.group(4), mat.group(5), mat.group(6), mat.group(7), mat.start(), s);
            }
        });

        // Replace #ids and .classes
        s = replace(s, css_ids_classes_regex, new ReplaceCallBack() {
            @Override
            public String run(Matcher mat, String s) {
                return css_ids_classes_callback(mat.group(), mat.group(1), mat.group(2), mat.start(), s);
            }
        });

        // Restore the saved string literals
        s = replace(s, regex_escaped_literal, new ReplaceCallBack() {
            @Override
            public String run(Matcher mat, String a) {
                String str = literals.get(mat.group(1).length() - 1);

                return "\"" + str + "\"";
            }
        });

        // Remove any special characters
        s = s.replaceAll(regex_specal_chars, "");

        // add * to stand-alone filters
        s = s.replaceAll(regex_filter_prefix, "$1*[");

        // add "/" between @attribute selectors
        s = s.replaceAll(regex_attr_prefix, "$1/@");

        /*
    Combine multiple filters?

    s = escapeChar(s, '[', ']', filter_char);
    s = s.replace(/(\x1D+)\]\[\1(.+?[^\x1D])\1\]/g, ' and ($2)$1]')
         */
        s = prependAxis(s, ".//"); // prepend ".//" axis to beginning of CSS selector
        return s;
    }

}
