package llj.asm.bytecode;

import llj.packager.jclass.ClassFileFormat;
import llj.util.ref.MapResolver;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Panel;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ClassView1 {

    private final JFrame window;

    private final List<ClassData> classes = new ArrayList<ClassData>();
    MapResolver<ClassData, String> cache = new MapResolver<ClassData, String>();
    private MethodData currentMethod;
    private final Map<MethodData, MethodStaticInfo> staticInfos = new HashMap<MethodData, MethodStaticInfo>();

    private final JTree classMembersTree;
    private final ClassMembersTreeModel classMembersTreeModel;

    private final JLabel methodData = new JLabel("<no method selected>");

    private final JTable instructionsTable;
    private final MethodTableModel instructionsTableModel;

    private final JTable localVarsTable;
    private final LocalVarsTableModel localVarsTableModel;
    // private final DependentClassesTableModel dependentClassesTableModel;

    private final JButton addDirToClasspathButton;
    private final List<File> classPathEntries = new ArrayList<File>();
    private final ClasspathTableModel classpathTableModel;


    public ClassView1() {

        window = new JFrame("ClassView");

        JMenuBar menuBar = new JMenuBar();

        {
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);

            {
                JMenuItem openFileItem = new JMenuItem("Open");
                openFileItem.setAction(new AbstractAction("Open") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser classChooser = new JFileChooser();
                        int result = classChooser.showOpenDialog(window);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File selected = classChooser.getSelectedFile();
                            try {
                                setFile(selected);
                            } catch (Exception ex) {
                                // TODO
                                ex.printStackTrace();
                            }
                        }

                    }

                });
                fileMenu.add(openFileItem);
            }

            {
                JMenuItem exitItem = new JMenuItem("Exit");
                exitItem.setAction(new AbstractAction("Exit") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        System.exit(0);
                    }

                });
                fileMenu.add(exitItem);
            }

        }


        window.setJMenuBar(menuBar);

        {
            JPanel contentPane = new JPanel();
            window.setContentPane(contentPane);
            contentPane.setLayout(new BorderLayout());
            JSplitPane split1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            contentPane.add(split1, BorderLayout.CENTER);

            {
                JPanel classPathPanel = new JPanel();
                classPathPanel.setLayout(new BorderLayout());

                JToolBar classPathToolbar = new JToolBar(JToolBar.HORIZONTAL);
                classPathPanel.add(classPathToolbar, BorderLayout.NORTH);

                {
                    addDirToClasspathButton = new JButton();
                    addDirToClasspathButton.setAction(new AbstractAction("Add directory") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            throw new RuntimeException();
//                            JFileChooser dirChooser = new JFileChooser();
//                            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//                            int status = dirChooser.showDialog(window, "Choose");
//                            if (status == JFileChooser.APPROVE_OPTION) {
//                                classPathEntries.add(dirChooser.getSelectedFile());
//                                classpathTableModel.fireTableDataChanged();
//                            }
                        }
                    });
                    classPathToolbar.add(addDirToClasspathButton);
                }

                classpathTableModel = new ClasspathTableModel();
                JTable classPathTable = new JTable(classpathTableModel);

                classPathTable.setPreferredSize(new Dimension(700, 200));

                classPathPanel.add(classPathTable, BorderLayout.CENTER);

                split1.setBottomComponent(classPathPanel);
            }


            JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            split1.setTopComponent(split2);

            {
                classMembersTreeModel = new ClassMembersTreeModel();
                classMembersTree = new JTree(classMembersTreeModel);
                classMembersTree.setRootVisible(false);

                split2.setLeftComponent(classMembersTree);

                classMembersTree.addTreeSelectionListener(new TreeSelectionListener() {
                    @Override
                    public void valueChanged(TreeSelectionEvent e) {
                        TreePath path = e.getPath();
                        if (path.getPathCount() == 3) {
                            MethodData methodData = (MethodData) path.getPathComponent(2);
                            methodSelected(methodData);
                        } else {
                            // ClassData classData = path.getPathComponent(1);
                        }
                    }
                });


                classMembersTree.setPreferredSize(new Dimension(150, 300));
            }



            {
                Panel comp = new Panel();
                comp.setLayout(new BorderLayout());
                split2.setRightComponent(comp);

                comp.add(methodData, BorderLayout.NORTH);

                JTabbedPane methodContentTabs = new JTabbedPane(JTabbedPane.BOTTOM);
                methodContentTabs.setPreferredSize(new Dimension(700, 300));

                {
                    instructionsTableModel = new MethodTableModel();
                    instructionsTable = new JTable(instructionsTableModel);
                    instructionsTable.setShowGrid(false);
                    instructionsTable.setFillsViewportHeight(true);
                    JScrollPane scrollPane = new JScrollPane(instructionsTable);
                    methodContentTabs.addTab("Table", scrollPane);

                    instructionsTable.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
                                showSelectedInstructionInfo();
                            }
                        }
                    });

                }

                {
                    localVarsTableModel = new LocalVarsTableModel();
                    localVarsTable = new JTable(localVarsTableModel);
                    localVarsTable.setShowGrid(true);
                    localVarsTable.setFillsViewportHeight(true);
                    JScrollPane scrollPane = new JScrollPane(localVarsTable);
                    methodContentTabs.addTab("Variables", scrollPane);
                }


                comp.add(methodContentTabs, BorderLayout.CENTER);
            }


        }

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.pack();

        window.setVisible(true);

    }

    private void showSelectedInstructionInfo() {
        int instrIndex = instructionsTable.getSelectedRow();

        Instruction instr = currentMethod.code.get(instrIndex);

        JFrame instrInfoFrame = new JFrame("Instruction info");
        Container contentPane = instrInfoFrame.getContentPane();
        contentPane.setLayout(new GridLayout(3, 1, 5, 5));

        Dimension fieldDimension = new Dimension(300, 25);

//        contentPane.add(new JLabel("Code", JLabel.RIGHT));
//        JTextField codeField = new JTextField();
//        codeField.setPreferredSize(fieldDimension);
//        contentPane.add(codeField);
//        codeField.setText(instr.toString());

        {
            JPanel inferredInfoPanel = new JPanel();
            inferredInfoPanel.setBorder(BorderFactory.createTitledBorder("Inferred static info"));
            inferredInfoPanel.setLayout(new GridLayout(3, 2, 5, 5));
            contentPane.add(inferredInfoPanel);

            inferredInfoPanel.add(new JLabel("Stack before", JLabel.RIGHT));
            JTextField stackBeforeField = new JTextField();
            stackBeforeField.setPreferredSize(fieldDimension);
            inferredInfoPanel.add(stackBeforeField);
            stackBeforeField.setText(staticInfos.get(currentMethod).getStackStateBefore(instrIndex));

            inferredInfoPanel.add(new JLabel("Stack after", JLabel.RIGHT));
            JTextField stackAfterField = new JTextField();
            stackAfterField.setPreferredSize(fieldDimension);
            inferredInfoPanel.add(stackAfterField);
            stackAfterField.setText(staticInfos.get(currentMethod).getStackStateAfter(instrIndex));

            inferredInfoPanel.add(new JLabel("Locals", JLabel.RIGHT));
            JTextField localsField = new JTextField();
            localsField.setPreferredSize(fieldDimension);
            inferredInfoPanel.add(localsField);
            localsField.setText(staticInfos.get(currentMethod).getLocalsBefore(instrIndex, true));

        }

        {
            JPanel loadedInfoPanel = new JPanel();
            loadedInfoPanel.setBorder(BorderFactory.createTitledBorder("Loaded static info"));
            loadedInfoPanel.setLayout(new GridLayout(2, 2, 5, 5));
            contentPane.add(loadedInfoPanel);

            loadedInfoPanel.add(new JLabel("Stack before", JLabel.RIGHT));
            JTextField stackBeforeField = new JTextField();
            stackBeforeField.setPreferredSize(fieldDimension);
            loadedInfoPanel.add(stackBeforeField);
            if (currentMethod.loadedStaticInfo != null) {
                stackBeforeField.setText(currentMethod.loadedStaticInfo.getStackStateBefore(instrIndex));
            }

            loadedInfoPanel.add(new JLabel("Locals", JLabel.RIGHT));
            JTextField localsField = new JTextField();
            localsField.setPreferredSize(fieldDimension);
            loadedInfoPanel.add(localsField);
            if (currentMethod.loadedStaticInfo != null) {
                localsField.setText(currentMethod.loadedStaticInfo.getLocalsBefore(instrIndex, false));
            }

        }


        instrInfoFrame.pack();
        instrInfoFrame.setVisible(true);
    }

    private void setFile(File file) throws Exception {

        Path path = FileSystems.getDefault().getPath(file.getAbsolutePath());
        Set<OpenOption> opts = new HashSet<OpenOption>();
        opts.add(StandardOpenOption.READ);
        FileChannel fileChannel = FileChannel.open(path, opts);

        ClassFileFormat classFormat = ClassFileFormat.readFrom(fileChannel);

        List<String> errors = classFormat.validate();

        ClassData classData = new ClassData(classFormat);

        classes.add(classData);
        cache.cache.put(classData.name, classData);

        classData.linkAll(cache);

        for (MethodData method : classData.methods) {
            if (!method.isAbstract() && !method.isNative()) {
                MethodStaticInfo staticInfo = MethodStaticInfo.infer(method);
                staticInfos.put(method, staticInfo);
            }
        }

        classMembersTreeModel.classAdded(classes.size() - 1);

//        methodsList.setModel(new AbstractListModel() {
//
//            @Override
//            public int getSize() {
//                return classData.methods.size();
//            }
//
//            @Override
//            public Object getElementAt(int index) {
//                return classData.methods.get(index).name;
//            }
//        });

    }

    public void methodSelected(MethodData methodData) {
        this.currentMethod = methodData;
        this.methodData.setText(methodData.toSignature());
        instructionsTableModel.setMethodData(methodData, staticInfos.get(methodData));
        instructionsTableModel.fireTableDataChanged();
    }

    private static class MethodTableModel extends AbstractTableModel {

        private MethodData methodData;
        private MethodStaticInfo staticInfo;

        public void setMethodData(MethodData methodData, MethodStaticInfo staticInfo) {
            this.methodData = methodData;
            this.staticInfo = staticInfo;
        }

        @Override
        public int getRowCount() {
            return methodData == null ? 0 : (methodData.code == null ? 0 : methodData.code.size());
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (methodData == null) return "";
            if (columnIndex == 1) {
                return methodData.code.get(rowIndex).toString();
            } else if (columnIndex == 0) {
                return String.valueOf(methodData.code.get(rowIndex).byteOffset);
            } else if (columnIndex == 2) {
//                StringBuilder fromLines = new StringBuilder();
//                for (MetaData.StateStaticInfo fromItm : methodData.code.get(rowIndex).meta.from)
//                    fromLines.append(fromItm.jumpedFromOffset).append(",");
//                return fromLines.toString();
                if (methodData.loadedStaticInfo != null) {
                    return methodData.loadedStaticInfo.getStackStateBefore(rowIndex).length() == 0 ? "" : "*";
                } else {
                    return "";
                }
            } else if (columnIndex == 3) {
                if (staticInfo != null) {
                    return staticInfo.getStackStateAfter(rowIndex);
                } else {
                    return "";
                }
            } else {
                return "";
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0: return "Line";
                case 1: return "Code";
                case 2: return "SI";
                case 3: return "Stack after";
                default: return "Column";
            }
        }
    }

    private static class LocalVarsTableModel extends AbstractTableModel {

        private LocalVariableTypes localVariableTypes;

        private LocalVarsTableModel() {

        }

        private void setLocalVariableTypes(LocalVariableTypes localVariableTypes) {
            this.localVariableTypes = localVariableTypes;
        }

        @Override
        public int getRowCount() {
            return localVariableTypes == null ? 0 : localVariableTypes.getSize();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return String.valueOf(rowIndex);
            } else if (columnIndex == 1) {
                Type localVarType = localVariableTypes.get(rowIndex);
                return localVarType == null ? "" : localVarType.toString();
            } else {
                return "";
            }
        }

        @Override
        public String getColumnName(int column) {
            switch (column) {
                case 0: return "Index";
                case 1: return "Type";
                default: return "Column";
            }
        }
    }

    private class ClasspathTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return classPathEntries.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (columnIndex == 0) {
                return classPathEntries.get(rowIndex).getAbsolutePath();
            } else if (columnIndex == 1) {
                return "...";
            } else {
                return "";
            }
        }
    }

    private class ClassMembersTreeModel implements TreeModel {

        private final EventListenerList eventListenerList = new EventListenerList();

        public static final String ROOT = "Root";

        @Override
        public Object getRoot() {
            return ROOT;
        }

        @Override
        public Object getChild(Object parent, int index) {
            if (parent.equals(ROOT)) {
                return classes.get(index);
            } else if (parent instanceof ClassData) {
                ClassData classData1 = (ClassData)parent;
                return classData1.methods.get(index);
            } else {
                return null;
            }
        }

        @Override
        public int getChildCount(Object parent) {
            if (parent.equals(ROOT)) {
                return classes.size();
            } else if (parent instanceof ClassData) {
                ClassData classData1 = (ClassData)parent;
                return classData1.methods.size();
            } else {
                return 0;
            }
        }

        @Override
        public boolean isLeaf(Object node) {
            if (node.equals(ROOT)) {
                return false;
            } else if (node instanceof ClassData) {
                return false;
            } else if (node instanceof MethodData) {
                return true;
            } else {
                return false;
            }

        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
            // not implemented, since there is no in-place editing in tree
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            if (parent.equals(ROOT)) {
                return classes.indexOf(child);
            } else if (parent instanceof ClassData) {
                ClassData classData1 = (ClassData)parent;
                return classData1.methods.indexOf(child);
            } else {
                return -1;
            }
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            eventListenerList.add(TreeModelListener.class, l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            eventListenerList.remove(TreeModelListener.class, l);
        }

        public void classAdded(int classIndex) {
            TreeModelListener[] listeners = eventListenerList.getListeners(TreeModelListener.class);
            TreeModelEvent event = new TreeModelEvent(this, new Object[]{ROOT}, new int[]{classIndex}, new Object[]{classes.get(classIndex)});
            for (TreeModelListener listener : listeners) {
                listener.treeNodesInserted(event);
            }
        }
    }

    private class DependentClassesTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public int getColumnCount() {
            return 0;  //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            return null;  //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    public String getMethodCode(MethodData methodData) {
        return "";
    }

    public static void main(String[] args) {
        try {
            // UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            // UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            ClassView1 classView1 = new ClassView1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
