/*
 * Copyright (C) 2018,2020 The LineageOS Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.baikalos.extras.dirac;

import android.content.Context;
import android.content.Intent;
import android.os.UserHandle;
import android.os.SystemProperties;


public final class DiracUtils {

    protected static DiracSound mDiracSound;
    private static boolean mInitialized;
    private static Context mContext;

    public static void initialize(Context context) {
        if (!mInitialized) {
            mContext = context;
            mDiracSound = new DiracSound(0, 0);
            mInitialized = true;
        }
    }

    public static void deinitialize(Context context) {
        if (mInitialized) {
            mContext = context;
            mDiracSound = null;
            mInitialized = false;
        }
    }

    protected static boolean getEnabled() {
        if (!mInitialized) return false;
        return mDiracSound.getEnabled();
    }

    protected static void setEnabled(boolean enable) {
        SystemProperties.set("persist.dirac.enabled", enable ? "1": "0" );
        if (!mInitialized) return;
        mDiracSound.setEnabled(enable);
    }


    protected static void setMusic(boolean enable) {
        SystemProperties.set("persist.dirac.music", enable ? "1": "0" );
        if (!mInitialized) return;
        mDiracSound.setMusic(enable ? 1 : 0);
    }

    protected static void setMovie(boolean enable) {
        SystemProperties.set("persist.dirac.movie", enable ? "1": "0" );
        if (!mInitialized) return;
        mDiracSound.setMovie(enable ? 1 : 0);
    }

    protected static void setSurround(int level) {
        SystemProperties.set("persist.dirac.surround", Integer.toString(level));
        if (!mInitialized) return;
        mDiracSound.setSurround(level);
    }

    protected static void setVoice(int level) {
        SystemProperties.set("persist.dirac.voice", Integer.toString(level));
        if (!mInitialized) return;
        mDiracSound.setVoice(level);
    }


    protected static int getMusic() {
        if (!mInitialized) return 0;
        return mDiracSound.getMusic();
    }
    protected static int getMovie() {
        if (!mInitialized) return 0;
        return mDiracSound.getMovie();
    }
    protected static int getVoice() {
        if (!mInitialized) return 0;
        return mDiracSound.getVoice();
    }
    protected static int getMode() {
        if (!mInitialized) return 0;
        return mDiracSound.getMode();
    }
    protected static int getSurround() {
        if (!mInitialized) return 0;
        return mDiracSound.getSurround();
    }
    protected static int getScenario() {
        if (!mInitialized) return 0;
        return mDiracSound.getScenario();
    }


    protected static boolean isDiracEnabled() {
        if (!mInitialized) return false;
        return mDiracSound != null && mDiracSound.getMusic() == 1;
    }

    protected static void setLevel(String preset) {
        if( preset == null ) return;
        String[] level = preset.split("\\s*,\\s*");

        SystemProperties.set("persist.dirac.level", preset);

        for (int band = 0; band <= level.length - 1; band++) {
            
            SystemProperties.set("persist.dirac.level." + Integer.toString(band), level[band]);
            if (mInitialized) mDiracSound.setLevel(band, Float.valueOf(level[band]));
        }


    }

    protected static String getLevel() {
        if (!mInitialized) return "";
        String selected = "";
        for (int band = 0; band <= 6; band++) {
            int temp = 0;
            try {
                temp = Integer.valueOf(SystemProperties.get("persist.dirac.level." + Integer.toString(band),"0"));
            } catch(Exception ex) {}
            selected += String.valueOf(temp);
            if (band != 6) selected += ",";
        }
        return selected;
    }

    protected static void setLevel(int band, int level) {
        SystemProperties.set("persist.dirac.level." + Integer.toString(band), Integer.toString(level));
        if (!mInitialized) return;
        mDiracSound.setLevel(band, Float.valueOf(level));
        SystemProperties.set("persist.dirac.level", getLevel());
    }

    protected static int getLevel(int band) {
        if (!mInitialized) return 0;
        return (int)mDiracSound.getLevel(band);
    }

    protected static void setHeadsetType(int paramInt) {
        SystemProperties.set("persist.dirac.headset", Integer.toString(paramInt));
        if (!mInitialized) return;
        mDiracSound.setHeadsetType(paramInt);
    }

    protected static void setScenario(int paramInt) {
        SystemProperties.set("persist.dirac.scenario", Integer.toString(paramInt));
        if (!mInitialized) return;
        mDiracSound.setScenario(paramInt);
    }


    protected static void setTestInt(String index, String value) {
        mDiracSound.setInt(index,value);
    }

    protected static void setTestString(String index, String value) {
        mDiracSound.setString(index,value);
    }

    public static void restore() {
        boolean boolVal = false;
        Integer intVal = 0;
        String stringVal = "";

        if( !mInitialized ) return;

        try {
            boolVal = "1".equals(SystemProperties.get("persist.dirac.enabled", "0"));
            mDiracSound.setEnabled(boolVal);

            boolVal = "1".equals(SystemProperties.get("persist.dirac.music", "0"));
            mDiracSound.setMusic(boolVal ? 1 : 0);

            boolVal = "1".equals(SystemProperties.get("persist.dirac.movie", "0"));
            mDiracSound.setMovie(boolVal ? 1 : 0);

            intVal = Integer.valueOf(SystemProperties.get("persist.dirac.surround", "0"));
            mDiracSound.setSurround(intVal);

            intVal = Integer.valueOf(SystemProperties.get("persist.dirac.voice", "0"));
            mDiracSound.setVoice(intVal);

            stringVal = SystemProperties.get("persist.dirac.level", null);
            if( stringVal != null ) setLevel(stringVal);

            intVal = Integer.valueOf(SystemProperties.get("persist.dirac.headset", "0"));
            mDiracSound.setHeadsetType(intVal);

            intVal = Integer.valueOf(SystemProperties.get("persist.dirac.scenario", "0"));
            mDiracSound.setScenario(intVal);
        } catch(Exception ex) {
        }
    }

}
