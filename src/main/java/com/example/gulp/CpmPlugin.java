/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.tom.cpm.api.CPMPlugin
 *  com.tom.cpm.api.ICPMPlugin
 *  com.tom.cpm.api.IClientAPI
 *  com.tom.cpm.api.ICommonAPI
 */
package com.example.gulp;

import com.example.gulp.CpmBridge;
import com.tom.cpm.api.CPMPlugin;
import com.tom.cpm.api.ICPMPlugin;
import com.tom.cpm.api.IClientAPI;
import com.tom.cpm.api.ICommonAPI;

@CPMPlugin
public class CpmPlugin
implements ICPMPlugin {
    public void initClient(final IClientAPI api) {
        CpmBridge.setClient(new CpmBridge.Client(){

            @Override
            public boolean play(String animation, int value) {
                return api.playAnimation(animation, value);
            }

            @Override
            public int maxValue(String animation) {
                return api.getAnimationMaxValue(animation);
            }
        });
    }

    public void initCommon(ICommonAPI api) {
    }

    public String getOwnerModId() {
        return "gulp";
    }
}

