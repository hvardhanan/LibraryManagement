import javax.swing.*;
import javax.swing.table.DefaultTableModel;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;


public class AdminDashboard extends JFrame {
    private JTextField usernameField, titleField, authorField, genreField, quantityField, searchField;
    private JPasswordField passwordField;
    private JComboBox<String> roleComboBox;
    private JButton addUserButton, updateUserButton, deleteUserButton;
    private JButton addBookButton, updateBookButton, deleteBookButton, searchButton;
    private JTable userTable, bookTable, lendingTable, returnTable;
    private DefaultTableModel userTableModel, bookTableModel, lendingTableModel, returnTableModel;

    class AddUserActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();

            if (username.isEmpty() || password.isEmpty() || role == null) {
                JOptionPane.showMessageDialog(null, "Please provide valid username, password, and role.");
                return;
            }

            Document userDoc = new Document("username", username)
                    .append("password", password)
                    .append("role", role);

            MongoCollection<Document> userCollection = MongoDBUtil.getCollection("users");
            userCollection.insertOne(userDoc);

            JOptionPane.showMessageDialog(null, "User added successfully!");

            loadUsersFromDatabase();
        }
    }

    class UpdateUserActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String role = (String) roleComboBox.getSelectedItem();

            // Validate inputs
            if (username.isEmpty() || password.isEmpty() || role == null) {
                JOptionPane.showMessageDialog(null, "Please provide valid username, password, and role.");
                return;
            }

            // Create a document with the updated user information
            Document updatedUserDoc = new Document("password", password)
                    .append("role", role);

            // Update the user document in the MongoDB collection
            MongoCollection<Document> userCollection = MongoDBUtil.getCollection("users");
            userCollection.updateOne(new Document("username", username), new Document("$set", updatedUserDoc));

            // Show success message
            JOptionPane.showMessageDialog(null, "User updated successfully!");

            // Refresh the user table
            loadUsersFromDatabase();
        }
    }

    class DeleteUserActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String username = usernameField.getText();

            // Validate input
            if (username.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please provide a valid username.");
                return;
            }

            // Delete the user from the MongoDB collection
            MongoCollection<Document> userCollection = MongoDBUtil.getCollection("users");
            userCollection.deleteOne(new Document("username", username));

            // Show success message
            JOptionPane.showMessageDialog(null, "User deleted successfully!");

            // Refresh the user table
            loadUsersFromDatabase();
        }
    }

    class AddBookActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String title = titleField.getText();
            String author = authorField.getText();
            String genre = genreField.getText();
            int quantity = Integer.parseInt(quantityField.getText());

            // Validate inputs
            if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || quantity <= 0) {
                JOptionPane.showMessageDialog(null, "Please provide valid book details.");
                return;
            }

            // Create a document for the new book
            Document bookDoc = new Document("title", title)
                    .append("author", author)
                    .append("genre", genre)
                    .append("quantity", quantity);

            // Insert the new book into the books collection
            MongoCollection<Document> bookCollection = MongoDBUtil.getCollection("books");
            bookCollection.insertOne(bookDoc);

            // Show success message
            JOptionPane.showMessageDialog(null, "Book added successfully!");

            // Refresh the book table
            loadBooksFromDatabase();
        }
    }

    class UpdateBookActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String title = titleField.getText();
            String author = authorField.getText();
            String genre = genreField.getText();
            int quantity = Integer.parseInt(quantityField.getText());

            // Validate inputs
            if (title.isEmpty() || author.isEmpty() || genre.isEmpty() || quantity <= 0) {
                JOptionPane.showMessageDialog(null, "Please provide valid book details.");
                return;
            }

            // Create a document with the updated book information
            Document updatedBookDoc = new Document("author", author)
                    .append("genre", genre)
                    .append("quantity", quantity);

            // Update the book document in the MongoDB collection
            MongoCollection<Document> bookCollection = MongoDBUtil.getCollection("books");
            bookCollection.updateOne(new Document("title", title), new Document("$set", updatedBookDoc));

            // Show success message
            JOptionPane.showMessageDialog(null, "Book updated successfully!");

            // Refresh the book table
            loadBooksFromDatabase();
        }
    }

    private void refreshUserDropdowns(JComboBox<String> lendingUserComboBox, JComboBox<String> returnUserComboBox) {
        String[] updatedUsers = getUsernamesFromDatabase();
        lendingUserComboBox.removeAllItems();
        returnUserComboBox.removeAllItems();

        for (String username : updatedUsers) {
            lendingUserComboBox.addItem(username);
            returnUserComboBox.addItem(username);
        }
    }


    class DeleteBookActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String title = titleField.getText();

            // Validate input
            if (title.isEmpty()) {
                JOptionPane.showMessageDialog(null, "Please provide a valid book title.");
                return;
            }

            // Delete the book from the MongoDB collection
            MongoCollection<Document> bookCollection = MongoDBUtil.getCollection("books");
            bookCollection.deleteOne(new Document("title", title));

            // Show success message
            JOptionPane.showMessageDialog(null, "Book deleted successfully!");

            // Refresh the book table
            loadBooksFromDatabase();
        }
    }

    class SearchBookActionListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            String searchTitle = titleField.getText().trim();
            String searchAuthor = authorField.getText().trim();
            String searchGenre = genreField.getText().trim();

            // Create a filter document based on the provided search fields
            Bson filter = new Document();

            if (!searchTitle.isEmpty()) {
                filter = Filters.and(filter, Filters.regex("title", ".*" + searchTitle + ".*", "i"));
            }
            if (!searchAuthor.isEmpty()) {
                filter = Filters.and(filter, Filters.regex("author", ".*" + searchAuthor + ".*", "i"));
            }
            if (!searchGenre.isEmpty()) {
                filter = Filters.and(filter, Filters.regex("genre", ".*" + searchGenre + ".*", "i"));
            }

            // Perform the search in the MongoDB collection
            MongoCollection<Document> bookCollection = MongoDBUtil.getCollection("books");
            FindIterable<Document> results = bookCollection.find(filter);

            // Clear the current table data
            bookTableModel.setRowCount(0);

            // Add search results to the table
            for (Document book : results) {
                String title = book.getString("title");
                String author = book.getString("author");
                String genre = book.getString("genre");
                int quantity = book.getInteger("quantity");
                bookTableModel.addRow(new Object[]{title, author, genre, quantity});
            }

            // If no books found, show a message
            if (!results.iterator().hasNext()) {
                JOptionPane.showMessageDialog(null, "No books found matching the search criteria.");
            }
        }
    }



    public AdminDashboard() {
        setTitle("Admin Dashboard");
        setSize(1000, 900); // Increased size to accommodate all features, including returns
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Tabbed Pane
        JTabbedPane tabbedPane = new JTabbedPane();

        // User Management Tab
        JPanel userPanel = createUserManagementPanel();
        tabbedPane.addTab("User Management", userPanel);

        // Book Management Tab
        JPanel bookPanel = createBookManagementPanel();
        tabbedPane.addTab("Book Management", bookPanel);

        // Lending Tab
        JPanel lendingPanel = createLendingPanel();
        tabbedPane.addTab("Lending", lendingPanel);

        // Return Tab
        JPanel returnPanel = createReturnPanel();
        tabbedPane.addTab("Return", returnPanel);

        add(tabbedPane);
    }

    // Create the User Management Panel
    private JPanel createUserManagementPanel() {
        JPanel userPanel = new JPanel();
        userPanel.setLayout(new BorderLayout());

        // Add User Panel
        JPanel addUserPanel = new JPanel();
        addUserPanel.setLayout(new FlowLayout());

        // Username field
        addUserPanel.add(new JLabel("Username:"));
        usernameField = new JTextField(15);
        addUserPanel.add(usernameField);

        // Password field
        addUserPanel.add(new JLabel("Password:"));
        passwordField = new JPasswordField(15);
        addUserPanel.add(passwordField);

        // Role combo box
        addUserPanel.add(new JLabel("Role:"));
        roleComboBox = new JComboBox<>(new String[] { "admin", "user" });
        addUserPanel.add(roleComboBox);

        // Add user button
        addUserButton = new JButton("Add User");
        addUserButton.addActionListener(new AddUserActionListener());
        addUserPanel.add(addUserButton);

        // Update user button
        updateUserButton = new JButton("Update User");
        updateUserButton.addActionListener(new UpdateUserActionListener());
        addUserPanel.add(updateUserButton);

        // Delete user button
        deleteUserButton = new JButton("Delete User");
        deleteUserButton.addActionListener(new DeleteUserActionListener());
        addUserPanel.add(deleteUserButton);

        userPanel.add(addUserPanel, BorderLayout.NORTH);

        // Table for users
        String[] userColumnNames = { "Username", "Password", "Role" };
        userTableModel = new DefaultTableModel(userColumnNames, 0);
        userTable = new JTable(userTableModel);
        userPanel.add(new JScrollPane(userTable), BorderLayout.CENTER);

        loadUsersFromDatabase();
        return userPanel;
    }

    // Create the Book Management Panel
    private JPanel createBookManagementPanel() {
        JPanel bookPanel = new JPanel();
        bookPanel.setLayout(new BorderLayout());

        // Add Book Panel
        JPanel addBookPanel = new JPanel();
        addBookPanel.setLayout(new FlowLayout());

        // Book Title field
        addBookPanel.add(new JLabel("Title:"));
        titleField = new JTextField(15);
        addBookPanel.add(titleField);

        // Author field
        addBookPanel.add(new JLabel("Author:"));
        authorField = new JTextField(15);
        addBookPanel.add(authorField);

        // Genre field
        addBookPanel.add(new JLabel("Genre:"));
        genreField = new JTextField(15);
        addBookPanel.add(genreField);

        // Quantity field
        addBookPanel.add(new JLabel("Quantity:"));
        quantityField = new JTextField(5);
        addBookPanel.add(quantityField);

        // Add book button
        addBookButton = new JButton("Add Book");
        addBookButton.addActionListener(new AddBookActionListener());
        addBookPanel.add(addBookButton);

        // Update book button
        updateBookButton = new JButton("Update Book");
        updateBookButton.addActionListener(new UpdateBookActionListener());
        addBookPanel.add(updateBookButton);

        // Delete book button
        deleteBookButton = new JButton("Delete Book");
        deleteBookButton.addActionListener(new DeleteBookActionListener());
        addBookPanel.add(deleteBookButton);

        bookPanel.add(addBookPanel, BorderLayout.NORTH);

        JPanel filterPanel = new JPanel();
        filterPanel.setLayout(new FlowLayout());
        filterPanel.add(new JLabel("Filter by Genre:"));

        JComboBox<String> genreFilterComboBox = new JComboBox<>(getUniqueGenres());
        filterPanel.add(genreFilterComboBox);

        genreFilterComboBox.addActionListener(e -> {
            String selectedGenre = (String) genreFilterComboBox.getSelectedItem();
            filterBooksByGenre(selectedGenre);
        });

        bookPanel.add(filterPanel, BorderLayout.SOUTH);

        // Table for books
        String[] bookColumnNames = { "Title", "Author", "Genre", "Quantity" };
        bookTableModel = new DefaultTableModel(bookColumnNames, 0);
        bookTable = new JTable(bookTableModel);
        bookPanel.add(new JScrollPane(bookTable), BorderLayout.CENTER);

        loadBooksFromDatabase();
        return bookPanel;
    }

    // Helper method to filter books by genre
    private void filterBooksByGenre(String genre) {
        MongoCollection<Document> bookCollection = MongoDBUtil.getCollection("books");

        bookTableModel.setRowCount(0); // Clear current rows
        FindIterable<Document> books;
        if ("All".equals(genre)) {
            books = bookCollection.find(); // Show all books
        } else {
            books = bookCollection.find(new Document("genre", genre));
        }

        for (Document book : books) {
            String title = book.getString("title");
            String author = book.getString("author");
            String bookGenre = book.getString("genre");
            int quantity = book.getInteger("quantity");
            bookTableModel.addRow(new Object[]{title, author, bookGenre, quantity});
        }
    }

    private String[] getUniqueGenres() {
        MongoCollection<Document> bookCollection = MongoDBUtil.getCollection("books");
        List<String> genres = bookCollection.distinct("genre", String.class).into(new ArrayList<>());
        genres.add(0, "All"); // Add an "All" option to show all books
        return genres.toArray(new String[0]);
    }


    // Create the Lending Panel
    private JPanel createLendingPanel() {
        JPanel lendingPanel = new JPanel();
        lendingPanel.setLayout(new BorderLayout());

        // Lending Panel
        JPanel lendBookPanel = new JPanel();
        lendBookPanel.setLayout(new FlowLayout());

        lendBookPanel.add(new JLabel("Username:"));
        JComboBox<String> userComboBox = new JComboBox<>(getUsernamesFromDatabase());
        lendBookPanel.add(userComboBox);

        lendBookPanel.add(new JLabel("Filter by Genre:"));
        JComboBox<String> genreFilterComboBox = new JComboBox<>(getUniqueGenres());
        lendBookPanel.add(genreFilterComboBox);

        lendBookPanel.add(new JLabel("Book:"));
        JComboBox<String> bookComboBox = new JComboBox<>();
        lendBookPanel.add(bookComboBox);

        // Populate books when a genre is selected
        genreFilterComboBox.addActionListener(e -> {
            String selectedGenre = (String) genreFilterComboBox.getSelectedItem();
            updateBooksByGenre(selectedGenre, bookComboBox);
        });

        JButton lendBookButton = new JButton("Lend Book");
        lendBookButton.addActionListener(new LendBookActionListener(userComboBox, bookComboBox));
        lendBookPanel.add(lendBookButton);

        lendingPanel.add(lendBookPanel, BorderLayout.NORTH);

        String[] lendingColumnNames = { "Username", "Book Title", "Lend Date", "Return Date" };
        lendingTableModel = new DefaultTableModel(lendingColumnNames, 0);
        lendingTable = new JTable(lendingTableModel);
        lendingPanel.add(new JScrollPane(lendingTable), BorderLayout.CENTER);

        loadLendingData();
        return lendingPanel;
    }

    // Helper method to update books by genre
    private void updateBooksByGenre(String genre, JComboBox<String> bookComboBox) {
        MongoCollection<Document> bookCollection = MongoDBUtil.getCollection("books");
        FindIterable<Document> books;
        if ("All".equals(genre)) {
            books = bookCollection.find();
        } else {
            books = bookCollection.find(new Document("genre", genre));
        }

        bookComboBox.removeAllItems(); // Clear current items
        for (Document book : books) {
            bookComboBox.addItem(book.getString("title"));
        }
    }


    // Create the Return Panel
    private JPanel createReturnPanel() {
        JPanel returnPanel = new JPanel();
        returnPanel.setLayout(new BorderLayout());

        JPanel returnBookPanel = new JPanel();
        returnBookPanel.setLayout(new FlowLayout());

        returnBookPanel.add(new JLabel("Username:"));
        JComboBox<String> returnUserComboBox = new JComboBox<>(getUsernamesFromDatabase());
        returnBookPanel.add(returnUserComboBox);

        returnBookPanel.add(new JLabel("Filter by Genre:"));
        JComboBox<String> genreFilterComboBox = new JComboBox<>(getUniqueGenres());
        returnBookPanel.add(genreFilterComboBox);

        returnBookPanel.add(new JLabel("Book:"));
        JComboBox<String> returnBookComboBox = new JComboBox<>();
        returnBookPanel.add(returnBookComboBox);

        genreFilterComboBox.addActionListener(e -> {
            String selectedGenre = (String) genreFilterComboBox.getSelectedItem();
            updateBooksByGenre(selectedGenre, returnBookComboBox);
        });

        JButton returnBookButton = new JButton("Return Book");
        returnBookButton.addActionListener(new ReturnBookActionListener(returnUserComboBox, returnBookComboBox));
        returnBookPanel.add(returnBookButton);

        returnPanel.add(returnBookPanel, BorderLayout.NORTH);

        String[] returnColumnNames = { "Username", "Book Title", "Return Date" };
        returnTableModel = new DefaultTableModel(returnColumnNames, 0);
        returnTable = new JTable(returnTableModel);
        returnPanel.add(new JScrollPane(returnTable), BorderLayout.CENTER);

        loadReturnedBooksData();
        return returnPanel;
    }

    private void updateBooksLentToUser(String username, JComboBox<String> returnBookComboBox) {
        MongoCollection<Document> lendingCollection = MongoDBUtil.getCollection("lendings");
        FindIterable<Document> lentBooks = lendingCollection.find(new Document("username", username));

        // Clear the current items in the combo box
        returnBookComboBox.removeAllItems();

        for (Document lentBook : lentBooks) {
            String bookTitle = lentBook.getString("bookTitle");
            returnBookComboBox.addItem(bookTitle);
        }

        if (returnBookComboBox.getItemCount() == 0) {
            JOptionPane.showMessageDialog(null, "This user has no books to return.");
        }
    }


    // Action Listener for Lending Books
    private class LendBookActionListener implements ActionListener {
        private JComboBox<String> userComboBox;
        private JComboBox<String> bookComboBox;

        public LendBookActionListener(JComboBox<String> userComboBox, JComboBox<String> bookComboBox) {
            this.userComboBox = userComboBox;
            this.bookComboBox = bookComboBox;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String username = (String) userComboBox.getSelectedItem();
            String bookTitle = (String) bookComboBox.getSelectedItem();

            MongoCollection<Document> bookCollection = MongoDBUtil.getCollection("books");
            Document book = bookCollection.find(new Document("title", bookTitle)).first();

            if (book != null) {
                int quantity = book.getInteger("quantity");
                if (quantity > 0) {
                    // Reduce quantity by 1
                    bookCollection.updateOne(new Document("title", bookTitle),
                            new Document("$set", new Document("quantity", quantity - 1)));

                    // Add to MongoDB lending collection
                    MongoCollection<Document> lendingCollection = MongoDBUtil.getCollection("lendings");
                    Document lendRecord = new Document("username", username)
                            .append("bookTitle", bookTitle)
                            .append("lendDate", LocalDate.now().toString())
                            .append("returnDate", LocalDate.now().plusDays(7).toString()); // Default lending period is 1 week

                    lendingCollection.insertOne(lendRecord);
                    JOptionPane.showMessageDialog(null, "Book lent successfully!");

                    loadLendingData();
                    loadBooksFromDatabase(); // Refresh book table
                } else {
                    JOptionPane.showMessageDialog(null, "Book is out of stock!");
                }
            } else {
                JOptionPane.showMessageDialog(null, "Book not found!");
            }
        }
    }




    // Action Listener for Returning Books
    private class ReturnBookActionListener implements ActionListener {
        private JComboBox<String> returnUserComboBox;
        private JComboBox<String> returnBookComboBox;

        public ReturnBookActionListener(JComboBox<String> returnUserComboBox, JComboBox<String> returnBookComboBox) {
            this.returnUserComboBox = returnUserComboBox;
            this.returnBookComboBox = returnBookComboBox;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String username = (String) returnUserComboBox.getSelectedItem();
            String bookTitle = (String) returnBookComboBox.getSelectedItem();

            if (bookTitle == null) {
                JOptionPane.showMessageDialog(null, "No book selected for return.");
                return;
            }

            MongoCollection<Document> bookCollection = MongoDBUtil.getCollection("books");
            Document book = bookCollection.find(new Document("title", bookTitle)).first();

            if (book != null) {
                int quantity = book.getInteger("quantity");
                // Increase quantity by 1
                bookCollection.updateOne(new Document("title", bookTitle),
                        new Document("$set", new Document("quantity", quantity + 1)));

                // Remove the book from the lending collection
                MongoCollection<Document> lendingCollection = MongoDBUtil.getCollection("lendings");
                lendingCollection.deleteOne(new Document("username", username).append("bookTitle", bookTitle));

                // Add the return record to the returns collection
                MongoCollection<Document> returnCollection = MongoDBUtil.getCollection("returns");
                Document returnRecord = new Document("username", username)
                        .append("bookTitle", bookTitle)
                        .append("returnDate", LocalDate.now().toString());

                returnCollection.insertOne(returnRecord);
                JOptionPane.showMessageDialog(null, "Book returned successfully!");

                // Refresh UI components
                loadReturnedBooksData();
                updateBooksLentToUser(username, returnBookComboBox); // Update books available for return
                loadBooksFromDatabase(); // Refresh book table
            } else {
                JOptionPane.showMessageDialog(null, "Book not found!");
            }
        }
    }



    // Load Lending Data from MongoDB
    private void loadLendingData() {
        MongoCollection<Document> lendingCollection = MongoDBUtil.getCollection("lendings");
        lendingTableModel.setRowCount(0);
        for (Document lend : lendingCollection.find()) {
            String username = lend.getString("username");
            String bookTitle = lend.getString("bookTitle");
            String lendDate = lend.getString("lendDate");
            String returnDate = lend.getString("returnDate");
            lendingTableModel.addRow(new Object[]{username, bookTitle, lendDate, returnDate});
        }
    }

    // Load Returned Books Data from MongoDB
    private void loadReturnedBooksData() {
        MongoCollection<Document> returnCollection = MongoDBUtil.getCollection("returns");
        returnTableModel.setRowCount(0);
        for (Document returnRecord : returnCollection.find()) {
            String username = returnRecord.getString("username");
            String bookTitle = returnRecord.getString("bookTitle");
            String returnDate = returnRecord.getString("returnDate");
            returnTableModel.addRow(new Object[]{username, bookTitle, returnDate});
        }
    }


    private String[] getUsernamesFromDatabase() {
        MongoCollection<Document> userCollection = MongoDBUtil.getCollection("users");
        // Use 'into' to convert the MongoIterable to a List
        List<String> usernamesList = userCollection.find()
                .map(user -> user.getString("username"))
                .into(new ArrayList<>());

        // Convert the List to an array
        return usernamesList.toArray(new String[0]);
    }


    // Get all books for combo box
    private String[] getBooksFromDatabase() {
        MongoCollection<Document> booksCollection = MongoDBUtil.getCollection("books");
        // Use 'into' to convert the MongoIterable to a List
        List<String> booksList = booksCollection.find()
                .map(book -> book.getString("title"))
                .into(new ArrayList<>());

        // Convert the List to an array
        return booksList.toArray(new String[0]);
    }

    private void loadUsersFromDatabase() {
        MongoCollection<Document> userCollection = MongoDBUtil.getCollection("users");
        userTableModel.setRowCount(0);
        for (Document user : userCollection.find()) {
            String username = user.getString("username");
            String password = user.getString("password");
            String role = user.getString("role");
            userTableModel.addRow(new Object[]{username, password, role});
        }
    }

    private void loadBooksFromDatabase() {
        MongoCollection<Document> booksCollection = MongoDBUtil.getCollection("books");
        bookTableModel.setRowCount(0);
        for (Document book : booksCollection.find()) {
            String title = book.getString("title");
            String author = book.getString("author");
            String genre = book.getString("genre");
            int quantity = book.getInteger("quantity");
            bookTableModel.addRow(new Object[]{title, author, genre, quantity});
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new AdminDashboard().setVisible(true));
    }
}
