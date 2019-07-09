package com.tuthill.dupecrawl;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class DupeCrawl {

    public static void main(final String[] args) {
        final List<FileInfo> fileList = new ArrayList<>();
        crawl(fileList, "/home/craig/craig.cavanaugh@gmail.com");
        System.out.println(fileList.size()+ " files found");

        System.out.println("Searching for duplicates");

        final List<DupeFile> dupeFileList = findDuplicates(fileList);

        System.out.println("Search complete");

        long totalCount = 0;

        for (DupeFile dupeFile : dupeFileList) {
            System.out.println(dupeFile.toString());
            totalCount += dupeFile.duplicatesList.size();
        }

        System.out.println(dupeFileList.size() + " duplicate sets found");
        System.out.println(totalCount + " duplicate files found");

        System.exit(0);
    }

    /**
     * Crawls a directory for file names
     *
     * @param fileList List to load files into
     * @param directory directory to crawl
     */
    private static void crawl(final List<FileInfo> fileList, final String directory) {
        File root = new File(directory);

        for (final File file : Objects.requireNonNull(root.listFiles())) {
            try {
                if (file.isDirectory()) {
                    crawl(fileList, file.getAbsolutePath());
                } else {    // add if not a symbolic link
                    if (!Files.isSymbolicLink(file.toPath())) {
                        fileList.add(new FileInfo(file));
                    }
                }
            } catch (NullPointerException e) {
                // swallow, posix non-readable file
            }
        }

        if (fileList.size() % 1000 == 0) {
            System.out.println(fileList.size()+ " files found");
        }
    }

    private static List<DupeFile> findDuplicates(final List<FileInfo> fileList) {
        final Map<String, DupeFile> dupeFileList = new HashMap<>();

        for (int i = 0; i < fileList.size(); i++) {
            for (final FileInfo fileInfo : fileList) {
                if (fileList.get(i) != fileInfo) {   // prevent self comparison with an identity check
                    if (fileList.get(i).equals(fileInfo)) {  // we have a match
                        /*System.out.println("match " + fileList.get(i).fullPath + ", "  + fileInfo.fullPath);
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }*/

                        final String fileName = fileList.get(i).fileName;

                        final DupeFile dupeFile = dupeFileList.computeIfAbsent(fileName, s -> new DupeFile(fileName));
                        dupeFile.addDuplicate(fileList.get(i));
                        dupeFile.addDuplicate(fileInfo);
                    }
                }
            }

            if (i % 100 == 0) {
                System.out.println(i + " of " + fileList.size() + " checks complete; " + dupeFileList.size() + " duplications");
            }
        }

        return new ArrayList<>(dupeFileList.values());
    }

    private static class DupeFile {
        final String fileName;
        Set<FileInfo> duplicatesList = new HashSet<>();

        DupeFile(final String fileName) {
            this.fileName = fileName;
        }

        void addDuplicate(FileInfo fileInfo) {
            duplicatesList.add(fileInfo);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fileName);
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append(fileName).append("\n");

            for (final FileInfo fileInfo : duplicatesList) {
                builder.append(fileInfo.fullPath).append("\n");
            }

            return builder.toString();
        }
    }

    private static class FileInfo implements Comparable<FileInfo> {
        final String fullPath;
        final String fileName;
        final long fileSize;

        FileInfo(final File file) {
            fullPath = file.getAbsolutePath();
            fileName = file.getName();
            fileSize = file.length();
        }

        @Override
        public int compareTo(final FileInfo that) {
            return this.fileName.compareTo(that.fileName);
        }

        @Override
        public boolean equals(final Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileInfo fileInfo = (FileInfo) o;
            return fileSize == fileInfo.fileSize &&
                           fileName.equals(fileInfo.fileName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(fullPath, fileName, fileSize);
        }
    }
}
