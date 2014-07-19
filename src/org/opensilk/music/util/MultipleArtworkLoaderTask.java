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

package org.opensilk.music.util;

import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;

import org.opensilk.music.api.meta.ArtInfo;
import org.opensilk.music.artwork.ArtworkImageView;
import org.opensilk.music.artwork.ArtworkManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by drew on 7/11/14.
 */
public class MultipleArtworkLoaderTask extends AsyncTask<Void, Void, Set<ArtInfo>> {
    private final Context context;
    private final long[] albumIds;
    private final ArtworkImageView[] views;

    public MultipleArtworkLoaderTask(Context context, long[] albumIds, ArtworkImageView... views) {
        this.context = context;
        this.albumIds = albumIds;
        this.views = views;
    }

    @Override
    protected Set<ArtInfo> doInBackground(Void... params) {
        Set<ArtInfo> artInfos = new HashSet<>(albumIds.length);
        Cursor c = CursorHelpers.makeLocalAlbumsCursor(context, albumIds);
        if (c != null) {
            if (c.moveToFirst()) {
                do {
                    ArtInfo info = CursorHelpers.makeArtInfoFromLocalAlbumCursor(c);
                    artInfos.add(info);
                } while (c.moveToNext() && artInfos.size() <= views.length);
            }
            c.close();
        }

        return artInfos;
    }

    @Override
    protected void onPostExecute(Set<ArtInfo> artInfos) {
        if (artInfos.size() >= views.length) {
            int ii=0;
            for (ArtInfo info : artInfos) {
                ArtworkManager.loadImage(info, views[ii++]);
                if (ii==views.length) break;
            }
        }
    }
}