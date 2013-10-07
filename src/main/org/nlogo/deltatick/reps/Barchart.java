package org.nlogo.deltatick.reps;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.DefaultCategoryItemRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.DefaultKeyedValues;
import org.jfree.data.KeyedValues;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import org.nlogo.deltatick.TraitDisplay;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
* Created by IntelliJ IDEA.
* User: aditiwagh
* Date: 2/25/13
* Time: 3:18 PM
* To change this template use File | Settings | File Templates.
*/
public class Barchart extends JPanel {
    String trait;
    ChartPanel chartPanel;
    JFreeChart chart;
    CategoryDataset dataset;
    TreeMap<String, Double> selectedVariationsPerc;
    TraitDisplay.PaintSupplier paintSupplier;

    public static final int BARCHART_WIDTH = 250;
    public static final int BARCHART_HEIGHT = 250;

//    public Barchart() {
//        trait = new String("Pick a trait");
//        dataset = new DefaultCategoryDataset();
//        selectedVariationsPerc = new HashMap<String, Double>();
//    }
    public Barchart(String traitName, TraitDisplay.PaintSupplier paintSupplier) {

        trait = traitName;
        this.paintSupplier = paintSupplier;
        dataset = new DefaultCategoryDataset();
        selectedVariationsPerc = new TreeMap<String, Double>();
        dataset = (DefaultCategoryDataset) createDataset();
        chart = createChart(dataset);

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(BARCHART_WIDTH, BARCHART_HEIGHT));
        this.setVisible(true);
        this.validate();
    }

    private CategoryDataset createDataset() {
        DefaultKeyedValues keyedValues = new DefaultKeyedValues();
        for (Map.Entry entry: selectedVariationsPerc.entrySet()) {
            keyedValues.addValue((String) entry.getKey(), (Double) entry.getValue());
        }
        this.dataset = DatasetUtilities.createCategoryDataset("Series", keyedValues);
        return this.dataset;
    }

    private JFreeChart createChart(CategoryDataset dataset) {
        chart = ChartFactory.createBarChart(
                //trait,
                " ",
                "Variation",
                "Percentage",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                false,
                false
        );

        // Set Background white
        chart.setBorderVisible(false);
        chart.setBackgroundPaint(null);

        // Get reference to plot for further customizaiton
        final CategoryPlot plot = (CategoryPlot) chart.getPlot();
        plot.setBackgroundPaint(null);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.getDomainAxis().setCategoryMargin(0.0);

        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setRange(0, 100);

        // Customize bars
        paintSupplier.reset();
        final BarRenderer customBarRenderer = new CustomBarRenderer(paintSupplier);
        customBarRenderer.setDrawBarOutline(true);
        customBarRenderer.setMaximumBarWidth(1.0);
        customBarRenderer.setShadowVisible(false);
        customBarRenderer.setItemMargin(0.0);
        customBarRenderer.setBarPainter(new StandardBarPainter());
        plot.setRenderer(customBarRenderer);

        return chart;
    }

    public void updateChart(String trait, HashMap<String, String> varPercent) {

        this.selectedVariationsPerc.clear();
        for(Map.Entry entry: varPercent.entrySet()) {
            //if ((int) Math.round(Double.parseDouble((String) entry.getValue())) > 0) {
                selectedVariationsPerc.put((String) entry.getKey(), Double.parseDouble((String) entry.getValue()));
            //}
        } // for

        this.trait = trait;
        CategoryDataset dataset = createDataset();
        chart = createChart(dataset);

        chartPanel.setChart(chart);
        chartPanel.setPreferredSize(new Dimension(BARCHART_WIDTH, BARCHART_HEIGHT));

        if (varPercent.size() > 0) {
            chartPanel.setVisible(true);
        }
        else {
            chartPanel.setVisible(false);
        }

        chartPanel.revalidate();

        this.setVisible(true);
        this.validate();
    }

    public JPanel getChartPanel() {
        return chartPanel;
    }

    public class CustomBarRenderer extends BarRenderer {
        TraitDisplay.PaintSupplier paintSupplier;

        /**
         * Creates a new Bar Renderer
         * @param paintSupplier
         */
        public CustomBarRenderer(TraitDisplay.PaintSupplier paintSupplier) {
            this.paintSupplier = paintSupplier;
        }
        public Paint getItemPaint(final int row, final int column) {
            // Make bars transparent (similar to NetLogo histograms)
            //return new Color(0, 0, 255, 0);
            // For colored bars, use the following line instead of the one for transparent colors
             return paintSupplier.getNextPaint();
        }
    }

}
