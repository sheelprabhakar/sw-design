package code4copy.filedownloader.client;

import com.code4copy.filedownloader.DownloadConfig;
import com.code4copy.filedownloader.DownloadRequest;
import com.code4copy.filedownloader.HttpAuthInfo;
import com.code4copy.filedownloader.HttpAuthMode;
import com.code4copy.filedownloader.client.HttpDownloadClient;
import com.code4copy.filedownloader.client.Range;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.ProtocolException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class HttpDownloadClientTest {
    private final String FILE_URL = "https://www.code4copy.com/file.txt";
    public static final String TEXT = "text";

    private static HttpClient httpClient;
    private static HttpResponse httpResponse;
    private static Header header;
    private static Header contentHeader;

    private static CloseableHttpResponse closeableHttpResponse;
    private static HttpEntity httpEntity;
    private static InputStream inputStream;

    @BeforeAll
    public static void init() {
        httpClient = Mockito.mock(HttpClient.class);
        httpResponse = Mockito.mock(HttpResponse.class);
        header = Mockito.mock(Header.class);
        contentHeader = Mockito.mock(Header.class);

        closeableHttpResponse = Mockito.mock(CloseableHttpResponse.class);
        httpEntity = Mockito.mock(HttpEntity.class);
        inputStream = new ByteArrayInputStream(TEXT.getBytes());
    }

    @Test
    public void test_download_ok() throws Exception {
        makeCheckRangeMocks("bytes");

        when(closeableHttpResponse.getCode()).thenReturn(HttpStatus.SC_PARTIAL_CONTENT);
        when(closeableHttpResponse.getEntity()).thenReturn(httpEntity);
        when(httpEntity.getContent()).thenReturn( new ByteArrayInputStream(TEXT.getBytes()));
        Mockito.when(httpEntity.getContent())
                .thenAnswer((Answer) invocation -> new ByteArrayInputStream(TEXT.getBytes()));

        when(httpEntity.getContentLength()).thenReturn(4l);
        when(httpClient.execute(Mockito.any(HttpGet.class))).thenReturn(closeableHttpResponse);

        DownloadRequest request = DownloadRequest.builder().url(FILE_URL).chunks(3)
                .downloadLocation(DownloadConfig.defaultConfig().getDownloadLocation())
                .authInfo(HttpAuthInfo.builder().authMode(HttpAuthMode.BASIC).userName("user").password("pwd")
                        .build()).build();
        HttpDownloadClient downloadClient = new HttpDownloadClient(request, httpClient);
        File file = downloadClient.download();
        assertTrue(file.exists());
        assertEquals(Files.readString(file.toPath()), TEXT+TEXT+TEXT);
    }
    @Test
    public void isRangeSupported_true() throws Exception {
        makeCheckRangeMocks("bytes");

        DownloadRequest request = DownloadRequest.builder().url(FILE_URL).build();
        HttpDownloadClient downloadClient = new HttpDownloadClient(request, httpClient);

        assertTrue(downloadClient.checkRangeSupported().isSupported());
    }

    private static void makeCheckRangeMocks(String bytes) throws ProtocolException, IOException {
        when(header.getValue()).thenReturn(bytes);
        when(contentHeader.getValue()).thenReturn("4");
        when(httpResponse.getCode()).thenReturn(HttpStatus.SC_OK);
        when(httpResponse.getHeader(HttpHeaders.ACCEPT_RANGES)).thenReturn(header);
        when(httpResponse.getHeader(HttpHeaders.CONTENT_LENGTH)).thenReturn(contentHeader);
        when(httpClient.execute(Mockito.any(HttpHead.class))).thenReturn(httpResponse);
    }

    @Test
    public void isRangeSupported_false() throws Exception {
        makeCheckRangeMocks("none");

        DownloadRequest request = DownloadRequest.builder().url(FILE_URL).build();
        HttpDownloadClient downloadClient = new HttpDownloadClient(request, httpClient);

        assertFalse(downloadClient.checkRangeSupported().isSupported());
    }

    @Test
    public void test_makeRangeList_ok(){
        List<Range> rangeList = HttpDownloadClient.makeRangeList(5, 20);
        Range range = rangeList.get(4);
        assertEquals(range.getFrom(),16);
        assertEquals(range.getTo(),19);
        range = rangeList.get(0);
        assertEquals(range.getFrom(),0);
        assertEquals(range.getTo(),3);

        range = rangeList.get(1);
        assertEquals(range.getFrom(),4);
        assertEquals(range.getTo(),7);
    }
}
