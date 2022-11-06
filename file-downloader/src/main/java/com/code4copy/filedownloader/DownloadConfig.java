package com.code4copy.filedownloader;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DownloadConfig {
    private int chunks;
    private String downloadLocation;

    public static DownloadConfig  defaultConfig(){
        String downloadLocation = System.getProperty("user.home")+"/Downloads/";
        return DownloadConfig.builder().chunks(5).downloadLocation(downloadLocation).build();
    }
}
