/*
 *  Copyright (C) 2010-2012 JPEXS
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
package com.jpexs.asdec.action;

import com.jpexs.asdec.tags.ASMSource;
import com.jpexs.asdec.tags.Container;
import com.jpexs.asdec.tags.DefineButton2Tag;
import com.jpexs.asdec.tags.DefineButtonTag;
import com.jpexs.asdec.tags.DefineSpriteTag;
import com.jpexs.asdec.tags.DoInitActionTag;
import com.jpexs.asdec.tags.ExportAssetsTag;
import com.jpexs.asdec.tags.ShowFrameTag;
import java.util.ArrayList;
import java.util.List;

public class TagNode {

   public List<TagNode> subItems;
   public Object tag;

   public TagNode(Object tag) {
      this.tag = tag;
      this.subItems = new ArrayList<TagNode>();
   }

   @Override
   public String toString() {
      return tag.toString();
   }
   
   public static List<TagNode> createTagList(List<Object> list) {
      List<TagNode> ret = new ArrayList<TagNode>();
      int frame = 1;
      List<TagNode> frames = new ArrayList<TagNode>();

      List<ExportAssetsTag> exportAssetsTags = new ArrayList<ExportAssetsTag>();
      for (Object t : list) {
         if (t instanceof ExportAssetsTag) {
            exportAssetsTags.add((ExportAssetsTag) t);
         }
         if (t instanceof ShowFrameTag) {
            TagNode tti = new TagNode("frame" + frame);

            for (int r = ret.size() - 1; r >= 0; r--) {
               if (!(ret.get(r).tag instanceof DefineSpriteTag)) {
                  if (!(ret.get(r).tag instanceof DefineButtonTag)) {
                     if (!(ret.get(r).tag instanceof DefineButton2Tag)) {
                        if (!(ret.get(r).tag instanceof DoInitActionTag)) {
                           tti.subItems.add(ret.get(r));
                           ret.remove(r);
                        }
                     }
                  }
               }
            }
            frame++;
            frames.add(tti);
         } else if (t instanceof ASMSource) {
            TagNode tti = new TagNode(t);
            ret.add(tti);
         } else if (t instanceof Container) {
            if (((Container) t).getItemCount() > 0) {

               TagNode tti = new TagNode(t);
               List<Object> subItems = ((Container) t).getSubItems();

               tti.subItems = createTagList(subItems);
               ret.add(tti);
            }
         }

      }
      ret.addAll(frames);
      for (int i = ret.size() - 1; i >= 0; i--) {
         if (ret.get(i).tag instanceof DefineSpriteTag) {
            ((DefineSpriteTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
         }
         if (ret.get(i).tag instanceof DefineButtonTag) {
            ((DefineButtonTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
         }
         if (ret.get(i).tag instanceof DefineButton2Tag) {
            ((DefineButton2Tag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
         }
         if (ret.get(i).tag instanceof DoInitActionTag) {
            ((DoInitActionTag) ret.get(i).tag).exportAssetsTags = exportAssetsTags;
         }
         if (ret.get(i).tag instanceof ASMSource) {
            ASMSource ass = (ASMSource) ret.get(i).tag;
            if (ass.containsSource()) {
               continue;
            }
         }
         if (ret.get(i).subItems.size() == 0) {
            ret.remove(i);
         }
      }
      return ret;
   }
}
