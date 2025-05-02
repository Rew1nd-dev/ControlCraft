package com.verr1.controlcraft.content.blocks.transmitter;

import com.verr1.controlcraft.foundation.data.PeripheralKey;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkManager {
    static Map<PeripheralKey, BlockPos> REGISTERED_POS = new ConcurrentHashMap<>();
    static private final Map<BlockPos, PeripheralKey> REGISTERED_KEYS = new ConcurrentHashMap<>();

    static Map<BlockPos, Integer> KEY_LIVES = new ConcurrentHashMap<>(); //dealing with unknown miss-unregister issue
    static int TICKS_BEFORE_EXPIRED = 10;

    public static void UnregisterWirelessPeripheral(PeripheralKey key) {
        if(key == null)return;
        if(!isRegistered(key))return;
        BlockPos value = REGISTERED_POS.get(key);
        unregister(key, value);
    }

    private static void unregister(PeripheralKey key, BlockPos value) {
        REGISTERED_POS.remove(key);
        REGISTERED_KEYS.remove(value);
        KEY_LIVES.remove(value);
    }

    private static void register(PeripheralKey key, BlockPos pos) {
        REGISTERED_POS.put(key, pos);
        REGISTERED_KEYS.put(pos, key);
        KEY_LIVES.put(pos, TICKS_BEFORE_EXPIRED);
    }

    public static void activate(BlockPos pos){
        if(!isRegistered(pos))return;
        KEY_LIVES.put(pos, TICKS_BEFORE_EXPIRED);
    }

    private static void tickActivated(){
        KEY_LIVES.forEach((k, v) -> {
            if(v < 0){
                UnregisterWirelessPeripheral(REGISTERED_KEYS.get(k));
            }
        });
        KEY_LIVES.entrySet().removeIf(e -> e.getValue() < 0);
        KEY_LIVES.entrySet().forEach(e-> e.setValue(e.getValue() - 1));
    }

    public static void tick(){
        tickActivated();
    }

    private static boolean canMove(PeripheralKey newKey, BlockPos content){
        return isRegistered(content) && !isRegistered(newKey);
    }


    private static void move(PeripheralKey newKey, BlockPos content){
        if(!canMove(newKey, content))return;
        PeripheralKey oldKey = REGISTERED_KEYS.get(content);
        REGISTERED_POS.remove(oldKey);
        REGISTERED_KEYS.remove(content);
        register(newKey, content);
    }

    public static boolean isRegistered(BlockPos pos){
        return REGISTERED_KEYS.containsKey(pos);
    }

    public static boolean isRegistered(PeripheralKey key){
        return REGISTERED_POS.containsKey(key);
    }

    public static BlockPos getRegisteredPeripheralPos(PeripheralKey key) {
        if(!isRegistered(key))return null;
        return REGISTERED_POS.get(key);
    }


    public static PeripheralKey registerAndGetKey(@NotNull PeripheralKey newKey, BlockPos content) {
        if(isRegistered(content)){
            if(canMove(newKey, content)){
                move(newKey, content);
                return newKey;
            }
            return REGISTERED_KEYS.get(content);
        }
        if(isRegistered(newKey))return PeripheralKey.NULL;
        register(newKey, content);

        return newKey;
    }

}
