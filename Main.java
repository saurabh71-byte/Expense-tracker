import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class Main {

    // Transaction class to hold each record
    static class Transaction {
        enum Type { INCOME, EXPENSE }
        Type type;
        String category;
        double amount;
        LocalDate date;

        public Transaction(Type type, String category, double amount, LocalDate date) {
            this.type = type;
            this.category = category;
            this.amount = amount;
            this.date = date;
        }

        @Override
        public String toString() {
            return type + "," + category + "," + amount + "," + date;
        }

        static Transaction fromString(String line) {
            // Format: TYPE,CATEGORY,AMOUNT,DATE (yyyy-MM-dd)
            String[] parts = line.split(",");
            if (parts.length != 4) return null;
            Type type = Type.valueOf(parts[0]);
            String category = parts[1];
            double amount = Double.parseDouble(parts[2]);
            LocalDate date = LocalDate.parse(parts[3]);
            return new Transaction(type, category, amount, date);
        }
    }

    static List<Transaction> transactions = new ArrayList<>();
    static Scanner scanner = new Scanner(System.in);

    // Predefined categories (you can expand)
    static final List<String> incomeCategories = Arrays.asList("Salary", "Business", "Interest", "Other");
    static final List<String> expenseCategories = Arrays.asList("Food", "Rent", "Travel", "Entertainment", "Other");

    public static void main(String[] args) {
        System.out.println("Welcome to Expense Tracker");

        // Load data if file provided as argument
        if (args.length > 0) {
            loadFromFile(args[0]);
        }

        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1. Add Income");
            System.out.println("2. Add Expense");
            System.out.println("3. Show Monthly Summary");
            System.out.println("4. Save Data to File");
            System.out.println("5. Exit");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1" -> addTransaction(Transaction.Type.INCOME);
                case "2" -> addTransaction(Transaction.Type.EXPENSE);
                case "3" -> showMonthlySummary();
                case "4" -> {
                    System.out.print("Enter filename to save: ");
                    String filename = scanner.nextLine();
                    saveToFile(filename);
                }
                case "5" -> {
                    System.out.println("Exiting... Goodbye!");
                    System.exit(0);
                }
                default -> System.out.println("Invalid choice, try again.");
            }
        }
    }

    static void addTransaction(Transaction.Type type) {
        System.out.println("Choose " + type + " category:");
        List<String> categories = (type == Transaction.Type.INCOME) ? incomeCategories : expenseCategories;

        for (int i = 0; i < categories.size(); i++) {
            System.out.println((i + 1) + ". " + categories.get(i));
        }
        System.out.print("Enter category number: ");
        int catIndex;
        try {
            catIndex = Integer.parseInt(scanner.nextLine()) - 1;
            if (catIndex < 0 || catIndex >= categories.size()) {
                System.out.println("Invalid category choice.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        String category = categories.get(catIndex);

        System.out.print("Enter amount: ");
        double amount;
        try {
            amount = Double.parseDouble(scanner.nextLine());
            if (amount <= 0) {
                System.out.println("Amount must be positive.");
                return;
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount.");
            return;
        }

        System.out.print("Enter date (yyyy-MM-dd) or leave empty for today: ");
        String dateStr = scanner.nextLine().trim();
        LocalDate date;
        if (dateStr.isEmpty()) {
            date = LocalDate.now();
        } else {
            try {
                date = LocalDate.parse(dateStr);
            } catch (Exception e) {
                System.out.println("Invalid date format.");
                return;
            }
        }

        Transaction t = new Transaction(type, category, amount, date);
        transactions.add(t);
        System.out.println("Transaction added successfully.");
    }

    static void showMonthlySummary() {
        System.out.print("Enter month and year (MM-yyyy): ");
        String input = scanner.nextLine();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-yyyy");
        LocalDate date;
        try {
            date = LocalDate.parse("01-" + input, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        } catch (Exception e) {
            System.out.println("Invalid month-year format.");
            return;
        }

        double totalIncome = 0;
        double totalExpense = 0;

        Map<String, Double> incomeByCategory = new HashMap<>();
        Map<String, Double> expenseByCategory = new HashMap<>();

        for (Transaction t : transactions) {
            if (t.date.getMonth() == date.getMonth() && t.date.getYear() == date.getYear()) {
                if (t.type == Transaction.Type.INCOME) {
                    totalIncome += t.amount;
                    incomeByCategory.put(t.category, incomeByCategory.getOrDefault(t.category, 0.0) + t.amount);
                } else {
                    totalExpense += t.amount;
                    expenseByCategory.put(t.category, expenseByCategory.getOrDefault(t.category, 0.0) + t.amount);
                }
            }
        }

        System.out.println("\nSummary for " + date.getMonth() + " " + date.getYear());
        System.out.println("Total Income: " + totalIncome);
        for (var entry : incomeByCategory.entrySet()) {
            System.out.printf("  %s : %.2f%n", entry.getKey(), entry.getValue());
        }

        System.out.println("Total Expense: " + totalExpense);
        for (var entry : expenseByCategory.entrySet()) {
            System.out.printf("  %s : %.2f%n", entry.getKey(), entry.getValue());
        }

        System.out.println("Net Savings: " + (totalIncome - totalExpense));
    }

    static void saveToFile(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            for (Transaction t : transactions) {
                writer.println(t.toString());
            }
            System.out.println("Data saved to " + filename);
        } catch (IOException e) {
            System.out.println("Error saving to file: " + e.getMessage());
        }
    }

    static void loadFromFile(String filename) {
        File file = new File(filename);
        if (!file.exists()) {
            System.out.println("File not found: " + filename);
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
                Transaction t = Transaction.fromString(line.trim());
                if (t != null) {
                    transactions.add(t);
                    count++;
                }
            }
            System.out.println(count + " transactions loaded from " + filename);
        } catch (IOException e) {
            System.out.println("Error loading from file: " + e.getMessage());
        }
    }
}
