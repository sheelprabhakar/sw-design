package code4copy.filedownloader.client;

import com.code4copy.filedownloader.client.ChunkDownloader;
import com.code4copy.filedownloader.client.DownloadedChunkInfo;
import com.code4copy.filedownloader.client.Range;
import org.apache.hc.client5.http.ConnectTimeoutException;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URI;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class ChunkDownloaderTest {

    public static final String TEXT = "text";
    private final String FILE_URL = "https://www.code4copy.com/file.jpg";


    private static HttpClient httpClient;
    private static CloseableHttpResponse httpResponse;
    private static HttpEntity httpEntity;
    private static InputStream inputStream;

    @BeforeAll
    public static void init() {
        httpClient = Mockito.mock(HttpClient.class);
        httpResponse = Mockito.mock(CloseableHttpResponse.class);
        httpEntity = Mockito.mock(HttpEntity.class);
        inputStream = new ByteArrayInputStream(TEXT.getBytes());

    }

    @Test
    public void test_call_ok() throws Exception {

        when(httpResponse.getCode()).thenReturn(HttpStatus.SC_PARTIAL_CONTENT);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(inputStream);
        when(httpEntity.getContentLength()).thenReturn(4l);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(httpResponse);

        ChunkDownloader downloadCallable = new ChunkDownloader(httpClient, null, URI.create( FILE_URL), 0, new Range(0,3));
        DownloadedChunkInfo downloadedChunkInfo = downloadCallable.call();
        assertTrue(!downloadedChunkInfo.isFailed());
        File f = new File(downloadedChunkInfo.getChunkPath());
        assertTrue(f.exists());
        assertTrue(Files.readString(f.toPath()).equals(TEXT));
    }

    @Test
    public void test_call_404() throws Exception {

        when(httpResponse.getCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(inputStream);
        when(httpEntity.getContentLength()).thenReturn(4l);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(httpResponse);

        ChunkDownloader downloadCallable = new ChunkDownloader(httpClient, null, URI.create( FILE_URL), 0, new Range(0,3));
        DownloadedChunkInfo downloadedChunkInfo = downloadCallable.call();
        assertTrue(downloadedChunkInfo.isFailed());
        assertTrue(downloadedChunkInfo.getHttpStatus() == HttpStatus.SC_NOT_FOUND );
    }

    @Test
    public void test_call_exception() throws Exception {

        when(httpResponse.getCode()).thenReturn(HttpStatus.SC_NOT_FOUND);
        when(httpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn(inputStream);
        when(httpEntity.getContentLength()).thenReturn(4l);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenThrow(new ConnectTimeoutException("Timeout"));

        ChunkDownloader downloadCallable = new ChunkDownloader(httpClient, null, URI.create( FILE_URL), 0, new Range(0,3));
        DownloadedChunkInfo downloadedChunkInfo = downloadCallable.call();
        assertTrue(downloadedChunkInfo.isFailed());
        assertTrue(downloadedChunkInfo.getErr().equals("Timeout") );
    }

}
