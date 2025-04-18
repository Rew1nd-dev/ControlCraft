package com.verr1.controlcraft.content.gui.screens;

import com.simibubi.create.foundation.gui.AbstractSimiScreen;
import com.simibubi.create.foundation.gui.AllIcons;
import com.simibubi.create.foundation.gui.widget.IconButton;
import com.verr1.controlcraft.content.blocks.camera.CameraBlockEntity;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import com.verr1.controlcraft.content.gui.widgets.SmallIconButton;
import com.verr1.controlcraft.foundation.BlockEntityGetter;
import com.verr1.controlcraft.content.gui.layouts.api.SizedScreenElement;
import com.verr1.controlcraft.foundation.managers.ClientCameraManager;
import com.verr1.controlcraft.foundation.type.descriptive.UIContents;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@OnlyIn(Dist.CLIENT)
public class CameraLinkScreen extends AbstractSimiScreen{

    private static final Map<String, CameraLinkData> SAVED_LINKS = new LinkedHashMap<>();
    private static final int MAX_SLOTS = 10;
    private static final int MAX_SLOTS_PER_COLUMN = 5;

    private final List<LabelWithButtons> selections = new ArrayList<>();

    private final GridLayout selectionLayout = new GridLayout();
    private final GridLayout buttonLayout = new GridLayout();

    SizedScreenElement background = ControlCraftGuiTextures.SIMPLE_BACKGROUND_QUARTER;
    SizedScreenElement tabBar = ControlCraftGuiTextures.TAB_BAR;

    IconButton resetButton = new IconButton(0, 0, AllIcons.I_TRASH).withCallback(this::reset);
    IconButton validateButton = new IconButton(0, 0, AllIcons.I_REFRESH).withCallback(this::validate);

    public CameraLinkScreen(Component title) {
        super(title);
    }

    private void createListButton(){
        /*
        selections.clear();
        SAVED_LINKS.values().stream().limit(10).forEach(
            d -> {

                FormattedLabel nameLabel = new FormattedLabel(0, 0, Component.literal(d.name));
                nameLabel.setText(Component.literal(d.name));
                SmallCheckbox linkButton = new SmallCheckbox(0, 0, 10, 10, Component.literal(""), false)
                        .withCallback(
                            cbx -> {
                                selectionGroup.visitExcept(
                                    btn -> !Objects.equals(btn.id(), cbx.id()),
                                    btn -> btn.setSelected(false)
                                );
                                return false;
                            }
                        )
                        .withID(d.name);
                selectionGroup.withButton(linkButton);

                selections.add(new LabelWithCheckBox(
                        nameLabel,
                        linkButton
                ));
            }
        );
        * */
        IntStream.range(0, MAX_SLOTS).forEach(
            $ -> {
                var lwc = new LabelWithButtons(
                        new FormattedLabel(0, 0, Component.literal("")),
                        new SmallIconButton(0, 0, ControlCraftGuiTextures.SMALL_BUTTON_YES).withToolTips(UIContents.CAMERA_LINK_ACCEPT.specific()),
                        new SmallIconButton(0, 0, ControlCraftGuiTextures.SMALL_BUTTON_NO).withToolTips(UIContents.CAMERA_LINK_DUMP.specific())
                    );

                selections.add(lwc);
            }
        );

        AtomicInteger i = new AtomicInteger(0);
        selections.forEach(s -> {
            int index = i.getAndIncrement();
            int cell_y = index % MAX_SLOTS_PER_COLUMN;
            int cell_x = index / MAX_SLOTS_PER_COLUMN;

            selectionLayout.addChild(s.createLayout(), cell_y, cell_x);

        });

        resetButton.getToolTip().addAll(UIContents.CAMERA_LINK_RESET.specific());
        validateButton.getToolTip().addAll(UIContents.CAMERA_LINK_VALIDATE.specific());

        selectionLayout.visitWidgets(this::addRenderableWidget);
        selectionLayout.columnSpacing(4);
        selectionLayout.rowSpacing(2);
        selectionLayout.setX(guiLeft + 3);
        selectionLayout.setY(guiTop + 5);

    }


    private void resetSelectionButtons(){
        selections.forEach(LabelWithButtons::reset);

        AtomicInteger i = new AtomicInteger(0);
        SAVED_LINKS.values().stream().limit(selections.size()).forEach(
            d -> {
                int index = i.getAndIncrement();
                selections
                    .get(index)
                    .setTo(d.name)
                    .setVisible(true)
                    .setLinkCallBack(
                        () -> link(d.name)
                    )
                    .setDumpCallBack(
                        () -> dump(d.name)
                    );
            }
        );


        selectionLayout.arrangeElements();
    }

    private void setControlButton(){

        buttonLayout.addChild(resetButton, 0, 0);
        buttonLayout.addChild(validateButton, 0, 1);
        buttonLayout.columnSpacing(4);
        buttonLayout.arrangeElements();

        buttonLayout.setX(guiLeft + 28);
        buttonLayout.setY(guiTop - buttonLayout.getHeight() - 2);


        addRenderableWidgets(resetButton, validateButton);
    }

    @Override
    protected void init() {
        setWindowSize(background.width(), background.height());
        super.init();
        createListButton();
        resetSelectionButtons();
        setControlButton();
        // validate();
    }
/*
*  private void link(){
        Optional.ofNullable(current()).map(SAVED_LINKS::get).ifPresent(
                s -> ClientCameraManager.link(s.pos())
        );
        onClose();
    }

    private void dump(){
        Optional.ofNullable(current())
                .map(SAVED_LINKS::get)
                .map(CameraLinkData::name)
                .ifPresent(
                    SAVED_LINKS::remove
                );
        resetSelectionButtons();
    }
    *
    * private String current(){
        AtomicReference<String> result = new AtomicReference<>("");
        selections.forEach(
            s -> {
                if(s.checkBox.selected()){
                    result.set(s.checkBox().id());
                }
            }
        );
        return result.get();
    }
* */

    private void link(String name){
        Optional.ofNullable(SAVED_LINKS.get(name)).map(CameraLinkData::pos).ifPresent(
            ClientCameraManager::link
        );
        onClose();
    }

    private void dump(String name){
        SAVED_LINKS.remove(name);
        resetSelectionButtons();
    }

    private void validate(){
        SAVED_LINKS.values().removeIf(
                d -> BlockEntityGetter.getLevelBlockEntityAt(Minecraft.getInstance().level, d.pos(), CameraBlockEntity.class).isEmpty()
        );
        resetSelectionButtons();
    }





    private void reset(){
        SAVED_LINKS.clear();
        resetSelectionButtons();
    }

    @Override
    protected void renderWindow(GuiGraphics graphics, int i, int i1, float v) {
        background.render(graphics, guiLeft, guiTop);
        tabBar.render(graphics, guiLeft, guiTop - 24);
    }


    public static void AddLink(BlockPos pos, String name){
        if(Objects.equals(name, ""))return;
        SAVED_LINKS.values().stream().filter(
                d -> d.pos.equals(pos)
        ).findFirst().ifPresent(
                d -> SAVED_LINKS.remove(d.name)
        );
        SAVED_LINKS.put(name, new CameraLinkData(pos, name));
    }


    record CameraLinkData(BlockPos pos, String name){}

    record LabelWithButtons(
            FormattedLabel label,
            SmallIconButton link,
            SmallIconButton dump
    ){

        public LabelWithButtons setTo(String id){
            label.setTextOnly(Component.literal(id));
            label.setWidth(50);
            return this;
        }

        public LabelWithButtons setVisible(boolean bl){
            label.visible = bl;
            link.visible = bl;
            dump.visible = bl;
            return this;
        }
        public LabelWithButtons reset(){
            setVisible(false);
            setTo("EMPTY");
            setDumpCallBack(() -> {});
            setLinkCallBack(() -> {});
            return this;
        }

        public LabelWithButtons setLinkCallBack(Runnable task){
            link.withCallback(task);
            return this;
        }

        public LabelWithButtons setDumpCallBack(Runnable task){
            dump.withCallback(task);
            return this;
        }

        public GridLayout createLayout(){
            GridLayout layout = new GridLayout();
            GridLayout buttonLayout = new GridLayout();

            buttonLayout.addChild(link, 0, 0);
            buttonLayout.addChild(dump, 0, 1);
            buttonLayout.columnSpacing(1);
            buttonLayout.rowSpacing(1);

            layout.addChild(label, 0, 0);
            layout.addChild(buttonLayout, 0, 1);
            layout.columnSpacing(4);
            layout.newCellSettings().alignVerticallyMiddle();

            return layout;
        }

    }
}
