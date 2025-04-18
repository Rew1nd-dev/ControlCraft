package com.verr1.controlcraft.content.gui.wand;

import com.simibubi.create.AllKeys;
import com.verr1.controlcraft.content.blocks.joints.AbstractJointBlockEntity;
import com.verr1.controlcraft.content.blocks.motor.*;
import com.verr1.controlcraft.content.blocks.slider.DynamicSliderBlockEntity;
import com.verr1.controlcraft.content.blocks.slider.KinematicSliderBlockEntity;
import com.verr1.controlcraft.foundation.data.WandSelection;
import com.verr1.controlcraft.foundation.managers.ClientOutliner;
import com.verr1.controlcraft.foundation.type.descriptive.WandGUIModesType;
import com.verr1.controlcraft.foundation.type.WandModesType;
import com.verr1.controlcraft.registry.ControlCraftItems;
import com.verr1.controlcraft.utils.MathUtils;
import com.verr1.controlcraft.utils.MinecraftUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.joml.Matrix4d;
import org.joml.Matrix4dc;
import org.joml.Vector3d;
import org.joml.Vector3dc;

import java.awt.*;
import java.util.Optional;

import static com.verr1.controlcraft.foundation.vsapi.ValkyrienSkies.toMinecraft;

@OnlyIn(Dist.CLIENT)
public class WandGUI implements IGuiOverlay {

    private final ModeSelectionScreen selectionScreen;
    public WandModesType currentType = WandModesType.DESTROY;

    private boolean shouldSetModeByLooking = false;

    public WandGUI(){
        selectionScreen = new ModeSelectionScreen(WandGUIModesType.getAllTypes(), this::setMode);
    }



    public void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        if (Minecraft.getInstance().screen != null)
            return;
        if (selectionScreen.focused) {
            selectionScreen.cycle((int) event.getScrollDelta());
            event.setCanceled(true);
        }
    }

    public void onKeyInput(int key, boolean pressed) {
        if (!isClientWandInHand())
            return;
        if (key != AllKeys.TOOL_MENU.getBoundCode())
            return;

        if (pressed && !selectionScreen.focused)
            selectionScreen.focused = true;
        if (!pressed && selectionScreen.focused) {
            selectionScreen.focused = false;
            selectionScreen.onClose();
        }
    }

    @Override
    public void render(ForgeGui gui, GuiGraphics graphics, float partialTicks, int width, int height) {
        if (Minecraft.getInstance().options.hideGui)
            return;
        if (isClientWandInHand()){
            selectionScreen.renderPassive(graphics, partialTicks);
        }

    }

    public static boolean isClientWandInHand(){
        return Optional.ofNullable(Minecraft.getInstance().player)
                .map(player -> player.getMainHandItem().getItem() == ControlCraftItems.ALL_IN_WAND.get())
                .orElse(false);
    }

    public void setModeByLooking(){
        if(WandModesType.modeOf(currentType).isRunning())return;
        Optional
            .ofNullable(MinecraftUtils.lookingAt())
            .ifPresent(
                blockEntity -> {
                    if(blockEntity instanceof DynamicRevoluteMotorBlockEntity || blockEntity instanceof KinematicRevoluteMotorBlockEntity){
                        currentType = WandModesType.SERVO;
                    }
                    if(blockEntity instanceof DynamicJointMotorBlockEntity || blockEntity instanceof KinematicJointMotorBlockEntity){
                        currentType = WandModesType.JOINT;
                    }
                    if(blockEntity instanceof DynamicSliderBlockEntity || blockEntity instanceof KinematicSliderBlockEntity){
                        currentType = WandModesType.SLIDER;
                    }
                    if(blockEntity instanceof AbstractJointBlockEntity){
                        currentType = WandModesType.HINGE;
                    }
                }
            );
    }

    public void renderOffset(){
        if(!isClientWandInHand() && !isWrenchInHand())return;
        Optional
            .ofNullable(MinecraftUtils.lookingAt())
            .filter(AbstractMotor.class::isInstance)
            .map(AbstractMotor.class::cast)
            .ifPresent(
                motor -> {
                    Matrix4dc transform = Optional.ofNullable(motor.getShipOn()).map(s -> s.getTransform().getShipToWorld()).orElse(new Matrix4d());
                    Vector3dc offsetCenter = transform.transformPosition(motor.getRotationCenterPosJOML());
                    ClientOutliner.drawOutline(toMinecraft(MathUtils.centerWithRadius(offsetCenter, 0.05)), Color.RED.getRGB(), "offset", 0.4);
                }
            );
    }

    public void tick(){
        if(!isClientWandInHand())return;
        selectionScreen.update();
        if(shouldSetModeByLooking)setModeByLooking();
        WandModesType.modeOf(currentType).onTick();
        renderOffset();
    }



    public void select(WandSelection selection){
        WandModesType.modeOf(currentType).onSelection(selection);
    }

    public void deselect(){
        WandModesType.modeOf(currentType).onDeselect();
    }

    public void flush(){
        WandModesType.modeOf(currentType).onClear();
    }

    public void confirm(){
        WandModesType.modeOf(currentType).onConfirm();
    }


    public void setMode(WandGUIModesType mode){
        switch (mode){
            case DISCONNECT:
                currentType = WandModesType.DESTROY;
                break;
            case DISCONNECT_ALL:
                currentType = WandModesType.DESTROY_ALL;
                break;
        }
        shouldSetModeByLooking = (mode == WandGUIModesType.CONNECT);
    }


    public void onRightClick(PlayerInteractEvent.RightClickBlock event){
        if(!event.getLevel().isClientSide)return;
        if(event.getHand() != InteractionHand.MAIN_HAND)return;
        if(!isClientWandInHand())return;
        WandSelection selection = new WandSelection(event.getPos(), event.getFace());
        select(selection);
    }


    public void onWandClick(InputEvent.InteractionKeyMappingTriggered event){
        if(isClientWandInHand())event.setSwingHand(false);
    }



    public void onRightClickEmpty(boolean shiftDown){
        if(!shiftDown)confirm();
        if(shiftDown)flush();
    }


    public void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event){
        if(!event.getLevel().isClientSide)return;
        if(!isClientWandInHand())return;
        flush();

    }

    public static boolean isWrenchInHand(){
        if(Minecraft.getInstance().player == null)return false;
        return Minecraft.getInstance().player.getMainHandItem().is(com.simibubi.create.AllItems.WRENCH.asItem());
    }

}
