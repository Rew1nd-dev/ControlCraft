package com.verr1.controlcraft.content.gui.layouts;

import com.verr1.controlcraft.content.gui.layouts.api.SwitchableTab;
import com.verr1.controlcraft.content.gui.layouts.api.TabListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.tabs.Tab;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.Objects;

@OnlyIn(Dist.CLIENT)
public class TabSwitch {
    @Nullable
    private SwitchableTab currentTab;
    @Nullable
    private ScreenRectangle tabArea;

    public void setTabArea(ScreenRectangle p_268042_) {
        this.tabArea = p_268042_;
        SwitchableTab $$1 = this.getCurrentTab();
        if ($$1 != null) {
            $$1.doLayout(p_268042_);
        }

    }

    public void setCurrentTab(SwitchableTab newTab, boolean p_276120_) {
        if (!Objects.equals(this.currentTab, newTab)) {
            if (this.currentTab != null) {
                this.currentTab.onRemovedTab();
            }

            this.currentTab = newTab;
            this.currentTab.onActivatedTab();
            if (this.tabArea != null) {
                newTab.doLayout(this.tabArea);
            }

            if (p_276120_) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
        }

    }

    @Nullable
    public SwitchableTab getCurrentTab() {
        return this.currentTab;
    }

    public void onScreenTick() {
        TabListener $$0 = this.getCurrentTab();
        if ($$0 != null) {
            $$0.onScreenTick();
        }

    }

    public boolean isCurrentTab(SwitchableTab other){
        return getCurrentTab() == other;
    }
}
