import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        String dbUrl = "jdbc:mysql://localhost:3306/DevTeamDB";
        String username = "root";
        String password = "1234";
        char choice = 'y';
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
            while(choice == 'y') {

                System.out.println("What do you want to do?\n" +
                        "i - input data\n" +
                        "v - view data\n" +
                        "d - delete data\n" +
                        "r - remove all data");

                char subChoice = scanner.nextLine().charAt(0);

                switch (subChoice) {
                    case 'i':
                        System.out.println("Selected: Input Data");
                        break;
                    case 'v':
                        System.out.println("Selected: View Data");
                        break;
                    case 'd':
                        System.out.println("Selected: Delete Data");
                        break;
                    case 'r':
                        System.out.println("Selected: Remove All Data");
                        break;
                    default:
                        System.out.println("Invalid choice");
                        break;
                }

                System.out.println("Do you want to do something else? (y/n)");
                choice = scanner.nextLine().charAt(0);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
