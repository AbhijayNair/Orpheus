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

package org.opensilk.music.ui.cards.event;

import com.andrew.apollo.R;

import org.opensilk.filebrowser.FileItem;

/**
 * Created by drew on 7/15/14.
 */
public class FileItemCardClick {

    public enum Event {
        OPEN(-1),
        PLAY_NEXT(R.id.popup_play_next),
        PLAY_ALL(R.id.popup_play_all),
        SHUFFLE_ALL(R.id.popup_shuffle_all),
        ADD_TO_QUEUE(R.id.popup_add_to_queue),
        ADD_TO_PLAYLIST(R.id.popup_add_to_playlist),
        SET_RINGTONE(R.id.popup_set_ringtone),
        DELETE(R.id.popup_delete);

        private final int resourceId;

        private Event(int resourceId) {
            this.resourceId = resourceId;
        }

        public static Event valueOf(int resourceId) {
            for (Event e: Event.values()) {
                if (e.resourceId == resourceId) {
                    return e;
                }
            }
            throw new IllegalArgumentException("Unknown id: "+ resourceId);
        }
    }

    public final Event event;
    public final FileItem file;

    public FileItemCardClick(Event event, FileItem file) {
        this.event = event;
        this.file = file;
    }
}
