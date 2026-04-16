package vtdi.keniel.filems.gui;

import javax.swing.*;
import java.awt.*;
import vtdi.keniel.filems.models.CourtCase;

public class CaseSummaryInternalFrame extends JInternalFrame {
    
    private JTextArea summaryArea;

    public CaseSummaryInternalFrame(CourtCase selectedCase) {
        super("Case Summary: " + selectedCase.getCaseNumber(), true, true, true, true);
        
        // Initialize the text area
        summaryArea = new JTextArea();
        summaryArea.setEditable(false); // Make it read-only
        
        // CRITICAL: Use a monospaced font for the ASCII alignment
        summaryArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        
        // Set the text using your existing model method
        summaryArea.setText(selectedCase.getFormattedConsoleView());
        
        // Add padding around the text
        summaryArea.setMargin(new Insets(10, 10, 10, 10));

        // Add to a scroll pane in case the summary is long
        JScrollPane scrollPane = new JScrollPane(summaryArea);
        add(scrollPane, BorderLayout.CENTER);

        // Standard frame settings
        setSize(500, 400);
        setVisible(true);
    }
}