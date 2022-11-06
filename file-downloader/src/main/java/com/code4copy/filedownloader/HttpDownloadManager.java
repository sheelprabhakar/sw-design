package com.code4copy.filedownloader;

import com.code4copy.filedownloader.api.DownloadManager;

import java.net.URI;

public class HttpDownloadManager implements DownloadManager {

    private final DownloadConfig downloadConfig;

    public HttpDownloadManager() {
        this.downloadConfig = DownloadConfig.defaultConfig();
    }
    public HttpDownloadManager(final DownloadConfig downloadConfig) {
        this.downloadConfig = downloadConfig;
    }

    @Override
    public int add(String file){
        return add(URI.create(file));
    }

    @Override
    public int add(URI file){
        return add(file, false);
    }

    @Override
    public int add(String file, boolean autostart){
        return add(URI.create(file), autostart);
    }

    @Override
    public int add(URI file, boolean autostart){
        return -1;
    }

    @Override
    public boolean cancel(int downloadId){
        return false;
    }

    @Override
    public boolean remove(int downloadId){
        return false;
    }

    @Override
    public boolean pause(int downloadId){
        return false;
    }

    @Override
    public boolean stop(int downloadId){
        return false;
    }

    @Override
    public boolean resume(int downloadId){
        return false;
    }

    @Override
    public boolean start(int downloadId){
        return false;
    }

}
