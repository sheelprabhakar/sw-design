package com.code4copy.filedownloader.client;

import com.code4copy.filedownloader.Utils;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import java.util.concurrent.Callable;

public class DownloadCallable implements Callable<DownloadedChunkInfo> {
    private final HttpClient httpClient;
    private final String authHeader;
    private final URI uri;

    private final Range range;
    private int chunkIndex;

    public DownloadCallable(final HttpClient httpClient, final String authHeader,
                            final URI uri, int chunkIndex, final Range range) {
        this.httpClient = httpClient;
        this.authHeader = authHeader;
        this.uri = uri;
        this.range = range;
        this.chunkIndex = chunkIndex;
    }

    @Override
    public DownloadedChunkInfo call() throws Exception {
        HttpGet getRequest = new HttpGet(this.uri.toURL().toString());
        DownloadedChunkInfo downloadedChunkInfo = DownloadedChunkInfo.builder().build();
        if(!Utils.isEmpty(authHeader)){
            getRequest.setHeader(HttpHeaders.AUTHORIZATION, authHeader);
        }

        CloseableHttpResponse response = null;
        try {
            response = (CloseableHttpResponse)this.httpClient.execute(getRequest);
            if(response.getCode() == HttpStatus.SC_PARTIAL_CONTENT)
            {
                downloadedChunkInfo.setChunkIndex(this.chunkIndex);
                downloadedChunkInfo.setContentLength(response.getEntity().getContentLength());
                InputStream inputStream = response.getEntity().getContent();
                File tempFile
                        = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
                java.nio.file.Files.copy(
                        inputStream,
                        tempFile.toPath(),
                        StandardCopyOption.REPLACE_EXISTING);
                inputStream.close();

                downloadedChunkInfo.setChunkPath(tempFile.toPath().toAbsolutePath().toString());
                downloadedChunkInfo.setFailed(false);

            }else{
                downloadedChunkInfo.setFailed(true);
                downloadedChunkInfo.setHttpStatus(response.getCode());
                downloadedChunkInfo.setErr("Download failed due to HTTP error.");
            }
        } catch (IOException e) {
            downloadedChunkInfo.setFailed(true);
            downloadedChunkInfo.setErr(e.getLocalizedMessage());
        }finally {
            if(response != null){
                response.close();
            }
        }
        return downloadedChunkInfo;
    }
}
