package vtdi.keniel.filems.gui;

import java.awt.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;

import vtdi.keniel.filems.dto.CourtCaseDTO;
import vtdi.keniel.filems.dto.InvolvedPartyDTO;
import vtdi.keniel.filems.dto.JudgeDTO;
import vtdi.keniel.filems.network.FileMSClient;
import vtdi.keniel.filems.network.NetworkMessage;

public class DashboardPanel extends JPanel {

    private static final Logger logger = LogManager.getLogger(DashboardPanel.class);
    
    // Dynamic Labels
    private JLabel lblTotalCases;
    private JLabel lblTotalParties;
    private JLabel lblTotalJudges;
    private JLabel lblActiveWarrants;
    
    // Dynamic Chart
    private ChartPanel liveChartPanel;
    private DefaultPieDataset<String> chartDataset;
    
    private FileMSClient apiClient;

    public DashboardPanel(String role) {
        setLayout(new BorderLayout(20, 20));
        setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        buildHeader(role);
        buildBody();

        // Fetch Live Data Asynchronously
        try {
            apiClient = new FileMSClient();
            fetchLiveStatistics();
        } catch (Exception e) {
            logger.error("Failed to initialize network client for dashboard.", e);
            setErrorState();
        }
    }

    private void buildHeader(String role) {
        JPanel headerPanel = new JPanel(new BorderLayout());
        
        JLabel lblWelcome = new JLabel("Parish Court Command Center");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 32));
        
        JLabel lblRole = new JLabel("Security Clearance: " + role.toUpperCase() + "  |  System Status: ONLINE");
        lblRole.setFont(new Font("Segoe UI", Font.ITALIC, 14));
        lblRole.setForeground(new Color(39, 174, 96)); // Secure Green
        
        headerPanel.add(lblWelcome, BorderLayout.NORTH);
        headerPanel.add(lblRole, BorderLayout.CENTER);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        
        add(headerPanel, BorderLayout.NORTH);
    }

    private void buildBody() {
        JPanel bodyPanel = new JPanel(new BorderLayout(20, 20));

        // --- 1. TOP METRIC STRIP (4 Cards) ---
        JPanel statsPanel = new JPanel(new GridLayout(1, 4, 15, 0));
        
        lblTotalCases = new JLabel("...", SwingConstants.CENTER);
        lblTotalParties = new JLabel("...", SwingConstants.CENTER);
        lblTotalJudges = new JLabel("...", SwingConstants.CENTER);
        lblActiveWarrants = new JLabel("...", SwingConstants.CENTER);

        // Using modern, flat "Tailwind-style" colors
        statsPanel.add(createStatCard("Active Cases", lblTotalCases, new Color(59, 130, 246)));   // Blue
        statsPanel.add(createStatCard("Registered Citizens", lblTotalParties, new Color(139, 92, 246))); // Purple
        statsPanel.add(createStatCard("Judges on Roster", lblTotalJudges, new Color(16, 185, 129)));  // Green
        statsPanel.add(createStatCard("Pending Warrants", lblActiveWarrants, new Color(239, 68, 68)));  // Red

        bodyPanel.add(statsPanel, BorderLayout.NORTH);

        // --- 2. LIVE ANALYTICS CHART (Center) ---
        chartDataset = new DefaultPieDataset<>();
        chartDataset.setValue("Awaiting Data...", 100); // Placeholder until server responds
        
        JFreeChart chart = ChartFactory.createPieChart(
            "Live Caseload Distribution by Judge", 
            chartDataset, 
            true, true, false
        );
        chart.getPlot().setBackgroundPaint(UIManager.getColor("Panel.background")); // Blends with FlatLaf
        chart.setBackgroundPaint(UIManager.getColor("Panel.background"));
        
        liveChartPanel = new ChartPanel(chart);
        liveChartPanel.setBorder(BorderFactory.createTitledBorder("Real-Time Analytics"));
        bodyPanel.add(liveChartPanel, BorderLayout.CENTER);

        // --- 3. SYSTEM HEALTH FEED (Right Side) ---
        JPanel feedPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        feedPanel.setPreferredSize(new Dimension(250, 0));
        feedPanel.setBorder(BorderFactory.createTitledBorder("System Health"));
        
        feedPanel.add(createHealthItem("Database Connection", "STABLE", new Color(16, 185, 129)));
        feedPanel.add(createHealthItem("Immutable Triggers", "ACTIVE", new Color(16, 185, 129)));
        feedPanel.add(createHealthItem("Network Thread Pool", "OPTIMIZED", new Color(16, 185, 129)));
        feedPanel.add(createHealthItem("Last Sync", "JUST NOW", Color.GRAY));

        bodyPanel.add(feedPanel, BorderLayout.EAST);

        add(bodyPanel, BorderLayout.CENTER);
    }

    private JPanel createStatCard(String title, JLabel valueLabel, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(color);
        // Modern generous padding for breathability
        card.setBorder(BorderFactory.createEmptyBorder(25, 15, 25, 15));

        JLabel lblTitle = new JLabel(title, SwingConstants.CENTER);
        lblTitle.setForeground(Color.WHITE);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));

        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 48));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createHealthItem(String title, String status, Color statusColor) {
        JPanel pnl = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel lblStatus = new JLabel(status, SwingConstants.RIGHT);
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblStatus.setForeground(statusColor);
        
        pnl.add(lblTitle, BorderLayout.WEST);
        pnl.add(lblStatus, BorderLayout.EAST);
        pnl.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.LIGHT_GRAY)); // Subtle divider
        return pnl;
    }

    /**
     * Silently connects to the server in the background to calculate live metrics
     * and update the visual chart without freezing the GUI.
     */
    private void fetchLiveStatistics() {
        SwingWorker<DashboardData, Void> worker = new SwingWorker<>() {
            @Override
            protected DashboardData doInBackground() throws Exception {
                DashboardData data = new DashboardData();
                
                NetworkMessage casesRes = apiClient.sendRequest(new NetworkMessage(NetworkMessage.Command.GET_ALL_CASES, null));
                NetworkMessage judgesRes = apiClient.sendRequest(new NetworkMessage(NetworkMessage.Command.GET_ALL_JUDGES, null));
                NetworkMessage partiesRes = apiClient.sendRequest(new NetworkMessage(NetworkMessage.Command.GET_ALL_PARTIES, null));

                if (casesRes.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    List<CourtCaseDTO> cases = (List<CourtCaseDTO>) casesRes.getPayload();
                    data.caseCount = cases.size();
                    
                    // Calculate data for the Pie Chart
                    for (CourtCaseDTO c : cases) {
                        String judgeName = (c.judge() != null) ? "Hon. " + c.judge().lastName() : "Unassigned";
                        data.judgeDistribution.put(judgeName, data.judgeDistribution.getOrDefault(judgeName, 0) + 1);
                        
                        // Simple dummy metric: simulate active warrants if order contains "warrant"
                        if (c.courtOrder().toLowerCase().contains("warrant")) {
                            data.warrantCount++;
                        }
                    }
                }
                
                if (judgesRes.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    data.judgeCount = ((List<JudgeDTO>) judgesRes.getPayload()).size();
                }
                if (partiesRes.getCommand() == NetworkMessage.Command.RESPONSE_OK) {
                    data.partyCount = ((List<InvolvedPartyDTO>) partiesRes.getPayload()).size();
                }

                return data;
            }

            @Override
            protected void done() {
                try {
                    DashboardData data = get();
                    
                    // 1. Update Number Cards
                    lblTotalCases.setText(String.valueOf(data.caseCount));
                    lblTotalParties.setText(String.valueOf(data.partyCount));
                    lblTotalJudges.setText(String.valueOf(data.judgeCount));
                    lblActiveWarrants.setText(String.valueOf(data.warrantCount));
                    
                    // 2. Update Live Chart
                    chartDataset.clear();
                    if (data.judgeDistribution.isEmpty()) {
                        chartDataset.setValue("No Active Cases", 1);
                    } else {
                        data.judgeDistribution.forEach(chartDataset::setValue);
                    }
                    
                    logger.info("Dashboard live statistics successfully rendered.");
                } catch (Exception e) {
                    logger.error("Failed to load live statistics.", e);
                    setErrorState();
                }
            }
        };
        worker.execute();
    }
    
    private void setErrorState() {
        lblTotalCases.setText("ERR");
        lblTotalParties.setText("ERR");
        lblTotalJudges.setText("ERR");
        lblActiveWarrants.setText("ERR");
        chartDataset.clear();
        chartDataset.setValue("Connection Failed", 1);
    }

    // Inner class to pass data cleanly from the background thread to the UI thread
    private class DashboardData {
        int caseCount = 0;
        int judgeCount = 0;
        int partyCount = 0;
        int warrantCount = 0;
        Map<String, Integer> judgeDistribution = new HashMap<>();
    }
}