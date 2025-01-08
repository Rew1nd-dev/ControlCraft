local flap_bearing_front = nil
local flap_bearing_back = nil
local flap_bearing_left = nil
local flap_bearing_right = nil

local spinal = nil

local collective = 0
local cyclic_pitch = 0
local cyclic_yaw = 0

local propeller_angle = 0

local function getFlapTilt(_collective, _pitch, _yaw, angle)
    return _collective + _pitch * math.sin(angle - _yaw)
end

local function getPeripherals()
    flap_bearing_back = peripheral.wrap("back")
    flap_bearing_front = peripheral.wrap("front")
    flap_bearing_left = peripheral.wrap("left")
    flap_bearing_right = peripheral.wrap("right")

    spinal = peripheral.wrap("top")
end

local function override_propeller_angle()
    propeller_angle = spinal.getRelativeAngle()
end

local function control_flap_angles()
    flap_bearing_front.setAngle(getFlapTilt(collective, cyclic_pitch, cyclic_yaw, propeller_angle))
    flap_bearing_back.setAngle(getFlapTilt(collective, cyclic_pitch, cyclic_yaw, propeller_angle) + math.deg2rad(180))
    flap_bearing_left.setAngle(getFlapTilt(collective, cyclic_pitch, cyclic_yaw, propeller_angle + math.deg2rad(90))
    flap_bearing_right.setAngle(getFlapTilt(collective, cyclic_pitch, cyclic_yaw, propeller_angle) + math.deg2rad(-90))
end

local function init()
    getPeripherals()
    rednet.open("bottom")
end

local function Always_Require_CYP()
    collective = math.deg2rad(20)
    cyclic_pitch = math.deg2rad(0)
    cyclic_yaw = math.deg2rad(0)
end

local function Always_Control()
    override_propeller_angle()
end

local function main()
    init()
    while true do
        parallel.waitForAll(Always_Require_CYP, Always_Control)
        os.sleep(0.05)
    end
end

main()
