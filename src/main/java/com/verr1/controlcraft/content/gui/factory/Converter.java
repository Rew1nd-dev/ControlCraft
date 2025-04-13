package com.verr1.controlcraft.content.gui.factory;

import com.simibubi.create.foundation.gui.widget.Label;
import com.verr1.controlcraft.content.gui.layouts.api.ComponentLike;
import com.verr1.controlcraft.content.gui.layouts.api.Descriptive;
import com.verr1.controlcraft.content.gui.layouts.api.LabelProvider;
import com.verr1.controlcraft.content.gui.layouts.api.TitleLabelProvider;
import com.verr1.controlcraft.content.gui.widgets.FormattedLabel;
import com.verr1.controlcraft.utils.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.function.UnaryOperator;

public class Converter {
    public static List<MutableComponent> overallOf(Descriptive<?> d){
        return d.overall().stream().map(Component::copy).toList();
    }

    public static List<MutableComponent> specificOf(Descriptive<?> d){
        return d.specific().stream().map(Component::copy).toList();
    }


    public static LabelProvider convert(LabelProvider prov, UnaryOperator<Style> title){
        FormattedLabel original = prov.toDescriptiveLabel();
        return new LabelProvider(){
            @Override
            public FormattedLabel toDescriptiveLabel() {
                return original.withTextStyle(title);
            }

            @Override
            public FormattedLabel toUILabel() {
                return toDescriptiveLabel();
            }
        };

    }

    public static<T extends Enum<?>> Descriptive<T> convert(
            Descriptive<T> prov,
            UnaryOperator<Style> specific,
            UnaryOperator<Style> title,
            UnaryOperator<Style> overall
    ){
        FormattedLabel original = prov.toDescriptiveLabel();
        return new Descriptive<>() {

            private final List<Component> converted_specific =
                    prov.specific().stream().map(Component::copy).map(e -> e.withStyle(specific)).map(Component.class::cast).toList();

            private final List<Component> converted_overall =
                    prov.overall().stream().map(Component::copy).map(e -> e.withStyle(overall)).map(Component.class::cast).toList();


            private final Component comp = prov.asComponent().copy().withStyle(title);


            @Override
            public T self() {
                return prov.self();
            }

            @Override
            public Class<T> clazz() {
                return prov.clazz();
            }

            @Override
            public FormattedLabel toDescriptiveLabel() {
                return original.withTextStyle(specific);
            }

            @Override
            public FormattedLabel toUILabel() {
                return toDescriptiveLabel();
            }


            @Override
            public List<Component> specific() {
                return converted_specific;
            }

            @Override
            public List<Component> overall() {
                return converted_overall;
            }

            @Override
            public @NotNull Component asComponent() {
                return comp;
            }
        };

    }

    public static LabelProvider convert(Component component){
        return new ComponentLike() {
            @Override
            public @NotNull Component asComponent() {
                return component;
            }

        };
    }

    public static MutableComponent nameOf(Descriptive<?> d){
        return d.asComponent().copy();
    }

    public static void alignLabel(TitleLabelProvider... labels){
        int max_len = MathUtils.max(Arrays.stream(labels).map(l -> l.title().getWidth()).toArray(Integer[]::new));
        for (var label : labels){
            label.title().setWidth(max_len);
        }
    }

    public static void alignLabel(Label... labels){
        int max_len = MathUtils.max(Arrays.stream(labels).map(AbstractWidget::getWidth).toArray(Integer[]::new));
        for (var label : labels){
            label.setWidth(max_len);
        }

    }

    public static void alignLabel(List<Label> labels){
        int max_len = MathUtils.max(labels.stream().map(AbstractWidget::getWidth).toArray(Integer[]::new));
        for (var label : labels){
            label.setWidth(max_len);
        }

    }

    public static Component lockViewComponent(boolean isLocked){
        return isLocked ?
                Component.literal("Locked").withStyle(s -> s.withColor(ChatFormatting.RED)) :
                Component.literal("Free").withStyle(s -> s.withColor(ChatFormatting.DARK_GREEN));
    }

    public static Style warnStyle(Style s){
        return s.withItalic(true).withColor(ChatFormatting.RED).withBold(true);
    }

    public static Style titleStyle(Style s){
        return s.withItalic(false).withColor(ChatFormatting.DARK_GRAY);
    }

    public static Style viewStyle(Style s){
        return s.withBold(true).withUnderlined(true).withItalic(false).withColor(ChatFormatting.DARK_GRAY);
    }

    public static Style optionStyle(Style s){
        return s.withBold(false).withItalic(true).withUnderlined(true).withColor(ChatFormatting.DARK_AQUA);
    }

    public static Style minMaxStyle(Style s){
        return s.withBold(false).withUnderlined(true).withColor(ChatFormatting.UNDERLINE).withColor(ChatFormatting.DARK_GRAY);
    }

    public static Style pidStyle(Style s){
        return titleStyle(s);
    }

    public static Style directionStyle(Style s){
        return s.withBold(true).withItalic(true).withColor(ChatFormatting.DARK_GRAY);
    }


}
