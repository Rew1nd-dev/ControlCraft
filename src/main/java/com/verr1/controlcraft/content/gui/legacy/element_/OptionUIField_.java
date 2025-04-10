package com.verr1.controlcraft.content.gui.legacy.element_;


import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.layouts.api.ComponentLike;
import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.content.gui.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.layouts.NetworkUIPort;
import com.verr1.controlcraft.content.gui.widgets.DescriptiveScrollInput;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import com.verr1.controlcraft.registry.ControlCraftGuiTextures;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.network.chat.Component;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class OptionUIField_<T extends Enum<?> & Descriptive<?>> extends NetworkUIPort<T> implements TitleLabelProvider {


    FormattedLabel title = new FormattedLabel(0, 0, Component.literal("LLLLL"));
    FormattedLabel value = new FormattedLabel(0, 0, Component.literal("LLLLL"));
    DescriptiveScrollInput<T> options;

    public FormattedLabel valueLabel() {
        return value;
    }

    public OptionUIField_(Consumer<T> write, Supplier<T> read, Class<T> clazz, Component titleText) {
        super(write, read);
        options = new DescriptiveScrollInput<>(0, 0, 10, 10, ControlCraftGuiTextures.SMALL_BUTTON_GREEN, clazz);
        title.setText(titleText);
        lateInit();
    }

    public OptionUIField_(Consumer<T> write, Supplier<T> read, Class<T> clazz, LabelProvider titleText) {
        super(write, read);
        options = new DescriptiveScrollInput<>(0, 0, 10, 10, ControlCraftGuiTextures.SMALL_BUTTON_GREEN, clazz);
        title = titleText.toDescriptiveLabel();
        lateInit();
    }

    protected void lateInit(){
        options.valueCalling(
                it -> Optional.of(it)
                        .map(ComponentLike::asComponent)
                        .ifPresent(value::setTextOnly)
        );
        setMaxLength();
    }

    private void setMaxLength(){
        AtomicInteger maxLen = new AtomicInteger(0);
        options.values().stream().map(ComponentLike::asComponent).forEach(c -> {
            int len = c.getString().length();
            if(len > maxLen.get()) maxLen.set(len);
        });
        value.setWidth(maxLen.get() * 6);
    }

    private T valueOfOption(){
        return options.valueOfOption();
    }


    @Override
    protected void initLayout(GridLayout layoutToFill) {
        layoutToFill.addChild(title, 0, 0);
        layoutToFill.addChild(value, 0, 1);
        layoutToFill.addChild(options, 0, 2);
    }

    @Override
    protected T readGUI() {
        return valueOfOption();
    }

    @Override
    protected void writeGUI(T value) {
        options.setToValue(value);
    }

    @Override
    public Label title() {
        return title;
    }

    @Override
    public Label[] titles() {
        return new Label[]{title, value};
    }
}
