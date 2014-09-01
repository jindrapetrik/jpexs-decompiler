package com.jpexs.proxy;

class Key {

    private String name = null;

    /**
     * Create a Key.
     */
    Key(String name) {
        this.name = name;
    }

    /**
     * Return a lowercase hashCode.
     */
    public int hashCode() {
        String s = name.toLowerCase();
        return s.hashCode();
    }

    /**
     * Return a lowercase equals.
     */
    public boolean equals(Object obj) {
        return name.equalsIgnoreCase(obj.toString());
    }

    /**
     * Return the key.
     */
    public String toString() {
        return name;
    }
}
