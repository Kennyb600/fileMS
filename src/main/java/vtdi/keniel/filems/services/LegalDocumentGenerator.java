package vtdi.keniel.filems.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vtdi.keniel.filems.dto.CourtCaseDTO; // Updated to use DTO

import java.io.File;
import java.time.LocalDate;

public class LegalDocumentGenerator {

    private static final Logger logger = LogManager.getLogger(LegalDocumentGenerator.class);
    private static final String EXPORT_DIRECTORY = "C:/FileMS_Vault/Warrants/";

    /**
     * Generates a Bench Warrant PDF automatically pulled from the CourtCaseDTO data.
     */
    public void generateBenchWarrant(CourtCaseDTO courtCase) {
        new File(EXPORT_DIRECTORY).mkdirs();
        
        String fileName = EXPORT_DIRECTORY + courtCase.caseNumber() + "_Bench_Warrant.pdf";

        try {
            PdfWriter writer = new PdfWriter(fileName);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            // 1. Build the Standardized Legal Header
            Paragraph header = new Paragraph("IN THE PARISH COURT")
                    .setTextAlignment(TextAlignment.CENTER)
                    .setBold()
                    .setFontSize(16);
            document.add(header);

            document.add(new Paragraph("CASE NUMBER: " + courtCase.caseNumber()).setBold());
            document.add(new Paragraph("DATE ISSUED: " + LocalDate.now()));
            document.add(new Paragraph("\n"));

            // 2. Safely extract names dealing with potential null values
            String respFirst = courtCase.respondent() != null ? courtCase.respondent().firstName() : "UNKNOWN";
            String respLast = courtCase.respondent() != null ? courtCase.respondent().lastName() : "RESPONDENT";
            String judgeFirst = courtCase.judge() != null ? courtCase.judge().firstName() : "PRESIDING";
            String judgeLast = courtCase.judge() != null ? courtCase.judge().lastName() : "JUDGE";
            String appFirst = courtCase.applicant() != null ? courtCase.applicant().firstName() : "UNKNOWN";
            String appLast = courtCase.applicant() != null ? courtCase.applicant().lastName() : "APPLICANT";

            // 3. Build the Body Context
            String bodyText = String.format(
                "TO ALL CONSTABLES: You are hereby commanded to arrest %s %s (Respondent) " +
                "and bring them before the Presiding Judge, His/Her Honour %s %s, to answer to the " +
                "complaint brought forth by %s %s (Applicant).",
                respFirst, respLast, judgeFirst, judgeLast, appFirst, appLast
            );

            document.add(new Paragraph(bodyText).setFontSize(12));

            // 4. Official Court Order Section
            document.add(new Paragraph("\nOFFICIAL COURT ORDER:").setBold().setUnderline());
            String orderText = courtCase.courtOrder() != null ? courtCase.courtOrder() : "No order specified.";
            document.add(new Paragraph(orderText).setItalic());

            // 5. Sign-off area
            document.add(new Paragraph("\n\n____________________________________"));
            document.add(new Paragraph("Signature of Presiding Judge / Registrar").setFontSize(10));

            document.close();
            logger.info("Successfully generated Bench Warrant PDF for case: " + courtCase.caseNumber());

        } catch (Exception e) {
            logger.error("Failed to generate PDF Warrant for case: " + courtCase.caseNumber(), e);
        }
    }
}