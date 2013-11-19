package org.nlogo.deltatick;

import org.nlogo.deltatick.xml.Trait;
import org.nlogo.deltatick.xml.Variation;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by IntelliJ IDEA.
 * User: aditiwagh
 * Date: 2/23/13
 * Time: 3:59 PM
 * To change this template use File | Settings | File Templates.
 */
public class TraitPreview extends JPanel {

    private JFrame myFrame;

    //Buttons & text
    private javax.swing.JButton cancel;
    private javax.swing.JButton add;
    private JLabel traitText;

    JPanel traitDistriPanel;
    TraitDisplay traitDisplay;
    LabelPanel labelPanel;
    boolean isTraitSelected;
    String myBreedName;

    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JList myTraitsList;
    private String selectedTraitName;
    private Trait selectedTrait;
    private JList myVariationsList;
    //to store breed and corresponding trait
    ListSelectionModel listSelectionModel;

    JTable traitInfoTable;
    TraitTableModel traitTableModel;

    TraitDistribution traitDistribution;

    ArrayList<Trait> traitsList = new ArrayList<Trait>();

    // Number of colums in the trait info table
    public static final int NUMBER_COLUMNS = 3;
    // column index where value is stored/displayed
    public static final int VARVALUE_COLUMN_INDEX = 1;
    public static final int VARVALUE_COLUMN_WIDTH = 50;
    // column index where variation name is stored/displayed
    public static final int VARNAME_COLUMN_INDEX = 0;
    public static final int VARNAME_COLUMN_WIDTH = 75;
    public static final int VARCHECKBOX_COLUMN_INDEX = 2;
    public static final int VARCHECKBOX_COLUMN_WIDTH = 75;

    public static final int TRAIT_TEXT_HEIGHT = 20;
    public static final int TRAIT_SCROLLPANE_WIDTH = 150;
    public static final int TRAIT_SCROLLPANE_HEIGHT = 125;
    public static final int TRAIT_TABLE_WIDTH = VARVALUE_COLUMN_WIDTH+VARNAME_COLUMN_WIDTH+VARCHECKBOX_COLUMN_WIDTH;
    public static final int TRAIT_TABLE_HEIGHT = TRAIT_SCROLLPANE_HEIGHT;
    public static final int TRAIT_DISTRIPANEL_WIDTH = TRAIT_SCROLLPANE_WIDTH + TRAIT_TABLE_WIDTH;
    public static final int TRAIT_DISTRIPANEL_HEIGHT = 60;

    // TOTAL HEIGHT AND WIDTH OF TRAITPREVIEW
    public static final int TRAITPREVIEW_TOTAL_WIDTH = TRAIT_SCROLLPANE_WIDTH + TRAIT_TABLE_WIDTH;
    public static final int TRAITPREVIEW_TOTAL_HEIGHT = TRAIT_TEXT_HEIGHT+TRAIT_TABLE_HEIGHT+TRAIT_DISTRIPANEL_HEIGHT;

    // Holds selected traits (and variations) as selected by the user
    // This should be used to instantiate the trait block
    HashMap<String, TraitState> selectedTraitsMap = new HashMap<String, TraitState>();
    // This maps holds the traits and variations before any modifications are made
    // in the species inspector panel. Basically, this is the state to revert to
    // in case the user clicks cancel.
    HashMap<String, TraitState> origSelectedTraitsMap = new HashMap<String, TraitState>();



    public TraitPreview(String myBreedName, TraitDisplay traitDisplay, LabelPanel labelPanel, JFrame myFrame) {
        this.myFrame = myFrame;
        this.myBreedName = myBreedName;
        this.traitDisplay = traitDisplay;
        this.labelPanel = labelPanel;
        traitDisplay.setBackground(Color.BLACK);
        traitDisplay.revalidate();
        initComponents();
    }

    public void setTraits(ArrayList<Trait> list) {
        this.traitsList = list;
    }

    private String [] getTraitTypes() {
        String[] traitTypes = new String[traitsList.size()];
        int i = 0;
        for (Trait trait : traitsList) {
            traitTypes[i] = new String(trait.getNameTrait());
            i++;
        }
         return traitTypes;
    }

    public boolean getIsTraitSelected() {
        return isTraitSelected;
    }

    public void showMe() {
        final String[] traitStrings = getTraitTypes();
                //breedBlock.getTraitNames();
        myTraitsList = new JList(traitStrings);
        myTraitsList.setModel(new javax.swing.AbstractListModel() {
            public int getSize() {
                return traitStrings.length;
            }
            public Object getElementAt(int i) {
                return traitStrings[i];
            }
        });
        jScrollPane1.setViewportView(myTraitsList);

        this.setVisible(true);
    }

    public void setTraitsListListener(ListSelectionListener listSelectionListener) {

        showMe();

        listSelectionModel = myTraitsList.getSelectionModel();
        listSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        listSelectionModel.addListSelectionListener(listSelectionListener);
    }

    public void setTraitTableModelListener(TableModelListener tableModelListener) {
        traitInfoTable.getModel().addTableModelListener(tableModelListener);
    }

    // MOUSE LISTENER TO DETECT CHANGES TO traitDistribution
    class traitDistriMouseMotionListener implements MouseMotionListener {

        // Implement MouseMotionListener interfaces
        // mouseDragged: Triggered when mouse is clicked-and-dragged on the object
        public void mouseDragged(MouseEvent e) {

            // Calculate and update percentages
            traitDistribution.updatePercentages();

            // Percentages may have changed, update selectedTraitsMap
            updateSelectedTraitsMap(traitInfoTable.getModel());

            // Update pie chart
            updatePieChart();

        } // mouseDragged

        // Other interfaces that must be implemented. Empty implementation is okay.
        public void mouseMoved(MouseEvent e){}

    } // traitDistriMouseMotionListener

    private void updateSelectedTraitsMap(TableModel model) {

        // If any variations are selected, add that trait+variations to selectedTraitList
        // Else (no variations selected), remove that trait if present in selectedTraitList
        boolean someVariationSelected = false;
        //HashMap<String, Variation> tmpVarHashMap = new HashMap<String, Variation>(selectedTrait.getVariationHashMap());
        HashMap<String, Variation> tmpVarHashMap = new HashMap<String, Variation>();
        for (int row = 0; row < model.getRowCount(); row++) {
            someVariationSelected = someVariationSelected || ((Boolean) model.getValueAt(row, VARCHECKBOX_COLUMN_INDEX));

            if ((Boolean) model.getValueAt(row, VARCHECKBOX_COLUMN_INDEX) == true) {
                String variationName = (String) model.getValueAt(row, VARNAME_COLUMN_INDEX);
                Variation tmpVariation = new Variation(selectedTrait.getVariationHashMap().get(variationName));
                tmpVariation.value = new String((String) model.getValueAt(row, VARVALUE_COLUMN_INDEX));
                tmpVarHashMap.put(variationName, tmpVariation);
            }
        } // for

        /// Update state of selectedTraitsMap
        // Remove first
        selectedTraitsMap.remove(selectedTraitName);

        if (someVariationSelected) {
            TraitState tmpTraitState = new TraitState(selectedTrait, traitDistribution.getSelectedVariationsPercent());
            // Now set the percentages in tmpVarHashMap from traitDistribution.getSelectedVariationsPercent()
            for (Map.Entry<String, String> entry: traitDistribution.getSelectedVariationsPercent().entrySet()) {
                if (tmpVarHashMap.containsKey(entry.getKey())) {
                    tmpVarHashMap.get(entry.getKey()).percent = (int) Math.round(Double.parseDouble(entry.getValue()));
                }
            }
            tmpTraitState.getVariationHashMap().clear();
            tmpTraitState.getVariationHashMap().putAll(tmpVarHashMap);
            selectedTraitsMap.put(selectedTraitName, tmpTraitState);
        }
        /// Update complete

    } // updateSelectedTraitsMap

    // From the given table model (with the table data), this function updates the traitDistriPanel
    // Reads which variations are selected and generates a traitDistribution accordingly
    // Also updates/initialized percentages for piechart
    //readPercent: if going to a different trait, needs to read percentage from memory. if adding variation to already selectd
    // trait, then only needs to automatically set percentages, then readpercent = false

    private void updateTraitDistriPanel(TableModel model, boolean readPercent) {

        ArrayList <String> selectedVariations = new ArrayList<String>();
        for (int row = 0; row < model.getRowCount(); row++) {
            if ((Boolean) model.getValueAt(row, VARCHECKBOX_COLUMN_INDEX) == true) {
                selectedVariations.add((String) model.getValueAt(row, VARNAME_COLUMN_INDEX));
                //selectedVariations.add((String) model.getValueAt(row, VARVALUE_COLUMN_INDEX));
            }
        }
        traitDistriPanel.remove(traitDistribution);
        validate();
        if (readPercent &&
            selectedTraitsMap.containsKey(selectedTraitName)) {
            //traitDistribution = new TraitDistribution(breed, selectedTraitName, selectedVariations);
            //TODO: do not use traitState.selectedVariationsHashMap. Instead create a temp map by iterating over traitState.variations (March 31, 2013)
            traitDistribution = new TraitDistribution(myBreedName, selectedTraitName, selectedVariations, selectedTraitsMap.get(selectedTraitName).selectedVariationsPercent);
        }
        else {
            traitDistribution = new TraitDistribution(myBreedName, selectedTraitName, selectedVariations);
        }
        traitDistribution.addMouseMotionListener(new traitDistriMouseMotionListener());
        traitDistriPanel.add(traitDistribution);
        validate(); // this is important because it updates the jpanel -Aditi (feb 23, 2013)

        traitDistribution.updatePercentages();

    } // updateTraitDistrPanel

    // Updates the pie chart
    private void updatePieChart() {
        // Create a temp hash map for value and percent
        HashMap<String, String> tmpHashMap = new HashMap<String, String>();
        // Create a hashmap of values and percent
        for (Map.Entry<String, String> entry: traitDistribution.getSelectedVariationsPercent().entrySet()) {
            // Make sure "dummy" isn't included in tmpHashMap
            if (selectedTraitsMap.get(selectedTraitName).getVariationHashMap().containsKey(entry.getKey())) {
                tmpHashMap.put(selectedTraitsMap.get(selectedTraitName).getVariationHashMap().get(entry.getKey()).value, entry.getValue());
            }
        }

        // In order to make bar chart look like NetLogo histogram (i.e. blanks for variations not selected)
        // All the variations must be passed to the charts. The variations that are not selected will have
        // 0.0 as their percentage. The charts should be able to handle the zero values.
        if (tmpHashMap.size() > 0) {
            // Charts need to show gaps for non-selected variations only if at least one variation is selected.
            for (String variationValue : getTrait(getSelectedTraitName()).getVariationsValuesList().values()) {
                if (!tmpHashMap.containsKey(variationValue)) {
                    tmpHashMap.put(variationValue, "0.0");
                }
            }
        }

        // Pass values+percent hashmap to charts
        traitDisplay.updateChart(selectedTraitName, tmpHashMap);
        traitDisplay.revalidate();
    }

    //Create and update Checkboxes for LabelPanel
//    public void makeCheckBoxes() {
//        labelPanel = new LabelPanel();
//    }

    public void updateCheckBoxes(HashMap<String, TraitState> temp) {
            labelPanel.updateData(temp.keySet());

    }

    public String[] getVariationTypes(String traitName) {
        String [] variations = null;
        for (Trait trait : traitsList) {
            if (trait.getNameTrait().equalsIgnoreCase(traitName)) {
                variations = new String[trait.getVariationsList().size()];
                trait.getVariationsList().toArray(variations);
            }
        }
        return variations;
    }

    private Trait getTrait(String traitName) {
        Trait returnTrait = null;
        for (Trait trait : traitsList) {
            if (trait.getNameTrait().equalsIgnoreCase(traitName)) {
                returnTrait = trait;
                break;
            }
        }
        return returnTrait;
    }

    public String getSelectedTraitName() {
        selectedTraitName = myTraitsList.getSelectedValue().toString();
        return selectedTraitName;
    }

    public void setSelectedTrait(String traitName) {
        myTraitsList.setSelectedValue(traitName, true);
    }

    public LabelPanel getLabelPanel() {
        return labelPanel;
    }

    public void initComponents() {
        traitText = new JLabel("Trait");
        traitText.setPreferredSize(new Dimension(TRAIT_SCROLLPANE_WIDTH, TRAIT_TEXT_HEIGHT));

        jScrollPane1 = new JScrollPane();
        jScrollPane1.setPreferredSize(new Dimension(TRAIT_SCROLLPANE_WIDTH, TRAIT_SCROLLPANE_HEIGHT));
        jScrollPane1.setMaximumSize(new Dimension(TRAIT_SCROLLPANE_WIDTH, TRAIT_SCROLLPANE_HEIGHT));

        traitInfoTable = new JTable(new TraitTableModel());
        traitTableModel = new TraitTableModel();
        traitInfoTable.setModel(traitTableModel);

        traitDistriPanel = new JPanel();
        traitDistriPanel.setLayout(new BoxLayout(traitDistriPanel, BoxLayout.Y_AXIS));
        traitDistriPanel.setPreferredSize(new Dimension(TRAIT_DISTRIPANEL_WIDTH, TRAIT_DISTRIPANEL_HEIGHT));
        traitDistriPanel.setMinimumSize(new Dimension(TRAIT_DISTRIPANEL_WIDTH, TRAIT_DISTRIPANEL_HEIGHT));
        TitledBorder titleMidPanel;
        titleMidPanel = BorderFactory.createTitledBorder("Variations in " + myBreedName);
        traitDistriPanel.setBorder(titleMidPanel);

        traitDistribution = new TraitDistribution();
        traitDistribution.addMouseMotionListener(new traitDistriMouseMotionListener());
        traitDistriPanel.add(traitDistribution);

        traitDistriPanel.validate();

        traitInfoTable.setPreferredScrollableViewportSize(new Dimension(TRAIT_TABLE_WIDTH, TRAIT_TABLE_HEIGHT));
        traitInfoTable.setPreferredSize(new Dimension(TRAIT_TABLE_WIDTH, TRAIT_TABLE_HEIGHT));
        traitInfoTable.setMinimumSize(new Dimension(TRAIT_TABLE_WIDTH, TRAIT_TABLE_HEIGHT));
        traitInfoTable.validate();
        JTableHeader header = traitInfoTable.getTableHeader();
        initColumnSizes(traitInfoTable);

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup()
                        .add(layout.createSequentialGroup()
                                .add(layout.createParallelGroup()
                                        .add(traitText)
                                        .add(jScrollPane1)
                                )
                                .add(layout.createParallelGroup()
                                        .add(header)
                                        .add(traitInfoTable)
                                ))
                        .add(traitDistriPanel)
        );

        layout.setVerticalGroup(
                layout.createSequentialGroup()
                        .add(layout.createParallelGroup()
                                .add(traitText)
                                .add(header)
                        )
                        .add(layout.createParallelGroup()
                                .add(jScrollPane1)
                                .add(traitInfoTable)
                        )
                        .add(traitDistriPanel)
        );
        validate();
    }

    // This function is called by the handler when a trait is clicked on (in the trait selection list)
    @SuppressWarnings("unchecked")
    public void updateTraitSelection(ListSelectionEvent e) {

        ListSelectionModel lsm = (ListSelectionModel)e.getSource();
        //myVariationsList = new JList();
        if (lsm.isSelectionEmpty()) {
            System.out.println("No trait selected");
        }
        else {

            // gen data[][] based on selected trait
            ArrayList<Object[]> tempTableData = new ArrayList<Object[]>();
            Vector< Vector<Object> > tmpTableData = new Vector< Vector<Object> >();
            for (Trait trait : traitsList) {
                if (trait.getNameTrait().equalsIgnoreCase(getSelectedTraitName())) {
                    selectedTrait = trait;
                    for (Map.Entry<String, Variation> entry : trait.getVariationHashMap().entrySet()) {
                        String key = entry.getKey();
                        Variation var = entry.getValue();
                        Object[] row = new Object[NUMBER_COLUMNS];
                        Vector<Object> rrow = new Vector<Object>();
                        rrow.setSize(NUMBER_COLUMNS);

                        boolean varSelected = false;
                        String value = new String (var.value);
                        if (selectedTraitsMap.containsKey(getSelectedTraitName())) {
                            //check if the variationhashmap in trait state has the variation selected & update value from traitstate
                            varSelected = selectedTraitsMap.get(getSelectedTraitName()).getVariationHashMap().containsKey(key);
                            if (varSelected) {
                                value = new String(selectedTraitsMap.get(getSelectedTraitName()).getVariationHashMap().get(key).value);
                            }
                        }

                        row[VARVALUE_COLUMN_INDEX] = new String(value);
                        row[VARNAME_COLUMN_INDEX] = new String(key);
                        row[VARCHECKBOX_COLUMN_INDEX] = new Boolean(varSelected);
                        tempTableData.add(row);

                        rrow.set(VARVALUE_COLUMN_INDEX, new String(value));
                        rrow.set(VARNAME_COLUMN_INDEX, new String(key));
                        rrow.set(VARCHECKBOX_COLUMN_INDEX, new Boolean(varSelected));
                        tmpTableData.add(rrow);
                    } // for map
                } // trait match
            } // for trait

            // Send data to tablemodel
            //((TraitTableModel) traitInfoTable.getModel()).setTraitData(tempTableData);
            ((TraitTableModel) traitInfoTable.getModel()).setTraitData(tmpTableData);
            ((TraitTableModel) traitInfoTable.getModel()).setColumnName(VARVALUE_COLUMN_INDEX, getSelectedTraitName());
            traitInfoTable.validate();

            // Pre-sort the table
            traitInfoTable.setAutoCreateRowSorter(true);
            RowSorter<TraitTableModel> sorter = (RowSorter<TraitTableModel>) traitInfoTable.getRowSorter();
            sorter.setSortKeys(Arrays.asList(new RowSorter.SortKey(VARVALUE_COLUMN_INDEX, SortOrder.ASCENDING)));
            //TableRowSorter sorter = new TableRowSorter<TraitTableModel>((TraitTableModel) traitInfoTable.getModel());
            //traitInfoTable.setRowSorter(sorter);
            //sorter.setSortsOnUpdates(true);
            // Presort done

            // Set center alignment
            DefaultTableCellRenderer dctr = new DefaultTableCellRenderer();
            dctr.setHorizontalAlignment(JLabel.CENTER);
            dctr.setHorizontalTextPosition(JLabel.CENTER);
            traitInfoTable.getColumnModel().getColumn(VARVALUE_COLUMN_INDEX).setCellRenderer(dctr);
            //traitInfoTable.getColumnModel().getColumn(VARNAME_COLUMN_INDEX).setCellRenderer(dctr);
            //traitInfoTable.getColumnModel().getColumn(VARCHECKBOX_COLUMN_INDEX).setCellRenderer(dctr);


            // Can/Must read percentages from selectedTraitsMaps or from memory based on what was previously done
            updateTraitDistriPanel(traitInfoTable.getModel(), true);

            updatePieChart();

        } // else
    }

    public void updateVariationSelection(TableModelEvent e) {
        TableModel model = (TableModel)e.getSource();

        int value = 0;
        String s = new String("");

        int row = e.getFirstRow();
        int col = e.getColumn();
    //Giving kids the option of setting a value for their variation (April 14, 2013)
//        if ((Boolean) model.getValueAt(row, col)) {
//            while (!(s.matches("\\d+"))) {
//                s = (String)JOptionPane.showInputDialog(
//                        myFrame,
//                        selectedTrait.getMessage(),
//                        "Customized Dialog",
//                        JOptionPane.PLAIN_MESSAGE,
//                        null,
//                        null,
//                        null);
//            }
//            value = Integer.parseInt(s);
//            model.setValueAt(s, row, VARVALUE_COLUMN_INDEX);
//        }

        // A new variation has been added/removed. Previous percentages are invalid
        // Hence no need to read percentages from selectedTraitsMap. Set 2nd parameter to false
        updateTraitDistriPanel(model, false);

        // Some variation selected/unselected
        // Update the hash map
        // In TableModelListener, map can ONLY be updated after updateTraitDistriPanel()
        updateSelectedTraitsMap(model);

        // Update chart to reflect percentages
        // This must be called *AFTER* updateSelectedTraitsMap because it reads variation values from the hashmap
        updatePieChart();

        updateCheckBoxes(selectedTraitsMap);

    }

    private void initColumnSizes(JTable table) {
        TraitTableModel model = (TraitTableModel)table.getModel();
        TableColumn column = null;
        Component comp = null;
        int headerWidth = 0;
        int cellWidth = 0;
        // TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();

        table.getColumnModel().getColumn(VARVALUE_COLUMN_INDEX).setPreferredWidth(VARVALUE_COLUMN_WIDTH);
        table.getColumnModel().getColumn(VARNAME_COLUMN_INDEX).setPreferredWidth(VARNAME_COLUMN_WIDTH);
        table.getColumnModel().getColumn(VARCHECKBOX_COLUMN_INDEX).setPreferredWidth(VARCHECKBOX_COLUMN_WIDTH);
    }


    //class TraitTableModel extends AbstractTableModel {
    class TraitTableModel extends DefaultTableModel {
        //private String[] columnNames = {"Add Variation?", "Description", "Value"};
        private Vector columnNames = new Vector<String>();
        //, "Edit"};

        private ArrayList<Object[]> tableData = new ArrayList<Object[]>();

        @SuppressWarnings("unchecked")
        TraitTableModel() {
            super();
            //dataVector = new Vector< Vector<Object> >();
            columnNames.setSize(NUMBER_COLUMNS);
            ((Vector<String>) columnNames).set(VARVALUE_COLUMN_INDEX, "Value");
            ((Vector<String>) columnNames).set(VARNAME_COLUMN_INDEX, "Description");
            ((Vector<String>) columnNames).set(VARCHECKBOX_COLUMN_INDEX, "Add Variation?");

            setColumnIdentifiers(columnNames);
            reset();
        }

        public void setTraitData(ArrayList<Object[]> source) {
            // Clear previous data
            tableData.clear();

            for (int i = 0; i < source.size(); i++) { // Clear the row (must start with empty row)
                Object[] row = new Object[NUMBER_COLUMNS];         // Generate the row
                for (int j = 0; j < NUMBER_COLUMNS; j++) {  // for j, Add the row to tableData
                    row[j] = source.get(i)[j];
                }
                tableData.add(row);
            } // for i
        }

        @SuppressWarnings("unchecked")
        public void setTraitData(Vector< Vector<Object> > source) {
            dataVector.clear();
            Vector myDataVector = new Vector< Vector<Object> >();
            for (int i = 0; i < source.size(); ++i) {
                Vector<Object> tVector = new Vector<Object>();
                for (int j = 0; j < source.get(i).size(); ++j) {
                    tVector.add(source.get(i).get(j));
                }
                myDataVector.add(tVector);
            }
            setDataVector(myDataVector, columnNames);
        }

        public void reset() {
            //dataVector.clear();
            setRowCount(0);
        }

        public boolean isCellEditable(int rowIndex, int columnIndex){
            return  (columnIndex == VARCHECKBOX_COLUMN_INDEX);
        }

//        public void setValueAt(Object value, int row, int col) {
//            //tableData.get(row)[col] = value;
//            Vector< Vector<Object> > myDataVector = new Vector(getDataVector());
//            //((Vector<Object>) dataVector.get(row)).set(col, value);
//            myDataVector.get(row).set(col, value);
//            setDataVector(myDataVector, columnNames);
//
//            fireTableCellUpdated(row, col);
//            // Fire all rows updated for re-sorting table
//            // fireTableRowsUpdated(0, getDataVector().size() - 1);
//        }

        /*
         * JTable uses this method to determine the default renderer/
         * editor for each cell.  If we didn't implement this method,
         * then the last column would contain text ("true"/"false"),
         * rather than a check box.
         */

        public Class getColumnClass(int c) {
            switch (c) {
                case VARVALUE_COLUMN_INDEX: { return String.class; }
                case VARNAME_COLUMN_INDEX: { return String.class; }
                case VARCHECKBOX_COLUMN_INDEX: { return Boolean.class; }
                default: { return null; }
            }
            //return getValueAt(0, c).getClass();
        }

        public int getColumnCount() {
            return columnNames.size();
        }

//       public int getRowCount() {
//           //return tableData.size();
//           return getDataVector().size();
//       }

//       public String getColumnName(int col) {
//            return (String) columnNames.get(col);
//       }

        @SuppressWarnings("unchecked")
        public void setColumnName(int col, String columnName) {
            if (col < NUMBER_COLUMNS) {
                ((Vector<String>) columnNames).set(col, columnName);
                //columnNames[col] = new String(columnName);
                setColumnIdentifiers(columnNames);
            }
        }

//       public Object getValueAt(int row, int col) {
//           //return tableData.get(row)[col];
//           Vector< Vector<Object> > myDataVector = new Vector(getDataVector());
//           //return ((Vector<Object>) dataVector.get(row)).get(col);
//           return myDataVector.get(row).get(col);
//       }
    }

    public int getTotalWidth() {
        return (TRAITPREVIEW_TOTAL_WIDTH + 50);
    }
    public int getTotalHeight() {
        return (TRAITPREVIEW_TOTAL_HEIGHT);// + LabelPanel.LABELPANEL_HEIGHT);
    }

    public TraitDistribution getTraitDistribution() {
        return traitDistribution;
    }

    public HashMap<String, TraitState> getTraitStateMap() {
        return selectedTraitsMap;
    }
    public HashMap<String, TraitState> getOrigTraitStateMap() {
        return origSelectedTraitsMap;
    }

    public void setSelectedTraitsMap(HashMap<String, TraitState> hashMap) {
        selectedTraitsMap.clear();
        // Does putAll perform deep copy?
        //selectedTraitsMap.putAll(hashMap);

        // Must perform deep copy
        for (Map.Entry<String, TraitState> entry : hashMap.entrySet()) {
            selectedTraitsMap.put(entry.getKey(), new TraitState(entry.getValue()));
        }

        // Add label panel checkboxes
        labelPanel.updateData(hashMap.keySet());
    }

    // Copies selectedTraitsMap to origSelectedTraitsMap (for fututre use)
    // This function must be called when the user presses okay
    public void saveOrigSelectedTraitsMap() {
        origSelectedTraitsMap.clear();
        for (Map.Entry<String, TraitState> entry : selectedTraitsMap.entrySet()) {
            origSelectedTraitsMap.put(entry.getKey(), new TraitState(entry.getValue()));
        }
    }
    // Loads the origSelectedTraitsMap
    // This function must be called when the user clicks on the on the inspect species button
    public void loadOrigSelectedTraitsMap() {
        // Copy existing traits to selectedTraitsMap
        setSelectedTraitsMap(origSelectedTraitsMap);

        // Clear the table
        ((TraitTableModel)traitInfoTable.getModel()).reset();

        // Make sure only checkboxes corresponding to existing traits are present
        updateCheckBoxes(origSelectedTraitsMap);

        // Make sure no trait is selected
        myTraitsList.clearSelection();

        // Display charts for only the existing traits
        traitDisplay.updateCharts(selectedTraitsMap);

        ((TitledBorder) traitDistriPanel.getBorder()).setTitle("Variations in " + myBreedName);
        traitDistriPanel.remove(traitDistribution);
        traitDistriPanel.validate();
    }

}







