package tfagaming.projects.minecraft.homestead.api;

import tfagaming.projects.minecraft.homestead.managers.ChunksManager;
import tfagaming.projects.minecraft.homestead.managers.RegionsManager;

public interface HomesteadAPI {
    RegionsManager getRegionsManager();
    ChunksManager getChunksManager();

    String getVersion();
}