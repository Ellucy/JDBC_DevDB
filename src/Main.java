import java.sql.*;
import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

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
                        "i - add another developer\n" +
                        "v - view data\n" +
                        "d - delete data\n" +
                        "r - remove all data");

                char subChoice = scanner.nextLine().charAt(0);

                switch (subChoice) {
                    case 'i':
                        System.out.print("Insert full name: ");
                        String name = scanner.nextLine();

                        System.out.print("Insert email: ");
                        String email = scanner.nextLine();

                        System.out.print("Insert existing programming language(s) separated by commas(Java,Python,JavaScript): ");
                        String languagesInput = scanner.nextLine();
                        List<String> languages = Arrays.stream(languagesInput.split(","))
                                .map(String::trim)
                                .collect(Collectors.toList());

                        System.out.print("Insert team(s) separated by commas(A,B): ");
                        String teamsInput = scanner.nextLine().toLowerCase();

                        if (teamsInput.equals("a")) {
                            teamsInput = "Team A";
                        }
                        if (teamsInput.equals("b")) {
                            teamsInput = "Team B";
                        }

                        List<String> teams = Arrays.stream(teamsInput.split(","))
                                .map(String::trim)
                                .collect(Collectors.toList());

                        addDeveloper(connection, name, email, new Date(System.currentTimeMillis()), languages, teams);
                        break;
                    case 'v':
                        readData(connection);
                        break;
                    case 'd':
                        System.out.println("Selected: Delete Data");
                        break;
                    case 'r':
                        char confirm;
                        System.out.println("Are you sure You want to delete all the data? You cannot undo this action (y/n)");
                        confirm = scanner.nextLine().charAt(0);

                        if (confirm == 'y') {
                            removeAllDevelopers(connection);
                        } else if (confirm == 'n') {
                            System.out.println("Good idea!");
                        } else {
                            System.out.println("Invalid input");
                        }
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

    public static void addDeveloper(Connection connection, String fullName, String email, Date hireDate,
                                    List<String> languages, List<String> teams) throws SQLException {

        String addDeveloperSql = "INSERT INTO developers (full_name, email, hire_date) VALUES (?, ?, ?)";
        String addLanguagesSql = "INSERT INTO developer_languages (developer_id, language_id) VALUES (?, " +
                "(SELECT language_id FROM programming_languages WHERE language_name = ?))";
        String addTeamsSql = "INSERT INTO team_members (team_id, developer_id) VALUES (" +
                "(SELECT team_id FROM developer_teams WHERE team_name = ?), ?)";

        try (PreparedStatement addDeveloperStatement = connection.prepareStatement(addDeveloperSql, Statement.RETURN_GENERATED_KEYS);
             PreparedStatement addLanguagesStatement = connection.prepareStatement(addLanguagesSql);
             PreparedStatement addTeamsStatement = connection.prepareStatement(addTeamsSql)) {

            addDeveloperStatement.setString(1, fullName);
            addDeveloperStatement.setString(2, email);
            addDeveloperStatement.setDate(3, hireDate);

            int affectedRows = addDeveloperStatement.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("Creating developer failed.");
            }

            try (ResultSet result = addDeveloperStatement.getGeneratedKeys()) {
                if (result.next()) {
                    int developerId = result.getInt(1);

                    for (String language : languages) {
                        addLanguagesStatement.setInt(1, developerId);
                        addLanguagesStatement.setString(2, language);
                        addLanguagesStatement.addBatch();
                    }

                    for (String team : teams) {
                        addTeamsStatement.setString(1, team);
                        addTeamsStatement.setInt(2, developerId);
                        addTeamsStatement.addBatch();
                    }

                    addLanguagesStatement.executeBatch();
                    addTeamsStatement.executeBatch();
                } else {
                    throw new SQLException("Creating developer failed, no ID obtained.");
                }
            }
        }
    }

    public static void removeAllDevelopers(Connection connection) throws SQLException {

        try {

            connection.setAutoCommit(false);

            // Delete from team_members
            String deleteTeamMembersSql = "DELETE FROM team_members WHERE developer_id IN (SELECT developer_id FROM developers)";
            try (PreparedStatement deleteTeamMembersStatement = connection.prepareStatement(deleteTeamMembersSql)) {
                deleteTeamMembersStatement.executeUpdate();
            }

            // Delete from developer_languages
            String deleteDeveloperLanguagesSql = "DELETE FROM developer_languages WHERE developer_id IN (SELECT developer_id FROM developers)";
            try (PreparedStatement deleteDeveloperLanguagesStatement = connection.prepareStatement(deleteDeveloperLanguagesSql)) {
                deleteDeveloperLanguagesStatement.executeUpdate();
            }

            // Delete from developers
            String deleteDevelopersSql = "DELETE FROM developers";
            try (PreparedStatement deleteDevelopersStatement = connection.prepareStatement(deleteDevelopersSql)) {
                deleteDevelopersStatement.executeUpdate();
            }
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }
}
