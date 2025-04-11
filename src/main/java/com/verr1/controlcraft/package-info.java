package com.verr1.controlcraft;

/*
*   TODO:
*    √ xyz offset of motors
*    × chunk loader on/off button, loads chunks base on ship's AABB size
*    × Angle limit of motors
*    √ Reworked gui
*    - Dynamic / Kinematic Spherical Joint
*    - Camera Remote Link
*    × Create Speed Input (However I want to remove this)
*    - PhysBearing, with wand connect compact
*    × Reworked Jet Particle
*    √ Gravitational Anchor, Resistance force at pos
*    √ Acceleration limit of thrusters and propellers (low speed == no limit, high speed == limit acceleration)
*    - Make Flap Bearing Easier to Assemble
*    √ Add Inertia Mass Fix for Scaled Ships
*   fixes:
*    - camera reset √
*    - propeller redstone is not saved √
*    - double rotation to adjust to qp, maybe the cause of non-shortest path in slerp space
* */