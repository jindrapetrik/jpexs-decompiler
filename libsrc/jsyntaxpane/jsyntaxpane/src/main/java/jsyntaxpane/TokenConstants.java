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

/**
 * Constants used by Tokens.
 * @author Ayman Al-Sairafi
 */
public class TokenConstants {
    /**
     * Token Kinds
     */
    // Operators and separators - Suitable for Java Type Languages
    public static final short EQ = 1;           // =
    public static final short GT = 2;           // >
    public static final short LT = 3;           // <
    public static final short NOT = 4;          // !
    public static final short COMP = 5;         // ~
    public static final short QUESTION = 6;     // ?
    public static final short COLON = 7;        // :
    public static final short EQEQ = 8;         // ==
    public static final short LTEQ = 9;         // <=
    public static final short GTEQ = 10;        // >=
    public static final short NOTEQ = 11;       // !=
    public static final short ANDAND = 12;      // &&
    public static final short OROR = 13;        // ||
    public static final short PLUSPLUS = 14;    // ++
    public static final short MINUSMINUS = 15;  // --
    public static final short PLUS = 16;        // +
    public static final short MINUS = 17;       // -
    public static final short MULT = 18;        // *
    public static final short DIV = 19;         // /
    public static final short AND = 20;         // &
    public static final short OR = 21;          // |
    public static final short XOR = 22;         // ^
    public static final short MOD = 23;         // %
    public static final short LSHIFT = 24;      // <<
    public static final short RSHIFT = 25;      // >>
    public static final short URSHIFT = 26;     // >>>
    public static final short PLUSEQ = 27;      // +=
    public static final short MINUSEQ = 28;     // -=
    public static final short MULTEQ = 29;      // *=
    public static final short DIVEQ = 30;       // /=
    public static final short ANDEQ = 31;       // &=
    public static final short OREQ = 32;        // |=
    public static final short XOREQ = 33;       // ^=
    public static final short MODEQ = 34;       // %=
    public static final short LSHIFTEQ = 35;    // <<=
    public static final short RSHIFTEQ = 36;    // >>=
    public static final short URSHIFTEQ = 37;   // >>>=
    public static final short LPAREN = 38;      // (
    public static final short RPAREN = 39;      // )
    public static final short LBRACE = 40;      // {
    public static final short RBRACE = 41;      // }
    public static final short LBRACK = 42;      // [
    public static final short RBRACK = 43;      // ]
    public static final short SEMICOLON = 44;   // ;
    public static final short COMMA = 46;       // ,
    public static final short DOT = 47;         // .

    // Keywords for Java Type Languages
    public static final short KW_START = 255;
    public static final short KW_abstract = KW_START + 0;
    public static final short KW_assert = KW_START + 1;
    public static final short KW_boolean = KW_START + 2;
    public static final short KW_break = KW_START + 3;
    public static final short KW_byte = KW_START + 4;
    public static final short KW_case = KW_START + 5;
    public static final short KW_catch = KW_START + 6;
    public static final short KW_char = KW_START + 7;
    public static final short KW_class = KW_START + 8;
    public static final short KW_const = KW_START + 9;
    public static final short KW_continue = KW_START + 10;
    public static final short KW_do = KW_START + 11;
    public static final short KW_double = KW_START + 12;
    public static final short KW_else = KW_START + 13;
    public static final short KW_extends = KW_START + 14;
    public static final short KW_final = KW_START + 15;
    public static final short KW_finally = KW_START + 16;
    public static final short KW_float = KW_START + 17;
    public static final short KW_for = KW_START + 18;
    public static final short KW_default = KW_START + 19;
    public static final short KW_implements = KW_START + 20;
    public static final short KW_import = KW_START + 21;
    public static final short KW_instanceof = KW_START + 22;
    public static final short KW_int = KW_START + 23;
    public static final short KW_interface = KW_START + 24;
    public static final short KW_long = KW_START + 25;
    public static final short KW_native = KW_START + 26;
    public static final short KW_new = KW_START + 27;
    public static final short KW_goto = KW_START + 28;
    public static final short KW_if = KW_START + 29;
    public static final short KW_public = KW_START + 30;
    public static final short KW_short = KW_START + 31;
    public static final short KW_super = KW_START + 32;
    public static final short KW_switch = KW_START + 33;
    public static final short KW_synchronized = KW_START + 34;
    public static final short KW_package = KW_START + 35;
    public static final short KW_private = KW_START + 36;
    public static final short KW_protected = KW_START + 37;
    public static final short KW_transient = KW_START + 38;
    public static final short KW_return = KW_START + 39;
    public static final short KW_void = KW_START + 40;
    public static final short KW_static = KW_START + 41;
    public static final short KW_while = KW_START + 42;
    public static final short KW_this = KW_START + 43;
    public static final short KW_throw = KW_START + 44;
    public static final short KW_throws = KW_START + 45;
    public static final short KW_try = KW_START + 46;
    public static final short KW_volatile = KW_START + 47;
    public static final short KW_strictfp = KW_START + 48;
}
