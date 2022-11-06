package com.code4copy.filedownloader;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RangeInfo {
    private boolean supported;
    private long contentLength;
}
