package vtdi.keniel.filems.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import vtdi.keniel.filems.models.CourtCase;
import vtdi.keniel.filems.models.InvolvedParty;
import vtdi.keniel.filems.models.Judge;
import vtdi.keniel.filems.utils.DatabaseConnection;

public class CourtCaseDAO implements ICourtCaseDAO {
    
    // Manage and Log All Exceptions requirement [cite: 27]
    private static final Logger logger = LogManager.getLogger(CourtCaseDAO.class);

    // --------------------------------------------------------
    // CREATE (Insert)
    // --------------------------------------------------------
    public boolean insertCase(CourtCase courtCase) {
        String sql = "INSERT INTO CourtCases (case_number, applicant_id, respondent_id, child_id, judge_id, court_order, order_date) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?)";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, courtCase.getCaseNumber());
            pstmt.setInt(2, courtCase.getApplicant().getId());
            pstmt.setInt(3, courtCase.getRespondent().getId());
            pstmt.setInt(4, courtCase.getChild().getId());
            pstmt.setInt(5, courtCase.getJudge().getId());
            pstmt.setString(6, courtCase.getCourtOrder());
            pstmt.setDate(7, java.sql.Date.valueOf(courtCase.getOrderDate()));
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully inserted new court case: " + courtCase.getCaseNumber());
                return true;
            }
        } catch (SQLException e) {
            logger.error("Error inserting court case " + courtCase.getCaseNumber() + ": " + e.getMessage(), e);
        }
        return false;
    }

    // --------------------------------------------------------
    // READ (Select with condition) [cite: 24]
    // --------------------------------------------------------
    public CourtCase getCaseByNumber(String caseNumber) {
        String sql = "SELECT * FROM CourtCases WHERE case_number = ?";
        CourtCase courtCase = null;

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, caseNumber);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    courtCase = mapResultSetToCase(rs);
                    logger.info("Successfully retrieved case: " + caseNumber);
                } else {
                    logger.warn("No case found with number: " + caseNumber);
                }
            }
        } catch (SQLException e) {
            logger.error("Error retrieving case " + caseNumber + ": " + e.getMessage(), e);
        }
        return courtCase;
    }

    // --------------------------------------------------------
    // READ ALL (Multiple select) [cite: 24]
    // --------------------------------------------------------
    public List<CourtCase> getAllCases() {
        List<CourtCase> caseList = new ArrayList<>();
        String sql = "SELECT * FROM CourtCases ORDER BY created_at DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                caseList.add(mapResultSetToCase(rs));
            }
            logger.info("Successfully retrieved " + caseList.size() + " cases from the database.");
            
        } catch (SQLException e) {
            logger.error("Error retrieving all cases: " + e.getMessage(), e);
        }
        return caseList;
    }

    // --------------------------------------------------------
    // UPDATE [cite: 24]
    // --------------------------------------------------------
    public boolean updateCase(CourtCase courtCase) {
        String sql = "UPDATE CourtCases SET applicant_id = ?, respondent_id = ?, child_id = ?, " +
                     "judge_id = ?, court_order = ?, order_date = ? WHERE case_number = ?";
                     
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, courtCase.getApplicant().getId());
            pstmt.setInt(2, courtCase.getRespondent().getId());
            pstmt.setInt(3, courtCase.getChild().getId());
            pstmt.setInt(4, courtCase.getJudge().getId());
            pstmt.setString(5, courtCase.getCourtOrder());
            pstmt.setDate(6, java.sql.Date.valueOf(courtCase.getOrderDate()));
            pstmt.setString(7, courtCase.getCaseNumber());
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully updated court case: " + courtCase.getCaseNumber());
                return true;
            } else {
                logger.warn("Attempted to update non-existent case: " + courtCase.getCaseNumber());
            }
        } catch (SQLException e) {
            logger.error("Error updating court case " + courtCase.getCaseNumber() + ": " + e.getMessage(), e);
        }
        return false;
    }

    // --------------------------------------------------------
    // DELETE [cite: 25]
    // --------------------------------------------------------
    public boolean deleteCase(String caseNumber) {
        String sql = "DELETE FROM CourtCases WHERE case_number = ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, caseNumber);
            
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logger.info("Successfully deleted court case: " + caseNumber);
                return true;
            } else {
                logger.warn("Attempted to delete non-existent case: " + caseNumber);
            }
        } catch (SQLException e) {
            logger.error("Error deleting court case " + caseNumber + ": " + e.getMessage(), e);
        }
        return false;
    }

    // --------------------------------------------------------
    // HELPER: Managing Result Sets [cite: 26]
    // --------------------------------------------------------
    private CourtCase mapResultSetToCase(ResultSet rs) throws SQLException {
        CourtCase courtCase = new CourtCase();
        courtCase.setCaseId(rs.getInt("case_id"));
        courtCase.setCaseNumber(rs.getString("case_number"));
        courtCase.setCourtOrder(rs.getString("court_order"));
        
        java.sql.Date sqlDate = rs.getDate("order_date");
        if (sqlDate != null) {
            courtCase.setOrderDate(sqlDate.toLocalDate());
        }

        InvolvedParty applicant = new InvolvedParty();
        applicant.setId(rs.getInt("applicant_id"));
        courtCase.setApplicant(applicant);

        InvolvedParty respondent = new InvolvedParty();
        respondent.setId(rs.getInt("respondent_id"));
        courtCase.setRespondent(respondent);

        InvolvedParty child = new InvolvedParty();
        child.setId(rs.getInt("child_id"));
        courtCase.setChild(child);

        Judge judge = new Judge();
        judge.setId(rs.getInt("judge_id"));
        courtCase.setJudge(judge);

        return courtCase;
    }
}