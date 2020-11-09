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
import llj.util.swing.GridBagByRowAdder;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
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

    }

    public void setFile(File file) {
        this.file = file;

        try {
            Set<OpenOption> opts = new HashSet<OpenOption>();
            opts.add(StandardOpenOption.READ);
            FileChannel fileChannel = FileChannel.open(file.toPath(), opts);

            Map<Object, DisplayFormat> displayFormatMap = new HashMap<>();

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

            for (TreeSelectionListener listener: tree.getTreeSelectionListeners()) {
                tree.removeTreeSelectionListener(listener);
            }

            DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
            root.setUserObject(file.getName());

//            tree.addTreeSelectionListener(new TreeSelectionListener() {
//                @Override
//                public void valueChanged(TreeSelectionEvent e) {
//                    if (e.getNewLeadSelectionPath().getLastPathComponent() == root) {
//                        contentScrollPane.setViewportView(null);
//                    }
//                }
//            });
            
            DefaultMutableTreeNode dosExeNode = new DefaultMutableTreeNode("DOS EXE");
            root.add(dosExeNode);

            DefaultMutableTreeNode dosHeaderNode = new DefaultMutableTreeNode("DOS EXE header");
            dosExeNode.add(dosHeaderNode);

            DefaultMutableTreeNode dosPayloadNode = new DefaultMutableTreeNode("DOS EXE data");
            dosExeNode.add(dosPayloadNode);
            
            DOSHeader<?> dosHeader = peFormat == null ? dosExeFormat.header : peFormat.dosHeader;

            DefaultMutableTreeNode peNode;

            DefaultMutableTreeNode peHeadersNode;
            
            DefaultMutableTreeNode coffHeaderNode;
            DefaultMutableTreeNode coffOptionalHeaderNode;
            DefaultMutableTreeNode sectionHeadersNode;
            IdentityHashMap<TreeNode, Section> sectionHeaderNodes;
            DefaultMutableTreeNode sectionsNode;
            IdentityHashMap<TreeNode, Section> sectionNodes;
            DefaultMutableTreeNode coffRelocationsNode;
            DefaultMutableTreeNode coffSymbolsNode;
            DefaultMutableTreeNode coffStringsNode;
            
            String fileFormatDescription;
            if (peFormat != null) {

                fileFormatDescription = "Windows PE";

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
                
                coffRelocationsNode = new DefaultMutableTreeNode("COFF relocations");
                peNode.add(coffRelocationsNode);

                coffSymbolsNode = new DefaultMutableTreeNode("COFF symbols");
                peNode.add(coffSymbolsNode);

                coffStringsNode =  new DefaultMutableTreeNode("COFF strings");
                peNode.add(coffStringsNode);
                
            } else {
                
                fileFormatDescription = "DOS EXE";
                peNode = null;
                peHeadersNode = null;
                coffHeaderNode = null;
                coffOptionalHeaderNode = null;
                sectionHeadersNode = null;
                sectionHeaderNodes = null;

                sectionsNode = null;
                sectionNodes = null;

                coffRelocationsNode = null;
                coffSymbolsNode = null;
                coffStringsNode = null;
            }
                
            Insets defaultInsets = new Insets(2, 5, 2, 5);

            GridBagConstraints col1 = new GridBagConstraints();
            col1.fill = GridBagConstraints.NONE;
            col1.anchor = GridBagConstraints.EAST;
            col1.insets = defaultInsets;
            col1.weightx = 0;
            col1.weighty = 0;

            GridBagConstraints col2 = new GridBagConstraints();
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

            GridBagConstraints col4 = new GridBagConstraints();
            col4.gridx = 3;
            col4.fill = GridBagConstraints.HORIZONTAL;
            col4.anchor = GridBagConstraints.NORTHWEST;
            col4.insets = defaultInsets;
            col4.weightx = 1;
            col4.weighty = 0;
            

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

                    } else if (path != null && path.getLastPathComponent() == dosExeNode) {
                        
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
                            JTextField valueTextField = new JTextField(dosHeader.getStringValue(fieldName, DisplayFormat.DEFAULT).get(), 10);
                            // valueTextField.setEnabled(false);
                            valueTextField.setEditable(false);

                            {
                                JPopupMenu popup = new JPopupMenu();
                                valueTextField.setComponentPopupMenu(popup);

                                JMenu subMenu = addDisplayFormatsMenu(dosHeader, fieldName, valueTextField);
                                popup.add(subMenu);

                            }
                            

                            JComponent filler = makeFiller();

                            dosExeRowAdder.addRow(dosExeFieldsPanel, nameLabel, valueTextField, filler); // offsetLabel


                        }

                        dosExeRowAdder.addBottomFillerTo(dosExeFieldsPanel);
                        dosExeFieldsPanel.setBackground(UIManager.getColor("List.background"));
                        
                        // dosExeFieldsPanel.setBorder(BorderFactory.createTitledBorder("Fields"));
                        dosExeGeneralPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

                        Box box = Box.createVerticalBox();
                        box.add(dosExeGeneralPanel);
                        // box.add(dosExeFieldsPanel);

                        box.setBackground(UIManager.getColor("List.background"));
                        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                        contentScrollPane.setViewportView(box);
                    } else if (path != null && path.getLastPathComponent() == peNode) {

                        JPanel peInfoPanel = new JPanel();

                        peInfoPanel.setLayout(new GridBagLayout());

                        GridBagByRowAdder fileInfoAdder = new GridBagByRowAdder(col1, col2, col4);

                        fileInfoAdder.addRow(peInfoPanel, new JLabel("PE offset: "), new JLabel(String.valueOf(peFormat.getPEOffset())), makeFiller());
                        fileInfoAdder.addRow(peInfoPanel, new JLabel("PE headers total size: "), new JLabel(String.valueOf(peFormat.getPEHeadersTotalSize() + peFormat.getSectionHeadersSize())), makeFiller());

                        fileInfoAdder.addBottomFillerTo(peInfoPanel);
                        peInfoPanel.setBackground(UIManager.getColor("List.background"));

                        peInfoPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

                        contentScrollPane.setViewportView(peInfoPanel);
                        
                    } else if (path != null && path.getLastPathComponent() == peHeadersNode) {

                        JPanel peInfoPanel = new JPanel();

                        peInfoPanel.setLayout(new GridBagLayout());

                        GridBagByRowAdder fileInfoAdder = new GridBagByRowAdder(col1, col2, col4);

                        fileInfoAdder.addRow(peInfoPanel, new JLabel("PE headers offset: "), new JLabel(String.valueOf(peFormat.getPEOffset())), makeFiller());
                        fileInfoAdder.addRow(peInfoPanel, new JLabel("PE headers size: "), new JLabel(String.valueOf(peFormat.getPEHeadersTotalSize())), makeFiller());
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
                        for (String fieldName: coffOptionalHeader.getNames()) {

                            JLabel nameLabel = new JLabel(fieldName + ":");

                            // JLabel offsetLabel = new JLabel("(offset:" + format.coffHeader.getOffset(fieldName) + ";" + "size:" + format.coffHeader.getSize(fieldName) + ")");
                            // JLabel valueLabel = new JLabel(format.header.getStringValue(fieldName));
                            JTextField valueTextField = new JTextField(coffOptionalHeader.getStringValue(fieldName, DisplayFormat.DEFAULT).get(), 10);
                            // valueTextField.setEnabled(false);
                            valueTextField.setEditable(false);

                            JComponent filler = makeFiller();

                            coffRowAdder.addRow(coffOptionalFieldsPanel, nameLabel, valueTextField, filler); // offsetLabel


                        }

                        coffRowAdder.addBottomFillerTo(coffOptionalFieldsPanel);
                        coffOptionalFieldsPanel.setBackground(UIManager.getColor("List.background"));
                        coffOptionalFieldsPanel.setBorder(BorderFactory.createTitledBorder("Fields"));

                        Box box = Box.createVerticalBox();
                        box.add(coffOptionalFieldsPanel);

                        box.setBackground(UIManager.getColor("List.background"));
                        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                        contentScrollPane.setViewportView(box);
                        
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
                            JTextField valueTextField = new JTextField(coffHeader.getStringValue(fieldName, displayFormatMap.getOrDefault(fieldName, DisplayFormat.DEFAULT)).get(), 10);
                            // valueTextField.setEnabled(false);
                            valueTextField.setEditable(false);
                            valueTextField.setToolTipText(tooltipText);

                            {
                                JPopupMenu popup = new JPopupMenu();
                                valueTextField.setComponentPopupMenu(popup);
                                
                                JMenu subMenu = addDisplayFormatsMenu(coffHeader, fieldName, valueTextField);
                                popup.add(subMenu);

                            }

                            JComponent filler = makeFiller();

                            coffRowAdder.addRow(coffFieldsPanel, nameLabel, valueTextField, filler); // offsetLabel


                        }

                        coffRowAdder.addBottomFillerTo(coffFieldsPanel);
                        coffFieldsPanel.setBackground(UIManager.getColor("List.background"));
                        coffFieldsPanel.setBorder(BorderFactory.createTitledBorder("Fields"));

                        Box box = Box.createVerticalBox();
                        box.add(coffFieldsPanel);

                        box.setBackground(UIManager.getColor("List.background"));
                        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                        contentScrollPane.setViewportView(box);
                        
                    } else if (path != null && path.getLastPathComponent() == sectionHeadersNode) {

                        JPanel sectionsInfoPanel = new JPanel();

                        sectionsInfoPanel.setLayout(new GridBagLayout());

                        GridBagByRowAdder sectionsAdder = new GridBagByRowAdder(col1, col2, col4);

                        
                        sectionsAdder.addRow(sectionsInfoPanel, new JLabel("Offset: "), new JLabel(String.valueOf(peFormat.getSectionHeadersOffset())), makeFiller());
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

                        for (String fieldName: section.sectionHeader.getNames()) {

                            JLabel nameLabel = new JLabel(fieldName + ":");

                            // JLabel offsetLabel = new JLabel("(offset:" + format.coffHeader.getOffset(fieldName) + ";" + "size:" + format.coffHeader.getSize(fieldName) + ")");
                            // JLabel valueLabel = new JLabel(format.header.getStringValue(fieldName));
                            JTextField valueTextField = new JTextField(section.sectionHeader.getStringValue(fieldName, DisplayFormat.DEFAULT).get(), 10);
                            // valueTextField.setEnabled(false);
                            valueTextField.setEditable(false);

                            JComponent filler = makeFiller();

                            sectionHeaderRowAdder.addRow(sectionFieldsPanel, nameLabel, valueTextField, filler); // offsetLabel


                        }

                        sectionHeaderRowAdder.addBottomFillerTo(sectionFieldsPanel);
                        sectionFieldsPanel.setBackground(UIManager.getColor("List.background"));
                        sectionFieldsPanel.setBorder(BorderFactory.createTitledBorder("Fields"));

                        Box box = Box.createVerticalBox();
                        box.add(sectionFieldsPanel);

                        box.setBackground(UIManager.getColor("List.background"));
                        box.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                        contentScrollPane.setViewportView(box);
                        
                    }
                }

                public JMenu addDisplayFormatsMenu(final IntrospectableFormat header, final String fieldName, final JTextField valueTextField) {
                    JMenu subMenu = new JMenu("Display as");
                    for (DisplayFormat displayFormat : DisplayFormat.values()) {
                        subMenu.add(displayFormat.name()).setAction(new AbstractAction(displayFormat.name()) {
                            @Override
                            public void actionPerformed(ActionEvent e) {
                                displayFormatMap.put(fieldName, displayFormat);
                                valueTextField.setText(header.getStringValue(fieldName, displayFormat).get());
                            }
                        });
                    }
                    return subMenu;
                }

                public JComponent makeFiller() {
                    JComponent filler = new JPanel();
                    filler.setOpaque(false);
                    return filler;
                }
            });

            tree.setEnabled(true);
            tree.expandPath(new TreePath(root));

            tree.repaint();

        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Was unable to read selected file", "File read error", JOptionPane.ERROR_MESSAGE);
        }

    }

    public static void main(String[] args) throws Exception {
        UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        VisualPE instance = new VisualPE();


    }

}
