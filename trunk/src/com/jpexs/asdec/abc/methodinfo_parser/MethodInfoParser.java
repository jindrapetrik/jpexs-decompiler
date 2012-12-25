/*
 *  Copyright (C) 2011 JPEXS
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.jpexs.asdec.abc.methodinfo_parser;

import com.jpexs.asdec.Main;
import com.jpexs.asdec.abc.ABC;
import com.jpexs.asdec.abc.types.MethodInfo;
import com.jpexs.asdec.abc.types.ValueKind;
import com.jpexs.asdec.abc.types.traits.TraitSlotConst;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class MethodInfoParser {

   public static boolean parseSlotConst(String text, TraitSlotConst trait, ABC abc) throws ParseException {
      MethodInfoLexer lexer = new MethodInfoLexer(new ByteArrayInputStream(text.getBytes()));
      ParsedSymbol symb;
      int type_index = -1;
      ValueKind value = new ValueKind(0, 0);
      try {
         ParsedSymbol symbType = lexer.yylex();
         if (symbType.type == ParsedSymbol.TYPE_STAR) {
            type_index = 0;
         } else if (symbType.type == ParsedSymbol.TYPE_MULTINAME) {
            type_index = (int) (long) (Long) symbType.value;
         } else {
            throw new ParseException("Multiname or * expected", lexer.yyline());
         }
         ParsedSymbol symbEqual = lexer.yylex();
         if (symbEqual.type == ParsedSymbol.TYPE_ASSIGN) {
            ParsedSymbol symbValue;
            String nstype = "";
            do {
               symbValue = lexer.yylex();
               if (symbValue.type >= 8 && symbValue.type <= 13) {
                  nstype = nstype + symbValue.type + ":";
               }
            } while (symbValue.type >= 8 && symbValue.type <= 13);
            if ((!nstype.equals("")) && (symbValue.type != ParsedSymbol.TYPE_NAMESPACE)) {
               throw new ParseException("Namespace expected", lexer.yyline());
            }
            int id = 0;
            switch (symbValue.type) {
               case ParsedSymbol.TYPE_INTEGER:
                  value = new ValueKind(abc.constants.forceGetIntId((Long) symbValue.value), ValueKind.CONSTANT_Int);
                  break;
               case ParsedSymbol.TYPE_FLOAT:
                  value = new ValueKind(abc.constants.forceGetDoubleId((Double) symbValue.value), ValueKind.CONSTANT_Double);
                  break;
               case ParsedSymbol.TYPE_STRING:
                  value = new ValueKind(abc.constants.forceGetStringId((String) symbValue.value), ValueKind.CONSTANT_Utf8);
                  break;
               case ParsedSymbol.TYPE_TRUE:
                  value = new ValueKind(0, ValueKind.CONSTANT_True);
                  break;
               case ParsedSymbol.TYPE_FALSE:
                  value = new ValueKind(0, ValueKind.CONSTANT_False);
                  break;
               case ParsedSymbol.TYPE_NULL:
                  value = new ValueKind(0, ValueKind.CONSTANT_Null);
                  break;
               case ParsedSymbol.TYPE_UNDEFINED:
                  value = new ValueKind(0, ValueKind.CONSTANT_Undefined);
                  break;
               case ParsedSymbol.TYPE_NAMESPACE:
                  if (nstype.equals("9:")) {
                     value = new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_PackageNamespace);
                  } else if (nstype.equals("9:10:")) {
                     value = new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_PackageInternalNs);
                  } else if (nstype.equals("13:")) {
                     value = new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_ProtectedNamespace);
                  } else if (nstype.equals("12:")) {
                     value = new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_ExplicitNamespace);
                  } else if (nstype.equals("11:13:")) {
                     value = new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_StaticProtectedNs);
                  } else if (nstype.equals("8:")) {
                     value = new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_PrivateNs);
                  } else if (nstype.equals("")) {
                     value = new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_Namespace);
                  } else {
                     throw new ParseException("Invalid type of namespace", lexer.yyline());
                  }
                  break;
               default:
                  throw new ParseException("Unexpected symbol", lexer.yyline());
            }
            symb = lexer.yylex();
            if (symb.type != ParsedSymbol.TYPE_EOF) {
               throw new ParseException("Unexpected symbol", lexer.yyline());
            }
         } else if (symbEqual.type == ParsedSymbol.TYPE_EOF) {
         } else {
            throw new ParseException("Unexpected symbol", lexer.yyline());
         }
      } catch (IOException ex) {
         return false;
      }
      trait.type_index = type_index;
      trait.value_kind = value.value_kind;
      trait.value_index = value.value_index;
      return true;
   }

   public static boolean parseReturnType(String text, MethodInfo update) throws ParseException {
      MethodInfoLexer lexer = new MethodInfoLexer(new ByteArrayInputStream(text.getBytes()));
      ParsedSymbol symb;
      int type = -1;
      try {
         symb = lexer.yylex();
         if (symb.type == ParsedSymbol.TYPE_STAR) {
            type = 0;
         } else if (symb.type == ParsedSymbol.TYPE_MULTINAME) {
            type = (int) (long) (Long) symb.value;
         } else {
            throw new ParseException("Multiname or * expected", lexer.yyline());
         }
         symb = lexer.yylex();
         if (symb.type != ParsedSymbol.TYPE_EOF) {
            throw new ParseException("Only one return type allowed", lexer.yyline());
         }
         update.ret_type = type;
         return true;
      } catch (IOException ex) {
      }
      return false;
   }

   public static boolean parseParams(String text, MethodInfo update, ABC abc) throws ParseException {
      MethodInfoLexer lexer = new MethodInfoLexer(new ByteArrayInputStream(text.getBytes()));
      List<String> paramNames = new ArrayList<String>();
      List<Long> paramTypes = new ArrayList<Long>();
      List<ValueKind> optionalValues = new ArrayList<ValueKind>();
      boolean hasOptional = false;
      boolean needsRest = false;
      try {
         ParsedSymbol symb;
         symb = lexer.yylex();
         while (symb.type != ParsedSymbol.TYPE_EOF) {
            if (symb.type == ParsedSymbol.TYPE_DOTS) {
               needsRest = true;
               symb = lexer.yylex();
               if (symb.type != ParsedSymbol.TYPE_IDENTIFIER) {
                  throw new ParseException("Identifier expected", lexer.yyline());
               }
               symb = lexer.yylex();
               if (symb.type != ParsedSymbol.TYPE_EOF) {
                  throw new ParseException("End expected after rest params", lexer.yyline());
               }
               break;
            }

            if (symb.type != ParsedSymbol.TYPE_IDENTIFIER) {
               throw new ParseException("Identifier expected", lexer.yyline());
            }
            paramNames.add((String) symb.value);
            symb = lexer.yylex();
            if (symb.type == ParsedSymbol.TYPE_COLON) {
               ParsedSymbol symbType = lexer.yylex();
               if (symbType.type == ParsedSymbol.TYPE_STAR) {
                  paramTypes.add(new Long(0));
               } else if (symbType.type == ParsedSymbol.TYPE_MULTINAME) {
                  paramTypes.add((Long) symbType.value);
               } else {
                  throw new ParseException("Multiname or * expected", lexer.yyline());
               }
               ParsedSymbol symbEqual = lexer.yylex();
               if (symbEqual.type == ParsedSymbol.TYPE_ASSIGN) {
                  hasOptional = true;
                  ParsedSymbol symbValue;
                  String nstype = "";
                  do {
                     symbValue = lexer.yylex();
                     if (symbValue.type >= 8 && symbValue.type <= 13) {
                        nstype = nstype + symbValue.type + ":";
                     }
                  } while (symbValue.type >= 8 && symbValue.type <= 13);
                  if ((!nstype.equals("")) && (symbValue.type != ParsedSymbol.TYPE_NAMESPACE)) {
                     throw new ParseException("Namespace expected", lexer.yyline());
                  }
                  int id = 0;
                  switch (symbValue.type) {
                     case ParsedSymbol.TYPE_INTEGER:
                        optionalValues.add(new ValueKind(abc.constants.forceGetIntId((Long) symbValue.value), ValueKind.CONSTANT_Int));
                        break;
                     case ParsedSymbol.TYPE_FLOAT:
                        optionalValues.add(new ValueKind(abc.constants.forceGetDoubleId((Double) symbValue.value), ValueKind.CONSTANT_Double));
                        break;
                     case ParsedSymbol.TYPE_STRING:
                        optionalValues.add(new ValueKind(abc.constants.forceGetStringId((String) symbValue.value), ValueKind.CONSTANT_Utf8));
                        break;
                     case ParsedSymbol.TYPE_TRUE:
                        optionalValues.add(new ValueKind(0, ValueKind.CONSTANT_True));
                        break;
                     case ParsedSymbol.TYPE_FALSE:
                        optionalValues.add(new ValueKind(0, ValueKind.CONSTANT_False));
                        break;
                     case ParsedSymbol.TYPE_NULL:
                        optionalValues.add(new ValueKind(0, ValueKind.CONSTANT_Null));
                        break;
                     case ParsedSymbol.TYPE_UNDEFINED:
                        optionalValues.add(new ValueKind(0, ValueKind.CONSTANT_Undefined));
                        break;
                     case ParsedSymbol.TYPE_NAMESPACE:
                        if (nstype.equals("9:")) {
                           optionalValues.add(new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_PackageNamespace));
                        } else if (nstype.equals("9:10:")) {
                           optionalValues.add(new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_PackageInternalNs));
                        } else if (nstype.equals("13:")) {
                           optionalValues.add(new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_ProtectedNamespace));
                        } else if (nstype.equals("12:")) {
                           optionalValues.add(new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_ExplicitNamespace));
                        } else if (nstype.equals("11:13:")) {
                           optionalValues.add(new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_StaticProtectedNs));
                        } else if (nstype.equals("8:")) {
                           optionalValues.add(new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_PrivateNs));
                        } else if (nstype.equals("")) {
                           optionalValues.add(new ValueKind((int) (long) (Long) symbValue.value, ValueKind.CONSTANT_Namespace));
                        } else {
                           throw new ParseException("Invalid type of namespace", lexer.yyline());
                        }
                        break;
                     default:
                        throw new ParseException("Unexpected symbol", lexer.yyline());
                  }
                  symb = lexer.yylex();
                  if (symb.type == ParsedSymbol.TYPE_COMMA) {
                  } else if (symb.type == ParsedSymbol.TYPE_EOF) {
                     break;
                  }
               } else if (symbEqual.type == ParsedSymbol.TYPE_COMMA) {
                  if (hasOptional) {
                     throw new ParseException("Parameter must have default value", lexer.yyline());
                  }
               } else if (symbEqual.type == ParsedSymbol.TYPE_EOF) {
                  if (hasOptional) {
                     throw new ParseException("Parameter must have default value", lexer.yyline());
                  }
                  break;
               } else {
                  throw new ParseException("Unexpected symbol", lexer.yyline());
               }
            } else if (symb.type == ParsedSymbol.TYPE_COMMA) {
            } else if (symb.type == ParsedSymbol.TYPE_EOF) {
               break;
            } else {
               throw new ParseException("Unexpected symbol", lexer.yyline());
            }
            symb = lexer.yylex();
         }
      } catch (IOException iex) {
         return false;
      }

      if (needsRest && (!optionalValues.isEmpty())) {
         throw new ParseException("Rest parameter canot be combined with default values", lexer.yyline());
      }

      update.param_types = new int[paramTypes.size()];
      for (int p = 0; p < paramTypes.size(); p++) {
         update.param_types[p] = (int) (long) paramTypes.get(p);
      }
      update.optional = (ValueKind[]) optionalValues.toArray(new ValueKind[optionalValues.size()]);
      update.unsetFlagHas_optional();
      if (!optionalValues.isEmpty()) {
         update.setFlagHas_optional();
      }
      update.unsetFlagNeed_rest();
      if (needsRest) {
         update.setFlagNeed_rest();
      }

      update.unsetFlagHas_paramnames();
      update.paramNames = new int[]{};
      boolean useParamNames = false;
      for (int p = 0; p < paramNames.size(); p++) {
         if (!paramNames.get(p).equals("param" + (p + 1))) {
            useParamNames = true;
         }
      }
      if (!Main.PARAM_NAMES_ENABLE) {
         useParamNames = false;
      }
      if (useParamNames) {
         update.setFlagHas_paramnames();
         update.paramNames = new int[paramNames.size()];
         for (int p = 0; p < paramNames.size(); p++) {
            update.paramNames[p] = abc.constants.forceGetStringId(paramNames.get(p));
         }
      }
      return true;

   }
}
