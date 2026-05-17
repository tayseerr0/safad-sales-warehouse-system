import db.DBConnection;

public static void main(String[] args) {
    if (DBConnection.testConnection()) {
        System.out.println("Connected to SAFAD database successfully.");
    } else {
        System.out.println("Failed to connect to SAFAD database.");
    }
}