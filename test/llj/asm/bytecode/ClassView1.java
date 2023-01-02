package llj.asm.bytecode;

import llj.packager.jclass.ClassFileFormat;
import llj.packager.jclass.FormatException;
import llj.util.ReadException;
import llj.util.ref.MapResolver;

import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.EventListenerList;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.Highlighter;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Panel;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.InvocationEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.swing.event.ListDataEvent.CONTENTS_CHANGED;

public class ClassView1 {

    private final JFrame window;

    private final List<ClassData> classes = new ArrayList<ClassData>();
    MapResolver<ClassData, String> cache = new MapResolver<ClassData, String>();
    private MethodData currentMethod;
    private final IdentityHashMap<MethodData, MethodStaticInfo> staticInfos = new IdentityHashMap<MethodData, MethodStaticInfo>();

    private final JTree classMembersTree;
    private final ClassMembersTreeModel classMembersTreeModel;

    private final JLabel methodData = new JLabel("<no method selected>");

//    private final JTable instructionsTable;
//    private final MethodTableModel instructionsTableModel;
//
//    private final JTable localVarsTable;
//    private final LocalVarsTableModel localVarsTableModel;
    // private final DependentClassesTableModel dependentClassesTableModel;

    private final ClasspathListModel classpathsListModel;
    private final JList classpathsList;
    private final List<File> classPathEntries = new ArrayList<File>();

    private final JPopupMenu codePopupMenu = new JPopupMenu(), classpathPopuMenu = new JPopupMenu();
    
    private File lastDir;
    // private final JTextPane codeText;
    private final JList codeLines;
    private final MethodListModel methodListModel;
    private final JList stackAfterList;
    private final JList stackBeforeList;


    public ClassView1() {

        window = new JFrame("ClassView");

        JMenuBar menuBar = new JMenuBar();

        {
            JMenu fileMenu = new JMenu("File");
            menuBar.add(fileMenu);

            {
                JMenuItem openFileItem = new JMenuItem(new AbstractAction("Open") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        JFileChooser classChooser = new JFileChooser(lastDir);
                        int result = classChooser.showOpenDialog(window);
                        if (result == JFileChooser.APPROVE_OPTION) {
                            File selected = classChooser.getSelectedFile();
                            try {
                                if (selected.exists()) {
                                    setFile(selected);
                                }
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

                JMenuItem addClasspathButton = new JMenuItem(new AbstractAction("Add dir") {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            JFileChooser dirChooser = new JFileChooser(lastDir);
                            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                            int status = dirChooser.showDialog(window, "Choose");
                            if (status == JFileChooser.APPROVE_OPTION) {
                                File selectedFile = dirChooser.getSelectedFile();
                                if (!classPathEntries.contains(selectedFile)) {
                                    classPathEntries.add(selectedFile);
                                }
                                classpathsListModel.changed();
                            }
                        }
                    });
                
                fileMenu.add(addClasspathButton);
            }
            
            {
                JMenuItem clearItem = new JMenuItem(new AbstractAction("Clear") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // TODO
                    }
                });
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

            JSplitPane split2 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            contentPane.add(split2, BorderLayout.CENTER);

            {

                JTabbedPane navigationTabs = new JTabbedPane(JTabbedPane.BOTTOM);
                navigationTabs.setPreferredSize(new Dimension(200, 300));

                split2.setLeftComponent(navigationTabs);

                {
                    classMembersTreeModel = new ClassMembersTreeModel();
                    classMembersTree = new JTree(classMembersTreeModel);

                    // classMembersTree.setRootVisible(false);
                    classMembersTree.setShowsRootHandles(true);

                    navigationTabs.addTab("Classes", new JScrollPane(classMembersTree));
                    

                    DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) classMembersTree.getCellRenderer();
                    renderer.setLeafIcon(null);
                    renderer.setClosedIcon(null);
                    renderer.setOpenIcon(null);

                    classMembersTree.addTreeSelectionListener(new TreeSelectionListener() {
                        @Override
                        public void valueChanged(TreeSelectionEvent e) {
                            TreePath path = e.getPath();
                            if (path.getPathCount() == 3) {
                                ClassData classData = (ClassData) path.getPathComponent(1);
                                MethodData methodData = getMethodBySignature(classData, (String) path.getPathComponent(2));
                                methodSelected(methodData);
                            } else {
                                // ClassData classData = path.getPathComponent(1);
                            }
                        }
                    });
                    classMembersTree.expandPath(new TreePath(classMembersTreeModel.getRoot()));

                    // DON'T use preferred size on JTree, or scrollbars don't appear if JTree doesn't fit into JScrollPane
                    // classMembersTree.setPreferredSize(new Dimension(150, 300));
                }

                {
                    classpathsListModel = new ClasspathListModel();
                    classpathsList = new JList(classpathsListModel);

                    navigationTabs.addTab("Locations", new JScrollPane(classpathsList));

                    // DON'T use preferred size on JTree, or scrollbars don't appear if JTree doesn't fit into JScrollPane
                    // classMembersTree.setPreferredSize(new Dimension(150, 300));
                }
                
                
            }


            codePopupMenu.add(new AbstractAction("Info") {
                @Override
                public void actionPerformed(ActionEvent e) {
                     showSelectedInstructionInfo();
                }
            });

            {

                AbstractAction loadClassFromInstructionAction = new AbstractAction("Load class") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int instrIndex = codeLines.getSelectedIndex();
                        Instruction instr = currentMethod.code.get(instrIndex);
                        String className = null;
                        if (instr instanceof InvokeInstruction) {
                            className = ((InvokeInstruction) instr).methodRef.classRef.getId();
                        } else if (instr instanceof FieldRefInstruction) {
                            className = ((FieldRefInstruction) instr).fieldRef.classRef.getId();
                        }
                        if (className != null) {
                            Path fullPath = tryFindClass(className);
                            try {
                                addClass(fullPath);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }
                };
                codePopupMenu.add(loadClassFromInstructionAction);
                loadClassFromInstructionAction.setEnabled(false);

                codePopupMenu.addPopupMenuListener(new PopupMenuListener() {

                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        int instrIndex = codeLines.getSelectedIndex();
                        Instruction instr = currentMethod.code.get(instrIndex);
                        if (!instr.isLinked()) {
                            loadClassFromInstructionAction.setEnabled(true);
                        } else {
                            loadClassFromInstructionAction.setEnabled(false);
                        }
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                });

            }

            {

                AbstractAction goToInstructionReferenceAction = new AbstractAction("Go to") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        int instrIndex = codeLines.getSelectedIndex();
                        Instruction instr = currentMethod.code.get(instrIndex);
                        String className = null;
                        if (instr instanceof InvokeInstruction) {
                            className = ((InvokeInstruction) instr).methodRef.classRef.getId();
                            // TODO maybe go to method ?
                        } else if (instr instanceof FieldRefInstruction) {
                            className = ((FieldRefInstruction) instr).fieldRef.classRef.getId();
                            // TODO go to class ? Or field ?
                        }
                        
                    }
                };
                codePopupMenu.add(goToInstructionReferenceAction);
                goToInstructionReferenceAction.setEnabled(false);

                codePopupMenu.addPopupMenuListener(new PopupMenuListener() {

                    @Override
                    public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                        int instrIndex = codeLines.getSelectedIndex();
                        Instruction instr = currentMethod.code.get(instrIndex);
                        if ((instr instanceof InvokeInstruction) || (instr instanceof FieldRefInstruction)) {
                            goToInstructionReferenceAction.setEnabled(true);
                        } else {
                            goToInstructionReferenceAction.setEnabled(false);
                        }
                    }

                    @Override
                    public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {

                    }

                    @Override
                    public void popupMenuCanceled(PopupMenuEvent e) {
                    }
                });

            }
            

            {

                JSplitPane split3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
                split2.setRightComponent(split3);
                
                JPanel comp = new JPanel();
                comp.setLayout(new BorderLayout());
                split3.setTopComponent(comp);

                comp.add(methodData, BorderLayout.NORTH);

//                JTabbedPane methodContentTabs = new JTabbedPane(JTabbedPane.BOTTOM);
//                methodContentTabs.setPreferredSize(new Dimension(700, 300));


                {
                    methodListModel = new MethodListModel();
                    codeLines = new JList<>(methodListModel);
                    codeLines.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
                    
                    codeLines.setComponentPopupMenu(codePopupMenu);

                    JScrollPane codeScrollPane = new JScrollPane(codeLines, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//                    methodContentTabs.addTab("CodeLines", codeScrollPane);
                    comp.add(codeScrollPane, BorderLayout.CENTER);
                }
                
                JTabbedPane infoTabs = new JTabbedPane(JTabbedPane.BOTTOM);
                split3.setBottomComponent(infoTabs);

                infoTabs.addTab("Class info", new JPanel());

                infoTabs.addTab("Method info", new JPanel());

                infoTabs.addTab("Instruction info", new JPanel());

                infoTabs.addTab("Local variables", new JPanel());

                JPanel stackEffect = new JPanel(new GridBagLayout());
                infoTabs.addTab("Stack effect", stackEffect);

                {

                    stackEffect.add(new JLabel("Stack before"), new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(10,10,0,10), 0, 0));
                    
                    DefaultListModel<String> stackBeforeListModel = new DefaultListModel<>();
                    stackBeforeList = new JList(stackBeforeListModel);
                    
                    JScrollPane scrollPane = new JScrollPane(stackBeforeList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                    scrollPane.setPreferredSize(new Dimension(200, 100));
                    scrollPane.setMinimumSize(new Dimension(200, 100));
                    
                    codeLines.addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            refreshStackBefore(stackBeforeListModel);
                        }
                    });
                    // we don't set data listener for model of codeLines list, since refreshStackBefore() uses selection index of codeLines, which may become beyond size of a list
                    // instead, when we change model of codeLines, we also request selected index to become 0, which refreshes stackBefore
                    stackEffect.add(scrollPane, new GridBagConstraints(0, 1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(5,10,10,10), 0, 0));
                }
                
                {

                    stackEffect.add(new JLabel("Stack after"), new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.VERTICAL, new Insets(10,10,0,10), 0, 0));
                    
                    DefaultListModel<String> stackAfterListModel = new DefaultListModel<>();
                    stackAfterList = new JList(stackAfterListModel);

                    JScrollPane scrollPane = new JScrollPane(stackAfterList, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                    scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
                    scrollPane.setPreferredSize(new Dimension(200, 100));
                    scrollPane.setMinimumSize(new Dimension(200, 100));

                    codeLines.addListSelectionListener(new ListSelectionListener() {
                        @Override
                        public void valueChanged(ListSelectionEvent e) {
                            refreshStackAfter(stackAfterListModel);
                        }
                    });
                    // we don't set data listener for model of codeLines list, since refreshStackAfter() uses selection index of codeLines, which may become beyond size of a list
                    // instead, when we change model of codeLines, we also request selected index to become 0, which refreshes stackAfter
                    
                    
                    stackEffect.add(scrollPane, new GridBagConstraints(1, 1, 1, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(5,10,10,10), 0, 0));
                }

                stackEffect.add(new JPanel(), new GridBagConstraints(2, 0, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0,0,0,0), 0, 0));
                stackEffect.add(new JPanel(), new GridBagConstraints(2, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.VERTICAL, new Insets(0,0,0,0), 0, 0));
                

//                {
//                    instructionsTableModel = new MethodTableModel();
//                    instructionsTable = new JTable(instructionsTableModel);
//                    instructionsTable.setShowGrid(false);
//                    instructionsTable.setFillsViewportHeight(true);
//                    JScrollPane scrollPane = new JScrollPane(instructionsTable);
//                    methodContentTabs.addTab("Code", scrollPane);
//
//                    instructionsTable.addMouseListener(new MouseAdapter() {
//                        @Override
//                        public void mouseClicked(MouseEvent e) {
//                            if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2) {
//                                showSelectedInstructionInfo();
//                            }
//                        }
//                    });
//                    
//                    double[] widths = new double[] {0.05, 0.7, 0.05, 0.29};
//                    int totalColumnWidth = instructionsTable.getColumnModel().getTotalColumnWidth();
//                    for (int i = 0; i < instructionsTable.getColumnCount(); i++) {
//                        instructionsTable.getColumnModel().getColumn(i).setPreferredWidth((int)(totalColumnWidth * widths[i]));
//                    }
//
//                }

//                {
//                    localVarsTableModel = new LocalVarsTableModel();
//                    localVarsTable = new JTable(localVarsTableModel);
//                    localVarsTable.setShowGrid(true);
//                    localVarsTable.setFillsViewportHeight(true);
//                    JScrollPane scrollPane = new JScrollPane(localVarsTable);
//                    methodContentTabs.addTab("Variables", scrollPane);
//                }


//                comp.add(methodContentTabs, BorderLayout.CENTER);
            }


        }

        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        window.pack();

        window.setVisible(true);

    }

    public void refreshStackBefore(DefaultListModel<String> stackBeforeListModel) {
        stackBeforeListModel.clear();
        List<String> stackBefore;
        if (codeLines.getSelectedIndex() == 0) {
            stackBefore = methodListModel.staticInfo.getStackStateBefore(0);
        } else {
            stackBefore = methodListModel.staticInfo.getStackStateAfter(codeLines.getSelectedIndex() - 1);
        }
        if (stackBefore.size() > 1) {  // check size to avoid errors with reverse() on lists created via Collections.singletonList()
            Collections.reverse(stackBefore);  // items in list represent stack top -> bottom
        }
        for (String s : stackBefore) {
            stackBeforeListModel.addElement(s);
        }
    }

    public void refreshStackAfter(DefaultListModel<String> stackAfterListModel) {
        stackAfterListModel.clear();
        List<String> stackAfter = methodListModel.staticInfo.getStackStateAfter(codeLines.getSelectedIndex());
        if (stackAfter.size() > 1) {  // check size to avoid errors with reverse() on lists created via Collections.singletonList()
            Collections.reverse(stackAfter);  // items in list represent stack top -> bottom
        }
        for (String s : stackAfter) {
            stackAfterListModel.addElement(s);
        }
    }
    
    public Path tryFindClass(String className) {
        for (File classPathEntry : classPathEntries) {
            if (classPathEntry.isDirectory()) {
                Path fullPath = classPathEntry.toPath().resolve(className + ".class");
                if (fullPath.toFile().exists()) {
                    return fullPath;
                }
            }
        }
        return null;
    }

    private void showSelectedInstructionInfo() {
        
        int instrIndex = codeLines.getSelectedIndex();

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
            List<String> stackStateBefore = staticInfos.get(currentMethod).getStackStateBefore(instrIndex);
            if (stackStateBefore.size() > 1) {  // check size to avoid errors with reverse() on lists created via Collections.singletonList()
                Collections.reverse(stackStateBefore);  // stack top -> bottom is printed left -> right
            }
            stackBeforeField.setText(stackStateBefore.toString());

            inferredInfoPanel.add(new JLabel("Stack after", JLabel.RIGHT));
            JTextField stackAfterField = new JTextField();
            stackAfterField.setPreferredSize(fieldDimension);
            inferredInfoPanel.add(stackAfterField);
            List<String> stackStateAfter = staticInfos.get(currentMethod).getStackStateAfter(instrIndex);
            if (stackStateAfter.size() > 1) {  // check size to avoid errors with reverse() on lists created via Collections.singletonList()
                Collections.reverse(stackStateAfter); // stack top -> bottom is printed left -> right
            }
            stackAfterField.setText(stackStateAfter.toString());

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
                List<String> stackStateBefore = currentMethod.loadedStaticInfo.getStackStateBefore(instrIndex);
                if (stackStateBefore.size() > 1) {  // check size to avoid errors with reverse() on lists created via Collections.singletonList()
                    Collections.reverse(stackStateBefore);
                }
                stackBeforeField.setText(stackStateBefore.toString());
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
        lastDir = path.getParent().toFile();
        
        if (!classPathEntries.contains(lastDir)) {
            classPathEntries.add(lastDir);
        }
        classpathsListModel.changed();

        addClass(path);

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

    private void addClass(Path path) throws IOException, ReadException, FormatException, LinkException {
        Set<OpenOption> opts = new HashSet<OpenOption>();
        opts.add(StandardOpenOption.READ);
        FileChannel fileChannel = FileChannel.open(path, opts);

        ClassFileFormat classFormat = ClassFileFormat.readFrom(fileChannel);

        List<String> errors = classFormat.validate();

        ClassData classData = new ClassData(classFormat);

        classes.add(classData);
        
        cache.cache.put(classData.name, classData);
        classData.linkAll(cache, false);
        for (ClassData prevClass : classes) {
            prevClass.linkAll(cache, false);
        }

        for (MethodData method : classData.methods) {
            if (!method.isAbstract() && !method.isNative()) {
                MethodStaticInfo staticInfo = MethodStaticInfo.infer(method);
                staticInfos.put(method, staticInfo);
            }
        }

        classMembersTreeModel.classAdded(classes.size() - 1);
    }

    public void methodSelected(MethodData methodData) {
        this.currentMethod = methodData;
        this.methodData.setText(methodData.toSignature());
        // instructionsTableModel.setMethodData(methodData, staticInfos.get(methodData));
        // instructionsTableModel.fireTableDataChanged();
        // showMethodText(codeText, methodData, staticInfos.get(methodData));
        
        methodListModel.setMethodData(methodData, staticInfos.get(methodData));
        // there are no listeners which refresh stackBefore and stackAfter on change of methodListModel, 
        // since stackBefore and stackAfter uses selection index of codeLines, which may become beyond size of a list
        // instead, here when we change model of codeLines, we also request selected index to become 0, which refreshes stackBefore and stackAfter
        codeLines.setSelectedIndex(0);
        

    }

    public void showMethodText(JTextPane codeText, MethodData methodData, MethodStaticInfo staticInfo) {
        if (methodData == null) return;
        StringBuilder istr = new StringBuilder();
        for (int i = 0; i < methodData.code.size(); i++) {
            Instruction instr = methodData.code.get(i);
            istr.append(String.format("%6d  ", instr.byteOffset));
            istr.append(instr.toString());
            istr.append("\n");
        }
        codeText.setText(istr.toString()); // this method is more convenient than using codeText.getDocument().insertString()
    }

//    private static class MethodTableModel extends AbstractTableModel {
//
//        private MethodData methodData;
//        private MethodStaticInfo staticInfo;
//
//        public void setMethodData(MethodData methodData, MethodStaticInfo staticInfo) {
//            this.methodData = methodData;
//            this.staticInfo = staticInfo;
//        }
//
//        @Override
//        public int getRowCount() {
//            return methodData == null ? 0 : (methodData.code == null ? 0 : methodData.code.size());
//        }
//
//        @Override
//        public int getColumnCount() {
//            return 4;
//        }
//
//        @Override
//        public Object getValueAt(int rowIndex, int columnIndex) {
//            if (methodData == null) return "";
//            if (columnIndex == 1) {
//                return methodData.code.get(rowIndex).toString();
//            } else if (columnIndex == 0) {
//                return String.valueOf(methodData.code.get(rowIndex).byteOffset);
//            } else if (columnIndex == 2) {
////                StringBuilder fromLines = new StringBuilder();
////                for (MetaData.StateStaticInfo fromItm : methodData.code.get(rowIndex).meta.from)
////                    fromLines.append(fromItm.jumpedFromOffset).append(",");
////                return fromLines.toString();
//                if (methodData.loadedStaticInfo != null) {
//                    return methodData.loadedStaticInfo.getStackStateBefore(rowIndex).length() == 0 ? "" : "*";
//                } else {
//                    return "";
//                }
//            } else if (columnIndex == 3) {
//                if (staticInfo != null) {
//                    return staticInfo.getStackStateAfter(rowIndex);
//                } else {
//                    return "";
//                }
//            } else {
//                return "";
//            }
//        }
//
//        @Override
//        public String getColumnName(int column) {
//            switch (column) {
//                case 0: return "Line";
//                case 1: return "Code";
//                case 2: return "SI";
//                case 3: return "Stack after";
//                default: return "Column";
//            }
//        }
//    }

    private static class MethodListModel implements ListModel<String> {
        
        private MethodData methodData;
        private MethodStaticInfo staticInfo;
        private final List<ListDataListener> listeners = new ArrayList<>(); 

        @Override
        public int getSize() {
            return getCodeSize(methodData);
        }

        @Override
        public String getElementAt(int index) {
            if (methodData == null) return "";
            if (methodData.code == null) return ""; // for abstract and native methods

            StringBuilder istr = new StringBuilder();
            Instruction instr = methodData.code.get(index);
            istr.append(String.format("%6d  ", instr.byteOffset));
            istr.append(instr.toString());
            return istr.toString();
            
        }

        @Override
        public void addListDataListener(ListDataListener l) {
            listeners.add(l);
        }

        @Override
        public void removeListDataListener(ListDataListener l) {
            listeners.remove(l);
        }

        public void setMethodData(MethodData methodData, MethodStaticInfo staticInfo) {
            this.methodData = methodData;
            this.staticInfo = staticInfo;
            for (ListDataListener listener : listeners) {
                listener.contentsChanged(new ListDataEvent(this, CONTENTS_CHANGED, 0, getCodeSize(methodData)));
            }
        }

        public static int getCodeSize(MethodData methodData) {
            return methodData == null ? 0 : methodData.code == null ? 0 : methodData.code.size();
        }

    }
    

//    private static class LocalVarsTableModel extends AbstractTableModel {
//
//        private LocalVariableTypes localVariableTypes;
//
//        private LocalVarsTableModel() {
//
//        }
//
//        private void setLocalVariableTypes(LocalVariableTypes localVariableTypes) {
//            this.localVariableTypes = localVariableTypes;
//        }
//
//        @Override
//        public int getRowCount() {
//            return localVariableTypes == null ? 0 : localVariableTypes.getSize();
//        }
//
//        @Override
//        public int getColumnCount() {
//            return 2;
//        }
//
//        @Override
//        public Object getValueAt(int rowIndex, int columnIndex) {
//            if (columnIndex == 0) {
//                return String.valueOf(rowIndex);
//            } else if (columnIndex == 1) {
//                Type localVarType = localVariableTypes.get(rowIndex);
//                return localVarType == null ? "" : localVarType.toString();
//            } else {
//                return "";
//            }
//        }
//
//        @Override
//        public String getColumnName(int column) {
//            switch (column) {
//                case 0: return "Index";
//                case 1: return "Type";
//                default: return "Column";
//            }
//        }
//    }
//
    private class ClasspathListModel extends AbstractListModel {

        @Override
        public int getSize() {
            return classPathEntries.size();
        }

        @Override
        public Object getElementAt(int index) {
            return classPathEntries.get(index).getAbsolutePath();
        }
        
        public void changed() {
            fireContentsChanged(this, 0, getSize());
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
                ClassData classData = (ClassData)parent;
                return classData.methods.get(index).toSignature();
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
            } else if (node instanceof String) {  // was: instanceof MethodData
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
                return getMethodIndexBySignature((ClassData)parent, (String)child);
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

    public static int getMethodIndexBySignature(ClassData classData, String signature) {
        for (int i = 0; i < classData.methods.size(); i++) {
            MethodData method = classData.methods.get(i);
            if (method.toSignature().equals(signature)) {
                return i;
            }
        }
        return -1;
    }

    public static MethodData getMethodBySignature(ClassData classData, String signature) {
        for (int i = 0; i < classData.methods.size(); i++) {
            MethodData method = classData.methods.get(i);
            if (method.toSignature().equals(signature)) {
                return method;
            }
        }
        return null;
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
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            ClassView1 classView1 = new ClassView1();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
//    private static class LineHighlighter implements Highlighter.HighlightPainter, CaretListener, MouseListener {
//        private final JTextComponent component;
//        private Color color;
//        private Rectangle lastHighlight;
//        
//        public LineHighlighter(JTextComponent component)         {
//            this(component, null);
//            setColor(deriveLighterColor(component.getSelectionColor()));
//        }
//
//        public LineHighlighter(JTextComponent component, Color color) {
//            this.component = component;
//            setColor( color );
//
//            component.addCaretListener( this );
////            component.addMouseListener( this );
////            component.addMouseMotionListener( this );
//
//            //  Turn highlighting on by adding a dummy highlight
//            try {
//                component.getHighlighter().addHighlight(0, 0, this);
//            } catch(BadLocationException ble) {}
//        }
//
//        public void setColor(Color color) {
//            this.color = color;
//        }
//
//        public static Color deriveLighterColor(Color color) {
//            double v = 3.5;
//            int red   = Math.min(255, (int)(color.getRed() * v));
//            int green = Math.min(255, (int)(color.getGreen() * v));
//            int blue  = Math.min(255, (int)(color.getBlue() * v));
//            return new Color(red, green, blue);
//        }
//
//        public void paint(Graphics g, int p0, int p1, Shape bounds, JTextComponent c)         {
//            try {
//                Rectangle r = c.modelToView(c.getCaretPosition());
//                g.setColor( color );
//                g.fillRect(0, r.y, c.getWidth(), r.height);
//
//                if (lastHighlight == null) {
//                    lastHighlight = r;
//                }
//            } catch(BadLocationException ble) {System.out.println(ble);}
//        }
//        
//        private void resetHighlight() {
//            //  Use invokeLater to make sure updates to the Document are completed,
//            //  otherwise Undo processing causes the modelToView method to loop.
//
//            SwingUtilities.invokeLater(new Runnable() {
//                public void run() {
//                    try {
//                        int offset =  component.getCaretPosition();
//                        Rectangle currentHighlight = component.modelToView(offset);
//                        
//                        //  Remove the highlighting from the previously highlighted line
//
//                        if (lastHighlight.y != currentHighlight.y) {
//                            component.repaint(0, lastHighlight.y, component.getWidth(), lastHighlight.height);
//                            lastHighlight = currentHighlight;
//                            component.repaint(0, currentHighlight.y, component.getWidth(), currentHighlight.height);
//                        }
//                    } catch(BadLocationException ble) {}
//                }
//            });
//        }
//
//        public void caretUpdate(CaretEvent e) {
//            resetHighlight();
//        }
//
//        public void mousePressed(MouseEvent e) { resetHighlight(); }
//        
//        public void mouseClicked(MouseEvent e) {}
//        public void mouseEntered(MouseEvent e) {}
//        public void mouseExited(MouseEvent e) {}
//        public void mouseReleased(MouseEvent e) {}
//        
//    }

}
