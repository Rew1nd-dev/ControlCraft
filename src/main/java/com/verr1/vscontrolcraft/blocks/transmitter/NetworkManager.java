package com.verr1.vscontrolcraft.blocks.transmitter;

import com.verr1.vscontrolcraft.blocks.recevier.PeripheralKey;
import net.minecraft.core.BlockPos;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NetworkManager {
    static Map<PeripheralKey, BlockPos> RegisteredPos = new ConcurrentHashMap<>();
    static private final Map<BlockPos, PeripheralKey> RegisteredKeys = new ConcurrentHashMap<>();

    static Map<BlockPos, Integer> keyLife = new ConcurrentHashMap<>(); //dealing with unknown miss-unregister issue
    static int TICKS_BEFORE_EXPIRED = 10;

    public static void UnregisterWirelessPeripheral(PeripheralKey key) {
        if(key == null)return;
        if(!isRegistered(key))return;
        BlockPos value = RegisteredPos.get(key);
        unregister(key, value);
    }

    private static void unregister(PeripheralKey key, BlockPos value) {
        RegisteredPos.remove(key);
        RegisteredKeys.remove(value);
        keyLife.remove(value);
    }

    private static void register(PeripheralKey key, BlockPos pos) {
        RegisteredPos.put(key, pos);
        RegisteredKeys.put(pos, key);
        keyLife.put(pos, TICKS_BEFORE_EXPIRED);
    }

    public static void activate(BlockPos pos){
        if(!isRegistered(pos))return;
        keyLife.put(pos, TICKS_BEFORE_EXPIRED);
    }

    public static void tickActivated(){
        keyLife.forEach((k, v) -> {
            if(v < 0){
                UnregisterWirelessPeripheral(RegisteredKeys.get(k));
            }
        });
        keyLife.entrySet().removeIf(e -> e.getValue() < 0);
        keyLife.entrySet().forEach(e-> e.setValue(e.getValue() - 1));
    }

    public static void tick(){
        tickActivated();
    }

    private static boolean canMove(PeripheralKey newKey, BlockPos content){
        return isRegistered(content) && !isRegistered(newKey);
    }


    private static void move(PeripheralKey newKey, BlockPos content){
        if(!canMove(newKey, content))return;
        PeripheralKey oldKey = RegisteredKeys.get(content);
        RegisteredPos.remove(oldKey);
        RegisteredKeys.remove(content);
        register(newKey, content);
    }

    public static boolean isRegistered(BlockPos pos){
        return RegisteredKeys.containsKey(pos);
    }

    public static boolean isRegistered(PeripheralKey key){
        return RegisteredPos.containsKey(key);
    }

    public static BlockPos getRegisteredPeripheralPos(PeripheralKey key) {
        if(!isRegistered(key))return null;
        return RegisteredPos.get(key);
    }


    public static PeripheralKey registerAndGetKey(@NotNull PeripheralKey newKey, BlockPos content) {
        if(isRegistered(content)){
            if(canMove(newKey, content)){
                move(newKey, content);
                return newKey;
            }
            return RegisteredKeys.get(content);
        }
        if(isRegistered(newKey))return PeripheralKey.NULL;
        register(newKey, content);

        return newKey;
    }

}
