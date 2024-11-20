import javax.swing.*;
import com.mongodb.client.MongoCollection;
import org.bson.Document;

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel statusLabel;

    public LoginFrame() {
        // Set up the window (JFrame)
        setTitle("Library Management System - Login");
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Center the window

        // Create and set up the JPanel
        JPanel panel = new JPanel();
        panel.setLayout(null);

        // Create and add the Username label
        JLabel usernameLabel = new JLabel("Username:");
        usernameLabel.setBounds(50, 50, 100, 25);
        panel.add(usernameLabel);

        // Create and add the Username text field
        usernameField = new JTextField();
        usernameField.setBounds(150, 50, 200, 25);
        panel.add(usernameField);

        // Create and add the Password label
        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(50, 100, 100, 25);
        panel.add(passwordLabel);

        // Create and add the Password field
        passwordField = new JPasswordField();
        passwordField.setBounds(150, 100, 200, 25);
        panel.add(passwordField);

        // Create and add the Login button
        loginButton = new JButton("Login");
        loginButton.setBounds(150, 150, 100, 30);
        panel.add(loginButton);

        // Create and add the status label for errors
        statusLabel = new JLabel("");
        statusLabel.setBounds(50, 190, 300, 25);
        panel.add(statusLabel);

        // Add the panel to the frame
        add(panel);

        // Add action listener for login button
        loginButton.addActionListener(e -> authenticateUser());
    }

    private void authenticateUser() {
        // Get the username and password input
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        // Get the users collection from MongoDB
        MongoCollection<Document> usersCollection = MongoDBUtil.getCollection("users");
        // Search for a matching username and password in the collection
        Document user = usersCollection.find(new Document("username", username).append("password", password)).first();

        // If user is found in the collection, check role and navigate to respective dashboard
        if (user != null) {
            String role = user.getString("role");
            JOptionPane.showMessageDialog(this, "Login Successful! Role: " + role);
            if ("admin".equals(role)) {
                new AdminDashboard().setVisible(true);
            } else {
                new UserDashboard().setVisible(true);
            }
            this.dispose(); // Close the login window
        } else {
            statusLabel.setText("Invalid username or password."); // Display error message if authentication fails
        }
    }

    public static void main(String[] args) {
        // Run the login frame on the Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}

class UserDashboard extends JFrame {
    public UserDashboard() {
        // Set up the User Dashboard window (JFrame)
        setTitle("User Dashboard");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Create and add label
        JLabel label = new JLabel("Welcome to User Dashboard!");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        add(label);
    }
}
