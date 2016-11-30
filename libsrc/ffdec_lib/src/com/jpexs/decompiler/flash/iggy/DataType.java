package com.jpexs.decompiler.flash.iggy;

/**
 * @author JPEXS
 */
public enum DataType {
    ubits, uint8_t, uint16_t, uint32_t, uint64_t, float_t, unknown, int64_t,
    widechar_t //or maybe just "string"? It has two bytes per character and is null terminated
}
