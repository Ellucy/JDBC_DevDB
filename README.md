### Author
- **Eleri Mets**
    - GitHub: [Ellucy](https://github.com/Ellucy)
  
### SQL to create a database with required tables and add some seed data
```sql
CREATE DATABASE DevTeamDB;
USE DevTeamDB;

    CREATE TABLE developers (
        developer_id INT PRIMARY KEY AUTO_INCREMENT,
        full_name VARCHAR(128) NOT NULL UNIQUE,
        email VARCHAR(128) NOT NULL,
        hire_date DATE NOT NULL
	);

    CREATE TABLE programming_languages (
        language_id INT PRIMARY KEY AUTO_INCREMENT,
        language_name VARCHAR(50) NOT NULL
	);
    
    CREATE TABLE developer_languages (
        developer_id INT,
        language_id INT,
        PRIMARY KEY (developer_id, language_id),
        FOREIGN KEY (developer_id) REFERENCES developers(developer_id),
        FOREIGN KEY (language_id) REFERENCES programming_languages(language_id)
	);

    CREATE TABLE developer_teams (
        team_id INT PRIMARY KEY AUTO_INCREMENT,
        team_name VARCHAR(100) NOT NULL
	);
    
    CREATE TABLE team_members (
        team_id INT,
        developer_id INT,
        PRIMARY KEY (team_id, developer_id),
        FOREIGN KEY (team_id) REFERENCES developer_teams(team_id),
        FOREIGN KEY (developer_id) REFERENCES developers(developer_id)
    );

    INSERT INTO developers (full_name, email, hire_date) VALUES
        ('John Doe', 'john.doe@example.com', '2023-01-20'),
        ('Jane Smith', 'jane.smith@example.com', '2023-02-15'),
        ('Flo Smidth', 'flo.smidth@example.com', '2023-02-15'),
        ('Hatef Johnson', 'hatef.johnson@example.com', '2023-11-11');

    INSERT INTO programming_languages (language_name) VALUES
        ('Java'),
        ('Python'),
        ('JavaScript');

    INSERT INTO developer_languages (developer_id, language_id) VALUES
        (1, 1),
        (1, 2),
        (1, 3),
        (2, 3),
        (3, 1),
        (4, 1),
        (4, 2);

    INSERT INTO developer_teams (team_name) VALUES
        ('Team A'),
        ('Team B');
    
    INSERT INTO team_members (team_id, developer_id) VALUES
        (1, 1),
        (1, 2),
        (1, 3),
        (2, 3),
        (2, 1),
        (2, 4);
```