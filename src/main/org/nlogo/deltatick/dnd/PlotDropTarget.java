package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.*;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.*;
import java.io.IOException;

public class PlotDropTarget
        extends DropTarget {

    public PlotDropTarget(PlotBlock block) {
        super(block);
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.quantityBlockFlavor);

        if (o instanceof QuantityBlock) {
            if (((QuantityBlock) o).getHisto() == false) {
                ((PlotBlock) block).removeQuantityblockPanel();
                addCodeBlock((QuantityBlock) o);
                ((QuantityBlock) o).showColorButton();
                if (((QuantityBlock) o).getNeedsTrait() == true) {
                    ((QuantityBlock) o).addTraitblockPanel();
                    new QuantityDropTarget((QuantityBlock) o);
                }

                ((QuantityBlock) o).validate();
                ((QuantityBlock) o).repaint();
                block.validate();
                return true;
            }
            else {
                //String message = new String(((QuantityBlock) o).getName() + " is a block for histograms, not line graphs.");
                String message = new String("Oops! You dragged a block for bar graphs. You can only add line graphs here.");
                JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
                return false;
            }
        }
        else {
            String message = new String("Oops! You can only add orange blocks here!");
            JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
            return false;
        }

    }

    public void dragEnter(DropTargetDragEvent dtde) {
        if ((dtde.isDataFlavorSupported(CodeBlock.quantityBlockFlavor))) {
            ((PlotBlock) block).showRectPanel();
            ((PlotBlock) block).addedRectPanel = true;
        }
    }

    public void dragExit(DropTargetEvent dte) {
        if (((PlotBlock) block).addedRectPanel) {
            ((PlotBlock) block).hideRectPanel();
            ((PlotBlock) block).addedRectPanel = false;
        }
    }
}
