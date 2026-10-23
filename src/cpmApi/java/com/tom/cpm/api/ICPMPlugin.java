package com.tom.cpm.api;

public interface ICPMPlugin {
    void initClient(IClientAPI api);
    void initCommon(ICommonAPI api);
    String getOwnerModId();
}
