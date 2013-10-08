package org.nlogo.deltatick.reps;


import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.PiePlot;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.general.PieDataset;
import org.nlogo.deltatick.TraitDisplay;

import javax.swing.JPanel;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/26/13
 * Time: 9:57 PM
 * To change this template use File | Settings | File Templates.
 */
public class Piechart extends JPanel {

    TreeMap<String, Double> selectedVariationsPerc;
    String trait;
    ChartPanel chartPanel;
    JFreeChart chart;
    DefaultPieDataset dataset;
    TraitDisplay.PaintSupplier paintSupplier;
    double startAngle;

    public static final int PIECHART_WIDTH = 230;
    public static final int PIECHART_HEIGHT = 250;

    public Piechart(String traitName, TraitDisplay.PaintSupplier paintSupplier) {
        trait = traitName;
        this.paintSupplier = paintSupplier;
        startAngle = Math.random() * 360.0;
        dataset = new DefaultPieDataset();
        selectedVariationsPerc = new TreeMap<String, Double>();
        dataset = (DefaultPieDataset) createDataset();
        chart = createChart(dataset);

        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(PIECHART_WIDTH, PIECHART_HEIGHT));
        chartPanel.setMaximumSize(new Dimension(PIECHART_WIDTH, PIECHART_HEIGHT));
        //chartPanel.setMaximumSize(new Dimension(250,250));
        this.setVisible(true);
        this.validate();
    }

    //used to be "static", changed it because i will have more than one dataset -Aditi (feb 27, 2013)?
    private PieDataset createDataset() {
        dataset.clear();
        for (Map.Entry entry: selectedVariationsPerc.entrySet()) {
            dataset.setValue((String) entry.getKey(), (Double) entry.getValue());
        }

        return dataset;
    }

    // public static
    private JFreeChart createChart(PieDataset dataset) {
        //JFreeChart
         chart = ChartFactory.createPieChart(
            trait,  // chart title
            dataset,             // data
            //true,               // include legend
            false,
            true,
            false
        );

        chart.setBorderVisible(false);
        chart.setBackgroundPaint(null);
        chart.setBorderVisible(false);

        PiePlot plot = (PiePlot) chart.getPlot();

        plot.setSectionOutlinesVisible(false);
        plot.setIgnoreZeroValues(true);
        plot.setLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        //plot.setSimpleLabels(true);
        plot.setNoDataMessage("No data available");
        plot.setCircular(false);
        plot.setLabelGap(0.02);
        plot.setBackgroundPaint(null);
        DecimalFormat df = (DecimalFormat) NumberFormat.getPercentInstance(Locale.US);
        df.applyPattern("##.##%");
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator("{0} ({2})", df, df));
        plot.setSimpleLabels(true);
        plot.setShadowPaint(null);
        plot.setStartAngle(startAngle);
        plot.setOutlineVisible(false);

        // Customize label
        // Background transparent, no border/outline
        plot.setLabelBackgroundPaint(new Color(0xFF, 0xFF, 0xFF, 0));
        plot.setLabelOutlinePaint(null);
        // Font
        plot.setLabelFont(new Font("Courier New", Font.PLAIN, 14));
        // No shadow
        plot.setLabelShadowPaint(null);

        paintSupplier.reset();
        for (Map.Entry<String, Double> entry: selectedVariationsPerc.entrySet()) {
            //if (entry.getValue() > 0.0) {
                plot.setSectionPaint((String) entry.getKey(), paintSupplier.getNextPaint());
            //}
        }
        // Set Colors
        return chart;
    }


    public JPanel getChartPanel() {
        return chartPanel;
    }

    public void updateChart(String trait, HashMap<String, String> varPercent) {

        this.selectedVariationsPerc.clear();
        for(Map.Entry entry: varPercent.entrySet()) {
            //if ((int) Math.round(Double.parseDouble((String) entry.getValue())) > 0) {
                selectedVariationsPerc.put((String) entry.getKey(), Double.parseDouble((String) entry.getValue()));
            //}
        } // for


        this.trait = trait;
        PieDataset dataset = createDataset();

        chart = createChart(dataset);
//        chart.setTitle(this.trait);
//        ((PiePlot) chart.getPlot()).setDataset(dataset);

        chartPanel.setChart(chart);


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
    private class PiechartMouseListener implements MouseListener {
        public void mouseReleased(MouseEvent e) {
        }
        public void mouseEntered(MouseEvent e) {
        }
        public void mouseClicked(MouseEvent e) {
        }
        public void mouseExited(MouseEvent e) {
        }
        public void mousePressed(MouseEvent e) {
            System.out.println("X = " + e.getX() + " Y = " + e.getY());
        }
    }

}
