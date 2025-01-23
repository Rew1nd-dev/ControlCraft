package com.verr1.vscontrolcraft.blocks.magnet;

import com.google.common.collect.Sets;
import com.verr1.vscontrolcraft.ControlCraft;
import com.verr1.vscontrolcraft.compat.valkyrienskies.generic.QueueForceInducer;
import com.verr1.vscontrolcraft.compat.valkyrienskies.magnet.LogicalMagnet;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3d;
import org.joml.Vector3dc;
import org.valkyrienskies.core.api.ships.ServerShip;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class MagnetManagerlegacy {
    private static int lazyTickRate = 30;
    private static int lazyTick = 0;
    private static int MAX_PAIR_CAN_MAKE_IN_1_TICK = 10;

    private static final ConcurrentHashMap<LogicalMagnet, LogicalMagnet> right2left = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<LogicalMagnet, LogicalMagnet> left2right = new ConcurrentHashMap<>();

    private static final Set<LogicalMagnet> waitingForPairing = Sets.newConcurrentHashSet();
    /*
    private static Iterator<LogicalMagnet> iterator_i = null;
    private static Iterator<LogicalMagnet> iterator_j = null;

    public Iterator<LogicalMagnet> getOrCreate_Iterator_i(){
        if(iterator_i == null)iterator_i = waitingForPairing.stream().iterator();
        return iterator_i;
    }

    public Iterator<LogicalMagnet> getOrCreate_Iterator_j(){
        if(iterator_i == null)iterator_j = waitingForPairing.stream().iterator();
        return iterator_j;
    }
    * */


    public static boolean isRegistered(LogicalMagnet element){
        return isRightKey(element) || isLeftKey(element);
    }

    private static boolean isRightKey(LogicalMagnet element){
        return right2left.containsKey(element);
    }

    private static boolean isLeftKey(LogicalMagnet element){
        return left2right.containsKey(element);
    }

    public static boolean safeRegister(LogicalMagnet left, LogicalMagnet right){
        if(isRegistered(left) || isRegistered(right))return false;
        register(left, right);
        return true;
    }

    private static void register(LogicalMagnet left, LogicalMagnet right){
        right2left.put(right, left);
        left2right.put(left, right);
    }

    private static void removeUnchecked(LogicalMagnet left, LogicalMagnet right){
        //if(!isLeftKey(left) || !isRightKey(right))return;
        right2left.remove(right);
        left2right.remove(left);
    }

    private static void remove(LogicalMagnet key){
        if(isRightKey(key)){
            LogicalMagnet leftKey = right2left.get(key);
            removeUnchecked(leftKey, key);
        }
        if(isLeftKey(key)){
            LogicalMagnet rightKey = left2right.get(key);
            removeUnchecked(key, rightKey);
        }
    }

    public static void wannaPair(LogicalMagnet magnet){
        if(isRegistered(magnet))return;
        if(waitingForPairing.contains(magnet))return;
        waitingForPairing.add(magnet);
    }

    private static void quitPair(LogicalMagnet magnet){
        waitingForPairing.remove(magnet);
    }

    private static boolean isValid(LogicalMagnet element){
        if(element.level().getExistingBlockEntity(element.pos()) instanceof MagnetBlockEntity magnet){
            return !magnet.isRemoved();
        }
        return false;
    }

    private static void removeInvalidRegistry(){
        List<LogicalMagnet> invalids = new ArrayList<>();
        right2left.forEach((r, l)->{
            if(isValid(r) && isValid(l))return;
            invalids.add(r);
        });
        invalids.forEach(MagnetManagerlegacy::remove);
    }

    public static void lazyTick(){
        removeInvalidRegistry();
    }

    private static @Nullable MagnetBlockEntity getExisting(LogicalMagnet element){
        if(element.level().getExistingBlockEntity(element.pos()) instanceof MagnetBlockEntity magnet){
            if(magnet.isRemoved())return null;
            return magnet;
        }
        return null;
    }

    private static @Nullable MagnetBlockEntity getExistingUnchecked(LogicalMagnet element){
        return (MagnetBlockEntity) element.level().getExistingBlockEntity(element.pos());
    }

    public static void applyAttractionFor(LogicalMagnet left, LogicalMagnet right){
        if(!(isValid(left) && isValid(right)))return;
        MagnetBlockEntity leftMagnet = getExistingUnchecked(left);
        MagnetBlockEntity rightMagnet = getExistingUnchecked(right);
        if(leftMagnet == null || rightMagnet == null)return;
        ServerShip leftShip = leftMagnet.getServerShipOn();
        ServerShip rightShip = rightMagnet.getServerShipOn();
        Vector3dc leftPos = leftMagnet.getPosition_wc();
        Vector3dc rightPos = rightMagnet.getPosition_wc();
        Vector3dc leftRelative = leftMagnet.getRelativePosition();
        Vector3dc rightRelative = rightMagnet.getRelativePosition();
        Vector3dc dir_l2r = new Vector3d(rightPos).sub(leftPos);
        double scale = 1 / (dir_l2r.lengthSquared() + 1e-3) * (leftMagnet.getStrength() * rightMagnet.getStrength());
        if(leftShip != null){
            QueueForceInducer qfi = QueueForceInducer.getOrCreate(leftShip);
            qfi.applyInvariantForceToPos(new Vector3d(dir_l2r).mul( scale), leftRelative);
        }
        if(rightShip != null){
            QueueForceInducer qfi = QueueForceInducer.getOrCreate(rightShip);
            qfi.applyInvariantForceToPos(new Vector3d(dir_l2r).mul(-scale), rightRelative);
        }

    }

    private static double distanceBetween(LogicalMagnet i, LogicalMagnet j){
        if(i.equals(j))return Integer.MAX_VALUE;
        //if(!i.level().dimensionType().equals(j.level().dimensionType()))return Integer.MAX_VALUE;
        if(!(isValid(i) && isValid(j)))return Integer.MAX_VALUE;
        return getExistingUnchecked(i).getPosition_wc().sub(getExistingUnchecked(j).getPosition_wc(), new Vector3d()).lengthSquared();
    }

    public static void makeOnePair(){
        Object[] magnets = waitingForPairing.toArray();
        if(magnets.length <= 1)return;
        LogicalMagnet m_i = (LogicalMagnet) magnets[0];
        LogicalMagnet m_j = (LogicalMagnet) magnets[0];
        for(int trial = 0; trial < 3; trial++){
            LogicalMagnet finalM_i = m_i;
            m_j = (LogicalMagnet)Arrays.stream(magnets).min(Comparator.comparing(m -> distanceBetween((LogicalMagnet)m, finalM_i))).orElse(m_j);
            LogicalMagnet finalM_j = m_j;
            m_i = (LogicalMagnet)Arrays.stream(magnets).min(Comparator.comparing(m -> distanceBetween((LogicalMagnet)m, finalM_j))).orElse(m_i);
        }
        if(m_i.equals(m_j)){
            ControlCraft.LOGGER.info("MakeOnePair Found Invalid Pair, Algorithm May Have Some Issue");
            return;
        }
        waitingForPairing.remove(m_i);
        waitingForPairing.remove(m_j);
        safeRegister(m_i, m_j);
    }

    public static void clearInvalidAtWaiting(){
        waitingForPairing.removeIf(w -> !isValid(w));
    }

    public static void makePairs(){
        clearInvalidAtWaiting();
        for(int i = 0; i < MAX_PAIR_CAN_MAKE_IN_1_TICK; i++){
            makeOnePair();
        }
    }


    public static void applyAttractionForAll(){
        right2left.forEach(MagnetManagerlegacy::applyAttractionFor);
    }

    public static void tick(){
        if(lazyTick-- < 0){
            lazyTick = lazyTickRate;
            lazyTick();
        }

        applyAttractionForAll();
        makePairs();
    }




}
