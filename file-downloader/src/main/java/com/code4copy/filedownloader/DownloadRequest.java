package com.code4copy.filedownloader;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DownloadRequest {
    private String url;
    private HttpAuthInfo authInfo;
    private boolean chunked;
    private String downloadLocation;
    private int chunks;

}
