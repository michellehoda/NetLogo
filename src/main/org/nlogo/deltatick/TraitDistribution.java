package org.nlogo.deltatick;

import org.nlogo.swingx.MultiSplitLayout;
import org.nlogo.swingx.MultiSplitPane;

import javax.swing.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/18/13
 * Time: 9:07 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitDistribution
        extends MultiSplitPane {

    final int traitDistributionWidth = TraitPreview.TRAITPREVIEW_TOTAL_WIDTH;
    MultiSplitLayout.Node modelRoot;
    String breed;
    String trait;
    HashMap<String, String> selectedVariationsValues = new HashMap<String, String>();
    HashMap<String, String> selectedVariationsPercent = new HashMap<String, String>();
    MultiSplitLayout.Divider dDiv;

    public TraitDistribution() {
        this.breed = "filler";
        this.trait = "filler_trait";
        this.validate();

    }

    public TraitDistribution(String breed, String trait, HashMap<String, String> selectedVariationsValues) {
        this.breed = breed;
        this.trait = trait;
        this.selectedVariationsValues.clear();
        this.selectedVariationsValues.putAll(selectedVariationsValues);
        initComponents();
    }

    public TraitDistribution(String breed, String trait, HashMap<String, String> selectedVariationsValues, HashMap<String, String>selectedVariationsPercent) {
        this.breed = breed;
        this.trait = trait;
        this.selectedVariationsValues.clear();
        this.selectedVariationsValues.putAll(selectedVariationsValues);
        this.selectedVariationsPercent.clear();
        this.selectedVariationsPercent.putAll(selectedVariationsPercent);
        initComponents();
    }


    public void setBreed(String breed) {
        this.breed = breed;
    }

    public void setTrait(String trait) {
        this.trait = trait;
    }

    private void initComponents() {
        boolean addDummy = (selectedVariationsValues.size() == 1);

        String nodeName = new String();
        String layout = new String();
        layout = "(ROW ";
        double weights = 1.0 / selectedVariationsValues.size();
        double totalWeight = 0.0;

        String totalWeightStr = "";
        String weightsStr = "";

        int i = 0;
        for (Map.Entry<String, String> entry : selectedVariationsValues.entrySet()) {
            String variation = entry.getKey();
            String value = entry.getValue();
            nodeName = variation.replace(' ', 'X');
            nodeName += " ";

//            BigDecimal bd = new BigDecimal(totalWeight);
//            BigDecimal rd = bd.setScale(2, BigDecimal.ROUND_HALF_DOWN);
//            totalWeight = rd.doubleValue();
            if (selectedVariationsPercent.size() == selectedVariationsValues.size()) {
                weights = Double.parseDouble(selectedVariationsPercent.get(variation)) / 100.0;
            }

            if (totalWeight + weights > 1.0) {
                weights = 0.99 - totalWeight;
            }
            layout = layout + "(LEAF name="+nodeName+ " weight="+weights+ ") ";
            totalWeight = totalWeight + weights;
        } // for

        if (addDummy) {
            layout = "(ROW (LEAF name="+nodeName+" weight=1.0) (LEAF name=dummy weight=0.0))";
        }
        else {
            layout = layout + ")";
        }

        if (selectedVariationsValues.size() > 0) {
            modelRoot = MultiSplitLayout.parseModel(layout);
            this.getMultiSplitLayout().setModel(modelRoot);

            for (Map.Entry<String, String> entry : selectedVariationsValues.entrySet()) {
                String variation = entry.getKey();
                String value = entry.getValue();
                nodeName = variation.replace(' ', 'X');

                if (addDummy) {
                    String leafName = "all " + " have " + value + " " + trait;
                    JLabel leaf = new JLabel(leafName);
                    leaf.setHorizontalAlignment(SwingConstants.CENTER);
                    this.add(leaf, nodeName);
                    JLabel dummy = new JLabel("dummy");
                    dummy.setPreferredSize(new Dimension(0, 0));
                    // For dummy, make divider zero-size
                    this.getMultiSplitLayout().setDividerSize(0);

                }
                else {
                    String leafName = value;
                    JLabel leaf = new JLabel(leafName);
                    leaf.setHorizontalAlignment(SwingConstants.CENTER);
                    leaf.setPreferredSize(new Dimension(5,5));
                    //leaf.setMargin(new Insets(0,0,0,0));
                    leaf.setBounds(0, 0, 0, 0);
                    this.add(leaf, nodeName);

                    // More than one variation
                    this.getMultiSplitLayout().setDividerSize(20);
                }
            }

            MultiSplitLayout.Split split = (MultiSplitLayout.Split) this.getMultiSplitLayout().getModel().getParent().getChildren().get(0);
            for (MultiSplitLayout.Node node : split.getChildren()){
                if (node instanceof MultiSplitLayout.Leaf) {
                    Rectangle rect = node.getBounds();
                }
                else if ((addDummy == false) &&
                         (node instanceof MultiSplitLayout.Divider)) {
                    this.getDividerPainter().paint(this.getGraphics(), (MultiSplitLayout.Divider) node);
                }
            }
            //calculatePercentage();
            this.setVisible(true);
            this.revalidate();
        }
        else {
            this.setVisible(false);
        }
    }


    // This function is called when the divider is clicked-and-dragged to change widths
    // See TraitPreview. MouseMotionListener on traitDistribution
    public void updatePercentages() {
        this.revalidate();
        int traitDistriWidth = this.getWidth();
        String nodeName = new String();
        if (this.getMultiSplitLayout().getModel().getParent() != null) {
            MultiSplitLayout.Split split = (MultiSplitLayout.Split) this.getMultiSplitLayout().getModel().getParent().getChildren().get(0);

            for (MultiSplitLayout.Node node : split.getChildren()) {
                if (node instanceof MultiSplitLayout.Leaf) {
                    if (! ((MultiSplitLayout.Leaf) node).getName().equalsIgnoreCase("dummy")) {  //why is it entering dummy?
                        float totalDivider;
                        if (this.selectedVariationsValues.size() > 1) {
                            totalDivider = (this.selectedVariationsValues.size() - 1);
                        }
                        else {
                            totalDivider = 0;
                        }
                        Rectangle rect = node.getBounds();
                        float width = rect.width;
                        double percentage = ((double) width / (double) (traitDistriWidth - (totalDivider * this.getMultiSplitLayout().getDividerSize()))) * 100.0;
                        BigDecimal per = new BigDecimal(percentage);
                        BigDecimal p = per.setScale(3, BigDecimal.ROUND_HALF_EVEN);
                        String perc = p.toString();

                        nodeName = ((MultiSplitLayout.Leaf) node).getName();
                        String variation = nodeName.replace('X', ' ');
                        this.savePercentages(variation, perc);
                    }
                }
            }
        }

    } // updatePercentages

    public void savePercentages(String variation, String percentage) {
        selectedVariationsPercent.put(variation, percentage);
    }

    public HashMap<String, String> getSelectedVariationsPercent() {
        return selectedVariationsPercent;
    }

    class Slider extends JButton {
        public Slider(String text) {

        }

            public void paintComponent (Graphics g) {
                    Rectangle r = getBounds();
                    int x = r.x + 20;
                    int y = r.y + 20;
                    int width = r.width - 40;
                    int height = r.height- 40;
                    g.setColor(Color.BLACK);
                    g.fillOval(x, y, width, height);
                    x += 2;
                    y += 2;
                    width -= 4;
                    height -= 4;
                    g.setColor(getBackground());
                    g.fillOval(x, y, width, height);
                    g.setColor(getForeground());
                    y += (height / 2) - 10;
                    g.drawString(getText(), x, y);
                }


        }
    }

