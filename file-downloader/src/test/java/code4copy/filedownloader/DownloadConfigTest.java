package code4copy.filedownloader;

import com.code4copy.filedownloader.DownloadConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;


public class DownloadConfigTest {
    @Test
    public void test_defaultConfig_of(){
        assertTrue( DownloadConfig.defaultConfig().getChunks() == 5);
    }
}
