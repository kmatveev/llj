package llj.util.swing;

import javax.swing.*;
import javax.swing.filechooser.FileSystemView;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ZipFilesystemView extends FileSystemView {

    private static FilenameFilter acceptAllFilter  = new FilenameFilter() {
        @Override
        public boolean accept(File dir, String name) {
            return true;
        }
    };
    private final ZipFile zipFile;
    // private final ZipRootFile ROOT;
    private final ZipItemFile ROOT;

    public ZipFilesystemView(ZipFile zipFile) {
        this.zipFile = zipFile;
        ROOT = new ZipDirectoryFile(zipFile, null, "");// new ZipRootFile(zipFile);
    }

    @Override
    public File createNewFolder(File containingDir) throws IOException {
        throw new IOException();
    }

    @Override
    public File[] getFiles(File dir, boolean useFileHiding) {
        List<File> result = new ArrayList<>();
        if (dir == ROOT) {
            return ROOT.listFiles();
        } else {
            return dir.listFiles();

        }
    }

    public File getDefaultDirectory() {
        return ROOT;
    }

    @Override
    public File[] getRoots() {
        return new File[] {ROOT};
    }

    @Override
    public String getSystemDisplayName(File f) {
        return f.getName();
    }

    @Override
    public Icon getSystemIcon(File f) {
        return UIManager.getIcon(f.isDirectory() ? "FileView.directoryIcon" : "FileView.fileIcon");
    }

    public Icon getSystemIcon(File f, int width, int height) {
        return super.getSystemIcon(f);
    }

    @Override
    public boolean isLink(File file) {
        return false;
    }

//    private static class ZipRootFile extends File {
//
//        private final ZipFile zipFile;
//
//        public ZipRootFile(ZipFile zipFile) {
//            super("");
//            this.zipFile = zipFile;
//        }
//
//        @Override
//        public boolean isDirectory() {
//            return true;
//        }
//
//        @Override
//        public boolean isAbsolute() {
//            return true;
//        }
//
//        @Override
//        public File getCanonicalFile() throws IOException {
//            return this;
//        }
//
//        @Override
//        public String[] list(FilenameFilter filter) {
//            List<ZipEntryFile> result = listFilesInternal(filter);
//            String[] resultStr = new String[result.size()];
//            for (int i = 0;i < result.size();i++) {
//                resultStr[i] = result.get(i).entry.getName();
//            }
//            return resultStr;
//       }
//
//        @Override
//        public String[] list() {
//            return list(acceptAllFilter);
//        }
//
//        private List<ZipEntryFile> listFilesInternal(FilenameFilter filter) {
//            List<ZipEntryFile> result = new ArrayList<>();
//            for (Enumeration<? extends ZipEntry> entryEnum = zipFile.entries(); entryEnum.hasMoreElements(); ) {
//                ZipEntry entry = entryEnum.nextElement();
//                String name = entry.getName();
//                if (name.indexOf("/") >= 0) {
//                    if (name.indexOf("/") == name.length() - 1) {
//                        result.add(new ZipEntryFile(zipFile, null, entry));
//                    }
//                } else {
//                    result.add(new ZipEntryFile(zipFile, null, entry));
//                }
//            }
//            return result;
//        }
//
//        @Override
//        public File[] listFiles() {
//            List<ZipEntryFile> result = listFilesInternal(acceptAllFilter);
//            return result.toArray(new File[result.size()]);
//        }
//
//        @Override
//        public File[] listFiles(FilenameFilter filter) {
//            List<ZipEntryFile> result = listFilesInternal(filter);
//            return result.toArray(new File[result.size()]);
//        }
//    }

    private static abstract class ZipItemFile extends File {

        protected final ZipFile zipFile;

        public ZipItemFile(ZipFile zipFile, File parent, String entryName) {
            super(parent, parent == null ? entryName : entryName.substring(parent.getPath().length()));
            this.zipFile = zipFile;
        }

        @Override
        public File[] listFiles() {
            return listFiles(acceptAllFilter);
        }

        @Override
        public String[] list() {
            return list(acceptAllFilter);
        }

        @Override
        public String[] list(FilenameFilter filter) {
            File[] files = listFiles(filter);
            String[] resultStr = new String[files.length];
            for (int i = 0;i < files.length;i++) {
                resultStr[i] = files[i].getName();
            }
            return resultStr;
        }

        public File getCanonicalFile() throws IOException {
            return this;
        }

        @Override
        public boolean exists() {
            return true;
        }

        public abstract String entryName();

        @Override
        public File[] listFiles(FilenameFilter filter) {
            return isDirectory() ? ZipDirectoryFile.getFiles(zipFile, entryName(), this) : new File[0];
        }


    }

    private static class ZipDirectoryFile extends ZipItemFile {

        private final String name; // this should be a full dir name, like "dir1/dir2/dir3"

        public ZipDirectoryFile(ZipFile zipFile,File parent, String entryName) {
            super(zipFile, parent, entryName);
            this.name = entryName;
        }

        @Override
        public boolean isDirectory() {
            return true;
        }

        @Override
        public boolean isFile() {
            return false;
        }

        @Override
        public String entryName() {
            return name;
        }

        private static File[] getFiles(ZipFile zipFile, String dirName, ZipItemFile parent) {
            List<ZipItemFile> result = new ArrayList<>();
            // first pass, we check declared entries which are children of specified dir
            for (Enumeration<? extends ZipEntry> entryEnum = zipFile.entries(); entryEnum.hasMoreElements();) {
                ZipEntry entry = entryEnum.nextElement();
                if (entry.getName().startsWith(dirName)) {
                    String remaining = entry.getName().substring(dirName.length());
                    if (remaining.length() > 0) {
                        if (remaining.indexOf("/") < 0) {
                            // TODO apply filter
                            result.add(new ZipEntryFile(zipFile, parent, entry));
                        } else if (remaining.indexOf("/") == remaining.length() - 1) {
                            // TODO apply filter
                            result.add(new ZipEntryFile(zipFile, parent, entry));
                        }
                    }
                }
            }
            // second pass, we generate artificial entries for dirs which are not declared explicitly
            for (Enumeration<? extends ZipEntry> entryEnum = zipFile.entries(); entryEnum.hasMoreElements();) {
                ZipEntry entry = entryEnum.nextElement();
                if (entry.getName().startsWith(dirName)) {
                    String remaining = entry.getName().substring(dirName.length());
                    if (remaining.length() > 0) {
                        if (remaining.indexOf("/") >= 0) {
                            String[] dirs = remaining.split("/");
                            String fullDir = dirName + dirs[0] + "/";
                            boolean found = false;
                            for (int i = 0; i < result.size(); i++) {
                                if (result.get(i).entryName().equals(fullDir)) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                result.add(new ZipDirectoryFile(zipFile, parent, fullDir));
                            }
                        }
                    }
                }
            }

            return result.toArray(new File[result.size()]);
        }

    }

    private static class ZipEntryFile extends ZipItemFile {

        private final ZipEntry entry;

        public ZipEntryFile(ZipFile zipFile, ZipItemFile parent, ZipEntry entry) {
            super(zipFile, parent, entry.getName());
            this.entry = entry;
        }

        @Override
        public boolean isDirectory() {
            return entry.isDirectory();
        }

        @Override
        public boolean isAbsolute() {
            return true;
        }

        @Override
        public boolean isFile() {
            return !entry.isDirectory();
        }

        @Override
        public String entryName() {
            return entry.getName();
        }
    }

}
