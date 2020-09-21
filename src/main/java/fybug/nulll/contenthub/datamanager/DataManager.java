package fybug.nulll.contenthub.datamanager;
import java.nio.file.Path;

public
class DataManager {
    private static Path TempFile = Path.of("tmp/temp").toAbsolutePath();

    public static
    void setTempFile(Path path) { TempFile = path.toAbsolutePath(); }

    public static
    Path getTempFile() { return TempFile; }
}
