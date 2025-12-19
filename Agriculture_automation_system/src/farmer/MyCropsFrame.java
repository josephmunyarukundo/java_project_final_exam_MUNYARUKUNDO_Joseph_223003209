package farmer;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MyCropsFrame extends JFrame {
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/agriculture_system";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "TheRealJeph";

    // TODO: set this from login/session
    private int farmerId = 1;

    private JTable cropTable;
    private CropTableModel tableModel;
    private JButton backBtn;

    public MyCropsFrame() {
        setTitle("My Crops");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(950, 600);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        // Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(39, 174, 96));
        headerPanel.setPreferredSize(new Dimension(950, 56));

        JLabel headerLabel = new JLabel("My Crops", JLabel.CENTER);
        headerLabel.setFont(new Font("SansSerif", Font.BOLD, 26));
        headerLabel.setForeground(Color.WHITE);
        headerPanel.add(headerLabel, BorderLayout.CENTER);

        backBtn = new JButton("Back");
        backBtn.setFont(new Font("SansSerif", Font.BOLD, 13));
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(new Color(39, 174, 96));
        backBtn.setFocusPainted(false);
        backBtn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 24));
        headerPanel.add(backBtn, BorderLayout.EAST);

        add(headerPanel, BorderLayout.NORTH);

        // Table
        tableModel = new CropTableModel();
        cropTable = new JTable(tableModel);
        cropTable.setRowHeight(34);
        cropTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 15));
        cropTable.getTableHeader().setBackground(new Color(39, 174, 96));
        cropTable.getTableHeader().setForeground(Color.WHITE);

        JScrollPane tableScroll = new JScrollPane(cropTable);
        tableScroll.setBorder(BorderFactory.createEmptyBorder(8, 30, 26, 30));

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(new Color(250, 252, 249));
        centerPanel.add(tableScroll, BorderLayout.CENTER);

        add(centerPanel, BorderLayout.CENTER);

        // Events
        backBtn.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new FarmerDashboardFrame().setVisible(true);
                MyCropsFrame.this.dispose();
            }
        });

        refreshTable();
    }

    private void refreshTable() {
        List<Crop> crops = new ArrayList<Crop>();
        Connection conn = null;
        PreparedStatement st = null;
        ResultSet rs = null;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            st = conn.prepareStatement("SELECT * FROM crops WHERE farmer_id = ?");
            st.setInt(1, farmerId);
            rs = st.executeQuery();
            while (rs.next()) {
                Object smObj = rs.getObject("soil_moisture_level");
                Double sm = null;
                if (smObj != null) {
                    sm = ((Number) smObj).doubleValue();
                }
                int fid = 0;
                Object fidObj = rs.getObject("farmer_id");
                if (fidObj != null) {
                    fid = rs.getInt("farmer_id");
                }
                crops.add(new Crop(
                    rs.getInt("crop_id"),
                    rs.getString("crop_name"),
                    sm,
                    rs.getString("planting_date"),
                    rs.getString("growth_stage"),
                    fid
                ));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (st != null) st.close(); } catch (SQLException e) {}
            try { if (conn != null) conn.close(); } catch (SQLException e) {}
        }
        tableModel.setCrops(crops);
        tableModel.fireTableDataChanged();
    }

    // Data class
    public static class Crop {
        int cropId;
        String cropName;
        Double soilMoisture;
        String plantingDate;
        String growthStage;
        int farmerId;

        public Crop(int id, String name, Double moisture, String plantDate,
                    String stage, int farmerId) {
            this.cropId = id;
            this.cropName = name;
            this.soilMoisture = moisture;
            this.plantingDate = plantDate;
            this.growthStage = stage;
            this.farmerId = farmerId;
        }
    }

    // Table model
    public static class CropTableModel extends AbstractTableModel {
        private final String[] colNames = {
            "Crop Name", "Soil Moisture", "Planting Date", "Growth Stage", "Farmer ID"
        };
        private List<Crop> rows = new ArrayList<Crop>();

        public void setCrops(List<Crop> cs) {
            rows = cs == null ? new ArrayList<Crop>() : cs;
        }

        public Crop getCropAt(int r) {
            return rows.get(r);
        }

        public int getRowCount() {
            return rows.size();
        }

        public int getColumnCount() {
            return colNames.length;
        }

        public String getColumnName(int c) {
            return colNames[c];
        }

        public Object getValueAt(int r, int c) {
            Crop cr = rows.get(r);
            switch (c) {
                case 0: return cr.cropName;
                case 1: return cr.soilMoisture;
                case 2: return cr.plantingDate;
                case 3: return cr.growthStage;
                case 4: return cr.farmerId;
                default: return null;
            }
        }

        public boolean isCellEditable(int r, int c) {
            return false;
        }

        public Class<?> getColumnClass(int c) {
            if (c == 1) return Double.class;
            if (c == 4) return Integer.class;
            return String.class;
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new MyCropsFrame().setVisible(true);
            }
        });
    }
}
