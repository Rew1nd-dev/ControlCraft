package com.verr1.controlcraft.content.gui.v1.layouts;

import com.verr1.controlcraft.content.blocks.OptionalSyncedBlockEntity;
import com.verr1.controlcraft.content.gui.v1.factory.GenericUIFactory;
import com.verr1.controlcraft.content.gui.v1.layouts.api.SwitchableTab;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.foundation.data.executor.ExpirableConditionRunnable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.Layout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static com.verr1.controlcraft.ControlCraftClient.CLIENT_DEFERRAL_EXECUTOR;

public class VerticalFlow implements SwitchableTab {
    private final GridLayout verticalLayout = new GridLayout();
    private final HashMap<NetworkKey, NetworkUIPort<?>> map = new HashMap<>();
    private final List<NetworkKey> entries; // this is meant to preserve the input order
    private final BlockPos boundBlockEntityPos;

    private final Runnable preDoLayout;
    private Component title;

    VerticalFlow(
            BlockPos boundPos,
            List<Pair<NetworkKey,
                    NetworkUIPort<?>>> entries, Runnable preDoLayout){
        this.boundBlockEntityPos = boundPos;
        this.entries = entries.stream().map(Pair::getLeft).toList();
        this.preDoLayout = preDoLayout;
        entries.forEach(e -> map.put(e.getLeft(), e.getRight()));
    }


    public void onScreenInit(){
        Minecraft mc = Minecraft.getInstance();
        Player p = mc.player;
        if(p == null)return;
        map.values().forEach(NetworkUIPort::onScreenInit);
        boundBlockEntity().ifPresentOrElse(
            be -> {
                NetworkKey[] keys = map.keySet().toArray(NetworkKey[]::new);
                be.request(keys);
                be.setDirty(keys);
                AtomicInteger row = new AtomicInteger();
                entries.forEach(port -> verticalLayout.addChild(map.get(port).layout(), row.getAndIncrement(), 0, 1, 2));
                verticalLayout.rowSpacing(4);
                verticalLayout.arrangeElements();
                var task = new ExpirableConditionRunnable
                        .builder(() -> map.values().forEach(NetworkUIPort::readToLayout))
                        .withCondition(() -> !be.isAnyDirty(keys))
                        .withExpirationTicks(40)
                        .withOrElse(
                                () -> p.sendSystemMessage(Component.literal("Block Entity Data Failed To Synced"))
                        )
                        .build();
                CLIENT_DEFERRAL_EXECUTOR.executeLater(task);
            },
            () -> p.sendSystemMessage(Component.literal("Block Entity Not Found !!!"))
        );


    }

    @Override
    public void onActivatedTab(){
        map.values().forEach(NetworkUIPort::onActivatedTab);
    }

    @Override
    public void onRemovedTab() {
        map.values().forEach(NetworkUIPort::onRemovedTab);
    }

    @Override
    public void onScreenTick() {
        map.values().forEach(NetworkUIPort::onScreenTick);
    }

    @Override
    public void onAddRenderable(Collection<AbstractWidget> toAdd) {
        map.values().forEach(p -> p.onAddRenderable(toAdd));
    }


    private void traverse(LayoutElement root, BiConsumer<WidgetContext, LayoutElement> consumer, Context context){
        consumer.accept(new WidgetContext(context.layer, root.getClass()), root);
        if (root instanceof Layout l) {
            l.visitChildren(c -> traverse(c, consumer, context.addLayer()));
        }
    }

    public void  traverseAllChildWidget(BiConsumer<WidgetContext, LayoutElement> consumer){
        traverse(verticalLayout, consumer, new Context());
    }



    public void onClose(){
        boundBlockEntity().ifPresent(
            be -> {
                NetworkKey[] keys = map.keySet().toArray(NetworkKey[]::new);
                // be.activateLock(keys); // maybe not needed
                map.values().forEach(NetworkUIPort::writeFromLayout);
                be.syncToServer(keys);
            }
        );
    }


    private Optional<OptionalSyncedBlockEntity> boundBlockEntity(){
        return GenericUIFactory.boundBlockEntity(boundBlockEntityPos, OptionalSyncedBlockEntity.class);
    }

    @Override
    public @NotNull Component getTabTitle() {
        return this.title;
    }

    public VerticalFlow withTitle(Component title) {
        this.title = title;
        return this;
    }

    @Override
    public void visitChildren(@NotNull Consumer<AbstractWidget> consumer) {
        traverseAllChildWidget((ctx, w) -> {
            if(w instanceof AbstractWidget aw)consumer.accept(aw);
        });
    }

    @Override
    public void doLayout(@NotNull ScreenRectangle screenRectangle) {
        preDoLayout.run();
        this.verticalLayout.setX(screenRectangle.left() + 6);
        this.verticalLayout.setY(screenRectangle.top() + 6);
        this.verticalLayout.arrangeElements();
        // FrameLayout.alignInRectangle(this.verticalLayout, screenRectangle, 0.5F, 0.16666667F);
    }

    public static class Context{
        public int layer = 0;

        public Context addLayer(){
            layer++;
            return this;
        }

    }

    public record WidgetContext(int layer, Class<?> clazz){
    }

    public static class builder{
        BlockPos pos;
        List<Pair<NetworkKey, NetworkUIPort<?>>> list = new ArrayList<>();
        Runnable preDoLayout = () -> {};

        public builder(BlockPos pos){
            this.pos = pos;
        }

        public VerticalFlow build(){
            return new VerticalFlow(pos, list, preDoLayout);
        }

        public builder withPreDoLayout(Runnable postDoLayout){
            this.preDoLayout = postDoLayout;
            return this;
        }

        public builder withPort(
                NetworkKey key,
                NetworkUIPort<?> port
        ){
            list.add(ImmutablePair.of(key, port));
            return this;
        }

    }


}
