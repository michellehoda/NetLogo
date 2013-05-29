package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.CodeBlock;
import org.nlogo.deltatick.HistogramBlock;
import org.nlogo.deltatick.PlotBlock;
import org.nlogo.deltatick.QuantityBlock;

import javax.swing.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 5/3/12
 * Time: 3:58 PM
 * To change this template use File | Settings | File Templates.
 */
public class HistoDropTarget
        extends DropTarget {

    public HistoDropTarget (PlotBlock block) {
        super(block);
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.quantityBlockFlavor);
        if (o instanceof QuantityBlock) {
            if (((QuantityBlock) o).getHisto() == true) {
                if (((PlotBlock) block).isHistogram() &&
                        (((PlotBlock) block).getMyBlocks().size() > 0)) {
                    String message = new String("You can only use one block here!");
                    JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
                else {

                ((PlotBlock) block).removeQuantityblockPanel();
                addCodeBlock((QuantityBlock) o);
                ((QuantityBlock) o).validate();
                block.validate();
                return true;
                }
            }
            else {
                // String message = new String(((QuantityBlock) o).getName() + " is a block for line graphs, not histograms.");
                String message = new String("Oops! You dragged a block for line graphs. You can only add bar graphs here.");
                JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
            }
        }
        else {
            String message = new String("Oops! You can only add orange blocks here!");
            JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);

        }
        return false;
    }
}

