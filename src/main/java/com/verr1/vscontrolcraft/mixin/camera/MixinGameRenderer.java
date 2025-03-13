package com.verr1.vscontrolcraft.mixin.camera;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.verr1.vscontrolcraft.base.ICameraAccessor;
import com.verr1.vscontrolcraft.blocks.camera.CameraBlockEntity;
import com.verr1.vscontrolcraft.blocks.camera.LinkedCameraManager;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.world.phys.Vec3;
import org.joml.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import org.valkyrienskies.core.api.ships.ClientShip;

import java.lang.Math;

@Mixin(value = GameRenderer.class)
abstract class MixinGameRenderer {
    @Shadow
    @Final
    private Minecraft minecraft;
    // region Mount the camera to the ship
    @Shadow
    @Final
    private Camera mainCamera;

    @Shadow
    protected abstract double getFov(Camera camera, float f, boolean bl);

    @Shadow
    public abstract Matrix4f getProjectionMatrix(double d);

    @WrapOperation(
            method = "renderLevel",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/LevelRenderer;prepareCullFrustum(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;Lorg/joml/Matrix4f;)V"
            )
    )
    private void setupCameraWithMountedShip(final LevelRenderer instance, final PoseStack ignore, final Vec3 vec3,
                                            final Matrix4f matrix4f, final Operation<Void> prepareCullFrustum, final float partialTicks,
                                            final long finishTimeNano, final PoseStack matrixStack) {

        final CameraBlockEntity linkedCamera = LinkedCameraManager.getLinkedCamera();
        if (linkedCamera == null) {
            prepareCullFrustum.call(instance, matrixStack, vec3, matrix4f);
            return;
        }

        ClientShip cameraClientShip = linkedCamera.getClientShip();
        Vector3dc cameraPosOnShip = linkedCamera.getCameraPositionShip();

        ((ICameraAccessor) mainCamera).controlCraft$setupWithShipMounted(
                this.minecraft.level,
                this.minecraft.getCameraEntity() == null ? this.minecraft.player : this.minecraft.getCameraEntity(),
                true,
                false,
                partialTicks,
                cameraClientShip,
                cameraPosOnShip
        );

        // Apply the ship render transform to [matrixStack]
        if(cameraClientShip != null){
            final Quaternionf invShipRenderRotation = new Quaternionf(
                    cameraClientShip.getRenderTransform().getShipToWorldRotation().conjugate(new Quaterniond())
            );
            matrixStack.mulPose(invShipRenderRotation);
        }

        // We also need to recompute [inverseViewRotationMatrix] after updating [matrixStack]
        {
            final Matrix3f matrix3f = new Matrix3f(matrixStack.last().normal());
            matrix3f.invert();
            RenderSystem.setInverseViewRotationMatrix(matrix3f);
        }

        // Camera FOV changes based on the position of the camera, so recompute FOV to account for the change of camera
        // position.
        final double fov = this.getFov(mainCamera, partialTicks, true);

        // Use [camera.getPosition()] instead of [vec3] because mounting the player to the ship has changed the camera
        // position.
        prepareCullFrustum.call(instance, matrixStack, mainCamera.getPosition(),
                this.getProjectionMatrix(Math.max(fov, this.minecraft.options.fov().get())));
    }
}





