package com.verr1.controlcraft.foundation.api;

import com.simibubi.create.foundation.gui.element.ScreenElement;
import net.minecraft.client.gui.GuiGraphics;

public interface SizedScreenElement extends ScreenElement {

    static SizedScreenElement wrap(ScreenElement element) {
        return new SizedScreenElement() {
            @Override
            public int width() {
                return 0;
            }

            @Override
            public int height() {
                return 0;
            }

            @Override
            public void render(GuiGraphics guiGraphics, int i, int i1) {
                element.render(guiGraphics, i, i1);
            }
        };
    }

    int width();

    int height();

}
