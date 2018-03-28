package com.jpexs.decompiler.flash.generators;

import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author JPEXS
 */
public class SwitchVariantsGenerator {

    /*
    switch(x)
    {
       place1:
       place2:
       place3:
    }
    
    Variants:
    A) place of default (place1,place2,place3 or no place)
    B) place1 can be same as place2. place2 can be same as place3 if place3 present.
        B.2) if there is default on that place, it cannot be same as following
    C) places that are not same as following place have break or do not have break,
        C.2) last one place does not have break (its presence does not matter)
     */
    public static void main(String[] args) {
        int numValueCases = 2;

        int maxSeedBits = getRandomBitsNeeded(numValueCases);
        long maxSeed = 1 << (maxSeedBits + 1);
        Set<Switch> switchSet = new HashSet<>();

        for (long s = 0; s < maxSeed; s++) {
            seed = s;
            Switch p = new Switch();

            //A
            p.defaultPlace = rnd(0, numValueCases + 1); //0 = no default clause        
            if (p.defaultPlace > 0) {
                p.numPlaces = numValueCases + 1;
            } else {
                p.numPlaces = numValueCases;
            }
            //B

            for (int place = 1; place < p.numPlaces /*not counting last*/; place++) {
                if (place == p.defaultPlace) { //B.2
                    p.placeSameAsNext.put(place, false);
                } else {
                    p.placeSameAsNext.put(place, rndBool());
                }
            }
            p.placeSameAsNext.put(p.numPlaces, Boolean.FALSE); //last one cannot be same, but put there false
            //C
            for (int place = 1; place < p.numPlaces /*last one break does not matter*/; place++) {
                if (!p.placeSameAsNext.get(place)) {
                    p.placeHasBreak.put(place, rndBool());
                }
            }
            switchSet.add(p);
        }

        Switch.toStringPrefix = Switch.TAB;
        int pos = 0;
        for (Switch s : switchSet) {
            pos++;
            System.out.println("function test" + pos + "()");
            System.out.println("{");
            System.out.print(s.toString());
            System.out.println("}");
        }
    }

    private static class Switch {

        public int defaultPlace;
        public int numPlaces;
        public Map<Integer, Boolean> placeSameAsNext = new HashMap<>();
        public Map<Integer, Boolean> placeHasBreak = new HashMap<>();

        public static final String TAB = "   ";

        public static String toStringPrefix = "";

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 31 * hash + this.defaultPlace;
            hash = 31 * hash + this.numPlaces;
            hash = 31 * hash + Objects.hashCode(this.placeSameAsNext);
            hash = 31 * hash + Objects.hashCode(this.placeHasBreak);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final Switch other = (Switch) obj;
            if (this.defaultPlace != other.defaultPlace) {
                return false;
            }
            if (this.numPlaces != other.numPlaces) {
                return false;
            }
            if (!Objects.equals(this.placeSameAsNext, other.placeSameAsNext)) {
                return false;
            }
            if (!Objects.equals(this.placeHasBreak, other.placeHasBreak)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            final String NL = "\r\n";
            sb.append(toStringPrefix).append("var _loc1_=random(100);").append(NL);
            sb.append(toStringPrefix).append("switch(_loc1_)").append(NL);
            sb.append(toStringPrefix).append("{").append(NL);
            int checkedVal = 0;
            for (int place = 1; place <= numPlaces; place++) {
                sb.append(toStringPrefix);
                if (defaultPlace == place) {
                    sb.append(TAB).append("default");
                } else {
                    checkedVal++;
                    sb.append(TAB).append("case ").append(checkedVal);
                }
                sb.append(":").append(NL);
                if (!placeSameAsNext.get(place)) {
                    sb.append(toStringPrefix).append(TAB).append(TAB).append("trace(\"place").append(place).append("\");").append(NL);
                    if (placeHasBreak.containsKey(place) && placeHasBreak.get(place)) {
                        sb.append(toStringPrefix).append(TAB).append(TAB).append("break;").append(NL);
                    }
                }
            }

            sb.append(toStringPrefix).append("}").append(NL);
            sb.append(toStringPrefix).append("trace(\"after switch\");").append(NL);
            return sb.toString();
        }

    }

    //private static Random RANDOM = new Random();
    private static long seed = 0;

    private static boolean rndBool() {
        return nextBit() == 1;
    }

    private static int rnd(int min, int max) {
        return nextInt(max - min + 1) + min;
    }

    private static int nextInt(int bound) {
        int bits = getBitsNeeded(bound);
        int value = 0;
        for (int i = 0; i < bits; i++) {
            value = value + (nextBit() << i);
        }
        return value;
    }

    private static int nextBit() {
        int bit = (int) (seed & 1);
        seed = seed >> 1;
        return bit;
    }

    private static int getBitsNeeded(int value) {
        int numBits = 0;
        while (value > 0) {
            numBits++;
            value = value >> 1;
        }
        return numBits;
    }

    private static int getRandomBitsNeeded(int numCases) {
        int x = 0;
        int value = numCases + 1;
        while (value > 0) {
            x++;
            value = value >> 1;
        }
        int y = numCases - 1;
        int z = y + 2;
        return x + y + z;
    }
}
