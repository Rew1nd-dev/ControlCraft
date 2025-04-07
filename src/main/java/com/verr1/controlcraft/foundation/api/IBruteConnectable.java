package com.verr1.controlcraft.foundation.api;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/*
 *  When Using Tool Gun to Connect Bodies, 2 Vertical Direction Is Selected At Assemble Ship:
 *            to-set-Align face and to-set-Forward face
 *  When This Ship Is BruteConnect By ICanBruteDirectionalConnect ,
 *           to-set-Align face will be force to be the opposite of getAlign()
 *           to-set-Forward face will be force to be set parallel with getForward()
 *  So In This Case, getAlign() should always vertical to getForward()
 *
 *  The bruteDirectionalConnectWith takes compDir as the second argument
 *  2 reference direction of Companion ship is required in order to calculate constrain
 *
 * */

public interface IBruteConnectable {

    void bruteDirectionalConnectWith(BlockPos pos, Direction align, Direction forward);

    Direction getAlign();

    Direction getForward();
}
