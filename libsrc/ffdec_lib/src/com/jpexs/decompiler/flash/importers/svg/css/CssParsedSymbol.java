package com.jpexs.decompiler.flash.importers.svg.css;

/**
 *
 * @author JPEXS
 */
public class CssParsedSymbol {
    public String value;
    public CssSymbolType type;

    public CssParsedSymbol(String value, CssSymbolType type) {
        this.value = value;
        this.type = type;
    }        
    
    public boolean isType(String... ss){
        if(type == CssSymbolType.OTHER){
            for(String s:ss){
                if(s.equals(value)){
                    return true;
                }
            }
        }
        return false;
    }
    
    public boolean isType(CssSymbolType... types){
         for(CssSymbolType type:types){
             if(this.type == type){
                 return true;
             }
         }
         return false;
    }

    @Override
    public String toString() {
        return type.toString() + ": " + value;
    }

}
