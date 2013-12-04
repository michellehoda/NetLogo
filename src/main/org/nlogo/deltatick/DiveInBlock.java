package org.nlogo.deltatick;

import java.awt.*;
import java.awt.List;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.*;

import org.nlogo.api.*;
import org.nlogo.deltatick.dialogs.ShapeSelector;
import org.nlogo.deltatick.xml.DiveIn;
import org.nlogo.hotlink.dialogs.ShapeIcon;
import org.picocontainer.Behavior;

import javax.swing.*;
import javax.swing.border.EtchedBorder;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 9/20/13
 * Time: 8:52 AM
 * To change this template use File | Settings | File Templates.
 */
public strictfp class DiveInBlock
        extends CodeBlock
        implements MouseMotionListener,
        MouseListener {

    transient DiveIn diveIn;
    transient JButton breedShapeButton;
    transient Frame parentFrame;
    //int curIconIndex;
    //Color curColor;
    JPanel rectPanel = new JPanel();
    Boolean removedRectPanel = false;
    public boolean addedRectPanel = false; //!< If true, rectPanel will appear/disappear as block is moved over breedblock
    Set<String> applicableBehaviorBlocks = new HashSet<String>();

    public DiveInBlock (DiveIn diveIn, Frame frame) {
        super(diveIn.getName(), ColorSchemer.getColor(3));
        setBorder(org.nlogo.swing.Utils.createWidgetBorder());
        this.diveIn = diveIn;
        if (diveIn.getApplicableBehaviorBlocks() != null) {
            applicableBehaviorBlocks = new HashSet<String>(Arrays.asList(diveIn.getApplicableBehaviorBlocks().split(",")));
        }
        this.addMouseMotionListener(this);
        this.parentFrame = frame;
        this.addMouseListener(this);
        this.setLocation(0, 0);
        this.showRemoveButton();
        addRect();
        //curIconIndex = 0;
        //curColor = Color.GRAY;
    }

    public boolean isBehaviorApplicable(String behaviorName) {
        for (String aBehBlock : applicableBehaviorBlocks) {
            if (aBehBlock.equalsIgnoreCase(behaviorName)) {
                return true;
            }
        }
        return false;
    }
    public void addRect() {
        rectPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
        rectPanel.setPreferredSize(new Dimension(this.getWidth(), 40));
        rectPanel.setBackground(getBackground());
        JLabel label = new JLabel();
        label.setText("Add \"eat\" block here");
        rectPanel.add(label);
        add(rectPanel);
        validate();
    }
    public void showRectPanel() {
        if (removedRectPanel) {
            //rectPanel.setVisible(true);
            this.add(rectPanel);
            this.validate();
            this.repaint();
        }
    }
    public void hideRectPanel() {
        if (removedRectPanel) {
            //rectPanel.setVisible(false);
            this.remove(rectPanel);
            this.validate();
            this.repaint();
        }
    }

    public java.util.List<BehaviorBlock> getMyBlocks() {
        java.util.List<BehaviorBlock> blocks = new ArrayList<BehaviorBlock>();
        for (CodeBlock block : myBlocks) {
            if (block instanceof BehaviorBlock) {
                blocks.add((BehaviorBlock) block);
            }
        }
        return blocks;
    }

    public String unPackAsCode() {
        String passBack = "";

        passBack += "ask predators [\n";
        passBack += "if student-in [\n";

        for (CodeBlock block : myBlocks) {
            passBack += block.unPackAsCode();
        }
        passBack += "]]\n";

        return passBack;
    }

    public Object getTransferData(DataFlavor dataFlavor)
            throws UnsupportedFlavorException {
        if (isDataFlavorSupported(dataFlavor)) {
            if (dataFlavor.equals(breedBlockFlavor)) {
                return this;
            }
            if (dataFlavor.equals(envtBlockFlavor)) {
                return this;
            }
            if (dataFlavor.equals(DataFlavor.stringFlavor)) {
                return unPackAsCode();
            }
            if (dataFlavor.equals(traitBlockFlavor)) {
                return unPackAsCode();
            }

        } else {
            return "Flavor Not Supported";
        }
        return null;
    }


    public String getDiveInCode() {
        return diveIn.getReporterCode();
    }

    public String setup() {
        return diveIn.getSetupCode();
        //return "create-predators 1 \n [set size 2 \n set shape \"bird\"]\n";
    }

    public void addBlock(CodeBlock block) {
        super.addBlock(block);
        if (removedRectPanel == false) {
            remove(rectPanel);
            removedRectPanel = true;
        }
        else {
            hideRectPanel();
        }
        validate();
    }
    public void removeBlock(CodeBlock block) {
        super.removeBlock(block);
        if (block instanceof BehaviorBlock) {
            myBlocks.remove(block);
        }
        if (myBlocks.size() == 0) {
            showRectPanel();
        }
    }

//    private final javax.swing.Action pickBreedShape =
//            new javax.swing.AbstractAction() {
//                public void actionPerformed(java.awt.event.ActionEvent e) {
//                }
//            };
//
//    public JButton makeBreedShapeButton() {
//        breedShapeButton = new JButton(new ShapeIcon(org.nlogo.shape.VectorShape.getDefaultShape()));
//        breedShapeButton.setActionCommand(this.getName());
//        breedShapeButton.addActionListener(this);
//        breedShapeButton.setSize(30, 30);
//        breedShapeButton.setBackground(color);
//        breedShapeButton.setToolTipText("Change shape");
//        return breedShapeButton;
//    }

    // when clicks on shape selection -a.
//    public void actionPerformed(java.awt.event.ActionEvent evt) {
//        ShapeSelector myShapeSelector = new ShapeSelector(parentFrame, allShapes(), this, curIconIndex, curColor);
//        myShapeSelector.setVisible(true);
//        //System.out.println(myShapeSelector.getChosenValue());
//        if (myShapeSelector.getChosenValue() >= 0) {
//            ShapeIcon shapeIcon = new ShapeIcon(myShapeSelector.getShape());
//            shapeIcon.setColor(myShapeSelector.getSelectedColor());
//            breedShapeButton.setIcon(shapeIcon);
//            breedShape = myShapeSelector.getChosenShape();
//            curColor = myShapeSelector.getSelectedColor();
//            curIconIndex = myShapeSelector.getChosenValue();
//        }
//    }

    // getting shapes from NL -a.
    String[] allShapes() {
        String[] defaultShapes =
                org.nlogo.util.Utils.getResourceAsStringArray
                        //("/system/defaultShapes.txt");  //default NetLogo shapes shapes (Sept 19, 2013)
                        ("/system/deltatickShapes.txt");      //DeltaTick shapes with animate objects (Sept 19, 2013)
        String[] libraryShapes =
                org.nlogo.util.Utils.getResourceAsStringArray
                        ("/system/libraryShapes.txt");
        String[] mergedShapes =
                new String[defaultShapes.length + 1 + libraryShapes.length];
        System.arraycopy(defaultShapes, 0,
                mergedShapes, 0,
                defaultShapes.length);
        mergedShapes[defaultShapes.length] = "";
        System.arraycopy(libraryShapes, 0,
                mergedShapes, defaultShapes.length + 1,
                libraryShapes.length);
        return defaultShapes; // NOTE right now just doing default
    }

    public java.util.List<org.nlogo.api.Shape> parseShapes(String[] shapes, String version) {
        return org.nlogo.shape.VectorShape.parseShapes(shapes, version);
    }

    public void mouseEnter(MouseEvent evt) {
    }

    public void mouseExit(MouseEvent evt) {
    }

    public void mouseEntered(MouseEvent evt) {
    }

    public void mouseExited(MouseEvent evt) {
    }

    public void mouseClicked(MouseEvent evt) {
    }

    public void mouseMoved(MouseEvent evt) {
    }

    public void mouseReleased(MouseEvent evt) {
    }

    int beforeDragX;
    int beforeDragY;

    int beforeDragXLoc;
    int beforeDragYLoc;

    public void mousePressed(MouseEvent evt) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen(point, this);
        beforeDragX = point.x;
        beforeDragY = point.y;
        beforeDragXLoc = getLocation().x;
        beforeDragYLoc = getLocation().y;
    }

    public void mouseDragged(MouseEvent evt) {
        Point point = evt.getPoint();
        javax.swing.SwingUtilities.convertPointToScreen(point, this);
        this.setLocation(
                point.x - beforeDragX + beforeDragXLoc,
                point.y - beforeDragY + beforeDragYLoc);
    }

}
