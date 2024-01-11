import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeveloperInfo {
    private final String name;
    private final String email;
    private final Date hireDate;
    private final List<String> programmingLanguages = new ArrayList<>();
    private final List<String> teams = new ArrayList<>();

    public DeveloperInfo(String name, String email, Date hireDate) {
        this.name = name;
        this.email = email;
        this.hireDate = hireDate;
    }

    public void addProgrammingLanguage(String language) {
        programmingLanguages.add(language);
    }

    public void addTeam(String team) {
        teams.add(team);
    }

    @Override
    public String toString() {
        return String.format("Developer %s (%s) started working %tF. Languages: %s. Works in team(s): %s",
                name, email, hireDate, String.join(", ", programmingLanguages), String.join(", ", teams));
    }
}
