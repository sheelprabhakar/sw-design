package com.code4copy.filedownloader.client;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Range {
    private long from;
    private long to;
}
