package tfagaming.projects.minecraft.homestead.api;

import tfagaming.projects.minecraft.homestead.Homestead;
import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;

public class API implements HomesteadAPI {
    public RegionsManager getRegionsManager() {
        return new RegionsManager();
    }

    public ChunksManager getChunksManager() {
        return new ChunksManager();
    }

    public String getVersion() {
        return Homestead.getVersion();
    }
}
