package com.jpexs.asdec.tags.base;

import com.jpexs.asdec.tags.ExportAssetsTag;
import com.jpexs.asdec.tags.Tag;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public abstract class CharacterTag extends Tag {

   public CharacterTag(int id, String name, byte[] data, long pos) {
      super(id, name, data, pos);
   }

   public abstract int getCharacterID();
   /**
    * List of ExportAssetsTag used for converting to String
    */
   public List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();

   @Override
   public String getName() {
      String nameAppend = "";
      for (ExportAssetsTag eat : exportAssetsTags) {
         int pos = eat.tags.indexOf(getCharacterID());
         if (pos > -1) {
            nameAppend = ": " + eat.names.get(pos);
         }
      }
      return super.getName() + " (" + getCharacterID() + nameAppend + ")";
   }

   @Override
   public String getExportName() {
      return super.getName() + "_" + getCharacterID();
   }
}
