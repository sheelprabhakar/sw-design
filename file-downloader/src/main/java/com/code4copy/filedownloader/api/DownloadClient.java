package com.code4copy.filedownloader.api;

import com.code4copy.filedownloader.RangeInfo;

import java.io.File;

public interface DownloadClient {
    File download() throws Exception;

    RangeInfo checkRangeSupported() throws Exception;
}
