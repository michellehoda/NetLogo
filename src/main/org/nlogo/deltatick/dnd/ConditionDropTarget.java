package org.nlogo.deltatick.dnd;

import org.nlogo.api.Patch;
import org.nlogo.deltatick.*;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

public class ConditionDropTarget
        extends DropTarget {

    public ConditionDropTarget(ConditionBlock cBlock) {
        super(cBlock);
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.codeBlockFlavor);
        if (o instanceof Component) {
            if (o instanceof BehaviorBlock) {
                System.out.println("Dropping behavior block");

                // Set this behavior's parent BreedBlock
                //((BehaviorBlock) o).setMyBreedBlock(((BreedBlock) block.getMyParent()));
                ((BehaviorBlock) o).setMyBreedBlock(((BreedBlock) block.getMyBreedBlock()));

                if (((BehaviorBlock) o).getIsMutate() == true) {

                    //((BreedBlock) (block).getMyParent()).setReproduceUsed(true);
                    ((BreedBlock) (block).getMyBreedBlock()).setReproduceUsed(true);
                }
                // If breed has traits, and any trait is applicable to this behavior block then show a the panel
                //if (((BreedBlock) block.getMyParent()).numTraits() > 0) {
                if (((BreedBlock) block.getMyBreedBlock()).numTraits() > 0) {
                    boolean addPanel = false;
                    for (String traitName : ((BehaviorBlock) o).getApplicableTraits()) {
                        if (((BreedBlock) block).hasTrait(traitName)) {
                            addPanel = true;
                        }
                    }
                    if (addPanel) {
                        ((BehaviorBlock) o).removeBehaviorInput();
                        ((BehaviorBlock) o).addTraitblockPanel();
                    }
                }
                addCodeBlock((BehaviorBlock) o);
                new BehaviorDropTarget((BehaviorBlock) o);
                return true;
            }
            if (o instanceof ConditionBlock) {
                addCodeBlock((ConditionBlock) o);
                new ConditionDropTarget((ConditionBlock) o);
                return true;
            }
            if (o instanceof PatchBlock) {
                addCodeBlock((PatchBlock) o);
                //new ConditionDropTarget((PatchBlock) o);
                return true;
            }
            //return false; - commented out by A. (nov 27)
        }
        return false;
    }
}
