/*
 * MIT License
 *
 * Copyright (c) 2020 OneManCrew
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.github.onemancrew.vertx.tftp;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

@RunWith(VertxUnitRunner.class)
public class TftpClientTest {


    public static final String TEST_FILE = "test.txt";

    Vertx vertx;

    @Before
    public void before(TestContext context) {
        vertx = Vertx.vertx();
    }

    @After
    public void after(TestContext context) {
        vertx.close(context.asyncAssertSuccess());
    }

    @Test
    public void testTFTPClient(TestContext context) throws Throwable {
        File currentDirFile = new File("");
        String currDir = currentDirFile.getAbsolutePath() + File.separator;
        String testStr = "1...2...3..4...test";
        writeToFile(testStr);
        Async async = context.async();
        TftpClient client = new TftpClient(vertx, "127.0.0.1", 69);
        client.upload(currDir + TEST_FILE, (progress) -> {
        }, (result) -> {
            if (result.failed()) {
                context.asyncAssertFailure();
                async.complete();
            } else {
                delFile(currDir + TEST_FILE);
                client.download(TEST_FILE, currDir, (result2) -> {
                    if (result2.failed()) {
                        context.asyncAssertFailure();
                    } else {
                        String tmp = readFromFile(currDir + TEST_FILE);
                        context.assertEquals(testStr,tmp);
                        delFile(currDir + TEST_FILE);
                    }
                    async.complete();

                });
            }
        });


    }

    private void delFile(String file) {
        try {
            Files.deleteIfExists(Paths.get(file));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToFile(String text) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(TEST_FILE));
        writer.write(text);
        writer.close();
    }

    private static String readFromFile(String filePath) {
        byte[] encoded = new byte[0];
        try {
            encoded = Files.readAllBytes(Paths.get(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String(encoded, StandardCharsets.UTF_8);

    }
}
