package com.jpexs.decompiler.flash.tags.base;

/**
 *
 * @author JPEXS
 */
public interface SoundStreamHeadTypeTag {

    public int getSoundFormat();

    public int getSoundRate();

    public int getSoundSize();

    public int getSoundType();

    public long getSoundSampleCount();

    public void setVirtualCharacterId(int ch);

    public int getCharacterID();

    public String getExportFormat();
}
