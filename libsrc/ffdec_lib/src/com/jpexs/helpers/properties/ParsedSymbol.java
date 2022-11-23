package com.jpexs.helpers.properties;

/**
 *
 * @author JPEXS
 */
public class ParsedSymbol {
    public Object value;

    public SymbolType type;

    public ParsedSymbol(SymbolType type, Object value) {
        this.type = type;
        this.value = value;        
    }

    @Override
    public String toString() {
        return type+": " + value;
    }
    
    
    
    
}
