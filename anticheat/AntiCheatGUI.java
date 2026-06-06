package anticheat;

import java.awt.*;
import javax.swing.*;
import javax.swing.table.*;

public class AntiCheatGUI extends JFrame {

    private final AntiCheatEngine engine = new AntiCheatEngine();
    private final ActionQueue queue = new ActionQueue();
    private final GameServer server = new GameServer(queue, engine);

    private JTextField tfPlayerId, tfValue;
    private JComboBox<String> cbType;
    private DefaultTableModel tableModel;
    private JTable playerTable;
    private JTextArea taLog;
    private JLabel lblStatus;

    private JSpinner spSpeed, spScore, spTeleport, spAimbot, spSuspicious, spBlocked;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AntiCheatGUI().setVisible(true));
    }

    public AntiCheatGUI() {
        super("Anti-Cheat Engine v1.0");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(950, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(5, 5));

        add(buildInputPanel(), BorderLayout.NORTH);
        add(buildTablePanel(), BorderLayout.CENTER);

        JPanel bottom = new JPanel(new GridLayout(2, 1));
        bottom.add(buildConfigPanel());
        bottom.add(buildLogPanel());
        add(bottom, BorderLayout.SOUTH);

        startServerThread();
    }

    private JPanel buildInputPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        p.setBorder(BorderFactory.createTitledBorder("Submit Action"));

        tfPlayerId = new JTextField(10);
        cbType = new JComboBox<>(new String[]{"MOVE", "SHOOT", "SCORE"});
        tfValue = new JTextField(8);
        lblStatus = new JLabel("Status: Ready");

        JButton btnSubmit = new JButton("Submit");
        JButton btnQueue = new JButton("Queue");
        btnSubmit.addActionListener(e -> doSubmit());
        btnQueue.addActionListener(e -> doQueue());

        p.add(new JLabel("Player ID:")); p.add(tfPlayerId);
        p.add(new JLabel("Type:"));     p.add(cbType);
        p.add(new JLabel("Value:"));    p.add(tfValue);
        p.add(btnSubmit);
        p.add(btnQueue);
        p.add(lblStatus);
        return p;
    }

    private JPanel buildTablePanel() {
        tableModel = new DefaultTableModel(
                new String[]{"Player ID", "Status", "Risk Score", "Violations"}, 0) {
            public boolean isCellEditable(int r, int c) { return false; }
        };

        playerTable = new JTable(tableModel);
        playerTable.setRowHeight(22);
        playerTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                String status = (String) t.getModel().getValueAt(row, 1);
                if (sel)                          c.setBackground(new Color(180, 200, 240));
                else if ("BLOCKED".equals(status))    c.setBackground(new Color(255, 180, 180));
                else if ("SUSPICIOUS".equals(status)) c.setBackground(new Color(255, 240, 150));
                else                              c.setBackground(Color.WHITE);
                c.setForeground(Color.BLACK);
                return c;
            }
        });

        JButton btnRefresh = new JButton("Refresh");
        JButton btnReset = new JButton("Reset Player");
        btnRefresh.addActionListener(e -> refreshTable());
        btnReset.addActionListener(e -> doReset());

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        btnRow.add(btnRefresh);
        btnRow.add(btnReset);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Player Status"));
        p.add(new JScrollPane(playerTable), BorderLayout.CENTER);
        p.add(btnRow, BorderLayout.SOUTH);
        return p;
    }

    private JPanel buildConfigPanel() {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 6));
        p.setBorder(BorderFactory.createTitledBorder("Thresholds"));

        spSpeed     = new JSpinner(new SpinnerNumberModel(CheatConfig.SPEED_THRESHOLD,     0, 99999, 100));
        spScore     = new JSpinner(new SpinnerNumberModel(CheatConfig.SCORE_THRESHOLD,     0, 99999, 50));
        spTeleport  = new JSpinner(new SpinnerNumberModel(CheatConfig.TELEPORT_THRESHOLD,  0, 99999, 100));
        spAimbot    = new JSpinner(new SpinnerNumberModel(CheatConfig.AIMBOT_WINDOW,       1, 9999,  1));
        spSuspicious= new JSpinner(new SpinnerNumberModel(CheatConfig.SUSPICIOUS_SCORE,    1, 999,   1));
        spBlocked   = new JSpinner(new SpinnerNumberModel(CheatConfig.BLOCKED_SCORE,       1, 999,   1));

        p.add(new JLabel("Speed:"));      p.add(spSpeed);
        p.add(new JLabel("Score:"));      p.add(spScore);
        p.add(new JLabel("Teleport:"));   p.add(spTeleport);
        p.add(new JLabel("Aimbot Win:")); p.add(spAimbot);
        p.add(new JLabel("Suspicious:")); p.add(spSuspicious);
        p.add(new JLabel("Blocked:"));    p.add(spBlocked);

        JButton btnApply = new JButton("Apply");
        btnApply.addActionListener(e -> applyConfig());
        p.add(btnApply);
        return p;
    }

    private JPanel buildLogPanel() {
        taLog = new JTextArea(5, 0);
        taLog.setEditable(false);
        taLog.setFont(new Font("Monospaced", Font.PLAIN, 11));

        JButton btnClear = new JButton("Clear");
        btnClear.addActionListener(e -> taLog.setText(""));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnRow.add(btnClear);

        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createTitledBorder("Event Log"));
        p.add(new JScrollPane(taLog), BorderLayout.CENTER);
        p.add(btnRow, BorderLayout.SOUTH);
        return p;
    }

    private void doSubmit() {
        String playerId = tfPlayerId.getText().trim();
        if (playerId.isEmpty()) { lblStatus.setText("Status: Player ID required"); return; }
        double value;
        try {
            value = Double.parseDouble(tfValue.getText().trim());
        } catch (NumberFormatException e) {
            lblStatus.setText("Status: Invalid value"); return;
        }

        RiskResult result = engine.evaluate(new Action(playerId, (String) cbType.getSelectedItem(), System.currentTimeMillis(), value));
        boolean clean = result.lastRule.equals("CLEAN");
        log("[SUBMIT] " + playerId + " -> " + (clean ? "CLEAN" : "FLAGGED") + " | Rule: " + result.lastRule);
        lblStatus.setText("Status: " + result.status);
        refreshTable();
    }

    private void doQueue() {
        String playerId = tfPlayerId.getText().trim();
        if (playerId.isEmpty()) { lblStatus.setText("Status: Player ID required"); return; }
        double value;
        try {
            value = Double.parseDouble(tfValue.getText().trim());
        } catch (NumberFormatException e) {
            lblStatus.setText("Status: Invalid value"); return;
        }

        Action action = new Action(playerId, (String) cbType.getSelectedItem(), System.currentTimeMillis(), value);
        if (!ActionValidator.isValid(action)) {
            log("[REJECTED] " + ActionValidator.getInvalidReason(action)); return;
        }
        queue.enqueue(action);
        log("[QUEUED] " + playerId + " | " + cbType.getSelectedItem() + " | " + value);
        lblStatus.setText("Status: Queued");
    }

    private void doReset() {
        int row = playerTable.getSelectedRow();
        if (row < 0) { lblStatus.setText("Status: Select a player first"); return; }
        String playerId = (String) tableModel.getValueAt(row, 0);
        engine.resetPlayer(playerId);
        log("[RESET] " + playerId);
        lblStatus.setText("Status: Reset done");
        refreshTable();
    }

    private void applyConfig() {
        CheatConfig.SPEED_THRESHOLD    = ((Number) spSpeed.getValue()).doubleValue();
        CheatConfig.SCORE_THRESHOLD    = ((Number) spScore.getValue()).doubleValue();
        CheatConfig.TELEPORT_THRESHOLD = ((Number) spTeleport.getValue()).doubleValue();
        CheatConfig.AIMBOT_WINDOW      = ((Number) spAimbot.getValue()).intValue();
        CheatConfig.SUSPICIOUS_SCORE   = ((Number) spSuspicious.getValue()).intValue();
        CheatConfig.BLOCKED_SCORE      = ((Number) spBlocked.getValue()).intValue();
        log("[CONFIG] Thresholds updated - Speed=" + CheatConfig.SPEED_THRESHOLD
            + " Score=" + CheatConfig.SCORE_THRESHOLD
            + " Teleport=" + CheatConfig.TELEPORT_THRESHOLD
            + " Aimbot=" + CheatConfig.AIMBOT_WINDOW
            + " Suspicious=" + CheatConfig.SUSPICIOUS_SCORE
            + " Blocked=" + CheatConfig.BLOCKED_SCORE);
        lblStatus.setText("Status: Config applied");
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        for (PlayerRecord rec : engine.getAllRecords().values()) {
            tableModel.addRow(new Object[]{
                rec.playerId, rec.getStatus(), rec.getRiskScore(), rec.getViolationCount()
            });
        }
    }

    private void startServerThread() {
        Thread t = new Thread(() -> {
            java.io.PrintStream old = System.out;
            java.io.OutputStream out = new java.io.OutputStream() {
                private final StringBuilder buf = new StringBuilder();
                public void write(int b) {
                    char ch = (char) b;
                    buf.append(ch);
                    if (ch == '\n') {
                        String line = buf.toString().trim();
                        buf.setLength(0);
                        if (!line.isEmpty()) SwingUtilities.invokeLater(() -> {
                            log("[SERVER] " + line);
                            refreshTable();
                        });
                    }
                }
            };
            System.setOut(new java.io.PrintStream(out, true));
            server.run();
            System.setOut(old);
        });
        t.setDaemon(true);
        t.start();
    }

    private void log(String msg) {
        SwingUtilities.invokeLater(() -> {
            taLog.append(msg + "\n");
            taLog.setCaretPosition(taLog.getDocument().getLength());
        });
    }
}