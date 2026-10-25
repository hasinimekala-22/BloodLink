
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BloodLink extends JFrame {

    private static final String URL =
        "jdbc:mysql://localhost:3306/bloodlink";
    private static final String USER = "root";
    private static final String PASS =
        System.getenv("BLOODLINK_DB_PASSWORD");

    private static final Color RED = new Color(190, 35, 43);
    private static final Color BG = new Color(245, 247, 250);
    private static final Color BORDER = new Color(220, 225, 232);

    private CardLayout cards;
    private JPanel content;

    private JLabel dbStatus;
    private JLabel donorCount;
    private JLabel availableCount;
    private JLabel requestCount;
    private JLabel matchCount;

    private JTextField searchText;
    private JComboBox<String> searchBlood;
    private JTable donorTable;
    private DefaultTableModel donorModel;

    private JTextField patientField;
    private JTextField hospitalField;
    private JTextField cityField;
    private JComboBox<String> requestBlood;
    private JComboBox<String> priorityBox;
    private JSpinner unitsSpinner;

    private JComboBox<String> matchRequestBox;
    private JTextArea matchOutput;

    private JTable historyTable;
    private JTable fairnessTable;
    private JTable fatigueTable;

    private static final String[] GROUPS = {
        "A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-"
    };

    public BloodLink() {
        setTitle("BloodLink - Emergency Blood Donor Matching");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(1180, 760);
        setMinimumSize(new Dimension(980, 650));
        setLocationRelativeTo(null);

        cards = new CardLayout();
        content = new JPanel(cards);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);
        root.add(sidebar(), BorderLayout.WEST);
        root.add(content, BorderLayout.CENTER);
        setContentPane(root);

        content.add(dashboard(), "Dashboard");
        content.add(registerPage(), "Register Donor");
        content.add(directoryPage(), "Donor Directory");
        content.add(availabilityPage(), "Availability");
        content.add(requestPage(), "Create Request");
        content.add(matchPage(), "Prioritized Match");
        content.add(historyPage(), "History");
        content.add(fairnessPage(), "Fairness Report");
        content.add(fatiguePage(), "Fatigue Matrix");

        ensureTables();
        checkDb();
        refreshDashboard();
    }

    private JPanel sidebar() {
        JPanel p = new JPanel();
        p.setPreferredSize(new Dimension(225, 0));
        p.setBackground(Color.WHITE);
        p.setBorder(new EmptyBorder(22, 12, 18, 12));
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));

        JLabel logo = new JLabel("\u2665 BLOODLINK");
        logo.setFont(new Font("SansSerif", Font.BOLD, 23));
        logo.setForeground(RED);
        p.add(logo);
        p.add(Box.createVerticalStrut(20));

        String[] pages = {
            "Dashboard", "Register Donor", "Donor Directory",
            "Availability", "Create Request", "Prioritized Match",
            "History", "Fairness Report", "Fatigue Matrix"
        };

        for (final String name : pages) {
            JButton b = new JButton(name);
            b.setMaximumSize(new Dimension(205, 38));
            b.setAlignmentX(Component.LEFT_ALIGNMENT);
            b.setHorizontalAlignment(SwingConstants.LEFT);
            b.setFocusPainted(false);
            b.setBackground(Color.WHITE);
            b.setBorder(new EmptyBorder(8, 10, 8, 5));

            b.addActionListener(e -> {
                if ("Dashboard".equals(name)) {
                    checkDb();
                    refreshDashboard();
                } else if ("Donor Directory".equals(name)) {
                    loadDonors();
                } else if ("Prioritized Match".equals(name)) {
                    loadRequestChoices();
                } else if ("History".equals(name)) {
                    loadHistory();
                } else if ("Fairness Report".equals(name)) {
                    loadFairness();
                } else if ("Fatigue Matrix".equals(name)) {
                    loadFatigue();
                }

                cards.show(content, name);
            });

            p.add(b);
            p.add(Box.createVerticalStrut(4));
        }

        p.add(Box.createVerticalGlue());
        p.add(new JLabel("B.Tech CSE Project | Java 8"));
        return p;
    }

    private JPanel page(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(0, 16));
        p.setBackground(BG);
        p.setBorder(new EmptyBorder(26, 28, 25, 28));

        JPanel heading = new JPanel();
        heading.setOpaque(false);
        heading.setLayout(new BoxLayout(heading, BoxLayout.Y_AXIS));

        JLabel t = new JLabel(title);
        t.setFont(new Font("SansSerif", Font.BOLD, 25));

        JLabel sub = new JLabel(subtitle);
        sub.setForeground(Color.DARK_GRAY);

        heading.add(t);
        heading.add(Box.createVerticalStrut(6));
        heading.add(sub);
        p.add(heading, BorderLayout.NORTH);

        return p;
    }

    private JPanel whitePanel() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new javax.swing.border.CompoundBorder(
            new LineBorder(BORDER),
            new EmptyBorder(18, 18, 18, 18)
        ));
        return p;
    }

    private JLabel stat(JPanel parent, String title) {
        JPanel box = whitePanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));

        JLabel label = new JLabel(title);
        label.setFont(new Font("SansSerif", Font.BOLD, 12));

        JLabel value = new JLabel("0");
        value.setFont(new Font("SansSerif", Font.BOLD, 32));

        box.add(label);
        box.add(Box.createVerticalStrut(12));
        box.add(value);
        parent.add(box);

        return value;
    }

    private JPanel dashboard() {
        JPanel p = page(
            "BloodLink Operations Desk",
            "Fairness-aware emergency blood donor matching prototype."
        );

        JPanel stats = new JPanel(new GridLayout(2, 2, 14, 14));
        stats.setOpaque(false);

        donorCount = stat(stats, "REGISTERED DONORS");
        availableCount = stat(stats, "AVAILABLE DONORS");
        requestCount = stat(stats, "BLOOD REQUESTS");
        matchCount = stat(stats, "MATCHED REQUESTS");

        JPanel wrap = new JPanel(new BorderLayout(0, 15));
        wrap.setOpaque(false);
        wrap.add(stats, BorderLayout.NORTH);

        JPanel note = whitePanel();
        note.setLayout(new BoxLayout(note, BoxLayout.Y_AXIS));

        dbStatus = new JLabel("Database status: checking...");
        dbStatus.setFont(new Font("SansSerif", Font.BOLD, 14));

        note.add(dbStatus);
        note.add(Box.createVerticalStrut(14));
        note.add(new JLabel(
            "Register donors, create requests and review ranked candidates."
        ));
        note.add(Box.createVerticalStrut(12));
        note.add(new JLabel(
            "Prototype only: medical staff must verify eligibility and compatibility."
        ));

        wrap.add(note, BorderLayout.CENTER);
        p.add(wrap, BorderLayout.CENTER);
        return p;
    }

    private void addField(JPanel p, GridBagConstraints g,
                          int row, String label, Component input) {
        g.gridy = row;
        g.gridx = 0;
        g.weightx = 0;
        p.add(new JLabel(label), g);

        g.gridx = 1;
        g.weightx = 1;
        p.add(input, g);
    }

    private JPanel registerPage() {
        JPanel p = page("Register New Donor",
            "Enter donor details and save them to MySQL.");

        JPanel form = whitePanel();
        form.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(8, 8, 8, 8);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        JTextField name = new JTextField(24);
        JTextField age = new JTextField();
        JTextField phone = new JTextField();
        JTextField city = new JTextField();

        JComboBox<String> gender = new JComboBox<String>(
            new String[]{"Female", "Male", "Other", "Prefer not to say"}
        );
        JComboBox<String> blood = new JComboBox<String>(GROUPS);
        JCheckBox available = new JCheckBox("Available to donate", true);
        available.setBackground(Color.WHITE);

        addField(form, g, 0, "Full name *", name);
        addField(form, g, 1, "Age (18-100)", age);
        addField(form, g, 2, "Gender", gender);
        addField(form, g, 3, "Blood group *", blood);
        addField(form, g, 4, "Phone", phone);
        addField(form, g, 5, "City", city);
        addField(form, g, 6, "Availability", available);

        JButton save = new JButton("Save Donor");
        save.setBackground(RED);
        save.setForeground(Color.WHITE);

        g.gridx = 1;
        g.gridy = 7;
        form.add(save, g);

        save.addActionListener(e -> {
            String donorName = name.getText().trim();

            if (donorName.isEmpty()) {
                msg("Please enter the donor name.");
                return;
            }

            Integer donorAge = null;
            try {
                if (!age.getText().trim().isEmpty()) {
                    donorAge = Integer.valueOf(age.getText().trim());
                    if (donorAge < 18 || donorAge > 100) {
                        throw new NumberFormatException();
                    }
                }
            } catch (NumberFormatException ex) {
                msg("Age must be between 18 and 100, or left blank.");
                return;
            }

            String sql = "INSERT INTO donors " +
                "(name, age, gender, blood_group, phone, city, available) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (Connection c = conn();
                 PreparedStatement s = c.prepareStatement(sql)) {

                s.setString(1, donorName);
                if (donorAge == null) {
                    s.setNull(2, Types.INTEGER);
                } else {
                    s.setInt(2, donorAge);
                }
                s.setString(3, (String) gender.getSelectedItem());
                s.setString(4, (String) blood.getSelectedItem());
                s.setString(5, phone.getText().trim());
                s.setString(6, city.getText().trim());
                s.setBoolean(7, available.isSelected());
                s.executeUpdate();

                msg("Donor registered successfully.");
                name.setText("");
                age.setText("");
                phone.setText("");
                city.setText("");
                refreshDashboard();

            } catch (Exception ex) {
                error(ex);
            }
        });

        p.add(form, BorderLayout.CENTER);
        return p;
    }

    private JPanel directoryPage() {
        JPanel p = page("Search & Donor Directory",
            "Search registered donors by name, phone, city or blood group.");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Color.WHITE);

        searchText = new JTextField(18);
        searchBlood = new JComboBox<String>(new String[]{
            "All groups", "A+", "A-", "B+", "B-",
            "AB+", "AB-", "O+", "O-"
        });

        JButton search = new JButton("Search");
        JButton all = new JButton("Show All");

        top.add(new JLabel("Keyword"));
        top.add(searchText);
        top.add(new JLabel("Blood group"));
        top.add(searchBlood);
        top.add(search);
        top.add(all);

        donorModel = model(new String[]{
            "ID", "Name", "Age", "Gender", "Blood",
            "Phone", "City", "Available"
        });

        donorTable = new JTable(donorModel);
        styleTable(donorTable);

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(top, BorderLayout.NORTH);
        center.add(new JScrollPane(donorTable), BorderLayout.CENTER);

        JButton toggle = new JButton("Toggle Selected Availability");
        JButton refresh = new JButton("Refresh");

        toggle.addActionListener(e -> toggleSelected());
        refresh.addActionListener(e -> loadDonors());

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.LEFT));
        bottom.setOpaque(false);
        bottom.add(toggle);
        bottom.add(refresh);
        center.add(bottom, BorderLayout.SOUTH);

        search.addActionListener(e -> loadDonors());
        all.addActionListener(e -> {
            searchText.setText("");
            searchBlood.setSelectedIndex(0);
            loadDonors();
        });

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel availabilityPage() {
        JPanel p = page("Donor Availability",
            "Manage donor availability in the directory.");

        JPanel box = whitePanel();
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(new JLabel(
            "Select a donor in the directory and toggle their availability."
        ));
        box.add(Box.createVerticalStrut(15));

        JButton open = new JButton("Open Donor Directory");
        open.addActionListener(e -> {
            loadDonors();
            cards.show(content, "Donor Directory");
        });

        box.add(open);
        p.add(box, BorderLayout.CENTER);
        return p;
    }

    private JPanel requestPage() {
        JPanel p = page("Create Blood Request",
            "Record a sample emergency blood request.");

        JPanel form = whitePanel();
        form.setLayout(new GridBagLayout());

        GridBagConstraints g = new GridBagConstraints();
        g.insets = new Insets(9, 9, 9, 9);
        g.fill = GridBagConstraints.HORIZONTAL;
        g.weightx = 1;

        patientField = new JTextField(24);
        hospitalField = new JTextField();
        cityField = new JTextField();
        requestBlood = new JComboBox<String>(GROUPS);
        unitsSpinner = new JSpinner(new SpinnerNumberModel(1, 1, 20, 1));
        priorityBox = new JComboBox<String>(
            new String[]{"Critical", "High", "Normal"}
        );

        addField(form, g, 0, "Patient/reference *", patientField);
        addField(form, g, 1, "Hospital *", hospitalField);
        addField(form, g, 2, "City", cityField);
        addField(form, g, 3, "Required blood group", requestBlood);
        addField(form, g, 4, "Units requested", unitsSpinner);
        addField(form, g, 5, "Priority", priorityBox);

        JButton save = new JButton("Save Request");
        g.gridx = 1;
        g.gridy = 6;
        form.add(save, g);

        save.addActionListener(e -> {
            String patient = patientField.getText().trim();
            String hospital = hospitalField.getText().trim();

            if (patient.isEmpty() || hospital.isEmpty()) {
                msg("Enter patient/reference and hospital.");
                return;
            }

            String sql = "INSERT INTO blood_requests " +
                "(patient_ref, hospital, city, blood_group, units, priority) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

            try (Connection c = conn();
                 PreparedStatement s = c.prepareStatement(sql)) {
                s.setString(1, patient);
                s.setString(2, hospital);
                s.setString(3, cityField.getText().trim());
                s.setString(4, (String) requestBlood.getSelectedItem());
                s.setInt(5, (Integer) unitsSpinner.getValue());
                s.setString(6, (String) priorityBox.getSelectedItem());
                s.executeUpdate();

                msg("Blood request saved.");
                patientField.setText("");
                hospitalField.setText("");
                cityField.setText("");
                refreshDashboard();

            } catch (Exception ex) {
                error(ex);
            }
        });

        p.add(form, BorderLayout.CENTER);
        return p;
    }

    private JPanel matchPage() {
        JPanel p = page("Prioritized Donor Matching",
            "Review candidates and their transparent ranking scores.");

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.setBackground(Color.WHITE);

        matchRequestBox = new JComboBox<String>();
        matchRequestBox.setPreferredSize(new Dimension(400, 28));

        JButton refresh = new JButton("Load Pending Requests");
        JButton run = new JButton("Run Match");

        top.add(matchRequestBox);
        top.add(refresh);
        top.add(run);

        matchOutput = new JTextArea();
        matchOutput.setEditable(false);
        matchOutput.setFont(new Font("Monospaced", Font.PLAIN, 13));

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(top, BorderLayout.NORTH);
        center.add(new JScrollPane(matchOutput), BorderLayout.CENTER);

        refresh.addActionListener(e -> loadRequestChoices());
        run.addActionListener(e -> runMatch());

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel historyPage() {
        JPanel p = page("Request History",
            "Review stored requests and their statuses.");

        historyTable = new JTable(model(new String[]{
            "Request ID", "Patient/reference", "Hospital", "City",
            "Blood group", "Units", "Priority", "Status", "Created"
        }));
        styleTable(historyTable);

        JButton refresh = new JButton("Refresh History");
        refresh.addActionListener(e -> loadHistory());

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(refresh, BorderLayout.NORTH);
        center.add(new JScrollPane(historyTable), BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel fairnessPage() {
        JPanel p = page("Fairness Report",
            "Compare recorded candidate-selection counts.");

        fairnessTable = new JTable(model(new String[]{
            "Donor ID", "Name", "Blood group", "City", "Recorded selections"
        }));
        styleTable(fairnessTable);

        JButton refresh = new JButton("Refresh Fairness Report");
        refresh.addActionListener(e -> loadFairness());

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(refresh, BorderLayout.NORTH);
        center.add(new JScrollPane(fairnessTable), BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private JPanel fatiguePage() {
        JPanel p = page("Donor Selection Frequency",
            "Selection counts are not a medical fatigue assessment.");

        fatigueTable = new JTable(model(new String[]{
            "Donor ID", "Name", "Age", "Available",
            "Recorded selections", "Screening note"
        }));
        styleTable(fatigueTable);

        JButton refresh = new JButton("Refresh Report");
        refresh.addActionListener(e -> loadFatigue());

        JPanel center = new JPanel(new BorderLayout(0, 10));
        center.setOpaque(false);
        center.add(refresh, BorderLayout.NORTH);
        center.add(new JScrollPane(fatigueTable), BorderLayout.CENTER);

        p.add(center, BorderLayout.CENTER);
        return p;
    }

    private Connection conn() throws SQLException {
        if (PASS == null || PASS.isEmpty()) {
            throw new SQLException(
                "Set BLOODLINK_DB_PASSWORD before launching BloodLink."
            );
        }
        return DriverManager.getConnection(URL, USER, PASS);
    }

    private void ensureTables() {
        String requests = "CREATE TABLE IF NOT EXISTS blood_requests (" +
            "request_id INT AUTO_INCREMENT PRIMARY KEY," +
            "patient_ref VARCHAR(100) NOT NULL," +
            "hospital VARCHAR(150) NOT NULL," +
            "city VARCHAR(100)," +
            "blood_group VARCHAR(5) NOT NULL," +
            "units INT NOT NULL DEFAULT 1," +
            "priority VARCHAR(20) NOT NULL," +
            "status VARCHAR(20) NOT NULL DEFAULT 'PENDING'," +
            "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        String logs = "CREATE TABLE IF NOT EXISTS match_logs (" +
            "match_id INT AUTO_INCREMENT PRIMARY KEY," +
            "request_id INT NOT NULL," +
            "donor_id INT NOT NULL," +
            "rank_position INT NOT NULL," +
            "score INT NOT NULL," +
            "matched_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)";

        try (Connection c = conn(); Statement s = c.createStatement()) {
            s.executeUpdate(requests);
            s.executeUpdate(logs);
        } catch (Exception ex) {
            if (dbStatus != null) {
                dbStatus.setText("Database setup failed: " + ex.getMessage());
                dbStatus.setForeground(RED);
            }
        }
    }

    private void checkDb() {
        try (Connection c = conn()) {
            dbStatus.setText("Database status: Connected");
            dbStatus.setForeground(new Color(0, 130, 65));
        } catch (Exception ex) {
            dbStatus.setText("Database status: Not connected");
            dbStatus.setForeground(RED);
        }
    }

    private int count(Statement s, String sql) throws SQLException {
        try (ResultSet r = s.executeQuery(sql)) {
            r.next();
            return r.getInt(1);
        }
    }

    private void refreshDashboard() {
        try (Connection c = conn(); Statement s = c.createStatement()) {
            donorCount.setText(String.valueOf(
                count(s, "SELECT COUNT(*) FROM donors")));
            availableCount.setText(String.valueOf(
                count(s, "SELECT COUNT(*) FROM donors WHERE available=TRUE")));
            requestCount.setText(String.valueOf(
                count(s, "SELECT COUNT(*) FROM blood_requests")));
            matchCount.setText(String.valueOf(
                count(s, "SELECT COUNT(DISTINCT request_id) FROM match_logs")));
        } catch (Exception ex) {
            donorCount.setText("-");
            availableCount.setText("-");
            requestCount.setText("-");
            matchCount.setText("-");
        }
    }

    private DefaultTableModel model(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private void styleTable(JTable table) {
        table.setRowHeight(27);
        table.setAutoCreateRowSorter(true);
        table.getTableHeader().setReorderingAllowed(false);
    }

    private void loadDonors() {
        if (donorModel == null) {
            return;
        }

        donorModel.setRowCount(0);
        String keyword = searchText.getText().trim();
        String group = (String) searchBlood.getSelectedItem();

        String sql = "SELECT donor_id, name, age, gender, blood_group, " +
            "phone, city, available FROM donors " +
            "WHERE (name LIKE ? OR city LIKE ? OR phone LIKE ?)";

        if (!"All groups".equals(group)) {
            sql += " AND blood_group=?";
        }
        sql += " ORDER BY name";

        try (Connection c = conn();
             PreparedStatement s = c.prepareStatement(sql)) {

            String q = "%" + keyword + "%";
            s.setString(1, q);
            s.setString(2, q);
            s.setString(3, q);

            if (!"All groups".equals(group)) {
                s.setString(4, group);
            }

            try (ResultSet r = s.executeQuery()) {
                while (r.next()) {
                    donorModel.addRow(new Object[]{
                        r.getInt("donor_id"),
                        r.getString("name"),
                        r.getObject("age"),
                        r.getString("gender"),
                        r.getString("blood_group"),
                        r.getString("phone"),
                        r.getString("city"),
                        r.getBoolean("available") ? "Yes" : "No"
                    });
                }
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void toggleSelected() {
        int row = donorTable.getSelectedRow();

        if (row < 0) {
            msg("Select a donor row first.");
            return;
        }

        int modelRow = donorTable.convertRowIndexToModel(row);
        int id = ((Number) donorModel.getValueAt(modelRow, 0)).intValue();

        try (Connection c = conn();
             PreparedStatement s = c.prepareStatement(
                 "UPDATE donors SET available=NOT available WHERE donor_id=?"
             )) {
            s.setInt(1, id);
            s.executeUpdate();
            loadDonors();
            refreshDashboard();
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void loadRequestChoices() {
        matchRequestBox.removeAllItems();

        String sql = "SELECT request_id, patient_ref, hospital, blood_group, " +
            "city, priority FROM blood_requests WHERE status='PENDING' " +
            "ORDER BY FIELD(priority,'Critical','High','Normal'), created_at";

        try (Connection c = conn();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {

            while (r.next()) {
                String city = r.getString("city");
                if (city == null) {
                    city = "";
                }

                matchRequestBox.addItem(
                    r.getInt("request_id") + " | " +
                    r.getString("patient_ref") + " | " +
                    r.getString("blood_group") + " | " +
                    r.getString("hospital") + " | " + city
                );
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private boolean compatible(String donor, String needed) {
        switch (needed) {
            case "O-":
                return donor.equals("O-");
            case "O+":
                return donor.equals("O-") || donor.equals("O+");
            case "A-":
                return donor.equals("O-") || donor.equals("A-");
            case "A+":
                return donor.equals("O-") || donor.equals("O+") ||
                    donor.equals("A-") || donor.equals("A+");
            case "B-":
                return donor.equals("O-") || donor.equals("B-");
            case "B+":
                return donor.equals("O-") || donor.equals("O+") ||
                    donor.equals("B-") || donor.equals("B+");
            case "AB-":
                return donor.equals("O-") || donor.equals("A-") ||
                    donor.equals("B-") || donor.equals("AB-");
            case "AB+":
                return true;
            default:
                return false;
        }
    }

    private void runMatch() {
        Object selected = matchRequestBox.getSelectedItem();

        if (selected == null) {
            msg("No pending request. Create a request first.");
            return;
        }

        int requestId = Integer.parseInt(
            selected.toString().split(" \\|")[0]);

        try (Connection c = conn()) {
            String needed;
            String requestCity;

            try (PreparedStatement s = c.prepareStatement(
                "SELECT blood_group, city FROM blood_requests WHERE request_id=?")) {
                s.setInt(1, requestId);

                try (ResultSet r = s.executeQuery()) {
                    if (!r.next()) {
                        msg("Request not found.");
                        return;
                    }
                    needed = r.getString("blood_group");
                    requestCity = r.getString("city");
                }
            }

            class Candidate {
                int id;
                int previousSelections;
                int score;
                String name;
                String blood;
                String city;
            }

            List<Candidate> candidates = new ArrayList<Candidate>();

            String sql = "SELECT d.donor_id, d.name, d.blood_group, d.city, " +
                "(SELECT COUNT(*) FROM match_logs m WHERE m.donor_id=d.donor_id) AS uses " +
                "FROM donors d WHERE d.available=TRUE AND (d.age IS NULL OR d.age>=18)";

            try (Statement s = c.createStatement();
                 ResultSet r = s.executeQuery(sql)) {

                while (r.next()) {
                    String blood = r.getString("blood_group");
                    if (!compatible(blood, needed)) {
                        continue;
                    }

                    Candidate x = new Candidate();
                    x.id = r.getInt("donor_id");
                    x.name = r.getString("name");
                    x.blood = blood;
                    x.city = r.getString("city");
                    x.previousSelections = r.getInt("uses");
                    x.score = 100 - x.previousSelections * 10;

                    if (requestCity != null && x.city != null &&
                        requestCity.trim().equalsIgnoreCase(x.city.trim())) {
                        x.score += 20;
                    }

                    candidates.add(x);
                }
            }

            Collections.sort(candidates, (a, b) ->
                Integer.compare(b.score, a.score));

            StringBuilder output = new StringBuilder();
            output.append("Request ID: ").append(requestId).append("\n");
            output.append("Required group: ").append(needed).append("\n");
            output.append("Candidates found: ").append(candidates.size()).append("\n\n");
            output.append("Rank | Donor ID | Name | Blood | City | Previous selections | Score\n");
            output.append("--------------------------------------------------------------------------\n");

            String insert = "INSERT INTO match_logs " +
                "(request_id, donor_id, rank_position, score) VALUES (?, ?, ?, ?)";

            try (PreparedStatement log = c.prepareStatement(insert)) {
                int rank = 0;

                for (Candidate x : candidates) {
                    rank++;
                    output.append(rank).append(" | ")
                        .append(x.id).append(" | ")
                        .append(x.name).append(" | ")
                        .append(x.blood).append(" | ")
                        .append(x.city == null ? "" : x.city).append(" | ")
                        .append(x.previousSelections).append(" | ")
                        .append(x.score).append("\n");

                    log.setInt(1, requestId);
                    log.setInt(2, x.id);
                    log.setInt(3, rank);
                    log.setInt(4, x.score);
                    log.addBatch();
                }

                if (rank > 0) {
                    log.executeBatch();
                }
            }

            if (candidates.isEmpty()) {
                output.append("No eligible candidates found in stored data.\n");
            } else {
                try (PreparedStatement s = c.prepareStatement(
                    "UPDATE blood_requests SET status='MATCHED' WHERE request_id=?")) {
                    s.setInt(1, requestId);
                    s.executeUpdate();
                }
            }

            output.append("\nRanking uses recorded selections and city match.");
            output.append("\nThis is not a medical compatibility decision.");
            output.append("\nA blood bank must independently verify suitability.");

            matchOutput.setText(output.toString());
            refreshDashboard();
            loadRequestChoices();

        } catch (Exception ex) {
            error(ex);
        }
    }

    private void loadHistory() {
        DefaultTableModel m = (DefaultTableModel) historyTable.getModel();
        m.setRowCount(0);

        String sql = "SELECT request_id, patient_ref, hospital, city, blood_group, " +
            "units, priority, status, created_at FROM blood_requests " +
            "ORDER BY created_at DESC";

        try (Connection c = conn();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                m.addRow(new Object[]{
                    r.getInt("request_id"), r.getString("patient_ref"),
                    r.getString("hospital"), r.getString("city"),
                    r.getString("blood_group"), r.getInt("units"),
                    r.getString("priority"), r.getString("status"),
                    r.getTimestamp("created_at")
                });
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void loadFairness() {
        DefaultTableModel m = (DefaultTableModel) fairnessTable.getModel();
        m.setRowCount(0);

        String sql = "SELECT d.donor_id, d.name, d.blood_group, d.city, " +
            "COUNT(ml.match_id) AS uses FROM donors d " +
            "LEFT JOIN match_logs ml ON d.donor_id=ml.donor_id " +
            "GROUP BY d.donor_id, d.name, d.blood_group, d.city " +
            "ORDER BY uses ASC, d.name";

        try (Connection c = conn();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                m.addRow(new Object[]{
                    r.getInt("donor_id"), r.getString("name"),
                    r.getString("blood_group"), r.getString("city"),
                    r.getInt("uses")
                });
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void loadFatigue() {
        DefaultTableModel m = (DefaultTableModel) fatigueTable.getModel();
        m.setRowCount(0);

        String sql = "SELECT d.donor_id, d.name, d.age, d.available, " +
            "COUNT(ml.match_id) AS uses FROM donors d " +
            "LEFT JOIN match_logs ml ON d.donor_id=ml.donor_id " +
            "GROUP BY d.donor_id, d.name, d.age, d.available " +
            "ORDER BY uses DESC";

        try (Connection c = conn();
             Statement s = c.createStatement();
             ResultSet r = s.executeQuery(sql)) {
            while (r.next()) {
                m.addRow(new Object[]{
                    r.getInt("donor_id"), r.getString("name"),
                    r.getObject("age"),
                    r.getBoolean("available") ? "Yes" : "No",
                    r.getInt("uses"),
                    "Verify actual donation history and eligibility"
                });
            }
        } catch (Exception ex) {
            error(ex);
        }
    }

    private void msg(String text) {
        JOptionPane.showMessageDialog(this, text, "BloodLink",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void error(Exception ex) {
        JOptionPane.showMessageDialog(this,
            "Operation failed: " + ex.getMessage(),
            "BloodLink database error",
            JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(
                    UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }

            new BloodLink().setVisible(true);
        });
    }
}
