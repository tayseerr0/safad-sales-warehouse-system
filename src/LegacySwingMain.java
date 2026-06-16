import db.DBConnection;
import ui.MainFrame;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class LegacySwingMain {

    public static void main(String[] args) {
        if (!DBConnection.testConnection()) {
            JOptionPane.showMessageDialog(
                    null,
                    "Failed to connect to SAFAD database.\nPlease check MySQL server, database name, username, and password.",
                    "Database Connection Error",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
