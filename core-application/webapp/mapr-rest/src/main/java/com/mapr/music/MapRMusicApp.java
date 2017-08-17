package com.mapr.music;

import com.mapr.music.api.AlbumEndpoint;
import com.mapr.music.util.CORSFilter;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.HashSet;
import java.util.Set;


@ApplicationPath("/api/1.0/")
public class MapRMusicApp extends Application {

    private Set<Object> singletons = new HashSet<>();

    // FIXME use DI
    public MapRMusicApp() {
        singletons.add(new AlbumEndpoint());
        singletons.add(new CORSFilter());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

}
