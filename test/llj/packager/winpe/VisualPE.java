package llj.packager.winpe;

import llj.packager.DisplayFormat;
import llj.packager.IntrospectableFormat;
import llj.packager.ar.ARFormat;
import llj.packager.coff.COFFHeader;
import llj.packager.coff.NameOrStringTablePointer;
import llj.packager.coff.Section;
import llj.packager.coff.SectionHeader;
import llj.packager.dosexe.DOSExeFormat;
import llj.packager.dosexe.DOSExeFormatException;
import llj.packager.dosexe.DOSHeader;
import llj.packager.objcoff.OBJCOFFFormat;
import llj.packager.winlib.ImportFormat;
import llj.packager.winlib.ImportHeader;
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
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.UIManager;
import javax.swing.event.MenuEvent;
import javax.swing.event.MenuListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.SeekableByteChannel;
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
                    if (file != null) {
                        classChooser.setCurrentDirectory(file.getParentFile());
                    }
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
            menuFile.add(new AbstractAction("Reload") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    setFile(file);
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

            String fileName = file.toPath().getFileName().toString().toLowerCase();
            if (fileName.endsWith(".exe") || fileName.endsWith(".dll")) {

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

            } else if (fileName.endsWith(".obj")) {

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
                
            } else if (fileName.endsWith(".lib")) {
                
                ARFormat format = new ARFormat();
                boolean formatOk = false;
                try {
                    format.readFrom(fileChannel);
                    formatOk = true;
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(frame, "Was unable to read selected file", "File read error", JOptionPane.ERROR_MESSAGE);
                }

                if (!formatOk) {
                    return;
                }

                removeAllRenderers();
                createAnyFormatRenderer(file, displayFormatMap, "LIBRARY");
                createLIBFormatRenderer(displayFormatMap, format, fileChannel);
                
                

            } else {
                JOptionPane.showMessageDialog(frame, "File extension is not recognized", "Unknown file type", JOptionPane.INFORMATION_MESSAGE);
            }

            tree.setEnabled(true);
            tree.expandPath(new TreePath(treeModel.getRoot()));

            tree.repaint();

            try {
                fileChannel.close();
            } catch (Exception e) {
                // ignore
            }


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

            IdentityHashMap<DefaultMutableTreeNode, ResourceEntry> resourceNodes = new IdentityHashMap<>();
            
            ResourceEntry rootWrapperEntry = new ResourceEntry(new ResourceDirectoryEntry()); // we will put some dummy empty directory so processChildren treats this node correctly
            rootWrapperEntry.resolvedName = "Resources";
            rootWrapperEntry.resolvedSubDirectory = peFormat.resourceRoot;

            resourcesRootNode = new DefaultMutableTreeNode(rootWrapperEntry.resolvedName);
            resourceNodes.put(resourcesRootNode, rootWrapperEntry);
            peNode.add(resourcesRootNode);
            processChildren(rootWrapperEntry, resourcesRootNode, resourceNodes, 1);
        
        DefaultMutableTreeNode relocationsNode = new DefaultMutableTreeNode("PE relocations");
        peNode.add(relocationsNode);
        IdentityHashMap<DefaultMutableTreeNode, BaseRelocationsBlock> relocationsBlockNodes = new IdentityHashMap<>();
        for (BaseRelocationsBlock relocBlock : peFormat.baseRelocations) {
            DefaultMutableTreeNode relocationsBlockNode = new DefaultMutableTreeNode(relocBlock.pageRva);
            relocationsBlockNodes.put(relocationsBlockNode, relocBlock);
            relocationsNode.add(relocationsBlockNode);
        }


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
                        String displayFormatPrefix = "DOS_HEADER_";
                        DisplayFormat selectedDisplayFormat = displayFormatMap.getOrDefault(displayFormatPrefix + fieldName, DisplayFormat.DEFAULT);
                        JTextField valueTextField = new JTextField(dosHeader.getStringValue(fieldName, selectedDisplayFormat).get(), 10);
                        // valueTextField.setEnabled(false);
                        valueTextField.setEditable(false);

                        {
                            JPopupMenu popup = new JPopupMenu();
                            valueTextField.setComponentPopupMenu(popup);

                            JMenuItem fieldInfoItem = addFieldInfoMenu(dosHeader, fieldName, 0, displayFormatPrefix, displayFormatMap);
                            popup.add(fieldInfoItem);

                            JMenu subMenu = addDisplayFormatsMenu(dosHeader, fieldName, valueTextField, displayFormatPrefix, displayFormatMap);
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

                    fileInfoAdder.addRow(peInfoPanel, new JLabel("PE offset from start of file: "), new JLabel(getDecAndHexStr(peFormat.getPEOffset())), makeFiller());


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

                    fileInfoAdder.addRow(peInfoPanel, new JLabel("PE headers offset from start of file: "), new JLabel(getDecAndHexStr(peFormat.getCOFFHeaderOffset())), makeFiller());
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

                    long coffOptionalHeaderOffset = peFormat.getCOFFHeaderOffset() + peFormat.coffHeader.getSize();
                    coffRowAdder.addRow(coffOptionalFieldsPanel, new JLabel("Offset from start of file: "), new JLabel(getDecAndHexStr(coffOptionalHeaderOffset)), makeFiller());
                    coffRowAdder.addRow(coffOptionalFieldsPanel, new JLabel("Declared size: "), new JLabel(String.valueOf(peFormat.coffHeader.sizeOfOptionalHeader)), makeFiller());
                    coffRowAdder.addRow(coffOptionalFieldsPanel, new JLabel("Used size: "), new JLabel(String.valueOf(coffOptionalHeader.getSize())), makeFiller());

                    coffRowAdder.addSingleComponentWholeRow(coffOptionalFieldsPanel, new JSeparator(), new Insets(5, 5, 5, 5));
                    
                    for (String fieldName : coffOptionalHeader.getNames()) {
                        
                        // this field is hadnled separatelly
                        if (fieldName.equals(COFFOptionalHeaderPE32.FieldPE32.DATA_DIRECTORY.name())) {
                            continue;
                        }

                        JLabel nameLabel = new JLabel(fieldName + ":");

                        // JLabel offsetLabel = new JLabel("(offset:" + format.coffHeader.getOffset(fieldName) + ";" + "size:" + format.coffHeader.getSize(fieldName) + ")");
                        // JLabel valueLabel = new JLabel(format.header.getStringValue(fieldName));
                        String peOptionalHeadersPrefix = "PE_COFF_OPTIONAL_";
                        DisplayFormat selectedDisplayFormat = displayFormatMap.getOrDefault(peOptionalHeadersPrefix + fieldName, DisplayFormat.DEFAULT);
                        JTextField valueTextField = new JTextField(coffOptionalHeader.getStringValue(fieldName, selectedDisplayFormat).get(), 10);
                        // valueTextField.setEnabled(false);
                        valueTextField.setEditable(false);

                        {
                            JPopupMenu popup = new JPopupMenu();
                            valueTextField.setComponentPopupMenu(popup);

                            JMenuItem fieldInfoItem = addFieldInfoMenu(coffOptionalHeader, fieldName, (int) (coffOptionalHeaderOffset), peOptionalHeadersPrefix, displayFormatMap);
                            popup.add(fieldInfoItem);

                            JMenu subMenu = addDisplayFormatsMenu(coffOptionalHeader, fieldName, valueTextField, peOptionalHeadersPrefix, displayFormatMap);
                            popup.add(subMenu);

                        }

                        JComponent filler = makeFiller();
                        coffRowAdder.addRow(coffOptionalFieldsPanel, nameLabel, valueTextField, filler); // offsetLabel

                    }
                    
                    coffRowAdder.addSingleComponentWholeRow(coffOptionalFieldsPanel, new JSeparator(), new Insets(5, 5, 5, 5));

                    {
                        DefaultTableModel directoryTableModel = new DefaultTableModel();
                        directoryTableModel.setColumnIdentifiers(new String[]{"Directory entry", "VirtualAddress", "Size", "Section", "Offset in section", "Offset in file"});

                        List<DirectoryEntry> dataDirectory = null;
                        if (peFormat.coffOptionalHeaderPE32Plus != null) {
                            dataDirectory = peFormat.coffOptionalHeaderPE32Plus.dataDirectory;
                        } else if (peFormat.coffOptionalHeaderPE32 != null) {
                            dataDirectory = peFormat.coffOptionalHeaderPE32.dataDirectory;
                        }
                        if (dataDirectory != null) {

                            for (DirectoryEntry directoryEntry : dataDirectory) {
                                Section correspondingSection = peFormat.findSectionByRVA(directoryEntry.VirtualAddress);
                                long offsetInSection = 0, offsetInFile = 0;
                                if (correspondingSection != null) {
                                    offsetInSection = directoryEntry.VirtualAddress - correspondingSection.sectionHeader.virtualAddress;
                                    offsetInFile = correspondingSection.sectionHeader.pointerToRawData + offsetInSection;
                                }
                                directoryTableModel.addRow(new String[]{
                                        directoryEntry.name,
                                        getDecAndHexStr(directoryEntry.VirtualAddress),
                                        String.valueOf(directoryEntry.Size),
                                        correspondingSection != null ? correspondingSection.getName() : "",
                                        correspondingSection != null ? String.valueOf(offsetInSection) : "",
                                        correspondingSection != null ? getDecAndHexStr(offsetInFile) : ""
                                        
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

                    coffRowAdder.addRow(coffFieldsPanel, new JLabel("Offset from start of file: "), new JLabel(getDecAndHexStr(peFormat.getCOFFHeaderOffset())), makeFiller());
                    coffRowAdder.addRow(coffFieldsPanel, new JLabel("Size: "), new JLabel(String.valueOf(peFormat.coffHeader.getSize())), makeFiller());

                    coffRowAdder.addSingleComponentWholeRow(coffFieldsPanel, new JSeparator(), new Insets(5, 5, 5, 5));

                    COFFHeader coffHeader = peFormat.coffHeader;
                    for (String fieldName : coffHeader.getNames()) {

                        JLabel nameLabel = new JLabel(fieldName + ":");

                        String tooltipText = "<html>Offset:" + coffHeader.getOffset(fieldName) + "<br>" + "Size:" + coffHeader.getSize(fieldName) + "</html>";

                        nameLabel.setToolTipText(tooltipText);

                        // JLabel offsetLabel = new JLabel("(offset:" + format.coffHeader.getOffset(fieldName) + ";" + "size:" + format.coffHeader.getSize(fieldName) + ")");
                        // JLabel valueLabel = new JLabel(format.header.getStringValue(fieldName));
                        String coffBasicHeadersPrefix = "PE_COFF_BASIC_";
                        DisplayFormat selectedDisplayFormat = displayFormatMap.getOrDefault(coffBasicHeadersPrefix + fieldName, DisplayFormat.DEFAULT);
                        JTextField valueTextField = new JTextField(coffHeader.getStringValue(fieldName, selectedDisplayFormat).get(), 10);
                        // valueTextField.setEnabled(false);
                        valueTextField.setEditable(false);
                        valueTextField.setToolTipText(tooltipText);

                        {
                            JPopupMenu popup = new JPopupMenu();
                            valueTextField.setComponentPopupMenu(popup);

                            JMenuItem fieldInfoItem = addFieldInfoMenu(coffHeader, fieldName, (int) peFormat.getCOFFHeaderOffset(), coffBasicHeadersPrefix, displayFormatMap);
                            popup.add(fieldInfoItem);

                            JMenu subMenu = addDisplayFormatsMenu(coffHeader, fieldName, valueTextField, coffBasicHeadersPrefix, displayFormatMap);
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


                    sectionsAdder.addRow(sectionsInfoPanel, new JLabel("Section headers offset from start of file: "), new JLabel(getDecAndHexStr(peFormat.getSectionHeadersOffset())), makeFiller());
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
                    sectionHeaderRowAdder.addRow(sectionFieldsPanel, new JLabel("Offset from start of file: "), new JLabel(getDecAndHexStr(sectionHeaderOffsetFromStartOfFile)), makeFiller());
                    sectionHeaderRowAdder.addRow(sectionFieldsPanel, new JLabel("Size: "), new JLabel(String.valueOf(SectionHeader.SIZE)), makeFiller());

                    sectionHeaderRowAdder.addSingleComponentWholeRow(sectionFieldsPanel, new JSeparator(), new Insets(10, 5, 10, 5));

                    SectionHeader sectionHeader = section.sectionHeader;
                    for (String fieldName : sectionHeader.getNames()) {

                        JLabel nameLabel = new JLabel(fieldName + ":");

                        // JLabel offsetLabel = new JLabel("(offset:" + format.coffHeader.getOffset(fieldName) + ";" + "size:" + format.coffHeader.getSize(fieldName) + ")");
                        // JLabel valueLabel = new JLabel(format.header.getStringValue(fieldName));
                        String sectionHeaders = "SECTIONS_";
                        DisplayFormat selectedDisplayFormat = displayFormatMap.getOrDefault(sectionHeaders + fieldName, DisplayFormat.DEFAULT);
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

                            JMenuItem fieldInfoItem = addFieldInfoMenu(sectionHeader, fieldName, (int) sectionHeaderOffsetFromStartOfFile, sectionHeaders, displayFormatMap);
                            popup.add(fieldInfoItem);

                            JMenu subMenu = addDisplayFormatsMenu(sectionHeader, fieldName, valueTextField, sectionHeaders, displayFormatMap);
                            popup.add(subMenu);

                        }

                    }

                    sectionHeaderRowAdder.addBottomFillerTo(sectionFieldsPanel);
                    sectionFieldsPanel.setBackground(UIManager.getColor("List.background"));
                    sectionFieldsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(sectionFieldsPanel);
                } else if (path != null && path.getLastPathComponent() == exportsNode) {

                    JPanel exportsPanel = new JPanel();

                    exportsPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder exportsRowAdder = new GridBagByRowAdder(col1, col2, col4);

                    DirectoryEntry exportDirectoryEntry = peFormat.getDirectoryEntry(PEFormat.EXPORTS_INDEX );
                    Section correspondingSection = peFormat.findSectionByRVA(exportDirectoryEntry.VirtualAddress);
                    if (correspondingSection != null) {
                        long offsetInSection = exportDirectoryEntry.VirtualAddress - correspondingSection.sectionHeader.virtualAddress;
                        long offsetInFile = correspondingSection.sectionHeader.pointerToRawData + offsetInSection;

                        exportsRowAdder.addRow(exportsPanel, new JLabel("Corresponding section: "), new JLabel(correspondingSection.getName()), makeFiller());
                        exportsRowAdder.addRow(exportsPanel, new JLabel("Offset in section: "), new JLabel(getDecAndHexStr(offsetInSection)), makeFiller());
                        exportsRowAdder.addRow(exportsPanel, new JLabel("Offset from start of file: "), new JLabel(getDecAndHexStr(offsetInFile)), makeFiller());
                        exportsRowAdder.addRow(exportsPanel, new JLabel("Size: "), new JLabel(String.valueOf(exportDirectoryEntry.Size)), makeFiller());

                        exportsRowAdder.addSingleComponentWholeRow(exportsPanel, new JSeparator(), new Insets(5, 5, 5, 5));

                        // todo info about export address table, export names table, export ordinals
                        //peFormat.exports.exportTableEntry.

                        exportsRowAdder.addSingleComponentWholeRow(exportsPanel, new JSeparator(), new Insets(5, 5, 5, 5));

                        DefaultTableModel exportsTableModel = new DefaultTableModel();
                        exportsTableModel.setColumnIdentifiers(new String[]{"Name RVA", "Name section+offset", "Name", "Export address index", "Export address RVA", "Export address section+offset"});

                        for (int i = 0; i < peFormat.exports.numFunctionEntries(); i++) {
                            long exportRvaOrForwarderRva = peFormat.exports.getByOrdinal(i).exportRvaOrForwarderRva;
                            long namePointerRva = peFormat.exports.exportTableEntry.namePointerRva + i * 4;
                            exportsTableModel.addRow(new String[]{
                                    String.valueOf(namePointerRva),
                                    peFormat.findSectionByRVA(namePointerRva).getName() + "+" + String.valueOf(namePointerRva - peFormat.findSectionByRVA(namePointerRva).sectionHeader.virtualAddress),
                                    peFormat.exports.exportedFunctionNames.get(i),
                                    String.valueOf(peFormat.exports.exportedFunctionOrdinalIndexes.get(i)),
                                    String.valueOf(exportRvaOrForwarderRva),
                                    peFormat.findSectionByRVA(exportRvaOrForwarderRva).getName() + "+" + String.valueOf(exportRvaOrForwarderRva - peFormat.findSectionByRVA(exportRvaOrForwarderRva).sectionHeader.virtualAddress)
                            });
                        }

                        JTable exportsTable = new JTable(exportsTableModel);

                        JScrollPane exportsTableScrollPane = new JScrollPane(exportsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                        exportsTable.setFillsViewportHeight(true);

                        exportsRowAdder.addSingleComponentWholeRow(exportsPanel, exportsTableScrollPane, new Insets(5, 5, 5, 5));

                        exportsRowAdder.addBottomFillerTo(exportsPanel);

                        exportsPanel.setBackground(UIManager.getColor("List.background"));
                        exportsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    }

                    contentScrollPane.setViewportView(exportsPanel);

                } else if (path != null && path.getLastPathComponent() == importsNode) {

                    JPanel importsPanel = new JPanel();

                    importsPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder importsRowAdder = new GridBagByRowAdder(col1, col2, col4);
                    
                    DirectoryEntry importDirectoryEntry = peFormat.getDirectoryEntry(PEFormat.IMPORTS_INDEX);
                    Section correspondingSection = peFormat.findSectionByRVA(importDirectoryEntry.VirtualAddress);
                    if (correspondingSection != null) {
                        long offsetInSection = importDirectoryEntry.VirtualAddress - correspondingSection.sectionHeader.virtualAddress;
                        long offsetInFile = correspondingSection.sectionHeader.pointerToRawData + offsetInSection;

                        importsRowAdder.addRow(importsPanel, new JLabel("Corresponding section: "), new JLabel(correspondingSection.getName()), makeFiller());
                        importsRowAdder.addRow(importsPanel, new JLabel("Offset in section: "), new JLabel(getDecAndHexStr(offsetInSection)), makeFiller());
                        importsRowAdder.addRow(importsPanel, new JLabel("Offset from start of file: "), new JLabel(getDecAndHexStr(offsetInFile)), makeFiller());
                        importsRowAdder.addRow(importsPanel, new JLabel("Size: "), new JLabel(String.valueOf(importDirectoryEntry.Size)), makeFiller());

                        importsRowAdder.addSingleComponentWholeRow(importsPanel, new JSeparator(), new Insets(5, 5, 5, 5));

                        DefaultTableModel importsTableModel = new DefaultTableModel();
                        importsTableModel.setColumnIdentifiers(new String[]{"Name RVA", "Name section+offset", "Name", "Import lookup table RVA", "Import lookup table section+offset", "Import address table RVA", "Import address table section+offset"});

                        for (ImportBlock importEntry : peFormat.imports) {
                            ImportDirectoryTableEntry importTableEntry = importEntry.importTableEntry;
                            importsTableModel.addRow(new String[]{
                                    String.valueOf(importTableEntry.nameRva),
                                    peFormat.findSectionByRVA(importTableEntry.nameRva).getName() + "+" + String.valueOf(importTableEntry.nameRva - peFormat.findSectionByRVA(importTableEntry.nameRva).sectionHeader.virtualAddress),
                                    importEntry.name,
                                    String.valueOf(importTableEntry.importLookupTableRva),
                                    peFormat.findSectionByRVA(importTableEntry.importLookupTableRva).getName() + "+" + String.valueOf(importTableEntry.importLookupTableRva - peFormat.findSectionByRVA(importTableEntry.importLookupTableRva).sectionHeader.virtualAddress),
                                    String.valueOf(importTableEntry.importAddressTableRva),
                                    peFormat.findSectionByRVA(importTableEntry.importAddressTableRva).getName() + "+" + String.valueOf(importTableEntry.importAddressTableRva - peFormat.findSectionByRVA(importTableEntry.importAddressTableRva).sectionHeader.virtualAddress),
                            });
                        }

                        JTable importsTable = new JTable(importsTableModel);

                        JScrollPane importsTableScrollPane = new JScrollPane(importsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                        importsTable.setFillsViewportHeight(true);

                        importsRowAdder.addSingleComponentWholeRow(importsPanel, importsTableScrollPane, new Insets(5, 5, 5, 5));

                        importsRowAdder.addBottomFillerTo(importsPanel);

                        importsPanel.setBackground(UIManager.getColor("List.background"));
                        importsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    }

                    contentScrollPane.setViewportView(importsPanel);
                    
                } else if (path != null && importBlockNodes.containsKey(path.getLastPathComponent())) {
                    ImportBlock importBlock = importBlockNodes.get(path.getLastPathComponent());

                    JPanel importBlockPanel = new JPanel();

                    importBlockPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder importFunctionsRowAdder = new GridBagByRowAdder(col1, col2, col4);

                    importFunctionsRowAdder.addRow(importBlockPanel, new JLabel("Name: "), new JLabel(importBlock.name), makeFiller());
                    ImportDirectoryTableEntry importTableEntry = importBlock.importTableEntry;
                    Section lookupTableSection = peFormat.findSectionByRVA(importTableEntry.importLookupTableRva);
                    importFunctionsRowAdder.addRow(importBlockPanel, new JLabel("Import lookup table section + offset: "), new JLabel(lookupTableSection.getName() + "+" + String.valueOf(importTableEntry.importLookupTableRva - lookupTableSection.sectionHeader.virtualAddress)), makeFiller());
                    importFunctionsRowAdder.addRow(importBlockPanel, new JLabel("Import lookup table num entries: "), new JLabel(String.valueOf(importBlock.numEntries())), makeFiller());
                    importFunctionsRowAdder.addRow(importBlockPanel, new JLabel("Import lookup table size: "), new JLabel(String.valueOf(importBlock.sizeInBytes())), makeFiller());
                    Section addressTableSection = peFormat.findSectionByRVA(importTableEntry.importAddressTableRva);
                    importFunctionsRowAdder.addRow(importBlockPanel, new JLabel("Import address table section + offset: "), new JLabel(addressTableSection.getName() + "+" + String.valueOf(importTableEntry.importAddressTableRva - addressTableSection.sectionHeader.virtualAddress)), makeFiller());

                    DefaultTableModel importedFunctionsTableModel = new DefaultTableModel();
                    importedFunctionsTableModel.setColumnIdentifiers(new String[]{"Name"});

                    // TODO replace this with detailed data with ordinal/name indicator
                    List<String> importedFunctions = importBlock.resolvedImportedFunctions;
                    for (String importedFunction : importedFunctions) {
                        importedFunctionsTableModel.addRow(new String[]{
                                importedFunction
                        });
                    }

                    JTable importedFunctionsTable = new JTable(importedFunctionsTableModel);

                    JScrollPane importedFunctionsTableScrollPane = new JScrollPane(importedFunctionsTable, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_ALWAYS);
                    importedFunctionsTable.setFillsViewportHeight(true);

                    importFunctionsRowAdder.addSingleComponentWholeRow(importBlockPanel, importedFunctionsTableScrollPane, new Insets(5, 5, 5, 5));
                    
                    importFunctionsRowAdder.addBottomFillerTo(importBlockPanel);

                    importBlockPanel.setBackground(UIManager.getColor("List.background"));
                    importBlockPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));


                    contentScrollPane.setViewportView(importBlockPanel);

                } else if (path != null && path.getLastPathComponent() == relocationsNode) {

                    JPanel relocationsPanel = new JPanel();

                    relocationsPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder relocationsRowAdder = new GridBagByRowAdder(col1, col2, col4);

                    DirectoryEntry relocsDirectoryEntry = peFormat.getDirectoryEntry(PEFormat.RELOCS_INDEX);
                    Section correspondingSection = peFormat.findSectionByRVA(relocsDirectoryEntry.VirtualAddress);
                    if (correspondingSection != null) {
                        long offsetInSection = relocsDirectoryEntry.VirtualAddress - correspondingSection.sectionHeader.virtualAddress;
                        long offsetInFile = correspondingSection.sectionHeader.pointerToRawData + offsetInSection;

                        relocationsRowAdder.addRow(relocationsPanel, new JLabel("Corresponding section: "), new JLabel(correspondingSection.getName()), makeFiller());
                        relocationsRowAdder.addRow(relocationsPanel, new JLabel("Offset in section: "), new JLabel(getDecAndHexStr(offsetInSection)), makeFiller());
                        relocationsRowAdder.addRow(relocationsPanel, new JLabel("Offset from start of file: "), new JLabel(getDecAndHexStr(offsetInFile)), makeFiller());
                        relocationsRowAdder.addRow(relocationsPanel, new JLabel("Size: "), new JLabel(String.valueOf(relocsDirectoryEntry.Size)), makeFiller());
                        relocationsRowAdder.addRow(relocationsPanel, new JLabel("Number of blocks "), new JLabel(String.valueOf(peFormat.baseRelocations.size())), makeFiller());

                        relocationsRowAdder.addBottomFillerTo(relocationsPanel);

                        relocationsPanel.setBackground(UIManager.getColor("List.background"));
                        relocationsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    }

                    contentScrollPane.setViewportView(relocationsPanel);
                    
                } else if (path != null && relocationsBlockNodes.containsKey(path.getLastPathComponent())) {

                    JPanel relocationsBlockPanel = new JPanel();

                    relocationsBlockPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder relocationsBlockRowAdder = new GridBagByRowAdder(col1, col2, col4);
                    
                    BaseRelocationsBlock block = relocationsBlockNodes.get(path.getLastPathComponent());
                    Section correspondingSection = peFormat.findSectionByRVA(block.pageRva);
                    if (correspondingSection != null) {
                        long offsetInSection = block.pageRva - correspondingSection.sectionHeader.virtualAddress;
                        long offsetInFile = correspondingSection.sectionHeader.pointerToRawData + offsetInSection;

                        relocationsBlockRowAdder.addRow(relocationsBlockPanel, new JLabel("Page RVA: "), new JLabel(String.valueOf(block.pageRva)), makeFiller());
                        relocationsBlockRowAdder.addRow(relocationsBlockPanel, new JLabel("Corresponding section: "), new JLabel(correspondingSection.getName()), makeFiller());
                        relocationsBlockRowAdder.addRow(relocationsBlockPanel, new JLabel("Offset in section: "), new JLabel(getDecAndHexStr(offsetInSection)), makeFiller());
                        relocationsBlockRowAdder.addRow(relocationsBlockPanel, new JLabel("Offset from start of file: "), new JLabel(getDecAndHexStr(offsetInFile)), makeFiller());
                        relocationsBlockRowAdder.addRow(relocationsBlockPanel, new JLabel("Size: "), new JLabel(String.valueOf(block.getSize())), makeFiller());
                        relocationsBlockRowAdder.addRow(relocationsBlockPanel, new JLabel("Number of entries "), new JLabel(String.valueOf(block.entries.size())), makeFiller());

                        relocationsBlockRowAdder.addBottomFillerTo(relocationsBlockPanel);

                        relocationsBlockPanel.setBackground(UIManager.getColor("List.background"));
                        relocationsBlockPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    }

                    contentScrollPane.setViewportView(relocationsBlockPanel);
                    
                    
                } else if (path != null && path.getLastPathComponent() == sectionsNode) {

                    JPanel sectionsDataOverviewPanel = new JPanel();

                    sectionsDataOverviewPanel.setLayout(new BorderLayout());

                    DefaultTableModel sectionsTableModel = new DefaultTableModel() {
                        @Override
                        public boolean isCellEditable(int row, int column) {
                            return false;
                        }
                    };
                    sectionsTableModel.setColumnIdentifiers(new String[] {"Name", "Offset in file", "Size in file", "End in file", "Start RVA", "Virtual size", "End RVA"});
                    refreshSectionsOverviewTable(sectionsTableModel, peFormat, displayFormatMap);
                    
                    JTable sectionsLayoutTable = new JTable(sectionsTableModel);
                    sectionsLayoutTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

                    {
                        JPopupMenu popup = new JPopupMenu();

                        // instead of sectionsLayoutTable.setComponentPopupMenu(popup) we will provide our own mouse listener,
                        // which will select row and cell even for right-click before showing context popup menu
                        sectionsLayoutTable.addMouseListener(new MouseAdapter() {
                            
                            @Override
                            public void mouseReleased(MouseEvent e) {
                                
                                int r = sectionsLayoutTable.rowAtPoint(e.getPoint());
                                if (r >= 0 && r < sectionsLayoutTable.getRowCount()) {
                                    if (!sectionsLayoutTable.isRowSelected(r)) {
                                        int c = sectionsLayoutTable.columnAtPoint(e.getPoint());
                                        if (c >= 0 && c < sectionsLayoutTable.getColumnCount()) {
                                            sectionsLayoutTable.changeSelection(r, c, false, false);
                                        } else {
                                            sectionsLayoutTable.clearSelection();
                                        }
                                    }
                                } else {
                                    sectionsLayoutTable.clearSelection();
                                }

                                if (sectionsLayoutTable.getSelectedRow() < 0) {
                                    return;
                                }
                                if (e.isPopupTrigger()) {
                                    popup.show(e.getComponent(), e.getX(), e.getY());
                                }
                            }
                        });
                        
                        popup.addPopupMenuListener(new PopupMenuListener() {

                            private JMenu formats;

                            @Override
                            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                                formats = addTableDisplayFormatsMenu(columnName(sectionsTableModel,sectionsLayoutTable.getSelectedColumn()), "SECTIONS_", displayFormatMap, sectionsTableModel);
                                popup.add(formats);
                            }

                            @Override
                            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
                                popup.remove(formats);
                            }

                            @Override
                            public void popupMenuCanceled(PopupMenuEvent e) {
                            }
                        });

                    }

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
                    
                } else if (path != null && path.getLastPathComponent() == resourcesRootNode) {

                    JPanel resourcesPanel = new JPanel();

                    resourcesPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder resourcesRowAdder = new GridBagByRowAdder(col1, col2, col4);

                    DirectoryEntry resourcesDirectoryEntry = peFormat.getDirectoryEntry(PEFormat.RESOURCES_INDEX);
                    Section correspondingSection = peFormat.findSectionByRVA(resourcesDirectoryEntry.VirtualAddress);
                    if (correspondingSection != null) {
                        long offsetInSection = resourcesDirectoryEntry.VirtualAddress - correspondingSection.sectionHeader.virtualAddress;
                        long offsetInFile = correspondingSection.sectionHeader.pointerToRawData + offsetInSection;

                        resourcesRowAdder.addRow(resourcesPanel, new JLabel("Corresponding section: "), new JLabel(correspondingSection.getName()), makeFiller());
                        resourcesRowAdder.addRow(resourcesPanel, new JLabel("Offset in section: "), new JLabel(getDecAndHexStr(offsetInSection)), makeFiller());
                        resourcesRowAdder.addRow(resourcesPanel, new JLabel("Offset from start of file: "), new JLabel(getDecAndHexStr(offsetInFile)), makeFiller());
                        resourcesRowAdder.addRow(resourcesPanel, new JLabel("Size: "), new JLabel(String.valueOf(resourcesDirectoryEntry.Size)), makeFiller());

                        resourcesRowAdder.addSingleComponentWholeRow(resourcesPanel, new JSeparator(), new Insets(5, 5, 5, 5));

                        resourcesRowAdder.addBottomFillerTo(resourcesPanel);

                        resourcesPanel.setBackground(UIManager.getColor("List.background"));
                        resourcesPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
                    }


                    contentScrollPane.setViewportView(resourcesPanel);
                    
                } else if (path != null && resourceNodes.containsKey(path.getLastPathComponent())) {
                    ResourceEntry entry = resourceNodes.get((DefaultMutableTreeNode)path.getLastPathComponent());
                    JPanel resourcePanel = new JPanel();
                    resourcePanel.setLayout(new GridBagLayout());
                    GridBagByRowAdder resourcesRowAdder = new GridBagByRowAdder(col1, col2, col4);
                    if (entry.dataEntry != null) {
                        resourcesRowAdder.addRow(resourcePanel, new JLabel("Name or ID: "), new JLabel(String.valueOf(entry.getEntryName(false))), makeFiller());
                        resourcesRowAdder.addRow(resourcePanel, new JLabel("Type: "), new JLabel("Entry"), makeFiller());
                        resourcesRowAdder.addRow(resourcePanel, new JLabel("Data RVA: "), new JLabel(getDecAndHexStr(entry.dataEntry.dataRva)), makeFiller());
                        Section correspondingSection = peFormat.findSectionByRVA(entry.dataEntry.dataRva);
                        if (correspondingSection != null) {
                            long offsetInSection = entry.dataEntry.dataRva - correspondingSection.sectionHeader.virtualAddress;
                            long offsetInFile = correspondingSection.sectionHeader.pointerToRawData + offsetInSection;
                            resourcesRowAdder.addRow(resourcePanel, new JLabel("Corresponding section: "), new JLabel(correspondingSection.getName()), makeFiller());
                            resourcesRowAdder.addRow(resourcePanel, new JLabel("Offset in section: "), new JLabel(getDecAndHexStr(offsetInSection)), makeFiller());
                            resourcesRowAdder.addRow(resourcePanel, new JLabel("Offset from start of file: "), new JLabel(getDecAndHexStr(offsetInFile)), makeFiller());
                        }
                        resourcesRowAdder.addRow(resourcePanel, new JLabel("Size: "), new JLabel(String.valueOf(entry.dataEntry.size)), makeFiller());
                    } else {
                        resourcesRowAdder.addRow(resourcePanel, new JLabel("Name or ID: "), new JLabel(String.valueOf(entry.getEntryName(false))), makeFiller());
                        resourcesRowAdder.addRow(resourcePanel, new JLabel("Type: "), new JLabel("Directory"), makeFiller());
                        resourcesRowAdder.addRow(resourcePanel, new JLabel("Num of entries: "), new JLabel(String.valueOf(entry.resolvedSubDirectory.entries.size())), makeFiller());
                    }
                    resourcesRowAdder.addBottomFillerTo(resourcePanel);

                    resourcePanel.setBackground(UIManager.getColor("List.background"));
                    resourcePanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(resourcePanel);

                }
                
            }

            public void refreshSectionsOverviewTable(DefaultTableModel tableModel, PEFormat peFormat, Map<Object, DisplayFormat> displayFormatMap) {
                tableModel.setNumRows(0);
                String prefix = "SECTIONS_";
                for (Section section : peFormat.sections) {
                    tableModel.addRow(new String[] {
                            section.getName(),
                            getDecAndHexStr(section.getOffsetInFile(), displayFormatMap.getOrDefault(prefix + columnName(tableModel, 1), DisplayFormat.DEFAULT)),
                            getDecAndHexStr(section.getSizeInFile(), displayFormatMap.getOrDefault(prefix + columnName(tableModel, 2), DisplayFormat.DEFAULT)),
                            getDecAndHexStr(section.getOffsetInFile() + section.getSizeInFile(), displayFormatMap.getOrDefault(prefix + columnName(tableModel, 3), DisplayFormat.DEFAULT)),
                            getDecAndHexStr(section.sectionHeader.virtualAddress, displayFormatMap.getOrDefault(prefix + columnName(tableModel, 4), DisplayFormat.DEFAULT)),
                            getDecAndHexStr(section.sectionHeader.physicalAddressOrVirtualSize, displayFormatMap.getOrDefault(prefix + columnName(tableModel, 5), DisplayFormat.DEFAULT)),
                            getDecAndHexStr(section.sectionHeader.virtualAddress + section.sectionHeader.physicalAddressOrVirtualSize, displayFormatMap.getOrDefault(prefix + columnName(tableModel, 6), DisplayFormat.DEFAULT))
                    });
                }
            }

            public String columnName(DefaultTableModel sectionsTableModel, int index) {
                return sectionsTableModel.getColumnName(index).toLowerCase().replace(' ','_');
            }

            public JMenuItem addFieldInfoMenu(final IntrospectableFormat header, final String fieldName, int formatStartOffset, String prefix, final Map<Object, DisplayFormat> displayFormatMap) {
                JMenuItem fieldInfoItem = new JMenuItem("Field info...");

                fieldInfoItem.setAction(new AbstractAction("Field info...") {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        showFieldInfoDialog(header, fieldName, formatStartOffset, prefix, displayFormatMap);
                    }
                });

                return fieldInfoItem;
            }

            public JMenu addTableDisplayFormatsMenu(final String fieldName, String prefix, final Map<Object, DisplayFormat> displayFormatMap, DefaultTableModel sectionsTableModel) {
                JMenu subMenu = new JMenu("Display column as");
                DisplayFormat[] displayFormats = new DisplayFormat[] {DisplayFormat.DEFAULT, DisplayFormat.DEC_NUMBER, DisplayFormat.HEX_NUMBER};

                for (DisplayFormat displayFormat : displayFormats) {
                    JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(new AbstractAction(displayFormat.name()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            displayFormatMap.put(prefix + fieldName, displayFormat);
                            refreshSectionsOverviewTable(sectionsTableModel, peFormat, displayFormatMap);
                        }
                    });
                    subMenu.add(menuItem);


                }

                subMenu.addMenuListener(new MenuListener() {
                    @Override
                    public void menuSelected(MenuEvent e) {

                        DisplayFormat lastSelection = displayFormatMap.getOrDefault(prefix + fieldName, DisplayFormat.DEFAULT);

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

            public JMenu addDisplayFormatsMenu(final IntrospectableFormat header, final String fieldName, final JTextField valueTextField, String prefix, final Map<Object, DisplayFormat> displayFormatMap) {
                JMenu subMenu = new JMenu("Display as");
                DisplayFormat[] displayFormats = IntrospectableFormat.filterSupported(header, fieldName, DisplayFormat.values());
                
                for (DisplayFormat displayFormat : displayFormats) {
                    JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(new AbstractAction(displayFormat.name()) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            displayFormatMap.put(prefix + fieldName, displayFormat);
                            valueTextField.setText(header.getStringValue(fieldName, displayFormat).get());
                        }
                    });
                    subMenu.add(menuItem);
                    

                }
                
                subMenu.addMenuListener(new MenuListener() {
                    @Override
                    public void menuSelected(MenuEvent e) {

                        DisplayFormat lastSelection = displayFormatMap.getOrDefault(prefix + fieldName, DisplayFormat.DEFAULT);
                        
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

            public void showFieldInfoDialog(IntrospectableFormat format, String fieldName, int formatOffset, String prefix, final Map<Object, DisplayFormat> displayFormatMap) {
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
                int fieldAbsOffset = format.getOffset(fieldName) + formatOffset;
                JTextField offsetText = new JTextField(String.valueOf(fieldAbsOffset) + " (" + "0x" + Long.toHexString(fieldAbsOffset) + ")", 20);
                offsetText.setEditable(false);
                fieldInfoAdder.addRow(fieldInfoPanel, new JLabel("Field offset from start of file: "), offsetText, makeFiller());
                
                fieldInfoAdder.addSingleComponentWholeRow(fieldInfoPanel, new JSeparator(), new Insets(5, 5, 5, 5));

                JComboBox<DisplayFormat> displayFormatComboBox = new JComboBox<>(new DefaultComboBoxModel<DisplayFormat>(IntrospectableFormat.filterSupported(format, fieldName, DisplayFormat.values())));
                DisplayFormat selectedDisplayFormat = displayFormatMap.getOrDefault(prefix + fieldName, DisplayFormat.DEFAULT);
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

    public String getDecAndHexStr(long val) {
        return getDecAndHexStr(val, DisplayFormat.DEFAULT);
    }

    public String getDecAndHexStr(long val, DisplayFormat format) {
        if (format == DisplayFormat.DEFAULT) {
            return String.valueOf(val) + " (" + "0x" + Long.toHexString(val) + ") ";
        } else if (format == DisplayFormat.DEC_NUMBER) {
            return String.valueOf(val);
        } else if (format == DisplayFormat.HEX_NUMBER) {
            return "0x" + Long.toHexString(val);
        } else {
            throw new UnsupportedOperationException();
        }
    }

    public void createLIBFormatRenderer(Map<Object, DisplayFormat> displayFormatMap,  ARFormat arFormat, SeekableByteChannel fileChannel) {



        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();

        DefaultMutableTreeNode libNode = new DefaultMutableTreeNode("LIB");
        root.add(libNode);

        IdentityHashMap<DefaultMutableTreeNode, Integer> fileNodes = new IdentityHashMap<>();
        for (int i = 0; i < arFormat.itemHeaders.size(); i++ ) {
            ARFormat.FileHeader item = arFormat.itemHeaders.get(i);
            String name = item.fileIdentifier;
            DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(name);
            fileNodes.put(fileNode, i);
            libNode.add(fileNode);
        }

        treeModel.reload();

        tree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                TreePath path = e.getNewLeadSelectionPath();

                if (path != null && path.getLastPathComponent() == libNode) {

                    JPanel libInfoPanel = new JPanel();

                    libInfoPanel.setLayout(new GridBagLayout());

                    GridBagByRowAdder libraryAdder = new GridBagByRowAdder(col1, col2, col4);

//                    int dosTotalSize = (dosHeader.nBlocks - 1) * 512 + dosHeader.lastSize;
//                    dosInfoAdder.addRow(libInfoPanel, new JLabel("DOS header size: "), new JLabel(String.valueOf(dosHeader.getDeclaredHeaderSize())), makeFiller());
//                    dosInfoAdder.addRow(libInfoPanel, new JLabel("DOS payload size: "), new JLabel(String.valueOf(dosTotalSize - dosHeader.getDeclaredHeaderSize())), makeFiller());
//                    dosInfoAdder.addRow(libInfoPanel, new JLabel("DOS exe total size: "), new JLabel(String.valueOf(dosTotalSize)), makeFiller());

                    libraryAdder.addBottomFillerTo(libInfoPanel);
                    libInfoPanel.setBackground(UIManager.getColor("List.background"));

                    libInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                    contentScrollPane.setViewportView(libInfoPanel);

                    
                } else if (path != null && fileNodes.containsKey(path.getLastPathComponent()) )  {

                    JPanel libraryEntryPanel = new JPanel();
                    libraryEntryPanel.setLayout(new GridBagLayout());
                    GridBagByRowAdder libraryEntryAdder = new GridBagByRowAdder(col1, col2, col4);
                    
                    try {
                        int fileIndex = fileNodes.get(path.getLastPathComponent());
                        long dataOffset = arFormat.itemDataOffsets.get(fileIndex);
                        int dataSize = arFormat.itemHeaders.get(fileIndex).fileSize;

                        ByteBuffer fileData = ByteBuffer.allocate(dataSize);
                        fileChannel.position(dataOffset);
                        fileChannel.read(fileData);
                        fileData.flip();
                        COFFHeader coffHeader = new COFFHeader();
                        coffHeader.readFrom(fileData);
                        
                        if (ImportHeader.canBeCreatedFrom(coffHeader)) {
                            fileData.clear();
                            ImportFormat importFormat = new ImportFormat();
                            importFormat.readFrom(fileData);

                            for (String fieldName : new String[] {ImportHeader.Field.SIZE_OF_DATA.name(), ImportHeader.Field.ORDINAL_OR_HINT.name(), ImportHeader.Field.IMPORT_TYPE.name(), ImportHeader.Field.NAME_TYPE.name()}) {

                                JLabel nameLabel = new JLabel(fieldName + ":");

                                String tooltipText = "<html>Offset:" + importFormat.header.getOffset(fieldName) + "<br>" + "Size:" + importFormat.header.getSize(fieldName) + "</html>";

                                nameLabel.setToolTipText(tooltipText);

                                // JLabel offsetLabel = new JLabel("(offset:" + format.coffHeader.getOffset(fieldName) + ";" + "size:" + format.coffHeader.getSize(fieldName) + ")");
                                // JLabel valueLabel = new JLabel(format.header.getStringValue(fieldName));
                                DisplayFormat selectedDisplayFormat = displayFormatMap.getOrDefault(fieldName, DisplayFormat.DEFAULT);
                                JTextField valueTextField = new JTextField(importFormat.header.getStringValue(fieldName, selectedDisplayFormat).get(), 30);
                                // valueTextField.setEnabled(false);
                                valueTextField.setEditable(false);
                                valueTextField.setToolTipText(tooltipText);

//                                {
//                                    JPopupMenu popup = new JPopupMenu();
//                                    valueTextField.setComponentPopupMenu(popup);
//
//                                    JMenuItem fieldInfoItem = addFieldInfoMenu(coffHeader, fieldName, (int) peFormat.getCOFFHeaderOffset(), displayFormatMap);
//                                    popup.add(fieldInfoItem);
//
//                                    JMenu subMenu = addDisplayFormatsMenu(coffHeader, fieldName, valueTextField, displayFormatMap);
//                                    popup.add(subMenu);
//
//                                }

                                libraryEntryAdder.addRow(libraryEntryPanel, nameLabel, valueTextField, makeFiller());

                            }
                            
                            JTextField dllNameField = new JTextField(importFormat.dllName, 30);
                            // dllNameField.setEnabled(false);
                            dllNameField.setEditable(false);

                            JTextField importedSymbolField = new JTextField(importFormat.importedSymbol, 30);
                            // importedSymbolField.setEnabled(false);
                            importedSymbolField.setEditable(false);



                            libraryEntryAdder.addRow(libraryEntryPanel, new JLabel("DLL name: "), dllNameField, makeFiller());
                            libraryEntryAdder.addRow(libraryEntryPanel, new JLabel("Function name: "), importedSymbolField, makeFiller());
                            
                        }

                        libraryEntryAdder.addBottomFillerTo(libraryEntryPanel);
                        libraryEntryPanel.setBackground(UIManager.getColor("List.background"));

                        libraryEntryPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                        contentScrollPane.setViewportView(libraryEntryPanel);
                        
                        
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });


    }

    public void processChildren(ResourceEntry directory, DefaultMutableTreeNode directoryNode, IdentityHashMap<DefaultMutableTreeNode, ResourceEntry> resourceNodes, int level) {
        if (directory.directoryEntry != null) {
            for (ResourceEntry entry : directory.resolvedSubDirectory.entries) {
                String name = entry.getEntryName(level == 1);
                DefaultMutableTreeNode entryNode = new DefaultMutableTreeNode(name);
                resourceNodes.put(entryNode, entry);
                directoryNode.add(entryNode);
                if (entry.resolvedSubDirectory != null) {
                    processChildren(entry, entryNode, resourceNodes, level + 1);
                }
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
