package muni.fi.api.helper;

import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.OutputStream;

@Slf4j
public class ResponseHandlerHelper {

    /**
     * Writes content to output stream, which can be prompted to be downloaded as a file from the browser
     *
     * @param response Http response to write content to
     * @param content  String content of the file to be downloaded
     */
    public static void writeContentToOutputStream(HttpServletResponse response, String content) {
        try {
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(content.getBytes());
            outputStream.flush();
            outputStream.close();
        } catch (IOException e) {
            String message = "Failed to download file";
            log.error(message, e);
            throw new RuntimeException(message);
        }
    }
}
