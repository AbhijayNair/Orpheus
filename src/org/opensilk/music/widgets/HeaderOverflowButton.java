/*
 * Copyright (C) 2014 OpenSilk Productions LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensilk.music.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.andrew.apollo.R;
import com.andrew.apollo.utils.ThemeHelper;

/**
 * Created by drew on 3/16/14.
 */
public class HeaderOverflowButton extends ImageView {

    private final int mOverflowDrawable;

    public HeaderOverflowButton(Context context) {
        this(context, null);
    }

    public HeaderOverflowButton(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HeaderOverflowButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        boolean isLightTheme = ThemeHelper.isLightTheme(getContext());
        if (isLightTheme) {
            mOverflowDrawable = R.drawable.ic_menu_moreoverflow_normal_holo_light;
        } else {
            mOverflowDrawable = R.drawable.ic_menu_moreoverflow_normal_holo_dark;
        }
        setImageResource(mOverflowDrawable);
    }

}