package llj.packager.winpe;

import llj.packager.DisplayFormat;
import llj.packager.IntrospectableFormat;
import llj.packager.coff.COFFHeader;
import llj.packager.coff.NameOrStringTablePointer;
import llj.packager.coff.Section;
import llj.packager.coff.SectionHeader;
import llj.packager.dosexe.DOSExeFormat;
import llj.packager.dosexe.DOSExeFormatException;
import llj.packager.dosexe.DOSHeader;
import llj.packager.objcoff.OBJCOFFFormat;
import llj.util.swing.GridBagByRowAdder;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class VisualPE {


    public final JFrame frame;
    public final JTree tree;
    public File file;
    private final DefaultTreeModel treeModel;
    private final JScrollPane contentScrollPane;
    
    private Properties displayProps = new Properties();

    private GridBagConstraints col1, col2, col4;

    public VisualPE() {
        
        try {
            displayProps.load(new FileInputStream("resources\\pe-display-default.txt"));
        } catch (Exception e) {
            System.err.println("Was unable to load display defaults");
        }

        frame = new JFrame("VisualPE");

        treeModel = new DefaultTreeModel(new DefaultMutableTreeNode("No file selected"));
        tree = new JTree(treeModel);
        // tree.setRootVisible(false);
        tree.setShowsRootHandles(true);

        DefaultTreeCellRenderer renderer = (DefaultTreeCellRenderer) tree.getCellRenderer();
        renderer.setLeafIcon(null);
        renderer.setClosedIcon(null);
        renderer.setOpenIcon(null);

        // tree.setEnabled(false);

        contentScrollPane = new JScrollPane();
        contentScrollPane.getViewport().setBackground(UIManager.getColor("List.background"));
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(tree), contentScrollPane);
        splitPane.setDividerLocation(180);
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

        {
            JMenuBar menuBar = new JMenuBar();

            JMenu menuFile = new JMenu("File");
            menuFile.add(new AbstractAction("Open") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser classChooser = new JFileChooser();
                    int result = classChooser.showOpenDialog(frame);
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
            menuFile.addSeparator();
            menuFile.add(new AbstractAction("Exit") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    frame.setVisible(false);
                }
            });

            menuBar.add(menuFile);

            frame.setJMenuBar(menuBar);
        }

        frame.setSize(550, 500);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        frame.setVisible(true);

        Insets defaultInsets = new Insets(2, 5, 2, 5);

        col1 = new GridBagConstraints();
        col1.fill = GridBagConstraints.NONE;
        col1.anchor = GridBagConstraints.EAST;
        col1.insets = defaultInsets;
        col1.weightx = 0;
        col1.weighty = 0;

        col2 = new GridBagConstraints();
        col2.fill = GridBagConstraints.HORIZONTAL;
        col2.anchor = GridBagConstraints.NORTHWEST;
        col2.insets = defaultInsets;
        col2.weightx = 0;
        col2.weighty = 0;

//                            GridBagConstraints col3 = new GridBagConstraints();
//                            col3.gridx = 2;
//                            col3.fill = GridBagConstraints.NONE;
//                            col3.anchor = GridBagConstraints.WEST;
//                            col3.insets = new Insets(2, 5, 2, 5);
//                            col3.weightx = 0;
//                            col3.weighty = 0;

        col4 = new GridBagConstraints();
        col4.gridx = 3;
        col4.fill = GridBagConstraints.HORIZONTAL;
        col4.anchor = GridBagConstraints.NORTHWEST;
        col4.insets = defaultInsets;
        col4.weightx = 1;
        col4.weighty = 0;
        

    }

    public void setFile(File file) {
        this.file = file;

        try {
            Set<OpenOption> opts = new HashSet<OpenOption>();
            opts.add(StandardOpenOption.READ);
            FileChannel fileChannel = FileChannel.open(file.toPath(), opts);

            for (TreeSelectionListener listener : tree.getTreeSelectionListeners()) {
                tree.removeTreeSelectionListener(listener);
            }
            
            Map<Object, DisplayFormat> displayFormatMap = new HashMap<>();

            if (file.toPath().getFileName().toString().endsWith(".exe")) {

                DOSExeFormat dosExeFormat = new DOSExeFormat();
                boolean formatOk = false;
                try {
                    dosExeFormat.readFrom(fileChannel);
                    formatOk = true;
                } catch (DOSExeFormatException e) {
                    JOptionPane.showMessageDialog(frame, "Was unable to read selected file", "File read error", JOptionPane.ERROR_MESSAGE);
                }
                if (!formatOk) {
                    return;
                }

                PEFormat peFormat;
                if (dosExeFormat.header.getHeaderExtensionsSize() > 0) {

                    fileChannel.position(0);

                    PEFormat pe = new PEFormat();
                    formatOk = false;
                    try {
                        pe.readFrom(fileChannel);
                        formatOk = true;
                    } catch (PEFormatException e) {
                        if (pe.peSignatureInPlace) {
                            JOptionPane.showMessageDialog(frame, "Was unable to read selected file", "File read error", JOptionPane.ERROR_MESSAGE);
                            formatOk = false;
                        } else {
                            // no PE signature, so it's not a PE format;
                            pe = null;
                            formatOk = true;
                        }
                    }
                    if (!formatOk) {
                        return;
                    }
                    peFormat = pe;
                } else {
                    peFormat = null;
                }

                removeAllRenderers();
                createAnyFormatRenderer(file, displayFormatMap, "Windows PE");
                createPEFormatRenderer(displayFormatMap, peFormat);

            } else if (file.toPath().getFileName().toString().endsWith(".obj")) {

                OBJCOFFFormat objFormat = new OBJCOFFFormat();
                try {
                    objFormat.readFrom(fileChannel);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                    return;
                }

                System.out.println("File: '" + file + "'; Size: " + fileChannel.size());


                {
                    System.out.println("Machine is: " + COFFHeader.Machine.valueOf(objFormat.coffHeader.machine));

                    List<COFFHeader.CharacteristicsField> fields = COFFHeader.CharacteristicsField.getAllSetInValue(objFormat.coffHeader.characteristics);
                    System.out.println("Characteristics are: " + fields);
                }

                {
                    long x = objFormat.getSectionHeadersOffset();
                    int n = objFormat.coffHeader.numberOfSections;
                    long y = objFormat.getSectionHeadersSize();
                    System.out.println("COFF Section headers start at: " + x + "; COFF section headers total size: " + y + "; COFF Section headers end at: " + (x + y));
                }

                List<Section> sections = objFormat.sections;
                System.out.println("Number of sections:" + sections.size());
                for (Section section : sections) {
                    SectionHeader sectionHeader = section.sectionHeader;
                    List<SectionHeader.CharacteristicsField> fields = SectionHeader.CharacteristicsField.getAllSetInValue(sectionHeader.characteristics);

                    System.out.println("Section name: \"" + new String(sectionHeader.name.name) + "\"; raw data at: " + sectionHeader.pointerToRawData + "; raw data size: " + sectionHeader.sizeOfRawData + "; raw data ends at: " + (sectionHeader.pointerToRawData + sectionHeader.sizeOfRawData));
                    System.out.println("  "  + "number of relocations: " + sectionHeader.numberOfRelocations);
                    System.out.println("  "  + "characteristics are: " + fields);
                }                
                
            }

            tree.setEnabled(true);
            tree.expandPath(new TreePath(treeModel.getRoot()));

            tree.repaint();


        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Was unable to read selected file", "File read error", JOptionPane.ERROR_MESSAGE);
        }

    }

    public void removeAllRenderers() {
        for (TreeSelectionListener selectionListener : tree.getTreeSelectionListeners()) {
            tree.removeTreeSelectionListener(selectionListener);
        }
        ((DefaultMutableTreeNode)treeModel.getRoot()).removeAllChildren();
        treeModel.reload();

        {
            JPanel emptyPanel = new JPanel();

            emptyPanel.setLayout(new BorderLayout());

            emptyPanel.setBackground(UIManager.getColor("List.background"));
            emptyPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            contentScrollPane.setViewportView(emptyPanel);
        }
        
    }

    public void createAnyFormatRenderer(final File file, final Map<Object, DisplayFormat> displayFormatMap, String fileFormatDescription) {

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        root.setUserObject(file.getName());

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getNewLeadSelectionPath();
                if (path != null && path.getLastPathComponent() == root) {

                    JPanel fileInfoPanel = new JPanel();

                    fileInfoPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder fileInfoAdder = new GridBagByRowAdder(col1, col2, col4);

                    fileInfoAdder.addRow(fileInfoPanel, new JLabel("File total size: "), new JLabel(String.valueOf(file.length())), makeFiller());
                    fileInfoAdder.addRow(fileInfoPanel, new JLabel("Recognized file format: "), new JLabel(String.valueOf(fileFormatDescription)), makeFiller());

                    fileInfoAdder.addBottomFillerTo(fileInfoPanel);
                    fileInfoPanel.setBackground(UIManager.getColor("List.background"));

                    fileInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(fileInfoPanel);


                }
            }
        });

    }

    public void createPEFormatRenderer(final Map<Object, DisplayFormat> displayFormatMap, final PEFormat peFormat) {

        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        
        DefaultMutableTreeNode dosExeNode = new DefaultMutableTreeNode("DOS EXE");
        root.add(dosExeNode);

        DefaultMutableTreeNode dosHeaderNode = new DefaultMutableTreeNode("DOS EXE header");
        dosExeNode.add(dosHeaderNode);

        DefaultMutableTreeNode dosPayloadNode = new DefaultMutableTreeNode("DOS EXE data");
        dosExeNode.add(dosPayloadNode);

        DOSHeader<?> dosHeader = peFormat.dosHeader;

        DefaultMutableTreeNode peNode;

        DefaultMutableTreeNode peHeadersNode;

        DefaultMutableTreeNode coffHeaderNode;
        DefaultMutableTreeNode coffOptionalHeaderNode;
        DefaultMutableTreeNode sectionHeadersNode;
        IdentityHashMap<TreeNode, Section> sectionHeaderNodes;
        DefaultMutableTreeNode sectionsNode;
        IdentityHashMap<TreeNode, Section> sectionNodes;
        DefaultMutableTreeNode exportsNode, importsNode, resourcesRootNode, exceptionsNode;
        DefaultMutableTreeNode coffRelocationsNode;
        DefaultMutableTreeNode coffSymbolsNode;
        DefaultMutableTreeNode coffStringsNode;


            peNode = new DefaultMutableTreeNode("COFF/PE");
            root.add(peNode);

            peHeadersNode = new DefaultMutableTreeNode("COFF/PE headers");
            peNode.add(peHeadersNode);

            coffHeaderNode = new DefaultMutableTreeNode("COFF header");
            peHeadersNode.add(coffHeaderNode);

            coffOptionalHeaderNode = new DefaultMutableTreeNode("COFF optional header");
            peHeadersNode.add(coffOptionalHeaderNode);

            sectionHeadersNode = new DefaultMutableTreeNode("Section headers");
            peHeadersNode.add(sectionHeadersNode);

            sectionHeaderNodes = new IdentityHashMap<>();
            for (Section section : peFormat.sections) {
                NameOrStringTablePointer name = section.sectionHeader.name;
                String nameText;
                if (name.type == NameOrStringTablePointer.Type.NAME) {
                    nameText = new String(name.name);
                } else {
                    nameText = "StringTablePointer:" + name.stringTablePointer;
                }
                DefaultMutableTreeNode sectionNode = new DefaultMutableTreeNode(nameText);
                sectionHeaderNodes.put(sectionNode, section);
                sectionHeadersNode.add(sectionNode);
            }

            sectionsNode = new DefaultMutableTreeNode("Sections data");
            peNode.add(sectionsNode);

            sectionNodes = new IdentityHashMap<>();
            for (Section section : peFormat.sections) {
                NameOrStringTablePointer name = section.sectionHeader.name;
                String nameText;
                if (name.type == NameOrStringTablePointer.Type.NAME) {
                    nameText = new String(name.name);
                } else {
                    nameText = "StringTablePointer:" + name.stringTablePointer;
                }
                DefaultMutableTreeNode sectionNode = new DefaultMutableTreeNode(nameText);
                sectionNodes.put(sectionNode, section);
                sectionsNode.add(sectionNode);
            }

            exportsNode = new DefaultMutableTreeNode("Exports");
            peNode.add(exportsNode);        
        
            importsNode = new DefaultMutableTreeNode("Imports");
            peNode.add(importsNode);
            IdentityHashMap<DefaultMutableTreeNode, ImportBlock> importBlockNodes = new IdentityHashMap<>();
            for (ImportBlock importBlock : peFormat.imports) {
                DefaultMutableTreeNode importBlockNode = new DefaultMutableTreeNode(importBlock.name);
                importBlockNodes.put(importBlockNode, importBlock);
                importsNode.add(importBlockNode);
            }

            IdentityHashMap<DefaultMutableTreeNode, ResourceEntry> resourceDirectoryNodes = new IdentityHashMap<>();
            
            ResourceEntry rootWrapperEntry = new ResourceEntry(null);
            rootWrapperEntry.resolvedName = "Resources";
            rootWrapperEntry.resolvedSubDirectory = peFormat.resourceRoot;

            resourcesRootNode = new DefaultMutableTreeNode(rootWrapperEntry.resolvedName);
            resourceDirectoryNodes.put(resourcesRootNode, rootWrapperEntry);
            peNode.add(resourcesRootNode);
            processChildren(rootWrapperEntry, resourcesRootNode, resourceDirectoryNodes, 1);


        coffRelocationsNode = new DefaultMutableTreeNode("COFF relocations");
            peNode.add(coffRelocationsNode);

            coffSymbolsNode = new DefaultMutableTreeNode("COFF symbols");
            peNode.add(coffSymbolsNode);

            coffStringsNode = new DefaultMutableTreeNode("COFF strings");
            peNode.add(coffStringsNode);

        treeModel.reload();

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getNewLeadSelectionPath();

                if (path != null && path.getLastPathComponent() == dosExeNode) {

                    JPanel dosInfoPanel = new JPanel();

                    dosInfoPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder dosInfoAdder = new GridBagByRowAdder(col1, col2, col4);

                    int dosTotalSize = (dosHeader.nBlocks - 1) * 512 + dosHeader.lastSize;
                    dosInfoAdder.addRow(dosInfoPanel, new JLabel("DOS header size: "), new JLabel(String.valueOf(dosHeader.getDeclaredHeaderSize())), makeFiller());
                    dosInfoAdder.addRow(dosInfoPanel, new JLabel("DOS payload size: "), new JLabel(String.valueOf(dosTotalSize - dosHeader.getDeclaredHeaderSize())), makeFiller());
                    dosInfoAdder.addRow(dosInfoPanel, new JLabel("DOS exe total size: "), new JLabel(String.valueOf(dosTotalSize)), makeFiller());

                    dosInfoAdder.addBottomFillerTo(dosInfoPanel);
                    dosInfoPanel.setBackground(UIManager.getColor("List.background"));

                    dosInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(dosInfoPanel);

                } else if (path != null && path.getLastPathComponent() == dosPayloadNode) {

                    JPanel dosInfoPanel = new JPanel();

                    dosInfoPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder fileInfoAdder = new GridBagByRowAdder(col1, col2, col4);

                    int dosTotalSize = (dosHeader.nBlocks - 1) * 512 + dosHeader.lastSize;
                    int dosPayloadSize = dosTotalSize - dosHeader.getDeclaredHeaderSize();

                    fileInfoAdder.addRow(dosInfoPanel, new JLabel("DOS payload offset: "), new JLabel(String.valueOf(dosHeader.getDeclaredHeaderSize())), makeFiller());
                    fileInfoAdder.addRow(dosInfoPanel, new JLabel("DOS payload size: "), new JLabel(String.valueOf(dosPayloadSize)), makeFiller());

                    fileInfoAdder.addBottomFillerTo(dosInfoPanel);
                    dosInfoPanel.setBackground(UIManager.getColor("List.background"));

                    dosInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(dosInfoPanel);


                } else if (path != null && path.getLastPathComponent() == dosHeaderNode) {

                    JPanel dosExeGeneralPanel = new JPanel();

                    dosExeGeneralPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder dosExeGeneralAdder = new GridBagByRowAdder(col1, col2, col4);

                    dosExeGeneralAdder.addRow(dosExeGeneralPanel, new JLabel("DOS Header total size: "), new JLabel(String.valueOf(dosHeader.getDeclaredHeaderSize())), makeFiller());
                    dosExeGeneralAdder.addRow(dosExeGeneralPanel, new JLabel("DOS Header standard area size: "), new JLabel(String.valueOf(dosHeader.FIXED_HEADER_SIZE)), makeFiller());
                    dosExeGeneralAdder.addRow(dosExeGeneralPanel, new JLabel("Extension area size: "), new JLabel(String.valueOf(dosHeader.getHeaderExtensionsSize())), makeFiller());
                    dosExeGeneralAdder.addRow(dosExeGeneralPanel, new JLabel("Relocations area size: "), new JLabel(String.valueOf(dosHeader.getRelocationsSize())), makeFiller());
                    dosExeGeneralAdder.addRow(dosExeGeneralPanel, new JLabel("Free space: "), new JLabel(String.valueOf(dosHeader.getFreeSpaceSize())), makeFiller());

                    dosExeGeneralAdder.addSingleComponentWholeRow(dosExeGeneralPanel, new JSeparator(), new Insets(10, 5, 10, 5));

                    // dosExeGeneralAdder.addBottomFillerTo(dosExeGeneralPanel);

                    // looks like having vertical filler is better than restricting max size
                    // dosExeGeneralPanel.setPreferredSize(dosExeGeneralPanel.getMinimumSize());
                    // dosExeGeneralPanel.setMaximumSize(dosExeGeneralPanel.getMinimumSize());

                    // dosExeGeneralPanel.setBackground(UIManager.getColor("List.background"));
                    // dosExeGeneralPanel.setBorder(BorderFactory.createTitledBorder("General info"));


                    JPanel dosExeFieldsPanel = dosExeGeneralPanel; // new JPanel();

                    // dosExeFieldsPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder dosExeRowAdder = dosExeGeneralAdder; // new GridBagByRowAdder(col1, col2, col4);

                    for (String fieldName : dosHeader.getNames()) {

                        JLabel nameLabel = new JLabel(fieldName + ":");

                        // JLabel offsetLabel = new JLabel("(offset:" + format.dosHeader.getOffset(fieldName) + ";" + "size:" + format.dosHeader.getSize(fieldName) + ")");
                        // JLabel valueLabel = new JLabel(format.header.getStringValue(fieldName));
                        DisplayFormat selectedDisplayFormat = displayFormatMap.getOrDefault(fieldName, DisplayFormat.DEFAULT);
                        JTextField valueTextField = new JTextField(dosHeader.getStringValue(fieldName, selectedDisplayFormat).get(), 10);
                        // valueTextField.setEnabled(false);
                        valueTextField.setEditable(false);

                        {
                            JPopupMenu popup = new JPopupMenu();
                            valueTextField.setComponentPopupMenu(popup);

                            JMenuItem fieldInfoItem = addFieldInfoMenu(dosHeader, fieldName, 0, displayFormatMap);
                            popup.add(fieldInfoItem);

                            JMenu subMenu = addDisplayFormatsMenu(dosHeader, fieldName, valueTextField, displayFormatMap);
                            popup.add(subMenu);

                        }


                        JComponent filler = makeFiller();

                        dosExeRowAdder.addRow(dosExeFieldsPanel, nameLabel, valueTextField, filler); // offsetLabel


                    }

                    dosExeRowAdder.addBottomFillerTo(dosExeFieldsPanel);
                    dosExeFieldsPanel.setBackground(UIManager.getColor("List.background"));

                    // dosExeFieldsPanel.setBorder(BorderFactory.createTitledBorder("Fields"));
                    dosExeGeneralPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(dosExeGeneralPanel);
                } else if (path != null && path.getLastPathComponent() == peNode) {

                    JPanel peInfoPanel = new JPanel();

                    peInfoPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder fileInfoAdder = new GridBagByRowAdder(col1, col2, col4);

                    String peFormatDetails;
                    if (peFormat.coffOptionalHeaderPE32Plus != null) {
                        peFormatDetails = "PE32+ (64 bit)";
                    } else if (peFormat.coffOptionalHeaderPE32 != null) {
                        peFormatDetails = "PE32 (32 bit)";
                    } else if (peFormat.coffOptionalHeaderStandard != null) {
                        peFormatDetails = "PE unknown";
                    } else {
                        peFormatDetails = "Unknown";
                    }
                    fileInfoAdder.addRow(peInfoPanel, new JLabel("PE format: "), new JLabel(peFormatDetails), makeFiller());

                    fileInfoAdder.addRow(peInfoPanel, new JLabel("PE offset from start of file: "), new JLabel(String.valueOf(peFormat.getPEOffset())), makeFiller());


                    JLabel headersTotalSizeLabel = new JLabel("PE headers and section headers total size: ");
                    JLabel sizeValLabel = new JLabel(String.valueOf(peFormat.getPEHeadersTotalSize() + peFormat.getSectionHeadersSize()));
                    fileInfoAdder.addRow(peInfoPanel, headersTotalSizeLabel, sizeValLabel, makeFiller());
                    String tooltipText = "Calculated as a sum of PE COFF headers and section headers";
                    headersTotalSizeLabel.setToolTipText(tooltipText);
                    sizeValLabel.setToolTipText(tooltipText);

                    fileInfoAdder.addBottomFillerTo(peInfoPanel);
                    peInfoPanel.setBackground(UIManager.getColor("List.background"));

                    peInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(peInfoPanel);

                } else if (path != null && path.getLastPathComponent() == peHeadersNode) {

                    JPanel peInfoPanel = new JPanel();

                    peInfoPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder fileInfoAdder = new GridBagByRowAdder(col1, col2, col4);

                    fileInfoAdder.addRow(peInfoPanel, new JLabel("PE headers offset from start of file: "), new JLabel(String.valueOf(peFormat.getCOFFHeaderOffset())), makeFiller());
                    fileInfoAdder.addRow(peInfoPanel, new JLabel("PE COFF headers size: "), new JLabel(String.valueOf(peFormat.getPEHeadersTotalSize())), makeFiller());
                    fileInfoAdder.addRow(peInfoPanel, new JLabel("Section headers total size: "), new JLabel(String.valueOf(peFormat.getSectionHeadersSize())), makeFiller());

                    fileInfoAdder.addBottomFillerTo(peInfoPanel);
                    peInfoPanel.setBackground(UIManager.getColor("List.background"));

                    peInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(peInfoPanel);


                } else if (path != null && path.getLastPathComponent() == coffOptionalHeaderNode) {

                    JPanel coffOptionalFieldsPanel = new JPanel();

                    coffOptionalFieldsPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder coffRowAdder = new GridBagByRowAdder(col1, col2, col4);

                    IntrospectableFormat coffOptionalHeader;
                    if (peFormat.coffOptionalHeaderPE32Plus != null) {
                        coffOptionalHeader = peFormat.coffOptionalHeaderPE32Plus;
                    } else if (peFormat.coffOptionalHeaderPE32 != null) {
                        coffOptionalHeader = peFormat.coffOptionalHeaderPE32;
                    } else if (peFormat.coffOptionalHeaderStandard != null) {
                        coffOptionalHeader = peFormat.coffOptionalHeaderStandard;
                    } else {
                        return;
                    }
                    for (String fieldName : coffOptionalHeader.getNames()) {
                        
                        // this field is hadnled separatelly
                        if (fieldName.equals(COFFOptionalHeaderPE32.FieldPE32.DATA_DIRECTORY.name())) {
                            continue;
                        }

                        JLabel nameLabel = new JLabel(fieldName + ":");

                        // JLabel offsetLabel = new JLabel("(offset:" + format.coffHeader.getOffset(fieldName) + ";" + "size:" + format.coffHeader.getSize(fieldName) + ")");
                        // JLabel valueLabel = new JLabel(format.header.getStringValue(fieldName));
                        DisplayFormat selectedDisplayFormat = displayFormatMap.getOrDefault(fieldName, DisplayFormat.DEFAULT);
                        JTextField valueTextField = new JTextField(coffOptionalHeader.getStringValue(fieldName, selectedDisplayFormat).get(), 10);
                        // valueTextField.setEnabled(false);
                        valueTextField.setEditable(false);

                        {
                            JPopupMenu popup = new JPopupMenu();
                            valueTextField.setComponentPopupMenu(popup);

                            JMenuItem fieldInfoItem = addFieldInfoMenu(coffOptionalHeader, fieldName, (int) (peFormat.getCOFFHeaderOffset() + COFFHeader.SIZE), displayFormatMap);
                            popup.add(fieldInfoItem);

                            JMenu subMenu = addDisplayFormatsMenu(coffOptionalHeader, fieldName, valueTextField, displayFormatMap);
                            popup.add(subMenu);

                        }

                        JComponent filler = makeFiller();
                        coffRowAdder.addRow(coffOptionalFieldsPanel, nameLabel, valueTextField, filler); // offsetLabel

                    }
                    
                    coffRowAdder.addSingleComponentWholeRow(coffOptionalFieldsPanel, new JSeparator(), new Insets(5, 5, 5, 5));

                    {
                        DefaultTableModel directoryTableModel = new DefaultTableModel();
                        directoryTableModel.setColumnIdentifiers(new String[]{"Directory entry", "VirtualAddress", "Size", "Section", "Offset in section"});

                        List<DirectoryEntry> dataDirectory = null;
                        if (peFormat.coffOptionalHeaderPE32Plus != null) {
                            dataDirectory = peFormat.coffOptionalHeaderPE32Plus.dataDirectory;
                        } else if (peFormat.coffOptionalHeaderPE32 != null) {
                            dataDirectory = peFormat.coffOptionalHeaderPE32.dataDirectory;
                        }
                        if (dataDirectory != null) {

                            for (DirectoryEntry directoryEntry : dataDirectory) {
                                Section correspondingSection = peFormat.findByRelativeVirtualAddress(directoryEntry.VirtualAddress);
                                directoryTableModel.addRow(new String[]{
                                        directoryEntry.name,
                                        String.valueOf(directoryEntry.VirtualAddress),
                                        String.valueOf(directoryEntry.Size),
                                        correspondingSection != null ? correspondingSection.getName() : "",
                                        correspondingSection != null ? String.valueOf(directoryEntry.VirtualAddress - correspondingSection.sectionHeader.virtualAddress) : ""
                                });

                            }
                        }

                        JTable dataDirectoryTable = new JTable(directoryTableModel);

                        JScrollPane sectionsTableScrollPane = new JScrollPane(dataDirectoryTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                        dataDirectoryTable.setFillsViewportHeight(true);

                        coffRowAdder.addSingleComponentWholeRow(coffOptionalFieldsPanel, sectionsTableScrollPane, new Insets(5, 5, 5, 5));
                        
                    }
                    

                    coffRowAdder.addBottomFillerTo(coffOptionalFieldsPanel);
                    coffOptionalFieldsPanel.setBackground(UIManager.getColor("List.background"));
                    coffOptionalFieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(coffOptionalFieldsPanel);

                } else if (path != null && path.getLastPathComponent() == coffHeaderNode) {

                    JPanel coffFieldsPanel = new JPanel();

                    coffFieldsPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder coffRowAdder = new GridBagByRowAdder(col1, col2, col4);

                    COFFHeader coffHeader = peFormat.coffHeader;
                    for (String fieldName : coffHeader.getNames()) {

                        JLabel nameLabel = new JLabel(fieldName + ":");

                        String tooltipText = "<html>Offset:" + coffHeader.getOffset(fieldName) + "<br>" + "Size:" + coffHeader.getSize(fieldName) + "</html>";

                        nameLabel.setToolTipText(tooltipText);

                        // JLabel offsetLabel = new JLabel("(offset:" + format.coffHeader.getOffset(fieldName) + ";" + "size:" + format.coffHeader.getSize(fieldName) + ")");
                        // JLabel valueLabel = new JLabel(format.header.getStringValue(fieldName));
                        DisplayFormat selectedDisplayFormat = displayFormatMap.getOrDefault(fieldName, DisplayFormat.DEFAULT);
                        JTextField valueTextField = new JTextField(coffHeader.getStringValue(fieldName, selectedDisplayFormat).get(), 10);
                        // valueTextField.setEnabled(false);
                        valueTextField.setEditable(false);
                        valueTextField.setToolTipText(tooltipText);

                        {
                            JPopupMenu popup = new JPopupMenu();
                            valueTextField.setComponentPopupMenu(popup);

                            JMenuItem fieldInfoItem = addFieldInfoMenu(coffHeader, fieldName, (int) peFormat.getCOFFHeaderOffset(), displayFormatMap);
                            popup.add(fieldInfoItem);

                            JMenu subMenu = addDisplayFormatsMenu(coffHeader, fieldName, valueTextField, displayFormatMap);
                            popup.add(subMenu);

                        }

                        JComponent filler = makeFiller();

                        coffRowAdder.addRow(coffFieldsPanel, nameLabel, valueTextField, filler); // offsetLabel


                    }

                    coffRowAdder.addBottomFillerTo(coffFieldsPanel);
                    coffFieldsPanel.setBackground(UIManager.getColor("List.background"));
                    coffFieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(coffFieldsPanel);

                } else if (path != null && path.getLastPathComponent() == sectionHeadersNode) {

                    JPanel sectionsInfoPanel = new JPanel();

                    sectionsInfoPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder sectionsAdder = new GridBagByRowAdder(col1, col2, col4);


                    sectionsAdder.addRow(sectionsInfoPanel, new JLabel("Section headers offset from start of file: "), new JLabel(String.valueOf(peFormat.getSectionHeadersOffset())), makeFiller());
                    sectionsAdder.addRow(sectionsInfoPanel, new JLabel("Number of sections: "), new JLabel(String.valueOf(peFormat.sections.size())), makeFiller());
                    sectionsAdder.addRow(sectionsInfoPanel, new JLabel("Total section headers size: "), new JLabel(String.valueOf(peFormat.getSectionHeadersSize())), makeFiller());

                    sectionsAdder.addBottomFillerTo(sectionsInfoPanel);
                    sectionsInfoPanel.setBackground(UIManager.getColor("List.background"));

                    sectionsInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(sectionsInfoPanel);


                } else if (path != null && sectionHeaderNodes.containsKey(path.getLastPathComponent())) {

                    Section section = sectionHeaderNodes.get(path.getLastPathComponent());

                    JPanel sectionFieldsPanel = new JPanel();

                    sectionFieldsPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder sectionHeaderRowAdder = new GridBagByRowAdder(col1, col2, col4);

                    int sectionIndex = peFormat.sections.indexOf(section);
                    long sectionHeaderOffsetFromStartOfFile = peFormat.getSectionHeadersOffset() + sectionIndex * SectionHeader.SIZE;
                    sectionHeaderRowAdder.addRow(sectionFieldsPanel, new JLabel("Offset from start of file: "), new JLabel(String.valueOf(sectionHeaderOffsetFromStartOfFile)), makeFiller());
                    sectionHeaderRowAdder.addRow(sectionFieldsPanel, new JLabel("Size: "), new JLabel(String.valueOf(SectionHeader.SIZE)), makeFiller());

                    sectionHeaderRowAdder.addSingleComponentWholeRow(sectionFieldsPanel, new JSeparator(), new Insets(10, 5, 10, 5));

                    SectionHeader sectionHeader = section.sectionHeader;
                    for (String fieldName : sectionHeader.getNames()) {

                        JLabel nameLabel = new JLabel(fieldName + ":");

                        // JLabel offsetLabel = new JLabel("(offset:" + format.coffHeader.getOffset(fieldName) + ";" + "size:" + format.coffHeader.getSize(fieldName) + ")");
                        // JLabel valueLabel = new JLabel(format.header.getStringValue(fieldName));
                        DisplayFormat selectedDisplayFormat = displayFormatMap.getOrDefault(fieldName, DisplayFormat.DEFAULT);
                        JTextField valueTextField = new JTextField(sectionHeader.getStringValue(fieldName, selectedDisplayFormat).get(), 10);
                        // valueTextField.setEnabled(false);
                        valueTextField.setEditable(false);

                        JComponent filler = makeFiller();

                        sectionHeaderRowAdder.addRow(sectionFieldsPanel, nameLabel, valueTextField, filler); // offsetLabel

                        String tooltipText = "<html>Offset:" + sectionHeader.getOffset(fieldName) + "<br>" + "Size:" + sectionHeader.getSize(fieldName) + "</html>";

                        nameLabel.setToolTipText(tooltipText);

                        valueTextField.setEditable(false);
                        valueTextField.setToolTipText(tooltipText);

                        {
                            JPopupMenu popup = new JPopupMenu();
                            valueTextField.setComponentPopupMenu(popup);

                            JMenuItem fieldInfoItem = addFieldInfoMenu(sectionHeader, fieldName, (int) sectionHeaderOffsetFromStartOfFile, displayFormatMap);
                            popup.add(fieldInfoItem);

                            JMenu subMenu = addDisplayFormatsMenu(sectionHeader, fieldName, valueTextField, displayFormatMap);
                            popup.add(subMenu);

                        }

                    }

                    sectionHeaderRowAdder.addBottomFillerTo(sectionFieldsPanel);
                    sectionFieldsPanel.setBackground(UIManager.getColor("List.background"));
                    sectionFieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(sectionFieldsPanel);
                } else if (path != null && path.getLastPathComponent() == importsNode) {

                    JPanel importsPanel = new JPanel();

                    importsPanel.setLayout(new BorderLayout());

                    DefaultTableModel importsTableModel = new DefaultTableModel();
                    importsTableModel.setColumnIdentifiers(new String[] {"Name RVA", "Name section+offset", "Name", "Import lookup table RVA", "Import lookup table section+offset", "Import address table RVA", "Import address table section+offset"});

                    for (ImportBlock importEntry : peFormat.imports) {
                        ImportDirectoryTableEntry importTableEntry = importEntry.importTableEntry;
                        importsTableModel.addRow(new String[] {
                                String.valueOf(importTableEntry.nameRva),
                                peFormat.findByRelativeVirtualAddress(importTableEntry.nameRva).getName() + "+" + String.valueOf(importTableEntry.nameRva - peFormat.findByRelativeVirtualAddress(importTableEntry.nameRva).sectionHeader.virtualAddress),
                                importEntry.name,
                                String.valueOf(importTableEntry.importLookupTableRva),
                                peFormat.findByRelativeVirtualAddress(importTableEntry.importLookupTableRva).getName() + "+" + String.valueOf(importTableEntry.importLookupTableRva - peFormat.findByRelativeVirtualAddress(importTableEntry.importLookupTableRva).sectionHeader.virtualAddress),
                                String.valueOf(importTableEntry.importAddressTableRva),
                                peFormat.findByRelativeVirtualAddress(importTableEntry.importAddressTableRva).getName() + "+" + String.valueOf(importTableEntry.importAddressTableRva - peFormat.findByRelativeVirtualAddress(importTableEntry.importAddressTableRva).sectionHeader.virtualAddress),
                        });
                    }

                    JTable importsTable = new JTable(importsTableModel);

                    JScrollPane importsTableScrollPane = new JScrollPane(importsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    importsTable.setFillsViewportHeight(true);

                    importsPanel.add(importsTableScrollPane, BorderLayout.CENTER);


                    importsPanel.setBackground(UIManager.getColor("List.background"));
                    importsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


                    contentScrollPane.setViewportView(importsPanel);
                    
                } else if (path != null && importBlockNodes.containsKey(path.getLastPathComponent())) {
                    ImportBlock section = importBlockNodes.get(path.getLastPathComponent());

                    JPanel importBlockPanel = new JPanel();

                    importBlockPanel.setLayout(new BorderLayout());

                    DefaultTableModel importedFunctionsTableModel = new DefaultTableModel();
                    importedFunctionsTableModel.setColumnIdentifiers(new String[] {"Name"});

                    // TODO replace this with detiled data with ordinal/name indicator
                    List<String> importedFunctions = section.resolvedImportedFunctions;
                    for (String importedFunction: importedFunctions) {
                        importedFunctionsTableModel.addRow(new String[] { 
                                importedFunction
                        });
                    }

                    JTable importedFunctionsTable = new JTable(importedFunctionsTableModel);

                    JScrollPane importedFunctionsTableScrollPane = new JScrollPane(importedFunctionsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    importedFunctionsTable.setFillsViewportHeight(true);

                    importBlockPanel.add(importedFunctionsTableScrollPane, BorderLayout.CENTER);


                    importBlockPanel.setBackground(UIManager.getColor("List.background"));
                    importBlockPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


                    contentScrollPane.setViewportView(importBlockPanel);
                    

                } else if (path != null && path.getLastPathComponent() == sectionsNode) {

                    JPanel sectionsDataOverviewPanel = new JPanel();

                    sectionsDataOverviewPanel.setLayout(new BorderLayout());

                    DefaultTableModel sectionsTableModel = new DefaultTableModel();
                    sectionsTableModel.setColumnIdentifiers(new String[] {"Name", "Offset in file", "Size in file", "End in file", "Start RVA", "Virtual size", "End RVA"});
                    
                    for (Section section : peFormat.sections) {
                        sectionsTableModel.addRow(new String[] {
                                section.getName(),
                                String.valueOf(section.getOffsetInFile()),
                                String.valueOf(section.getSizeInFile()),
                                String.valueOf(section.getOffsetInFile() + section.getSizeInFile()),
                                String.valueOf(section.sectionHeader.virtualAddress),
                                String.valueOf(section.sectionHeader.physicalAddressOrVirtualSize),
                                String.valueOf(section.sectionHeader.virtualAddress + section.sectionHeader.physicalAddressOrVirtualSize)
                        });
                    }
                    
                    JTable sectionsLayoutTable = new JTable(sectionsTableModel);

                    JScrollPane sectionsTableScrollPane = new JScrollPane(sectionsLayoutTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    sectionsLayoutTable.setFillsViewportHeight(true);

                    sectionsDataOverviewPanel.add(sectionsTableScrollPane, BorderLayout.CENTER);


                    sectionsDataOverviewPanel.setBackground(UIManager.getColor("List.background"));
                    sectionsDataOverviewPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


                    contentScrollPane.setViewportView(sectionsDataOverviewPanel);
                    
                } else if (path != null && sectionNodes.containsKey(path.getLastPathComponent())) {
                    Section section = sectionNodes.get(path.getLastPathComponent());

                    JPanel sectionDataOverviewPanel = new JPanel();

                    sectionDataOverviewPanel.setLayout(new BorderLayout());

                    DefaultTableModel usagesTableModel = new DefaultTableModel();
                    usagesTableModel.setColumnIdentifiers(new String[] {"Offset", "Size", "End", "Description"});

                    List<Section.Usage> usages = section.usages;
                    usages.sort(new Comparator<Section.Usage>() {
                        @Override
                        public int compare(Section.Usage o1, Section.Usage o2) {
                            return (o1.offset > o2.offset) ? 1 : (o1.offset == o2.offset) ? 0 : -1;
                        }
                    });
                    for (Section.Usage usage: usages) {
                        usagesTableModel.addRow(new String[] {
                                String.valueOf(usage.offset),
                                String.valueOf(usage.length),
                                String.valueOf(usage.offset + usage.length),
                                String.valueOf(usage.description)
                        });
                    }

                    JTable usagesTable = new JTable(usagesTableModel);

                    JScrollPane sectionsTableScrollPane = new JScrollPane(usagesTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    usagesTable.setFillsViewportHeight(true);

                    sectionDataOverviewPanel.add(sectionsTableScrollPane, BorderLayout.CENTER);


                    sectionDataOverviewPanel.setBackground(UIManager.getColor("List.background"));
                    sectionDataOverviewPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


                    contentScrollPane.setViewportView(sectionDataOverviewPanel);
                    
                }
                
            }

            public JMenuItem addFieldInfoMenu(final IntrospectableFormat header, final String fieldName, int formatStartOffset, final Map<Object, DisplayFormat> displayFormatMap) {
                JMenuItem fieldInfoItem = new JMenuItem("Field info...");

                fieldInfoItem.setAction(new AbstractAction("Field info...") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showFieldInfoDialog(header, fieldName, formatStartOffset, displayFormatMap);
                    }
                });

                return fieldInfoItem;
            }


            public JMenu addDisplayFormatsMenu(final IntrospectableFormat header, final String fieldName, final JTextField valueTextField, final Map<Object, DisplayFormat> displayFormatMap) {
                JMenu subMenu = new JMenu("Display as");
                DisplayFormat[] displayFormats = IntrospectableFormat.filterSupported(header, fieldName, DisplayFormat.values());
                
                for (DisplayFormat displayFormat : displayFormats) {
                    JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(new AbstractAction(displayFormat.name()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            displayFormatMap.put(fieldName, displayFormat);
                            valueTextField.setText(header.getStringValue(fieldName, displayFormat).get());
                        }
                    });
                    subMenu.add(menuItem);
                    

                }
                
                subMenu.addMenuListener(new MenuListener() {
                    @Override
                    public void menuSelected(MenuEvent e) {

                        DisplayFormat lastSelection = displayFormatMap.getOrDefault(fieldName, DisplayFormat.DEFAULT);
                        
                        for (int i = 0; i < subMenu.getItemCount(); i++) {
                            JCheckBoxMenuItem menuItem = (JCheckBoxMenuItem)subMenu.getItem(i);
                            menuItem.setState(lastSelection.name().equals(menuItem.getText()));
                        }
                        
                    }

                    @Override
                    public void menuDeselected(MenuEvent e) {
                        

                    }

                    @Override
                    public void menuCanceled(MenuEvent e) {

                    }
                });
                return subMenu;
            }

            public void showFieldInfoDialog(IntrospectableFormat format, String fieldName, int formatOffset, final Map<Object, DisplayFormat> displayFormatMap) {
                JDialog dialog = new JDialog(frame, "Field info");

                JPanel fieldInfoPanel = new JPanel();

                fieldInfoPanel.setLayout(new GridBagLayout());

                GridBagByRowAdder fieldInfoAdder = new GridBagByRowAdder(col1, col4, col2);

                JTextField fieldNameText = new JTextField(fieldName, 20);
                fieldNameText.setEditable(false);
                fieldInfoAdder.addRow(fieldInfoPanel, new JLabel("Field name: "), fieldNameText, makeFiller());
                JTextField sizeText = new JTextField(String.valueOf(format.getSize(fieldName)), 20);
                sizeText.setEditable(false);
                fieldInfoAdder.addRow(fieldInfoPanel, new JLabel("Field size: "), sizeText, makeFiller());
                JTextField offsetText = new JTextField(String.valueOf(format.getOffset(fieldName) + formatOffset), 20);
                offsetText.setEditable(false);
                fieldInfoAdder.addRow(fieldInfoPanel, new JLabel("Field offset from start of file: "), offsetText, makeFiller());
                
                fieldInfoAdder.addSingleComponentWholeRow(fieldInfoPanel, new JSeparator(), new Insets(5, 5, 5, 5));

                JComboBox<DisplayFormat> displayFormatComboBox = new JComboBox<>(new DefaultComboBoxModel<DisplayFormat>(IntrospectableFormat.filterSupported(format, fieldName, DisplayFormat.values())));
                DisplayFormat selectedDisplayFormat = displayFormatMap.getOrDefault(fieldName, DisplayFormat.DEFAULT);
                displayFormatComboBox.setSelectedItem(selectedDisplayFormat);
                fieldInfoAdder.addRow(fieldInfoPanel, new JLabel("Field format: "), displayFormatComboBox, makeFiller());

                JTextField valueTextField = new JTextField(format.getStringValue(fieldName, selectedDisplayFormat).get(), 20);
                fieldInfoAdder.addRow(fieldInfoPanel, new JLabel("Field value: "), valueTextField, makeFiller());
                
                displayFormatComboBox.addItemListener(new ItemListener() {
                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        valueTextField.setText(format.getStringValue(fieldName, (DisplayFormat)displayFormatComboBox.getSelectedItem()).get());
                    }
                });

                fieldInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                dialog.getContentPane().add(fieldInfoPanel, BorderLayout.CENTER);
                
                JPanel buttonsPanel = new JPanel();
                buttonsPanel.setLayout(new FlowLayout());
                buttonsPanel.add(new JButton(new AbstractAction("OK") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dialog.hide();
                    }
                }));
                buttonsPanel.add(new JButton(new AbstractAction("Cancel") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        dialog.hide();
                    }
                }));
                dialog.getContentPane().add(buttonsPanel, BorderLayout.SOUTH);

                dialog.setSize(300, 200);
                dialog.setLocation(frame.getX() + 50, frame.getY() + 50);
                dialog.show();

            }
        });

    }

    public void processChildren(ResourceEntry directory, DefaultMutableTreeNode directoryNode, IdentityHashMap<DefaultMutableTreeNode, ResourceEntry> resourceDirectoryNodes, int level) {
        for (ResourceEntry entry : directory.resolvedSubDirectory.entries) {
            String name = entry.resolvedName != null ? entry.resolvedName : ((level == 1) && (ResourceDirectory.ResourceType.findById(entry.directoryEntry.integerID) != null) ? (ResourceDirectory.ResourceType.findById(entry.directoryEntry.integerID).name()) : String.valueOf(entry.directoryEntry.integerID));
            DefaultMutableTreeNode entryNode = new DefaultMutableTreeNode(name);
            resourceDirectoryNodes.put(entryNode, entry);
            directoryNode.add(entryNode);
            if (entry.resolvedSubDirectory != null) {
                processChildren(entry, entryNode, resourceDirectoryNodes, level + 1);
            }
        }
    }

    public static JComponent makeFiller() {
        JComponent filler = new JPanel();
        filler.setOpaque(false);
        return filler;
    }
    

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        VisualPE instance = new VisualPE();


    }

}
