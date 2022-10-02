package shuar.reducer.file;

import shuar.Shuar.Reducer;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.stream.Stream;

import static shuar.util.Utils.runUnchecked;

public class FileReducer implements Reducer<String, URI>, AutoCloseable {
    private final URI uri;
    private final OutputStream bos;
    private final boolean appendNewline;

    private final static byte[] LINE_TERMINATOR = System.lineSeparator().getBytes();

    public FileReducer(File file) {
        this(file, true);
    }

    public FileReducer(File file, boolean appendNewline) {
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        uri = file.toURI();
        this.appendNewline = appendNewline;
    }

    @Override
    public URI reduce(Stream<String> items) {
        items.forEach(item -> runUnchecked(() -> {
            bos.write(item.getBytes());
            if (appendNewline) {
                bos.write(LINE_TERMINATOR);
            }
        }));
        return uri;
    }

    @Override
    public void close() throws Exception {
        bos.flush();
        bos.close();
    }
}
