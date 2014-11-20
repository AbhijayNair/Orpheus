/*
 * Copyright (c) 2014 OpenSilk Productions LLC
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.opensilk.music.ui2;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andrew.apollo.Config;
import com.andrew.apollo.model.Genre;
import com.andrew.apollo.model.LocalAlbum;
import com.andrew.apollo.model.LocalArtist;
import com.andrew.apollo.model.LocalSongGroup;
import com.andrew.apollo.model.Playlist;

import org.opensilk.common.flow.AppFlow;
import org.opensilk.common.flow.Screen;
import org.opensilk.common.mortarflow.MortarContextFactory;
import org.opensilk.common.util.ViewUtils;
import org.opensilk.music.R;
import org.opensilk.music.theme.OrpheusTheme;
import org.opensilk.music.ui.profile.AlbumFragment;
import org.opensilk.music.ui.profile.GenreFragment;
import org.opensilk.music.ui.profile.PlaylistFragment;
import org.opensilk.music.ui.profile.SongGroupFragment;
import org.opensilk.music.ui2.main.Main;
import org.opensilk.music.ui2.main.QueueScreen;
import org.opensilk.common.dagger.DaggerInjector;
import org.opensilk.music.ui2.profile.AlbumScreen;
import org.opensilk.music.ui2.profile.ArtistScreen;
import org.opensilk.music.ui2.profile.GenreScreen;
import org.opensilk.music.ui2.profile.PlaylistScreen;
import org.opensilk.music.ui2.profile.SongGroupScreen;

import butterknife.ButterKnife;
import dagger.ObjectGraph;
import flow.Flow;
import flow.Layouts;
import mortar.Mortar;

/**
 * Created by drew on 11/8/14.
 */
public class ProfileActivity extends BaseSwitcherActivity implements DaggerInjector {

    public static class Blueprint extends BaseMortarActivity.Blueprint {

        public Blueprint(String scopeName) {
            super(scopeName);
        }

        @Override
        public Object getDaggerModule() {
            return new Module();
        }

    }

    @dagger.Module (
            includes = {
                    BaseSwitcherActivity.Module.class,
                    Main.Module.class,
            },
            injects = ProfileActivity.class
    )
    public static class Module {

    }

    public static final String ACTION_ARTIST = "open_artist";
    public static final String ACTION_ALBUM = "open_album";
    public static final String ACTION_PLAYLIST = "open_playlist";
    public static final String ACTION_GENRE = "open_genre";
    public static final String ACTION_SONG_GROUP = "open_song_group";

    @Override
    protected mortar.Blueprint getBlueprint(String scopeName) {
        return new Blueprint(scopeName);
    }

    @Override
    protected void setupTheme() {
        OrpheusTheme orpheusTheme = mSettings.getTheme();
        setTheme(mSettings.isDarkTheme() ? orpheusTheme.profileDark : orpheusTheme.profileLight);
    }

    @Override
    public Screen getDefaultScreen() {
        Bundle b = getIntent().getBundleExtra(Config.EXTRA_DATA);
        Screen s = null;
        String action = getIntent().getAction();
        switch (action) {
            case ACTION_ARTIST:
                s = new ArtistScreen(b.<LocalArtist>getParcelable(Config.EXTRA_DATA));
                break;
            case ACTION_ALBUM:
                s = new AlbumScreen(b.<LocalAlbum>getParcelable(Config.EXTRA_DATA));
                break;
            case ACTION_PLAYLIST:
                s = new PlaylistScreen(b.<Playlist>getParcelable(Config.EXTRA_DATA));
                break;
            case ACTION_GENRE:
                s = new GenreScreen(b.<Genre>getParcelable(Config.EXTRA_DATA));
                break;
            case ACTION_SONG_GROUP:
                s = new SongGroupScreen(b.<LocalSongGroup>getParcelable(Config.EXTRA_DATA));
                break;
            default:
                finish();
                break;
        }
        return s;
    }

    @Override
    protected void setupView() {
        setContentView(R.layout.main_profile);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpButtonEnabled(true);
        getSupportActionBar().setDisplayOptions(0, ActionBar.DISPLAY_SHOW_TITLE);

        AppFlow.loadInitialScreen(this);

    }

    /*
     * DaggerInjector for fragments
     */

    @Override
    public void inject(Object o) {
        Mortar.getScope(this).getObjectGraph().inject(o);
    }

    @Override
    public ObjectGraph getObjectGraph() {
        return Mortar.getScope(this).getObjectGraph();
    }

}
