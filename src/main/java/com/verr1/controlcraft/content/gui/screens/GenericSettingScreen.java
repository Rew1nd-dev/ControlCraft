package com.verr1.controlcraft.content.gui.screens;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.element.GuiGameElement;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.verr1.controlcraft.content.gui.layouts.TabSwitch;
import com.verr1.controlcraft.content.gui.layouts.VerticalFlow;
import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.content.gui.widgets.TabIconButton;
import com.verr1.controlcraft.foundation.api.SizedScreenElement;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.navigation.ScreenAxis;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class GenericSettingScreen extends AbstractSimiScreen {

    public static class builder{
        ArrayList<Runnable> tickTasks = new ArrayList<>();
        ItemStack renderedStack = null;
        List<TabWithButton> tabs = new ArrayList<>();
        TabSwitch tabManager = new TabSwitch();
        SizedScreenElement background = ControlCraftGuiTextures.SIMPLE_BACKGROUND_QUARTER;
        BlockPos pos;

        public builder(BlockPos pos){
            this.pos = pos;
        }

        public builder withTab(String name, VerticalFlow tab){
            IconButton button = new TabIconButton(0, 0,
                    SizedScreenElement.wrap(AllIcons.I_TOOLBOX),
                    ControlCraftGuiTextures.TAB_BUTTON_FRAME, tab, tabManager)
                    .withTooltip(List.of(Component.literal(name)));
            tabs.add(new TabWithButton(tab, button));
            return this;
        }

        public builder withTab(Descriptive<?> name, VerticalFlow tab){
            Descriptive<?> converted = name.convertTo(
                    s -> s,
                    s -> s.withItalic(true).withBold(true).withColor(ChatFormatting.GOLD),
                    s -> s
            );

            List<Component> tooltips = new ArrayList<>(List.of(converted.asComponent(),
                    Component
                        .literal("Descriptions:")
                        .withStyle(s -> s.withColor(ChatFormatting.GRAY).withItalic(true))));

            tooltips.addAll(converted.specific().stream().map(
                    s -> s.copy().withStyle(s1 -> s1.withColor(ChatFormatting.AQUA).withItalic(true))
            ).toList());

            IconButton button = new TabIconButton(0, 0,
                    SizedScreenElement.wrap(AllIcons.I_TOOLBOX),
                    ControlCraftGuiTextures.TAB_BUTTON_FRAME, tab, tabManager)
                    .withTooltip(tooltips);
            tabs.add(new TabWithButton(tab, button));
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
    List<TabWithButton> tabs;
    TabSwitch tabManager;
    SizedScreenElement background;
    SizedScreenElement tabBar = ControlCraftGuiTextures.TAB_BAR;
    BlockPos pos;
    GridLayout buttonLayout = new GridLayout();
    IconButton applyButton = new IconButton(0, 0, AllIcons.I_CONFIRM).withCallback(this::apply);
    IconButton refreshButton = new IconButton(0, 0, AllIcons.I_REFRESH).withCallback(this::refresh);

    GenericSettingScreen(
            BlockPos pos,
            List<TabWithButton> tabs,
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
        this.tabs.forEach(t -> buttonLayout.addChild(t.button, 0, x.getAndIncrement()));
        if(this.tabs.size() <= 1)buttonLayout.visitChildren(w -> {if(w instanceof AbstractWidget ab)ab.visible = false;});
    }


    @Override
    protected void init() {

        setWindowSize(background.width(), background.height());
        super.init();
        buttonLayout.columnSpacing(4);
        buttonLayout.arrangeElements();
        buttonLayout.setX(guiLeft + 28);
        int common_button_y = guiTop - buttonLayout.getHeight() - 2;
        buttonLayout.setY(common_button_y);
        applyButton.setX(guiLeft + 3);
        applyButton.setY(common_button_y);

        refreshButton.setX(guiLeft + background.width() - 21);
        refreshButton.setY(common_button_y);

        tabManager.setTabArea(ScreenRectangle.of(
                ScreenAxis.HORIZONTAL, guiLeft, guiTop, background.width(), background.height())
        );


        tabs.forEach(
                tab -> {
                    tab.tab.onScreenInit();
                    addRenderableWidgets(tab.button);
                }
        );

        ArrayList<AbstractWidget> widgets = new ArrayList<>();
        widgets.add(applyButton);
        widgets.add(refreshButton);
        tabs.forEach(tab -> tab.tab.onAddRenderable(widgets));
        widgets.forEach(this::addRenderableWidget);


        tabs.forEach(t -> tabManager.setCurrentTab(t.tab, false));
        tabs.stream().findFirst().ifPresent(t -> tabManager.setCurrentTab(t.tab, true));

    }


    public void apply(){
        tabs.forEach(t -> {
            t.tab.apply();
            t.tab.syncUI();
        });
    }

    @Override
    public void onClose() {
        super.onClose();
        // tabs.forEach(t -> t.tab.apply());
    }

    public void refresh(){
        tabs.forEach(t -> {
            t.tab.syncUI();
        });
    }

    @Override
    public void tick() {
        super.tick();
        tabs.forEach(t -> t.tab.onScreenTick());
        tickTasks.forEach(Runnable::run);
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int i, int i1, float v) {
        background.render(graphics, guiLeft, guiTop);
        tabBar.render(graphics, guiLeft, guiTop - 24);
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
