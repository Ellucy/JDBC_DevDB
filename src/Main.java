import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {

        String dbUrl = "jdbc:mysql://localhost:3306/DevTeamDB";
        String username = "root";
        String password = "1234";
        char choice = 'y';
        Scanner scanner = new Scanner(System.in);

        try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
            while (choice == 'y') {

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
                        readData(connection);
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

    public static void readData(Connection connection) throws SQLException {

        String sql =
                "SELECT " +
                    "d.developer_id, " +
                    "d.full_name, " +
                    "d.email, " +
                    "d.hire_date, " +
                    "GROUP_CONCAT(DISTINCT pl.language_name) AS languages, " +
                    "GROUP_CONCAT(DISTINCT dt.team_name) AS teams\n" +
                "FROM developers d\n" +
                "LEFT JOIN developer_languages dl ON (d.developer_id = dl.developer_id)\n" +
                "LEFT JOIN programming_languages pl ON (dl.language_id = pl.language_id)\n" +
                "LEFT JOIN team_members tm ON (d.developer_id = tm.developer_id)\n" +
                "LEFT JOIN developer_teams dt ON (tm.team_id = dt.team_id)\n" +
                "GROUP BY d.developer_id\n" +
                "ORDER BY d.developer_id";

        Statement statement = connection.createStatement();
        ResultSet result = statement.executeQuery(sql);

        Map<Integer, DeveloperInfo> developersMap = new HashMap<>();

        while (result.next()) {
            int developerId = result.getInt("developer_id");
            String name = result.getString("d.full_name");
            String email = result.getString("d.email");
            Date date = result.getDate("d.hire_date");
            String programmingLanguage = result.getString("languages");
            String team = result.getString("teams");

            DeveloperInfo developerInfo = developersMap.computeIfAbsent(developerId, k -> new DeveloperInfo(name, email, date));
            developerInfo.addProgrammingLanguage(programmingLanguage);
            developerInfo.addTeam(team);
        }

        for (DeveloperInfo developerInfo : developersMap.values()) {
            System.out.println(developerInfo.toString());
        }
    }
}
