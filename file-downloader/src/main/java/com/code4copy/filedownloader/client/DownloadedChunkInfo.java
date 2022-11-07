package com.code4copy.filedownloader.client;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadedChunkInfo {
    private String chunkPath;
    private long contentLength;
    private int chunkIndex;
    private boolean failed;
    private String err;
    private int httpStatus;
}
