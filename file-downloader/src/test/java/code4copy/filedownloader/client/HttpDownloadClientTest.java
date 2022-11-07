package code4copy.filedownloader.client;

import com.code4copy.filedownloader.DownloadRequest;
import com.code4copy.filedownloader.client.HttpDownloadClient;
import com.code4copy.filedownloader.client.Range;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.classic.methods.HttpHead;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

public class HttpDownloadClientTest {
    private final String FILE_URL = "https://www.code4copy.com/file.jpg";


    private static HttpClient httpClient;
    private static HttpResponse httpResponse;
    private static Header header;
    private static Header contentHeader;

    @BeforeAll
    public static void init() {
        httpClient = Mockito.mock(HttpClient.class);
        httpResponse = Mockito.mock(HttpResponse.class);
        header = Mockito.mock(Header.class);
        contentHeader = Mockito.mock(Header.class);
    }

    @Test
    public void isRangeSupported_true() throws Exception {
        when(header.getValue()).thenReturn("bytes");
        when(contentHeader.getValue()).thenReturn("123");
        when(httpResponse.getCode()).thenReturn(HttpStatus.SC_OK);
        when(httpResponse.getHeader(HttpHeaders.ACCEPT_RANGES)).thenReturn(header);
        when(httpResponse.getHeader(HttpHeaders.CONTENT_LENGTH)).thenReturn(contentHeader);
        when(httpClient.execute(Mockito.any(HttpHead.class))).thenReturn(httpResponse);

        DownloadRequest request = DownloadRequest.builder().url(FILE_URL).build();
        HttpDownloadClient downloadClient = new HttpDownloadClient(request, httpClient);

        assertTrue(downloadClient.checkRangeSupported().isSupported());
    }

    @Test
    public void isRangeSupported_false() throws Exception {
        when(header.getValue()).thenReturn("none");
        when(contentHeader.getValue()).thenReturn("123");
        when(httpResponse.getCode()).thenReturn(HttpStatus.SC_OK);
        when(httpResponse.getHeader(HttpHeaders.ACCEPT_RANGES)).thenReturn(header);
        when(httpResponse.getHeader(HttpHeaders.CONTENT_LENGTH)).thenReturn(contentHeader);
        when(httpClient.execute(Mockito.any(HttpHead.class))).thenReturn(httpResponse);

        DownloadRequest request = DownloadRequest.builder().url(FILE_URL).build();
        HttpDownloadClient downloadClient = new HttpDownloadClient(request, httpClient);

        assertFalse(downloadClient.checkRangeSupported().isSupported());
    }

    @Test
    public void test_makeRangeList_ok(){
        List<Range> rangeList = HttpDownloadClient.makeRangeList(5, 20);
        Range range = rangeList.get(4);
        assertEquals(range.getFrom(),16);
        assertEquals(range.getTo(),20);
        range = rangeList.get(0);
        assertEquals(range.getFrom(),0);
        assertEquals(range.getTo(),3);

        range = rangeList.get(1);
        assertEquals(range.getFrom(),4);
        assertEquals(range.getTo(),7);
    }
}
