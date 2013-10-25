package org.nlogo.deltatick;

import org.nlogo.deltatick.dialogs.ColorComboBoxRenderer;
import org.nlogo.deltatick.dialogs.ShapeSelectorWithoutColor;
import org.nlogo.deltatick.xml.Trait;
import org.nlogo.hotlink.dialogs.ShapeIcon;
import org.nlogo.shape.VectorShape;
import scala.actors.threadpool.Arrays;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;
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
    public SpeciesEditorPanel(String[] allBreedNames, ArrayList<Trait> traits, JFrame jFrame) {
        this.myFrame = jFrame;
        // Initialize ArrayList<> allBreedNames like below. Arrays.asList doesn't compile on
        // the commandline. Warnings for unchecked case breaks the build
        this.allBreedNames = new ArrayList<String>();
        for (int i = 0; i < allBreedNames.length; i++) {
            this.allBreedNames.add(allBreedNames[i]);
        }
        this.allTraits = new ArrayList<Trait>(traits);
        this.topPanel = new SpeciesEditorTopPanel(allBreedNames);

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
                        .addComponent(topPanel)
                        .addGroup(layout.createSequentialGroup()
                                .addComponent(midPanel)
                                .addComponent(sidePanel))
                        .addComponent(bottomPanel)
        );
        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addComponent(topPanel)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(midPanel)
                                .addComponent(sidePanel))
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
    public String getMyMaxNumber() {
        return topPanel.getMaxNumber();
    }
    public String getMyBreedShape() {
        return topPanel.getBreedShape();
    }
    public String getMyBreedColorName() {
        return topPanel.getBreedColorName();
    }
    public HashMap<String, TraitState> getTraitStateMap() {
        return traitPreview.getTraitStateMap();
    }
    public TraitPreview getTraitPreview() {
        return traitPreview;
    }

    private class SpeciesEditorTopPanel extends JPanel {

        // Components
        private JLabel breedNameLabel;
        private JLabel breedSetupNumberLabel;
        private JTextField breedSetupNumberText;
        private JLabel breedMaxNumberLabel;
        private JTextField breedMaxNumberText;
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
        // GroupLayout layout;

        public SpeciesEditorTopPanel(String [] allBreedNames) {

            // Initialize labels and components
            // Species name
            this.breedNameLabel = new JLabel("Choose a species:");
            // Initialize name combobox
            this.breedNamesComboBox = new JComboBox(allBreedNames);
            this.breedNamesComboBox.setSelectedIndex(0);
            // Species setup number
            this.breedSetupNumberLabel = new JLabel("Start Number:");
            this.breedSetupNumberText = new JTextField("25");
            // Species max number
            this.breedMaxNumberLabel = new JLabel("Max Number:");
            this.breedMaxNumberText = new JTextField("100");
            // Species color
            this.breedColorLabel = new JLabel("Choose Color:");
            // Set up colors
            COLORS = new ArrayList<Color>();
            COLORS.add(new Color(0x99, 0x99, 0x99)); // GRAY
            COLORS.add(new Color(0xFF, 0x66, 0x66)); // RED
            COLORS.add(new Color(0x66, 0x66, 0xFF)); // PURPLEBLUE
            COLORS.add(new Color(0x66, 0xFF, 0x66)); // GREEN
            COLORS.add(new Color(0xFF, 0xFF, 0x66)); // YELLOW
            COLORS.add(new Color(0x66, 0xFF, 0xFF)); // CYAN
            COLORS.add(new Color(0xFF, 0x66, 0xFF)); // PINK
            COLORS.add(new Color(0x66, 0x00, 0x00)); // BROWN
            COLORS.add(new Color(0xFF, 0x66, 0x00)); // ORANGE
            this.breedColorComboBox = new JComboBox(COLORS.toArray());
            // Speies shape
            this.breedShapeLabel = new JLabel("Choose Shape:");
            this.breedShapeButton = new JButton(new ShapeIcon(org.nlogo.shape.VectorShape.getDefaultShape()));
            breedShapeButtonActionListener = new BreedShapeButtonActionListener(myFrame,
                    breedShapeButton,
                    breedColorComboBox,
                    breedNamesComboBox.getSelectedItem().toString(),
                    breedShape);

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
            breedSetupNumberText.setPreferredSize(new Dimension(25, 10));
            // Max number
            breedMaxNumberLabel.setPreferredSize(new Dimension(75, 10));
            breedMaxNumberText.setPreferredSize(new Dimension(25, 10));
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

            this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

            this.add(breedNameLabel);
            this.add(breedNamesComboBox);
            this.add(Box.createRigidArea(new Dimension(10,10)));
            this.add(breedSetupNumberLabel);
            this.add(breedSetupNumberText);
            this.add(Box.createRigidArea(new Dimension(10,10)));
            this.add(breedMaxNumberLabel);
            this.add(breedMaxNumberText);
            this.add(Box.createRigidArea(new Dimension(10,10)));
            this.add(breedColorLabel);
            this.add(breedColorComboBox);
            this.add(breedShapeLabel);
            this.add(breedShapeButton);
        }

        public String getSelectedBreed() {
            return (String) breedNamesComboBox.getSelectedItem();
        }
        public String getSetupNumber() {
            return breedSetupNumberText.getText();
        }
        public String getMaxNumber() {
            return breedMaxNumberText.getText();
        }
        public String getBreedShape() {
           return breedShapeButtonActionListener.getBreedShape();
        }
        public Color getBreedColor() {
           return (Color) breedColorComboBox.getSelectedItem();
        }
        public String getBreedColorName() {
            Color selectedColor = (Color) breedColorComboBox.getSelectedItem();
            int colorARGB = 0xff000000 + selectedColor.getRGB();
            return org.nlogo.api.Color.getClosestColorNameByARGB(colorARGB);
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
                            ("/system/defaultShapes.txt");  //default NetLogo shapes shapes (Sept 19, 2013)
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
