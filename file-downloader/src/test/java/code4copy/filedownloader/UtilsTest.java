package code4copy.filedownloader;

import com.code4copy.filedownloader.Utils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class UtilsTest {
    @Test
    public void test_concatFiles_ok() throws Exception {
        File tempFile1
                = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
        Files.writeString(tempFile1.toPath(), "abc");
        File tempFile2
                = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
        Files.writeString(tempFile2.toPath(), "123");

        File tempFileMerged
                = File.createTempFile(UUID.randomUUID().toString(), ".tmp");
        Utils.concatFiles(tempFileMerged, Arrays.asList(tempFile1, tempFile2));
        assertTrue(Files.readString(tempFileMerged.toPath()).equals("abc123"));

        tempFile1.delete();
        tempFile2.delete();
        tempFileMerged.delete();

    }
    @Test
    public void test_is_empty_ok(){
        assertTrue( Utils.isEmpty(""));
        assertTrue( Utils.isEmpty(" "));
        assertTrue( Utils.isEmpty(null));
        assertTrue( !Utils.isEmpty("dddd"));
        assertTrue( !Utils.isEmpty("a"));
    }
}
