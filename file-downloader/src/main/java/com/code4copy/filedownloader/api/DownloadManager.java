package com.code4copy.filedownloader.api;

import java.net.URI;

public interface DownloadManager {
    int add(String file);

    int add(URI file);

    int add(String file, boolean autostart);

    int add(URI file, boolean autostart);

    boolean cancel(int downloadId);

    boolean remove(int downloadId);

    boolean pause(int downloadId);

    boolean stop(int downloadId);

    boolean resume(int downloadId);

    boolean start(int downloadId);
}
