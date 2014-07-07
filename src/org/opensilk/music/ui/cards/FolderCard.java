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

package org.opensilk.music.ui.cards;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.andrew.apollo.R;
import com.squareup.otto.Bus;

import org.opensilk.music.api.model.Folder;
import org.opensilk.music.ui.cards.event.FolderCardClick;
import org.opensilk.music.ui.cards.event.FolderCardClick.Event;
import org.opensilk.music.widgets.ColorCodedThumbnail;
import org.opensilk.silkdagger.qualifier.ForFragment;

import javax.inject.Inject;

import butterknife.InjectView;
import it.gmariotti.cardslib.library.internal.Card;

/**
 * Created by drew on 6/19/14.
 */
public class FolderCard extends AbsBundleableCard<Folder> {

    @Inject @ForFragment
    Bus mBus; //Injected by adapter

    @InjectView(R.id.folder_thumb)
    ColorCodedThumbnail mThumbnail;

    public FolderCard(Context context, Folder data) {
        this(context, data, R.layout.listcard_folder_inner);
    }

    public FolderCard(Context context, Folder data, int innerLayout) {
        super(context, data, innerLayout);
    }

    @Override
    protected void init() {
        setOnClickListener(new OnCardClickListener() {
            @Override
            public void onClick(Card card, View view) {
                mBus.post(new FolderCardClick(Event.OPEN, mData));
            }
        });
    }

    @Override
    protected void onInnerViewSetup() {
        mCardTitle.setText(mData.name);
        mCardSubTitle.setVisibility(View.GONE);
        mThumbnail.init(mData.name != null ? mData.name : "DIR");
    }

    @Override
    protected void onCreatePopupMenu(PopupMenu m) {
        m.inflate(R.menu.popup_play_all);
        m.inflate(R.menu.popup_shuffle_all);
        m.inflate(R.menu.popup_add_to_queue);
        m.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.popup_play_all:
                        mBus.post(new FolderCardClick(Event.PLAY_ALL, mData));
                        return true;
                    case R.id.popup_shuffle_all:
                        mBus.post(new FolderCardClick(Event.SHUFFLE_ALL, mData));
                        return true;
                    case R.id.popup_add_to_queue:
                        mBus.post(new FolderCardClick(Event.ADD_TO_QUEUE, mData));
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    protected int getListLayout() {
        return R.layout.listcard_folder_inner;
    }

    @Override
    protected int getGridLayout() {
        return R.layout.gridcard_folder_inner;
    }
}
