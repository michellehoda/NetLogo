package org.nlogo.deltatick.dnd;

import org.nlogo.deltatick.BehaviorBlock;
import org.nlogo.deltatick.CodeBlock;
import org.nlogo.deltatick.QuantityBlock;
import org.nlogo.deltatick.TraitBlockNew;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 9/27/13
 * Time: 10:40 AM
 * To change this template use File | Settings | File Templates.
 */
public class QuantityDropTarget
        extends DropTarget {

            QuantityBlock qBlock;

            public QuantityDropTarget(QuantityBlock qBlock) {
                super(qBlock);
                this.qBlock = qBlock;
            }

            protected boolean dropComponent(Transferable transferable)
                    throws IOException, UnsupportedFlavorException {
                Object o = transferable.getTransferData(CodeBlock.codeBlockFlavor);
                if (o instanceof Component) {
                    if (o instanceof TraitBlockNew) {
                            addCodeBlock((TraitBlockNew) o);
                            ((TraitBlockNew) o).setMyParent(qBlock);
                            ((TraitBlockNew) o).hideRemoveButton();
                            qBlock.setHistoTrait((TraitBlockNew) o);
                            qBlock.removeInput();
                            qBlock.removeTraitblockPanel();
                            qBlock.validate();
                            ((TraitBlockNew) o).validate();

                            return true;

                    }
                }
                return false;
            }
        }



