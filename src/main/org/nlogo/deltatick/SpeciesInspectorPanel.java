package org.nlogo.deltatick;

import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import java.awt.*;
import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 1/20/13
 * Time: 11:34 PM
 * To change this template use File | Settings | File Templates.
 */
public class SpeciesInspectorPanel extends JPanel {

    BreedBlock myParent;
    JPanel topPanel = new JPanel();
    JPanel midPanel = new JPanel();
    JPanel sidePanel = new JPanel(true);
    JPanel bottomPanel = new JPanel();
    JLabel lifeSpanLabel = new JLabel();
    JTextField lifeSpanBlank = new JTextField();
    JLabel energyLabel = new JLabel("What is the max energy?"); //TODO add myParent.plural()
    JTextField energyBlank = new JTextField();
    JButton cancelButton = new JButton("Cancel"); //actionListener in deltaticktab - March 2, 2013
    JButton okayButton = new JButton("Okay"); // actionListener in deltaticktab -March 2, 2013

    JFrame myFrame;

    SpeciesInspector speciesInspector = new SpeciesInspector();
    TraitPreview traitPreview;
    TraitDisplay traitDisplay = new TraitDisplay();
    LabelPanel labelPanel;


    public SpeciesInspectorPanel(BreedBlock myParent, JFrame myFrame) {
        this.myParent = myParent;
        this.myFrame = myFrame;
        energyBlank.setMaximumSize(new Dimension(20, 30));
        lifeSpanBlank.setMaximumSize(new Dimension(20, 30));
        updateText();
        //activateButtons();

        addPanels();
    }

    public void updateText() {
        energyBlank.setText(myParent.getMaxEnergy());
        lifeSpanBlank.setText(myParent.getMaxAge());
        lifeSpanLabel.setText("How old do " + myParent.plural() + " live to be?");
    }

    public void addPanels() {

        setupTopPanel();
        setupSidePanel();
        setupMidPanel();
        setupBottomPanel();

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

        // Hide age and energy
        // Temporary -- for prototype implementation
        topPanel.setVisible(false);

        revalidate();
        myFrame.validate();
        myFrame.pack();
    }

    public void setupTopPanel() {
        //topPanel.setBackground(ColorSchemer.getColor(3));
        ////midPanel.setBackground(ColorSchemer.getColor(3));
        //sidePanel.setBackground(ColorSchemer.getColor(3));
        //bottomPanel.setBackground(ColorSchemer.getColor(3));
        TitledBorder titleTopPanel;
        titleTopPanel = BorderFactory.createTitledBorder("Set up");
        topPanel.setBorder(titleTopPanel);

        lifeSpanLabel.setText("How old do " + myParent.plural() + " live to be?");

        GroupLayout layout = new GroupLayout(topPanel);
        topPanel.setLayout(layout);

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .addGap(5)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(lifeSpanLabel)
                                .addComponent(lifeSpanBlank))
                        .addGap(5)
                        .addGroup(layout.createParallelGroup()
                                .addComponent(energyLabel)
                                .addComponent(energyBlank))
        );

        layout.setHorizontalGroup(
                layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup()
                    //.addComponent(breedNameLabel)
                    .addComponent(lifeSpanLabel)
                    .addComponent(energyLabel))
                .addGroup(layout.createParallelGroup()
                .addGap(10)
               // .addComponent(breedName)
               )
                .addGroup(layout.createParallelGroup()
                .addComponent(lifeSpanBlank, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(energyBlank, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(50)
        );

        validate();

    }

    public void setupSidePanel() {
        TitledBorder titleSidePanel;
        titleSidePanel = BorderFactory.createTitledBorder("");
        sidePanel.setBorder(titleSidePanel);
        //sidePanel.setPreferredSize(new Dimension(600, 200));
        traitDisplay = new TraitDisplay(sidePanel, myFrame);

        sidePanel.add(traitDisplay);
        sidePanel.setVisible(false); // testing jframe size
        sidePanel.validate();
    }

    public void setupMidPanel() {
        midPanel.setLayout(new BoxLayout(midPanel, BoxLayout.Y_AXIS));

        TitledBorder titleMidPanel;
        titleMidPanel = BorderFactory.createTitledBorder("");
        midPanel.setBorder(titleMidPanel);

        labelPanel = new LabelPanel(myParent.getOwnVarNames());
        TitledBorder titleLabelPanel;
        titleLabelPanel = BorderFactory.createTitledBorder("Set Labels           l");
        labelPanel.setBorder(titleLabelPanel);
        labelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        //labelPanel.setPreferredSize(new Dimension(LabelPanel.LABELPANEL_WIDTH, LabelPanel.LABELPANEL_HEIGHT));

        traitPreview = new TraitPreview(myParent.plural(), traitDisplay, labelPanel, myFrame);
        traitPreview.setTraits(myParent.getTraits());
        traitPreview.setTraitsListListener(new TraitListSelectionHandler());
        traitPreview.setTraitTableModelListener(new TraitTableModelListener());
        traitPreview.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Add to midPanel
        midPanel.add(traitPreview);
        //midPanel.add(labelPanel);

        labelPanel.setMinimumSize(new Dimension(traitPreview.getTotalWidth()-10, LabelPanel.LABELPANEL_HEIGHT));
        labelPanel.setPreferredSize(new Dimension(traitPreview.getTotalWidth()-10, LabelPanel.LABELPANEL_HEIGHT));
        midPanel.setPreferredSize(new Dimension(traitPreview.getTotalWidth(), traitPreview.getTotalHeight()));
        midPanel.revalidate();
    }

    public void setupBottomPanel() {
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

    public String getEndListSpan() {
        return lifeSpanBlank.getText().toString();
    }

    public String getHighestEnergy() {
        return energyBlank.getText().toString();
    }

    public SpeciesInspector getSpeciesInspector() {
        return speciesInspector;
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

    public BreedBlock getMyParent() {
        return myParent;
    }

    public TraitPreview getTraitPreview() {
        return traitPreview;
    }

//    public void setSelectedTrait() {
//        speciesInspector.addToSelectedTraitsList(traitPreview.getSelectedTrait());
//    }

    public void setSelectedVariations(Trait trait, Variation variation) {
        speciesInspector.addtoSelectedVariations(trait, variation);
    }

    public HashMap<String, TraitState> getTraitStateMap() {
        return traitPreview.getTraitStateMap();
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