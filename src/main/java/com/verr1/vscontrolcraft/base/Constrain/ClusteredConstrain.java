package com.verr1.vscontrolcraft.base.Constrain;

import com.verr1.vscontrolcraft.ControlCraft;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class ClusteredConstrain {
    private final HashMap<Integer, HashSet<Long>> group2ships = new HashMap<>();
    private final HashMap<Integer, HashSet<SavedConstrainObject>> group2constraints = new HashMap<>();
    private final HashMap<Long, Integer> getShipGroup = new HashMap<>();
    private int groupIDCounter = 0;


    public boolean empty(){
        return group2ships.isEmpty();
    }

    public static ClusteredConstrain create(List<SavedConstrainObject> constraints) {
        ClusteredConstrain clusteredConstrain = new ClusteredConstrain();
        constraints.forEach(clusteredConstrain::Join_Create_Merge);
        return clusteredConstrain;
    }

    public Integer getGroupId(Long shipID){
        return getShipGroup.get(shipID);
    }

    public HashSet<SavedConstrainObject> getGroupConstraints(Integer groupID){
        return group2constraints.get(groupID);
    }

    public void deleteGroup(Integer groupID){
        group2ships.get(groupID).forEach(getShipGroup::remove);
        group2ships.remove(groupID);
        group2constraints.remove(groupID);
    }

    public void merge(Integer g_0, Integer g_1, SavedConstrainObject constrain){
        if(!g_0.equals(g_1)){
            group2ships.get(g_0).addAll(group2ships.get(g_1));
            group2ships.get(g_0).forEach(shipID -> getShipGroup.put(shipID, g_0));
            group2ships.remove(g_1);
        }
        group2constraints.get(g_0).add(constrain);
    }

    public void createWith(Long shipID_0, Long shipID_1, SavedConstrainObject constrain){
        groupIDCounter++;
        group2ships.computeIfAbsent(groupIDCounter, k -> new HashSet<>()).add(shipID_0);
        group2ships.computeIfAbsent(groupIDCounter, k -> new HashSet<>()).add(shipID_1);
        getShipGroup.put(shipID_0, groupIDCounter);
        getShipGroup.put(shipID_1, groupIDCounter);
        group2constraints.computeIfAbsent(groupIDCounter, k -> new HashSet<>()).add(constrain);
    }

    public void join(Integer groupID, Long shipID, SavedConstrainObject constrain){
        if(!group2ships.containsKey(groupID))return;
        getShipGroup.put(shipID, groupID);
        group2ships.get(groupID).add(shipID);
    }

    public void Join_Create_Merge(SavedConstrainObject constraint){
        try{
            Long shipID_0 = constraint.constrain().constraint().getShipId0();
            Long shipID_1 = constraint.constrain().constraint().getShipId1();
            Integer groupID_0 = getGroupId(shipID_0);
            Integer groupID_1 = getGroupId(shipID_1);
            if(groupID_0 == null && groupID_1 == null){
                createWith(shipID_0, shipID_1, constraint);
            }
            else if(groupID_0 == null){
                join(groupID_1, shipID_0, constraint);
            }
            else if(groupID_1 == null){
                join(groupID_0, shipID_1, constraint);
            }
            else{
                merge(groupID_0, groupID_1, constraint);
            }
        }catch (Exception e){
            ControlCraft.LOGGER.error("Failed To Load Constrain: " + constraint.toString());
        }


    }

}
