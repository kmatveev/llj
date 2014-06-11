package llj.asm.bytecode;

public class SizeStats {

    public static void main(String[] args) {

//        File currentDir = new File("." + File.separatorChar);
//        if (!currentDir.isDirectory()) throw new RuntimeException();
//
//        FilenameFilter filter = new FilenameFilter() {
//            @Override
//            public boolean accept(File dir, String name) {
//                return name.endsWith(".class");
//            }
//        };
//
//        Map<String, Integer> total = new HashMap<String, Integer>();
//        int totalSize = 0;
//        for (File file : currentDir.listFiles(filter)) {
//            try {
//                ClassFileFormat format = new ClassFileFormat();
//                System.out.println("File:" + file.getName() + "; size:" + file.length());
//                totalSize += file.length();
//                Map<String, Integer> statistics = new HashMap<String, Integer>();
//                format.readFrom(Channels.newChannel(new FileInputStream(file)), statistics);
//                System.out.println("Has compact references:" + format.constPool.hasCompactRefs() + "; Has compact strings:" + format.constPool.hasCompactStrings());
//                for (Map.Entry<String, Integer> statEntry : statistics.entrySet()) {
//                    System.out.print(statEntry.getKey() + "=" + statEntry.getValue() + "; ");
//                    Integer prevValue = total.get(statEntry.getKey()) == null ? new Integer(0) : total.get(statEntry.getKey()) ;
//                    total.put(statEntry.getKey(), new Integer(statEntry.getValue().intValue() + prevValue.intValue()));
//                }
//                {
//                    int compactConstants = format.constPool.writeToCompact(new DummyWritableChannel());
//                    int originalConstants = statistics.get("Constants");
//                    int ratio = (compactConstants * 100) / originalConstants;
//                    System.out.println("Compact constants=" + compactConstants + "(" + ratio + "%)");
//                }
//                System.out.println(" ");
//            } catch (Exception e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        System.out.println("Total:" + totalSize);
//        for (Map.Entry<String, Integer> statEntry : total.entrySet()) {
//            int percentage = (100 * statEntry.getValue())/totalSize;
//            System.out.println(statEntry.getKey() + "=" + statEntry.getValue() + "(" + percentage + "%)");
//        }
    }

}
