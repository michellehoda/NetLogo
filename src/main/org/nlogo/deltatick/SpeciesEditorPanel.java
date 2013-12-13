package org.nlogo.deltatick;

import org.nlogo.deltatick.dialogs.ColorComboBoxRenderer;
import org.nlogo.deltatick.dialogs.ShapeSelectorWithoutColor;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.hotlink.dialogs.ShapeIcon;
import org.nlogo.shape.VectorShape;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created with IntelliJ IDEA.
 * User: salilw
 * Date: 10/11/13
 * Time: 11:29 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpeciesEditorPanel extends JPanel {
    // Data
    ArrayList<String> allBreedNames;
    ArrayList<Trait> allTraits;
    TraitPreview traitPreview;
    TraitDisplay traitDisplay;
    JButton okayButton;
    JButton cancelButton;
    // If this is true, a new breedblock will be created.
    boolean makeNewBreedBlock = true;
    // The breed blocks that this panel belongs to
    BreedBlock myBreedBlock;

    // GUI Stuff
    JFrame myFrame;

    SpeciesEditorTopPanel topPanel;
    //JPanel topPanel = new JPanel();
    JPanel midPanel = new JPanel();
    JPanel sidePanel = new JPanel();
    JPanel bottomPanel = new JPanel();

    // Constructor
    public SpeciesEditorPanel(String[] allBreedNames,
                              ArrayList<String []> allBreedSetupNumbers,
                              ArrayList<Trait> traits,
                              JFrame jFrame) {
        this.myFrame = jFrame;
        // Initialize ArrayList<> allBreedNames like below. Arrays.asList doesn't compile on
        // the commandline. Warnings for unchecked case breaks the build
        this.allBreedNames = new ArrayList<String>();
        for (int i = 0; i < allBreedNames.length; i++) {
            this.allBreedNames.add(allBreedNames[i]);
        }
        this.allTraits = new ArrayList<Trait>(traits);
        this.topPanel = new SpeciesEditorTopPanel(allBreedNames, allBreedSetupNumbers);

        traitDisplay = new TraitDisplay(sidePanel, myFrame);

        okayButton = new JButton("Okay");
        cancelButton = new JButton("Cancel");

        // Setup panels
        setupPanels();
    }

    private void setupPanels() {
        // Set up the panels
        setupTopPanel();
        setupSidePanel();
        setupMidPanel();
        setupBottomPanel();

        // Now set the layout
        GroupLayout layout = new GroupLayout(myFrame.getContentPane());
        myFrame.getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(topPanel)
                                        .addComponent(midPanel)
                                )
                                .addComponent(sidePanel)
                        )
                        .addComponent(bottomPanel)

        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(layout.createSequentialGroup()
                                        .addComponent(topPanel)
                                        .addComponent(midPanel)
                                )
                                .addComponent(sidePanel)
                        )
                        .addComponent(bottomPanel)
        );
        myFrame.validate();
        myFrame.pack();

    }
    private void setupTopPanel() {
        topPanel.validate();
    }
    private void setupMidPanel() {
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));

        TitledBorder titleMidPanel;
        titleMidPanel = BorderFactory.createTitledBorder("");
        midPanel.setBorder(titleMidPanel);

        LabelPanel labelPanel = new LabelPanel(new ArrayList<String>());

        traitPreview = new TraitPreview(topPanel.getSelectedBreed(), this.traitDisplay, labelPanel, myFrame);
        traitPreview.setTraits(allTraits);
        traitPreview.setTraitsListListener(new TraitListSelectionHandler());
        traitPreview.setTraitTableModelListener(new TraitTableModelListener());
        traitPreview.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add to midPanel
        midPanel.add(traitPreview);

//        TitledBorder titleLabelPanel;
//        titleLabelPanel = BorderFactory.createTitledBorder("Set Labels           l");
//        labelPanel.setBorder(titleLabelPanel);
//        labelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
//        labelPanel.setMinimumSize(new Dimension(traitPreview.getTotalWidth()-10, LabelPanel.LABELPANEL_HEIGHT));
        labelPanel.setPreferredSize(new Dimension(TraitPreview.TRAITPREVIEW_TOTAL_WIDTH, LabelPanel.LABELPANEL_HEIGHT));
        //midPanel.add(labelPanel);

        midPanel.setPreferredSize(new Dimension(TraitPreview.TRAITPREVIEW_TOTAL_WIDTH, traitPreview.getTotalHeight()));
        midPanel.setMaximumSize(new Dimension(TraitPreview.TRAITPREVIEW_TOTAL_WIDTH, traitPreview.getTotalHeight()));
        midPanel.revalidate();

    }
    private void setupSidePanel() {
        TitledBorder titleSidePanel;
        titleSidePanel = BorderFactory.createTitledBorder("");
        sidePanel.setBorder(titleSidePanel);
        //sidePanel.setPreferredSize(new Dimension(600, 200));

        sidePanel.add(traitDisplay);
        sidePanel.setVisible(false); // testing jframe size
        sidePanel.validate();
    }

    private void setupBottomPanel() {
        GroupLayout layout = new GroupLayout(bottomPanel);
        bottomPanel.setLayout(layout);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGap(10)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(cancelButton)
                                .addComponent(okayButton))
        );

        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .addGroup(layout.createSequentialGroup()
                                .addGap(165)
                                .addComponent(cancelButton)
                                .addGap(10)
                                .addComponent(okayButton))
        );
        layout.linkSize(SwingConstants.HORIZONTAL, cancelButton, okayButton);
        validate();

    }

    public void removeBreedNameComboBox() {
        topPanel.removeBreedNameComboBox();
        myFrame.setTitle("Species Editor: " + topPanel.getSelectedBreed());
    }
    public JButton getOkayButton() {
        return okayButton;
    }
    public JButton getCancelButton() {
        return cancelButton;
    }
    public JFrame getMyFrame() {
        return myFrame;
    }
    public boolean getMakeNewBreedBlock() {
        return makeNewBreedBlock;
    }
    public void setMakeNewBreedBlock(boolean flag) {
        makeNewBreedBlock = flag;
    }
    public void setMyBreedBlock(BreedBlock breedBlock) {
        this.myBreedBlock = breedBlock;
    }
    public BreedBlock getMyBreedBlock() {
        return myBreedBlock;
    }
    public String getMyBreedName() {
        return topPanel.getSelectedBreed();
    }
    public String getMySetupNumber() {
        return topPanel.getSetupNumber();
    }
    public String getMyBreedShape() {
        return topPanel.getBreedShape();
    }
    public VectorShape getMyBreedVectorShape() {
        return topPanel.getBreedVectorShape();
    }
    public String getMyBreedColorName() {
        return topPanel.getBreedColorName();
    }
    public Color getMyBreedColor() {
        return topPanel.getBreedColor();
    }
    public int getMyBreedColorRGB() {
        return  topPanel.getBreedColorRGB();
    }
    public HashMap<String, TraitState> getTraitStateMap() {
        return traitPreview.getTraitStateMap();
    }
    public TraitPreview getTraitPreview() {
        return traitPreview;
    }
    public void setSelectedBreed(String breedName) {
        topPanel.setSelectedBreed(breedName);
    }
    public void setSetupNumber(String setupNumber) {
        topPanel.setSetupNumer(setupNumber);
    }
    public void setBreedShape(String breedShape) {
        topPanel.setBreedShape(breedShape);
    }
    public void setBreedColor(int colorRGB) {
        topPanel.setBreedColor(colorRGB);
    }

    private class SpeciesEditorTopPanel extends JPanel {
        private final int BORDER_PADDING = 10;
        HashMap<String, String[]> breedNameSetupNumbers;
        HashMap<String, DefaultComboBoxModel> breedNameSetupComboModelMap;
        // Components
        private JLabel breedNameLabel;
        private JLabel breedSetupNumberLabel;
        private JComboBox breedSetupNumberComboBox;
        private JComboBox breedNamesComboBox;
        // Color
        private JComboBox breedColorComboBox;
        private JLabel breedColorLabel;
        ArrayList<Color> COLORS;
        // Shape
        private JLabel breedShapeLabel;
        private JButton breedShapeButton;
        BreedShapeButtonActionListener breedShapeButtonActionListener;
        String breedShape = "default";

        // The layout
        GroupLayout layout;

        public SpeciesEditorTopPanel(String[] allBreedNames,
                                     ArrayList<String []> allBreedSetupNumbers) {
            // Initialize HashMap
            breedNameSetupNumbers = new HashMap<String, String[]>();
            breedNameSetupComboModelMap = new HashMap<String, DefaultComboBoxModel>();
            for (int i = 0; i < allBreedNames.length; i++) {
                breedNameSetupNumbers.put(allBreedNames[i], allBreedSetupNumbers.get(i));
                breedNameSetupComboModelMap.put(allBreedNames[i], new DefaultComboBoxModel(allBreedSetupNumbers.get(i)));
            }

            // Initialize labels and components
            // Species name
            this.breedNameLabel = new JLabel("Which species do you want to add? ");
            // Initialize name combobox
            this.breedNamesComboBox = new JComboBox(allBreedNames);
            this.breedNamesComboBox.setSelectedIndex(0);
            this.breedNamesComboBox.addItemListener(new breedNamesComboBoxActionListener());
            // Species setup number
            this.breedSetupNumberLabel = new JLabel("How many individuals of this species to begin with?");
            this.breedSetupNumberComboBox = new JComboBox(breedNameSetupComboModelMap.get(allBreedNames[0]));
            this.breedSetupNumberComboBox.setSelectedIndex(0);
            // Species color
            this.breedColorLabel = new JLabel("What color do you want it to be?");
            // Set up colors
            COLORS = new ArrayList<Color>();
            COLORS.add(new Color(0x99, 0x00, 0x00));
            COLORS.add(new Color(0x99, 0x4C, 0x00));
            COLORS.add(new Color(0x99, 0x99, 0x00));
            COLORS.add(new Color(0xCC, 0xCC, 0x00));
            COLORS.add(new Color(0x66, 0xCC, 0x00));
            COLORS.add(new Color(0x00, 0x99, 0x00));
            COLORS.add(new Color(0x00, 0x99, 0x99));
            COLORS.add(new Color(0x00, 0x4C, 0x99));
            COLORS.add(new Color(0x00, 0x00, 0x99));
            COLORS.add(new Color(0x4C, 0x00, 0x99));
            COLORS.add(new Color(0x99, 0x00, 0x99));
            COLORS.add(new Color(0x99, 0x00, 0x4C));
            COLORS.add(new Color(0x99, 0x99, 0x99));
            COLORS.add(new Color(0x40, 0x40, 0x40));
            COLORS.add(new Color(0x20, 0x20, 0x20));
            COLORS.add(new Color(0x00, 0x00, 0x00));
            this.breedColorComboBox = new JComboBox(COLORS.toArray());
            this.breedColorComboBox.setSelectedIndex(0);
            // Species shape
            this.breedShapeLabel = new JLabel("What should it look like?");
            this.breedShapeButton = new JButton();
            breedShapeButtonActionListener = new BreedShapeButtonActionListener(myFrame,
                    breedShapeButton,
                    breedColorComboBox,
                    breedNamesComboBox.getSelectedItem().toString(),
                    breedShape);
            ShapeIcon breedShapeIcon = new ShapeIcon(breedShapeButtonActionListener.getShape());
            breedShapeIcon.setColor((Color) breedColorComboBox.getSelectedItem());
            this.breedShapeButton.setIcon(breedShapeIcon);

            // Do the layout
            setupLayout();
            this.revalidate();
        }

        private void setupLayout() {
            // Name
            breedNameLabel.setPreferredSize(new Dimension(100, 10));
            breedNamesComboBox.setPreferredSize(new Dimension(100,10));
            // Setup number
            breedSetupNumberLabel.setPreferredSize(new Dimension(75, 10));
            breedSetupNumberComboBox.setPreferredSize(new Dimension(25, 10));
            // Shape
            breedShapeLabel.setPreferredSize(new Dimension(75, 10));
            breedShapeButton.setPreferredSize(new Dimension(25, 10));
            breedShapeButton.addActionListener(breedShapeButtonActionListener);
            // Color ComboBox
            breedColorLabel.setPreferredSize(new Dimension(75, 10));
            ColorComboBoxRenderer renderer = new ColorComboBoxRenderer();
            renderer.setPreferredSize(new Dimension(50, 15));
            breedColorComboBox.setRenderer(renderer);
            ComboBoxItemListener itemListener = new ComboBoxItemListener(breedColorComboBox,
                    breedShapeButton,
                    breedShapeButtonActionListener);
            breedColorComboBox.addItemListener(itemListener);

            // Set up the layout
            layout = new GroupLayout(this);
            this.setLayout(layout);
            // The Horizontal Group
            // Create parallel groups
            GroupLayout.ParallelGroup hpgroup1 = layout.createParallelGroup(GroupLayout.Alignment.LEADING, false);
            hpgroup1.addComponent(breedNameLabel);
            hpgroup1.addComponent(breedSetupNumberLabel);
            hpgroup1.addComponent(breedColorLabel);
            hpgroup1.addComponent(breedShapeLabel);
            GroupLayout.ParallelGroup hpgroup2 = layout.createParallelGroup(GroupLayout.Alignment.LEADING, false);
            hpgroup2.addComponent(breedNamesComboBox);
            hpgroup2.addComponent(breedSetupNumberComboBox);
            hpgroup2.addComponent(breedColorComboBox);
            hpgroup2.addComponent(breedShapeButton);
            // Create the sequential group
            GroupLayout.SequentialGroup hsgroup = layout.createSequentialGroup();
            hsgroup.addGroup(hpgroup1);
            hsgroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
            hsgroup.addGroup(hpgroup2);

            // Set the horizontal group
            layout.setHorizontalGroup(hsgroup);

            // The Vertical Group
            // Create parallel groups
            GroupLayout.ParallelGroup vpgroup1 = layout.createParallelGroup(GroupLayout.Alignment.LEADING, false);
            vpgroup1.addComponent(breedNameLabel);
            vpgroup1.addComponent(breedNamesComboBox);
            GroupLayout.ParallelGroup vpgroup2 = layout.createParallelGroup(GroupLayout.Alignment.LEADING, false);
            vpgroup2.addComponent(breedSetupNumberLabel);
            vpgroup2.addComponent(breedSetupNumberComboBox);
            GroupLayout.ParallelGroup vpgroup4 = layout.createParallelGroup(GroupLayout.Alignment.LEADING, false);
            vpgroup4.addComponent(breedColorLabel);
            vpgroup4.addComponent(breedColorComboBox);
            GroupLayout.ParallelGroup vpgroup5 = layout.createParallelGroup(GroupLayout.Alignment.LEADING, false);
            vpgroup5.addComponent(breedShapeLabel);
            vpgroup5.addComponent(breedShapeButton);
            // Create the sequential group
            GroupLayout.SequentialGroup vsgroup = layout.createSequentialGroup();
            vsgroup.addGroup(vpgroup1);
            vsgroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
            vsgroup.addGroup(vpgroup2);
            vsgroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
            vsgroup.addGroup(vpgroup4);
            vsgroup.addPreferredGap(LayoutStyle.ComponentPlacement.RELATED);
            vsgroup.addGroup(vpgroup5);

            // Set the vertical group
            layout.setVerticalGroup(vsgroup);

            this.setBorder(new EmptyBorder(BORDER_PADDING,
                    BORDER_PADDING,
                    BORDER_PADDING,
                    BORDER_PADDING));
            validate();

        }

        public void removeBreedNameComboBox() {
            breedNamesComboBox.setVisible(false);
            breedNameLabel.setVisible(false);
        }
        public void setSelectedBreed(String breedName) {
            for (int i = 0; i < breedNamesComboBox.getItemCount(); i++) {
                if (((String) breedNamesComboBox.getItemAt(i)).equalsIgnoreCase(breedName)) {
                    breedNamesComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        public void setSetupNumer(String setupNumer) {
            for (int i = 0; i < breedSetupNumberComboBox.getItemCount(); i++) {
                if (((String) breedSetupNumberComboBox.getItemAt(i)).equalsIgnoreCase(setupNumer)) {
                    breedSetupNumberComboBox.setSelectedIndex(i);
                    break;
                }
            }
        }
        public void setBreedShape(String breedShape) {
            breedShapeButtonActionListener.setBreedShape(breedShape);
            // Update breed shape button icon and color
            ShapeIcon breedShapeIcon = new ShapeIcon(breedShapeButtonActionListener.getShape());
            breedShapeIcon.setColor((Color) breedColorComboBox.getSelectedItem());
            breedShapeButton.setIcon(breedShapeIcon);
        }
        public void setBreedColor(int breedColorRGB) {
            for (int i = 0; i < breedColorComboBox.getItemCount(); i++) {
                if (((Color) breedColorComboBox.getItemAt(i)).getRGB() == breedColorRGB) {
                    breedColorComboBox.setSelectedIndex(i);
                    // Update breed shape button icon and color
                    ShapeIcon breedShapeIcon = new ShapeIcon(breedShapeButtonActionListener.getShape());
                    breedShapeIcon.setColor((Color) breedColorComboBox.getSelectedItem());
                    breedShapeButton.setIcon(breedShapeIcon);
                    break;
                }
            }
        }
        public String getSelectedBreed() {
            return (String) breedNamesComboBox.getSelectedItem();
        }
        public String getSetupNumber() {
            return (String) breedSetupNumberComboBox.getSelectedItem();
        }
        public String getBreedShape() {
           return breedShapeButtonActionListener.getBreedShape();
        }
        public VectorShape getBreedVectorShape() {
            return breedShapeButtonActionListener.getShape();
        }
        public Color getBreedColor() {
           return (Color) breedColorComboBox.getSelectedItem();
        }
        public String getBreedColorName() {
            Color selectedColor = (Color) breedColorComboBox.getSelectedItem();
            int colorARGB = 0xff000000 + selectedColor.getRGB();
            return org.nlogo.api.Color.getClosestColorNameByARGB(colorARGB);
        }
        public int getBreedColorRGB() {
            Color selectedColor = (Color) breedColorComboBox.getSelectedItem();
            return selectedColor.getRGB();
        }
        private class breedNamesComboBoxActionListener implements ItemListener {
            @Override
            public void itemStateChanged(ItemEvent e) {
                //To change body of implemented methods use File | Settings | File Templates.
                String breedName = (String) e.getItem();
                breedSetupNumberComboBox.setModel(breedNameSetupComboModelMap.get(breedName));
                breedSetupNumberComboBox.setSelectedIndex(0);
            }
        }
    }

    class ComboBoxItemListener implements ItemListener {
        JComboBox colorComboBox;
        JButton shapeButton;
        BreedShapeButtonActionListener breedShapeButtonActionListener;

        ComboBoxItemListener(JComboBox comboBox, JButton button, BreedShapeButtonActionListener buttonActionListener) {
            colorComboBox = comboBox;
            shapeButton = button;
            breedShapeButtonActionListener = buttonActionListener;
        }
        @Override
        public void itemStateChanged(ItemEvent e) {
            //To change body of implemented methods use File | Settings | File Templates.
            ShapeIcon shapeIcon = new ShapeIcon(breedShapeButtonActionListener.getShape());
            Color selectedColor = (Color) e.getItem();
            shapeIcon.setColor(selectedColor);
            shapeButton.setIcon(shapeIcon);
        }
    }

    class BreedShapeButtonActionListener implements ActionListener {
        // The shape selector
        ShapeSelectorWithoutColor myShapeSelector;

        JButton shapeButton;
        JComboBox colorComboBox;
        int curIconIndex;
        String breedShape;

        // Constructor
        public BreedShapeButtonActionListener(JFrame frame, JButton button, JComboBox comboBox, String breedName, String shape) {
            this.curIconIndex = 0;
            myShapeSelector = new ShapeSelectorWithoutColor(frame, allShapes(), breedName, curIconIndex);
            this.colorComboBox = comboBox;
            this.shapeButton = button;
            this.breedShape = shape;
        }
        // Returns shapes
        private String[] allShapes() {
            String[] defaultShapes =
                    org.nlogo.util.Utils.getResourceAsStringArray
                            ("/system/deltatickShapes.txt");  //default NetLogo shapes shapes (Sept 19, 2013)
            return defaultShapes;
        }
        // actionPerformed method that s called when the button is clicked
        public void actionPerformed(ActionEvent e) {
            myShapeSelector.setVisible(true);
            if (myShapeSelector.getChosenValue() >= 0) {
                ShapeIcon shapeIcon = new ShapeIcon(myShapeSelector.getShape());
                shapeIcon.setColor((Color) colorComboBox.getSelectedItem());
                shapeButton.setIcon(shapeIcon);
                breedShape = myShapeSelector.getChosenShape();
                curIconIndex = myShapeSelector.getChosenValue();
            }
        }
        public String getBreedShape() {
            return breedShape;
        }
        public VectorShape getShape() {
            return myShapeSelector.getShape();
        }
        public void setBreedShape(String breedShape) {
            this.breedShape = new String(breedShape);
            myShapeSelector.setChosenShape(breedShape);
        }
    }

    // Implements listener when a trait is clicked on
    class TraitListSelectionHandler implements ListSelectionListener {
        // TraitTableModelListener traitTableModelListener = new TraitTableModelListener();
        public void valueChanged(ListSelectionEvent e) {
            //sidePanel.setVisible(true);
            traitPreview.updateTraitSelection(e);
            myFrame.pack();

        } // valueChanged
    } // TraitListSelectionHandler

    // Implements listener when the variation table is modified
    class TraitTableModelListener implements TableModelListener {
        public void tableChanged(TableModelEvent e) {

            if (e.getColumn() == TraitPreview.VARCHECKBOX_COLUMN_INDEX) {
                traitPreview.updateVariationSelection(e);
                updateTraitDisplay();
            }
        }
    }

    public void updateTraitDisplay() {
        traitDisplay.validate();
        sidePanel.validate();
        if (traitPreview.getTraitStateMap().size() == 0) {
            sidePanel.setVisible(false);
        }
        else {
            sidePanel.setVisible(true);
        }
        sidePanel.validate();
        myFrame.pack();
    }

}
