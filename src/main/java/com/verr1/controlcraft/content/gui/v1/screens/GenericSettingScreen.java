package com.verr1.controlcraft.content.gui.v1.screens;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.verr1.controlcraft.content.gui.v1.layouts.TabSwitch;
import com.verr1.controlcraft.content.gui.v1.layouts.VerticalFlow;
import com.verr1.controlcraft.foundation.api.SizedScreenElement;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GenericSettingScreen extends AbstractSimiScreen {

    public static class builder{
        ArrayList<Runnable> tickTasks = new ArrayList<>();
        ItemStack renderedStack = null;
        HashMap<String, TabWithButton> tabs = new HashMap<>();
        TabSwitch tabManager = new TabSwitch();
        SizedScreenElement background = ControlCraftGuiTextures.SIMPLE_BACKGROUND_QUARTER;
        BlockPos pos;

        public builder(BlockPos pos){
            this.pos = pos;
        }

        public builder withTab(String name, VerticalFlow tab){
            IconButton button = new IconButton(0, 0, AllIcons.I_TOOLBOX).withCallback(
                    () -> tabManager.setCurrentTab(tab, true)

            );
            tabs.put(name, new TabWithButton(tab, button));
            return this;
        }

        public builder withBackground(SizedScreenElement background){
            this.background = background;
            return this;
        }


        public builder withRenderedStack(ItemStack stack){
            this.renderedStack = stack;
            return this;
        }

        public GenericSettingScreen build(){
            return new GenericSettingScreen(pos, tabs, tabManager, background, renderedStack, tickTasks);
        }

        public builder withTickTask(Runnable task){
            tickTasks.add(task);
            return this;
        }

    }


    public record TabWithButton(VerticalFlow tab, IconButton button){ }

    List<Runnable> tickTasks;
    ItemStack renderedStack;
    HashMap<String, TabWithButton> tabs;
    TabSwitch tabManager;
    SizedScreenElement background;
    BlockPos pos;
    GridLayout buttonLayout = new GridLayout();

    GenericSettingScreen(
            BlockPos pos,
            HashMap<String, TabWithButton> tabs,
            TabSwitch tabManager,
            SizedScreenElement background,
            ItemStack renderedStack,
            List<Runnable> tasks
    ){

        this.pos = pos;
        this.tabs = tabs;
        this.tickTasks = tasks;
        this.tabManager = tabManager;
        this.background = background;
        this.renderedStack = renderedStack;

        AtomicInteger x = new AtomicInteger(0);
        this.tabs.values().forEach(t -> buttonLayout.addChild(t.button, 0, x.getAndIncrement()));
        if(this.tabs.values().size() <= 1)buttonLayout.visitChildren(w -> {if(w instanceof AbstractWidget ab)ab.visible = false;});
    }


    @Override
    protected void init() {

        setWindowSize(background.width(), background.height());
        super.init();
        buttonLayout.columnSpacing(4);
        buttonLayout.arrangeElements();
        buttonLayout.setX(guiLeft);
        buttonLayout.setY(guiTop - buttonLayout.getHeight());

        tabManager.setTabArea(ScreenRectangle.of(
                ScreenAxis.HORIZONTAL, guiLeft, guiTop, background.width(), background.height())
        );


        tabs.values().forEach(
                tab -> {
                    tab.tab.onScreenInit();
                    addRenderableWidgets(tab.button);
                }
        );

        ArrayList<AbstractWidget> widgets = new ArrayList<>();
        tabs.values().forEach(tab -> tab.tab.onAddRenderable(widgets));
        widgets.forEach(this::addRenderableWidget);


        tabs.values().forEach(t -> tabManager.setCurrentTab(t.tab, false));
        tabs.values().stream().findFirst().ifPresent(t -> tabManager.setCurrentTab(t.tab, true));

    }




    @Override
    public void onClose() {
        super.onClose();
        tabs.values().forEach(t -> t.tab.onClose());

    }

    @Override
    public void tick() {
        super.tick();
        tabs.values().forEach(t -> t.tab.onScreenTick());
        tickTasks.forEach(Runnable::run);
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int i, int i1, float v) {
        background.render(graphics, guiLeft, guiTop);
        ItemStack renderStack = renderedStack();
        int x = guiLeft + 30;
        int y = guiTop + background.height();

        if(renderStack == null)return;
        GuiGameElement.of(renderStack)
                .scale(3)
                .at(x, y, 0)
                .render(graphics);
    }

    protected @Nullable ItemStack renderedStack(){
        return renderedStack;
    }

}
