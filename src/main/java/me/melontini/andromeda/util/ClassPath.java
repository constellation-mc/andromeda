package me.melontini.andromeda.util;

import lombok.Getter;
import lombok.SneakyThrows;
import me.melontini.dark_matter.api.base.util.Utilities;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;

public final class ClassPath {

    Set<Path> scanned = new HashSet<>();
    Set<Info> infos = new TreeSet<>(Comparator.comparing(info -> info.name));

    private ClassPath() {

    }

    public static ClassPath from(URL... urls) {
        ClassPath classPath = new ClassPath();
        for (URL url : urls) {
            classPath.addUrl(url);
        }
        return classPath;
    }

    public static ClassPath from(ClassLoader cl) {
        ClassPath classPath = new ClassPath();
        classPath.scanClassLoader(cl);
        return classPath;
    }

    public void scanClassLoader(ClassLoader cl) {
        if (cl.getParent() != null) scanClassLoader(cl.getParent());

        if (cl instanceof URLClassLoader ucl) {
            for (URL url : ucl.getURLs()) {
                this.addUrl(url);
            }
        }
    }

    public Set<Info> getTopLevelRecursive(String pckg) {
        String s = pckg.replace('/', '.');
        Set<Info> set = new TreeSet<>(Comparator.comparing(info -> info.name));

        for (Info info : infos) {
            if (info.getName().startsWith(s)) set.add(info);
        }

        return Collections.unmodifiableSet(set);
    }

    public void addUrl(URL url) {
        if (url == null) return;

        Path path = Utilities.supplyUnchecked(() -> Path.of(url.toURI()));

        if (scanned.contains(path)) return;

        if (Files.isDirectory(path)) {
            scan(path);
        } else {
            scanJar(path);
        }
    }

    @SneakyThrows
    private void scanJar(Path path) {
        FileSystem fs = Utilities.supplyUnchecked(() -> {
            try {
                return FileSystems.getFileSystem(path.toUri());
            } catch (Exception e) {
                return FileSystems.newFileSystem(path, Collections.emptyMap());
            }
        });

        scan(fs.getRootDirectories().iterator().next());

        fs.close();
    }

    @SneakyThrows
    private void scan(Path path) {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                String name = path.relativize(file).toString();
                if (name.endsWith(".class")) {
                    String clsName = name.substring(0, name.length() - ".class".length()).replace('\\', '.').replace('/', '.');
                    ClassPath.this.infos.add(new Info(file, clsName));
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    @Getter
    public static class Info {

        private final Path path;
        private final String name;

        Info(Path path, String name) {
            this.path = path;
            this.name = name;
        }

        public byte[] readAllBytes() throws IOException {
            return Files.readAllBytes(path);
        }

        public String packageName() {
            int c = name.lastIndexOf('.');
            return c < 0 ? "" : name.substring(0, c);
        }
    }
}