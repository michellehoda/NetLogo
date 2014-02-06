package org.nlogo.deltatick.dnd;

import org.nlogo.app.DeltaTickTab;
import org.nlogo.deltatick.*;

import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTargetDragEvent;
import java.awt.dnd.DropTargetEvent;
import java.io.IOException;

public class BreedDropTarget
        extends DropTarget {

    DeltaTickTab deltaTickTab;

    public BreedDropTarget(BreedBlock block, DeltaTickTab deltaTickTab) {
        super(block);

        this.deltaTickTab = deltaTickTab;
    }

    protected boolean dropComponent(Transferable transferable)
            throws IOException, UnsupportedFlavorException {
        Object o = transferable.getTransferData(CodeBlock.codeBlockFlavor);
        if (o instanceof Component) {
            if (o instanceof ConditionBlock) {
                if (((BreedBlock) block).addedRectPanel) {
                    ((BreedBlock) block).hideRectPanel();
                    ((BreedBlock) block).addedRectPanel = false;
                }
                addCodeBlock((ConditionBlock) o);
                deltaTickTab.addCondition((ConditionBlock) o);
                return true;
            } else if (o instanceof BehaviorBlock) {
                if (((BreedBlock) block).addedRectPanel) {
                    ((BreedBlock) block).hideRectPanel();
                    ((BreedBlock) block).addedRectPanel = false;
                }
                addCodeBlock((BehaviorBlock) o);
                ((BehaviorBlock) o).setMyBreedBlock((BreedBlock) this.block);
                //Inform buildPanel that a reproduce block is being used to make slider on interface
                if (((BehaviorBlock) o).getIsMutate() == true) {
                    ((BreedBlock) block).setReproduceUsed(true);
                }

                // If breed has traits, and any trait is applicable to this behavior block then show a the panel
                if (((BreedBlock) block).numTraits() > 0) {
                    boolean addPanel = false;
                    for (String traitName : ((BehaviorBlock) o).getApplicableTraits()) {
                        if (((BreedBlock) block).hasTrait(traitName)) {
                            // Trait applies, add it directly
                            ((BehaviorBlock) o).removeBehaviorInput();
                            ((BehaviorBlock) o).setTrait(traitName, ((BreedBlock) block).getMyTraitBlock(traitName).getTraitOffsetVarName());
                            // addPanel = true;
                        }
                    }
                    if (addPanel) {
                        ((BehaviorBlock) o).removeBehaviorInput();
                        ((BehaviorBlock) o).addTraitblockPanel();
                    }
                }
//                addCodeBlock((BehaviorBlock) o);
                new BehaviorDropTarget((BehaviorBlock) o);
                return true;
            }
            else if (o instanceof TraitBlock) {
//                ((TraitBlock) o).setMyParent((BreedBlock) block);
//                TraitBlock tBlock = new TraitBlock((TraitBlock) o);
//                addCodeBlock(tBlock);
                //((TraitBlock) tBlock).setMyParent((BreedBlock) block);
                //deltaTickTab.addTrait((TraitBlock) tBlock);
                addCodeBlock((TraitBlock) o);
                ((TraitBlock) o).setMyParent((BreedBlock) block);
                deltaTickTab.addTrait((TraitBlock)o);
                return true;
            } else if (o instanceof OperatorBlock) {
                addCodeBlock((OperatorBlock) o);
                deltaTickTab.addOperator((OperatorBlock) o);
                return true;
            }
            //commented this out because I don't need patchBlocks to be dropped into breedBlocks -A. (Nov 27)
            //else if( o instanceof PatchBlock ) {
            //  addCodeBlock( (PatchBlock) o);
            //return true;
            // }
        }
        return false;
    }

    public void dragEnter(DropTargetDragEvent dtde) {
        if ((dtde.isDataFlavorSupported(CodeBlock.behaviorBlockFlavor)) ||
             (dtde.isDataFlavorSupported(CodeBlock.conditionBlockFlavor))) {
            ((BreedBlock) block).showRectPanel();
            ((BreedBlock) block).addedRectPanel = true;
        }
    }

    public void dragExit(DropTargetEvent dte) {
        if (((BreedBlock) block).addedRectPanel) {
            ((BreedBlock) block).hideRectPanel();
            ((BreedBlock) block).addedRectPanel = false;
        }
    }

}
