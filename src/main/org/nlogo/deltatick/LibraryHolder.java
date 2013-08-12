package org.nlogo.deltatick;

import javax.swing.*;
import javax.swing.JTabbedPane;
import javax.swing.JPanel;
import javax.swing.JComponent;
import java.awt.*;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Condition;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.nlogo.deltatick.*;
import org.nlogo.deltatick.xml.Trait;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/21/12
 * Time: 8:00 PM
 * To change this template use File | Settings | File Templates.
 */

    // will need an array of tabs/panels so learners can open multiple libraries, and close any of them when they want
    //(Feb 21, 2012)
    //TODO: Get name of file for name of panel and ability to close tabs

public class LibraryHolder extends JPanel {
    private JTabbedPane tabbedPane;
    private int countTabs;
    private int currentTab;
    JComponent panel;
    JScrollPane tabScrollPane;
    JButton exit;
    ArrayList<JComponent> arrayPanels = new ArrayList<JComponent>();
    int policy;
    int newPolicy;
    // should eventually have an array of panels that you can add to and remove from (Feb 21, 2012)
    Tab tab;
    List<TraitBlock> traits = new LinkedList<TraitBlock>();
    List<TraitBlockNew> traitsNew = new LinkedList<TraitBlockNew>();
    List<OperatorBlock> operatorBlocksList = new LinkedList<OperatorBlock>();
    ArrayList<BehaviorBlock> behaviorBlocksList = new ArrayList<BehaviorBlock>();
    ArrayList<ConditionBlock> conditionBlocksList = new ArrayList<ConditionBlock>();
    //ArrayList<QuantityBlock> quantityBlocksList = new ArrayList<QuantityBlock>();
    Map<String, QuantityBlock> quantityBlocksMap = new HashMap<String, QuantityBlock>();
    ArrayList<String> variations;



    public LibraryHolder() {
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        this.setPreferredSize(new Dimension(260, 0));
        tabbedPane = new JTabbedPane();
        add(tabbedPane);
        tabbedPane.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent e) {
                }
        });
        countTabs = 0;
        currentTab = 0;
    }

     // This is called only for TraitBlock & operatorBlock
      //TODo This changes the name of the tab to "panel" (March 9)
    //Commented out to try TraitBlockNew (March 16, 2013)
//    public void addTraittoTab( TraitBlock block, int numberTraits ) {
//        String tabName = new String("Your blocks" + numberTraits);
//
//        if (numberTraits == 1) {
//            panel = (JComponent) new JPanel();
//            panel.setLayout( new BoxLayout (panel, BoxLayout.Y_AXIS) );
//        }
//        panel.add(block);
//        tabbedPane.addTab(tabName , panel);
//        traits.add(block);
//    }

    public void addTraittoTab( TraitBlockNew block, int numberTraits ) {
        String tabName = new String("Your blocks");

        // Following 2 pieces commented to figure out if traitblock can be added to the same panel as library blocks
//        if (numberTraits == 1) {
//            panel = (JComponent) new JPanel();
//            panel.setLayout( new BoxLayout (panel, BoxLayout.Y_AXIS) );
//        }
//        panel.add(block);
//        tabbedPane.addTab(tabName, panel);
        addBlock(block);
        traitsNew.add(block);
    }

    public void removeTraitBlock ( TraitBlockNew tBlock ) {
        traits.remove(tBlock);
        panel.remove(tBlock);
        panel.validate();
    }

    public void removeTab(int i){
    	tabbedPane.removeTabAt(i);

    	return;
    }

    public void addOperatortoTab( OperatorBlock oBlock ) {
        panel.add(oBlock);
        tabbedPane.addTab( "Your blocks", panel );
        operatorBlocksList.add(oBlock);
    }

    public void makeNewTab() {
        JPanel somePanel = new JPanel();
        panel = (JComponent) new JPanel();
        panel.setLayout( new BoxLayout (panel, BoxLayout.Y_AXIS) );
        //JScrollPane sp = new JScrollPane(panel);
        tabScrollPane = new JScrollPane();
        tabScrollPane.setViewportView(panel);
        //tabScrollPane.setPreferredSize(new Dimension (300, 600));
        tabScrollPane.setVisible(true);
        tabScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        //tabbedPane.addTab( "name", panel );
        // somePanel.add(tabScrollPane);
        //tabbedPane.addTab( "name", somePanel );
        tabbedPane.addTab( "name", tabScrollPane );
        panel.revalidate();
        tabScrollPane.revalidate();
        JButton close = new JButton();
        countTabs++;
       }


    public void addBlock ( CodeBlock block ) {
        panel.add ( block );
        //tabScrollPane.add( block );
        //panel.setPreferredSize(new Dimension(block.getPreferredWidth() + 30, 600));
        //tabScrollPane.setPreferredSize(new Dimension(block.getPreferredWidth() + 30, 600));
        panel.revalidate();
        tabScrollPane.revalidate();
        tabbedPane.revalidate();
    }

    public void setTabName( String name ) {
        // TODO Change to using indexOfTabComponent
        tabbedPane.setTitleAt((countTabs - 1), name);
    }

    public String[] getTrait() {
        String[] names = new String [traits.size()];
        int i = 0;
        for (TraitBlock trait : traits) {
            names[i] = trait.getTraitName();
            i++;
        }
        return names;
    }

    public void clear() {
        //traits.clear();

    }
    public List<TraitBlockNew> getTraitBlocks() {
        return traitsNew;
    }

    public void addToBehaviorBlocksList(BehaviorBlock behBlock) {
        behaviorBlocksList.add(behBlock);
    }
    public ArrayList<BehaviorBlock> getBehaviorBlocksList() {
        return behaviorBlocksList;
    }
    public BehaviorBlock getBehaviorBlock(String targetBlockName) {
        for (BehaviorBlock behaviorBlock : behaviorBlocksList) {
            if (behaviorBlock.getName().equalsIgnoreCase(targetBlockName)) {
                return behaviorBlock;
            }
        }
        return null;
    }

    public void addToConditionBlocksList(ConditionBlock conditionBlock) {
        conditionBlocksList.add(conditionBlock);
    }

    public ArrayList<ConditionBlock> getConditionBlocksList() {
        return conditionBlocksList;
    }

    public void addToQuantityBlocksMap(QuantityBlock quantityBlock) {
        quantityBlocksMap.put(quantityBlock.getName(), quantityBlock);
    }
    public Map<String, QuantityBlock> getQuantityBlocksMap() {
        return quantityBlocksMap;
    }

    public int getCountTabs(){
    	return countTabs;
    }

}
