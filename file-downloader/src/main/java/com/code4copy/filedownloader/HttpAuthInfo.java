package com.code4copy.filedownloader;

import lombok.*;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HttpAuthInfo {
    private HttpAuthMode authMode;
    private String bearerToken;
    private String userName;
    private String password;
    private String apiKey;
}
