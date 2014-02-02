package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.*;

import javax.swing.JOptionPane;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 3/16/13
 * Time: 6:17 PM
 * To change this template use File | Settings | File Templates.
 */
public class BehaviorDropTarget
        extends DropTarget {

    BehaviorBlock behBlock;

    public BehaviorDropTarget(BehaviorBlock bBlock) {
        super(bBlock);
        this.behBlock = bBlock;
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.codeBlockFlavor);
        if (o instanceof Component) {
            if (o instanceof TraitBlockNew) {

                if (! behBlock.getMyBreedBlock().plural().equalsIgnoreCase(((TraitBlockNew) o).getBreedName()) ) {
                    String message = "Oops! This block does not belong to this species!";
                    JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
                    return false;
                }
                else if (
                      !behBlock.getIsTrait() &&
                      behBlock.getApplicableTraits().contains(((TraitBlockNew) o).getTraitName()) ) {

                    addCodeBlock((TraitBlockNew) o);
                    ((TraitBlockNew) o).setMyParent(behBlock.getMyBreedBlock());
                    //((TraitBlockNew) o).lookBetter();

                    behBlock.removeTraitblockPanel();
                    behBlock.removeBehaviorInput(); // assuming only one behaviorInput so will correspond to trait (March 25, 2013)
                    behBlock.setTrait(((TraitBlockNew) o).getTraitName());
                    behBlock.getMyBreedBlock().addBlock((TraitBlockNew) o);// so BreedBlock knows it has a traitBlock in one of its behBlocks (March 25, 2013)

                    behBlock.validate();
                    ((TraitBlockNew) o).validate();

                    return true;
                }
                else if (
                        !behBlock.getIsTrait() &&
                                behBlock.getApplicableTraits().contains(((TraitBlockNew) o).getTraitName()) == false) {
                    String message = "";
                    message += "Oops, " + ((TraitBlockNew) o).getTraitName() +
                            " does not make sense in " + behBlock.getName() + "!";
                    JOptionPane.showMessageDialog(null, message, "Oops!", JOptionPane.INFORMATION_MESSAGE);
                }
                else {
                    return false;
                }
            }
        }
        return false;
    }
}

