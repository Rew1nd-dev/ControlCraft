package com.verr1.controlcraft.content.gui.v1.layouts.api;

import com.verr1.controlcraft.content.gui.v1.widgets.FormattedLabel;

public interface LabelProvider {

    FormattedLabel toDescriptiveLabel();

    FormattedLabel toUILabel();

}
