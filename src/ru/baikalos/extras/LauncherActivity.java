/*
 * Copyright (C) 2017 AICP
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


package ru.baikalos.extras;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import java.util.Arrays;

import ru.baikalos.extras.utils.Util;

public class LauncherActivity extends SettingsActivity {

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initShortcutManager();
    }

    private void initShortcutManager() {
        final ShortcutManager shortcutManager = getSystemService(ShortcutManager.class);

        if (Util.isPackageEnabled(Constants.BAIKALOS_OTA_PACKAGE, this.getPackageManager())) {
            // Intent for launching BAIKALOS OTA
            final Intent INTENT_OTA = new Intent().setComponent(new ComponentName(
                    Constants.BAIKALOS_OTA_PACKAGE, Constants.BAIKALOS_OTA_ACTIVITY));
            INTENT_OTA.setAction(Intent.ACTION_VIEW);

            ShortcutInfo baikalosotaShortcut = new ShortcutInfo.Builder(this, "baikalosota")
                    .setShortLabel(getString(R.string.baikalos_ota_title))
                    .setLongLabel(getString(R.string.baikalos_ota_title))
                    .setIcon(Icon.createWithResource(this,
                            R.drawable.ic_launcher_shortcut_ota))
                    .setIntent(INTENT_OTA)
                    .setRank(0)
                    .build();
            shortcutManager.setDynamicShortcuts(Arrays.asList(baikalosotaShortcut));
        } else {
            ShortcutInfo downloadsShortcut = new ShortcutInfo.Builder(this, "downloads")
                    .setShortLabel(getString(R.string.baikalos_downloads_title))
                    .setLongLabel(getString(R.string.baikalos_downloads_title))
                    .setIcon(Icon.createWithResource(this,
                            R.drawable.ic_launcher_shortcut_downloads))
                    .setIntent(new Intent(Intent.ACTION_VIEW, Uri.parse(
                            Util.getDownloadLinkForDevice(this))))
                    .setRank(0)
                    .build();
            shortcutManager.setDynamicShortcuts(Arrays.asList(downloadsShortcut));
        }
    }
}
