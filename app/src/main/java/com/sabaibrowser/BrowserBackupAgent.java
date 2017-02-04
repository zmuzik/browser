/*
 * Copyright (C) 2009 The Android Open Source Project
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

package com.sabaibrowser;

import android.app.backup.BackupAgent;
import android.app.backup.BackupDataInput;
import android.app.backup.BackupDataOutput;
import android.os.ParcelFileDescriptor;

import java.io.IOException;

/**
 * Settings backup agent for the Android browser.  Currently the only thing
 * stored is the set of bookmarks.  It's okay if I/O exceptions are thrown
 * out of the agent; the calling code handles it and the backup operation
 * simply fails.
 *
 * @hide
 */
public class BrowserBackupAgent extends BackupAgent {
    static final String TAG = "BrowserBackupAgent";
    static final boolean DEBUG = false;

    static final String BOOKMARK_KEY = "_bookmarks_";
    /**
     * this version num MUST be incremented if the flattened-file schema ever changes
     */
    static final int BACKUP_AGENT_VERSION = 0;

    /**
     * This simply preserves the existing state as we now prefer Chrome Sync
     * to handle bookmark backup.
     */
    @Override
    public void onBackup(ParcelFileDescriptor oldState, BackupDataOutput data,
                         ParcelFileDescriptor newState) throws IOException {
    }

    /**
     * Restore from backup -- reads in the flattened bookmark file as supplied from
     * the backup service, parses that out, and rebuilds the bookmarks table in the
     * browser database from it.
     */
    @Override
    public void onRestore(BackupDataInput data, int appVersionCode,
                          ParcelFileDescriptor newState) throws IOException {
    }
}
