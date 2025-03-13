package com.verr1.controlcraft.foundation.data;

public record PeripheralKey(String Name, long Protocol) {
    public static PeripheralKey NULL = new PeripheralKey("null", 0);
    @Override
    public int hashCode() {
        return Long.hashCode(Protocol);
    }


}
