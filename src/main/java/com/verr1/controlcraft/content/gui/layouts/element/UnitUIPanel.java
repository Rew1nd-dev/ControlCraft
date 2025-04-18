package com.verr1.controlcraft.content.gui.layouts.element;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import com.verr1.controlcraft.content.gui.widgets.SmallIconButton;
import com.verr1.controlcraft.foundation.data.NetworkKey;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.core.BlockPos;

import java.util.function.Function;

public class UnitUIPanel extends TypedUIPanel<Double> implements TitleLabelProvider {

    protected final FormattedLabel title;
    protected final SmallIconButton unitButton = new SmallIconButton(0, 0, ControlCraftGuiTextures.SMALL_BUTTON_YES).withCallback(this::trigger);

    public UnitUIPanel(
            BlockPos boundPos,
            NetworkKey key,
            Class<Double> dataType,
            Double defaultValue,
            LabelProvider titleProv
    ) {
        super(
                boundPos,
                key,
                dataType,
                defaultValue
        );
        title = titleProv.toDescriptiveLabel();
    }



    @Override
    protected void initLayout(GridLayout layoutToFill) {
        layoutToFill.addChild(title, 0, 0);
        layoutToFill.addChild(unitButton, 0, 1);
    }

    @Override
    protected Double readGUI() {
        return 0.0;
    }


    @Override
    public Label title() {
        return title;
    }
}
