package org.nlogo.deltatick.reps;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import org.nlogo.deltatick.TraitDisplay;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
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
    DefaultCategoryDataset dataset;
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

        this.dataset.clear();
        for (Map.Entry entry: selectedVariationsPerc.entrySet()) {
            this.dataset.addValue((Double) entry.getValue(), (String) entry.getKey(), (String) entry.getKey());
        } // for
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


        // set the range axis to display integers only...
        final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
        rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        rangeAxis.setRange(0, 100);

        // disable bar outlines...
        final BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.25); // 25% of total width
        renderer.setShadowVisible(false);
        renderer.setBarPainter(new StandardBarPainter());
        if( dataset.getColumnCount() > 2) {
            renderer.setItemMargin(-2);
        }
        else {
            renderer.setItemMargin(-1);
        }

        int i = 0;
        paintSupplier.reset();
        for (Map.Entry entry: selectedVariationsPerc.entrySet()) {
            renderer.setSeriesPaint(i, paintSupplier.getNextPaint());
            i++;
        }


        return chart;
    }

    public void updateChart(String trait, HashMap<String, String> varPercent) {

        this.selectedVariationsPerc.clear();
        for(Map.Entry entry: varPercent.entrySet()) {
            if ((int) Math.round(Double.parseDouble((String) entry.getValue())) > 0) {
                selectedVariationsPerc.put((String) entry.getKey(), Double.parseDouble((String) entry.getValue()));
            }
        } // for

        this.trait = trait;
        CategoryDataset dataset = createDataset();
        chart = createChart(dataset);

        //chart.setTitle(this.trait);
        //((CategoryPlot) chart.getPlot()).setDataset(dataset);

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

}