package io.github.jbellis.brokk.analyzer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

/**
 * Abstraction for a filename relative to the repo.  This exists to make it less difficult to ensure
 * that different filename objects can be meaningfully compared, unlike bare Paths which may
 * or may not be absolute, or may be relative to the jvm root rather than the repo root.
 */
public class RepoFile implements BrokkFile {
    private static final long serialVersionUID = 1L;
    private transient Path root;
    private transient Path relPath;

    /**
     * root must be pre-normalized; we will normalize relPath if it is not already
     */
    public RepoFile(Path root, Path relPath) {
        // We can't rely on these being set until after deserialization
        if (root != null && relPath != null) {
            if (!root.isAbsolute()) {
                throw new IllegalArgumentException("Root must be absolute");
            }
            if (!root.equals(root.normalize())) {
                throw new IllegalArgumentException("Root must be normalized");
            }
            if (relPath.isAbsolute()) {
                throw new IllegalArgumentException("RelPath must be relative");
            }
            relPath = relPath.normalize();
        }
        this.root = root;
        this.relPath = relPath;
    }

    public RepoFile(Path root, String relName) {
        this(root, Path.of(relName));
    }

    @Override
    public Path absPath() {
        return root.resolve(relPath);
    }

    public void create() throws IOException {
        Files.createDirectories(absPath().getParent());
        Files.createFile(absPath());
    }

    public void write(String st) throws IOException {
        Files.createDirectories(absPath().getParent());
        Files.writeString(absPath(), st);
    }

    /**
     * Also relative
     */
    public String getParent() {
        return Optional.ofNullable(relPath.getParent())
                .map(Path::toString)
                .orElse("");
    }

    @Override
    public String toString() {
        return relPath.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RepoFile)) return false;
        RepoFile repoFile = (RepoFile) o;
        return Objects.equals(root, repoFile.root) && 
               Objects.equals(relPath, repoFile.relPath);
    }

    @Override
    public int hashCode() {
        return relPath.hashCode();
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        // store the string forms of root/relPath
        oos.writeUTF(root.toString());
        oos.writeUTF(relPath.toString());
    }

    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        // read all non-transient fields
        ois.defaultReadObject();
        // reconstitute root/relPath from the strings
        String rootString = ois.readUTF();
        String relString = ois.readUTF();
        // both must be absolute/relative as before
        root = Path.of(rootString);
        if (!root.isAbsolute()) {
            throw new IllegalArgumentException("Root must be absolute");
        }

        relPath = Path.of(relString);
        if (relPath.isAbsolute()) {
            throw new IllegalArgumentException("RelPath must be relative");
        }
    }
}
