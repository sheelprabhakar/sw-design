package com.code4copy.filedownloader.client;

import com.code4copy.filedownloader.DownloadConfig;
import com.code4copy.filedownloader.DownloadRequest;
import com.code4copy.filedownloader.RangeInfo;
import com.code4copy.filedownloader.Utils;
import com.code4copy.filedownloader.api.DownloadClient;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.*;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class HttpDownloadClient implements DownloadClient {

    private final HttpClient httpClient;
    private final DownloadRequest request;
    private ExecutorService executor = Executors.newFixedThreadPool(5);

    public HttpDownloadClient(final DownloadRequest request) {
        this.request = request;
        this.httpClient = HttpClients.createDefault();
    }
    public HttpDownloadClient(final DownloadRequest request, final HttpClient httpClient) {
        this.httpClient = httpClient;
        this.request = request;
    }

    private String getAuthHeader(DownloadRequest request){
        return null;
    }

    @Override
    public File download() throws Exception {

        RangeInfo rangeInfo = this.checkRangeSupported();
        List<Callable<DownloadedChunkInfo>> callableTasks = new ArrayList<>();
        List<Range> rangeList = makeRangeList(request.getChunks(), rangeInfo.getContentLength());
        String authHeader = getAuthHeader(request);
        int index =0;
        for(Range r : rangeList){
            ChunkDownloader callable = new ChunkDownloader(this.httpClient, authHeader, URI.create(request.getUrl()),
                    index++,r);
            callableTasks.add(callable);
        }

        List<Future<DownloadedChunkInfo>> futures = executor.invokeAll(callableTasks);
        List<File> files = new ArrayList<>();
        // Sequence is same as submitted
       for(Future<DownloadedChunkInfo> future: futures){
           DownloadedChunkInfo downloadedChunkInfo = future.get();
           if(downloadedChunkInfo.isFailed()){
               throw new Exception(downloadedChunkInfo.getErr());
           }
           files.add(new File(downloadedChunkInfo.getChunkPath()));
       }
       // Merge downloaded files
        // Make file path
        String folder = request.getDownloadLocation();
       if(Utils.isEmpty(folder)){
           folder = DownloadConfig.defaultConfig().getDownloadLocation();
       }
       // Make sure folder exists
        File f = new File(folder);
       if(!f.exists()) {
           f.mkdirs();
       }
       String [] parts = request.getUrl().split("/");
       String fileName = parts[parts.length-1];
       Path path = Path.of(folder, fileName);
       // To Do new file name with number for duplicate name
       Utils.concatFiles(path.toFile(), files);
       return path.toFile();
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

    @Override
    public RangeInfo checkRangeSupported() throws Exception {
        HttpHead headRequest = new HttpHead(this.request.getUrl());
        String authHeader = getAuthHeader(this.request);
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
