package com.jpexs.decompiler.flash.abc.avm2.graph;

import com.jpexs.decompiler.flash.abc.avm2.model.DoubleValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.IntegerValueAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NameValuePair;
import com.jpexs.decompiler.flash.abc.avm2.model.NewArrayAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NewObjectAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.NullAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.StringAVM2Item;
import com.jpexs.decompiler.flash.abc.avm2.model.UndefinedAVM2Item;
import com.jpexs.decompiler.flash.ecma.ArrayType;
import com.jpexs.decompiler.flash.ecma.Null;
import com.jpexs.decompiler.flash.ecma.ObjectType;
import com.jpexs.decompiler.flash.ecma.Undefined;
import com.jpexs.decompiler.graph.GraphTargetDialect;
import com.jpexs.decompiler.graph.GraphTargetItem;
import com.jpexs.decompiler.graph.model.FalseItem;
import com.jpexs.decompiler.graph.model.TrueItem;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author JPEXS
 */
public class AVM2GraphTargetDialect extends GraphTargetDialect {

    public static final GraphTargetDialect INSTANCE = new AVM2GraphTargetDialect();
    
    private AVM2GraphTargetDialect() {
        
    }
    
    @Override
    public String getName() {
        return "AVM2";
    }        

    @Override
    public GraphTargetItem valToItem(Object r) {
        if (r == null) {
            return null;
        }
        if (r instanceof Boolean) {
            if ((Boolean) r) {
                return new TrueItem(this, null, null);
            } else {
                return new FalseItem(this, null, null);
            }
        }
        if (r instanceof String) {
            return new StringAVM2Item(null, null, (String) r);
        }
        if (r instanceof Long) {
            return new DoubleValueAVM2Item(null, null, (double) (Long) r);
        }
        if (r instanceof Integer) {
            return new IntegerValueAVM2Item(null, null, (Integer) r);
        }

        if (r instanceof Double) {
            return new DoubleValueAVM2Item(null, null, (Double) r);
        }
        if (r instanceof Null) {
            return new NullAVM2Item(null, null);
        }
        if (r instanceof Undefined) {
            return new UndefinedAVM2Item(null, null);
        }
        if (r instanceof ArrayType) {
            List<GraphTargetItem> vals = new ArrayList<>();
            ArrayType at = (ArrayType) r;
            for (Object v : at.values) {
                vals.add(valToItem(v));
            }
            return new NewArrayAVM2Item(null, null, vals);
        }
        if (r instanceof ObjectType) {
            List<NameValuePair> props = new ArrayList<>();
            ObjectType ot = (ObjectType) r;
            for (String k : ot.getAttributeNames()) {
                props.add(new NameValuePair(valToItem(k), valToItem(ot.getAttribute(k))));
            }
            return new NewObjectAVM2Item(null, null, props);
        }
        return null;
    }
}
