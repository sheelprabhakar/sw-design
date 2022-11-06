package com.code4copy.filedownloader.client;

import com.code4copy.filedownloader.DownloadRequest;
import com.code4copy.filedownloader.RangeInfo;
import com.code4copy.filedownloader.api.DownloadClient;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class HttpDownloadClient implements DownloadClient {

    private final HttpClient httpClient;

    private ExecutorService executor = Executors.newFixedThreadPool(5);

    public HttpDownloadClient() {
        this.httpClient = HttpClients.createDefault();
    }
    public HttpDownloadClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    private String getAuthHeader(DownloadRequest request){
        return null;
    }

    public void process(DownloadRequest request) throws Exception {
        RangeInfo rangeInfo = this.checkRangeSupported(request);
        List<Range> rangeList = makeRangeList(request.getChunks(), rangeInfo.getContentLength());
    }

    public static List<Range> makeRangeList(int chunks, long contentLength) {
        long chunkLength = contentLength/chunks;
        List<Range> rangeList = new ArrayList<>();
        for (int i = 0; i < chunks-1; ++i){
            rangeList.add(new Range(i* chunkLength, (i+1)* chunkLength -1));
        }
        rangeList.add(new Range((chunks-1)* chunkLength, (chunks)* chunkLength + contentLength % chunks));
        return rangeList;
    }

    public RangeInfo checkRangeSupported(DownloadRequest request) throws Exception {
        HttpHead headRequest = new HttpHead(request.getUrl());
        String authHeader = getAuthHeader(request);
        RangeInfo rangeInfo = RangeInfo.builder().supported(false).build();
        if(authHeader != null && authHeader.trim().length() > 0){
            headRequest.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }
        HttpResponse response = null;
        try {
            response = this.httpClient.execute(headRequest);
            if(response.getCode() == HttpStatus.SC_OK)
            {
                Header header = response.getHeader(HttpHeaders.ACCEPT_RANGES);
                if(header != null && header.getValue().equals("bytes")){
                    rangeInfo.setSupported(true);
                    rangeInfo.setContentLength(Long.parseLong(response.getHeader(HttpHeaders.CONTENT_LENGTH).getValue()));
                    return rangeInfo;
                }
            }

        } catch (IOException e) {
            throw new Exception("Error in executing HEAD request", e);
        } catch (ProtocolException e) {
            return rangeInfo;
        }
        return rangeInfo;
    }
}
