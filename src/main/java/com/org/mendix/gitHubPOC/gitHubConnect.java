package com.org.mendix.gitHubPOC;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.eclipse.egit.github.core.RepositoryCommit;
import org.eclipse.egit.github.core.RepositoryId;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.CommitService;
import org.eclipse.egit.github.core.service.RepositoryService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.Serializable;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class gitHubConnect implements Serializable {




    public static  Dataset<Row> gitHubCheck() throws IOException {

        /*
            Initialising the Hadoop properties and Spark session
            on local Master
        */

        System.setProperty("hadoop.home.dir", "D:\\User\\sharibabu\\hadoop");

        SparkSession spark = SparkSession
                .builder()
                .appName("gitHubCheck")

                .master("local")
                .getOrCreate();

        JavaSparkContext javaSparkContext = new JavaSparkContext(spark.sparkContext());


        // Using GITHUB java client API, to read the repo
        GitHubClient client = new GitHubClient();
        RepositoryService service = new RepositoryService(client);
        RestTemplate restTemplate = new RestTemplate();

        // Setting up the headers to read the repo configs
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
        HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);

        // Using Rest service- Hitting the required Git HUB URL
        List<Map> response = restTemplate.exchange("https://api.github.com/users/bambulab/repos", HttpMethod.GET, entity, List.class).getBody();

        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
        LocalDateTime curentTimeNow = LocalDateTime.now();

        CommitService commitService = new CommitService();
        Dataset<Row> filteredDf = null;

        // Looping across the repos and loading the commit history
        for (int i = 0; i < response.size(); i++) {

            Map value = response.get(i);
            String repoName = (String) value.get("name");
            Map ownerDetail = (Map) value.get("owner");
            String ownerName = (String) ownerDetail.get("login");
            RepositoryId repositoryId = new RepositoryId(ownerName, repoName);
            List<RepositoryCommit> commitList = commitService.getCommits(repositoryId);

            // Using parallelize distributing the data across the machines
            JavaRDD<Row> rowRDD = javaSparkContext.parallelize(commitList).
                    map(RepositoryCommit -> RowFactory.create(RepositoryCommit.getCommit().getAuthor().getName(),
                            RepositoryCommit.getCommit().getAuthor().getEmail(),
                            formatter.format(RepositoryCommit.getCommit().getAuthor().getDate()),
                            RepositoryCommit.getCommit().getMessage(),
                            RepositoryCommit.getSha()));

            // creation of Schema for the dataframe
            List<StructField> fields = new ArrayList<>();
            fields.add(DataTypes.createStructField("Name", DataTypes.StringType, false));
            fields.add(DataTypes.createStructField("Email", DataTypes.StringType, false));
            fields.add(DataTypes.createStructField("Commit_Date", DataTypes.StringType, false));
            fields.add(DataTypes.createStructField("Commit_Message", DataTypes.StringType, false));
            fields.add(DataTypes.createStructField("Commit_ID", DataTypes.StringType, false));
            StructType schema = DataTypes.createStructType(fields);

            //Create Data frame
            Dataset<Row> df = spark.createDataFrame(rowRDD, schema);
            // filter data frame to the specific date

            filteredDf = df.filter(" Commit_Date between '01-07-2022' and  '15-07-2022' ");
            System.out.println(filteredDf.showString(10, 0, false));

            // saving data to Hive dB
            //filteredDf.write().mode("append").saveAsTable("gitHub_History");
        }

        return filteredDf;
    }




/*
DB connection for PostgreSQL and inserting the Dataframe
 */

    public static void dbConnection (Dataset<Row> df) throws SQLException {
        Connection connnection = null;
        final ResultSet[] resultSet = {null};
        int resultCount = 0;
        try{
            String postgreURL = "jdbc:postgresql://localhost:5432/mendix";
            String userName = "postgres";
            String password = "Srini@6655";
             connnection = DriverManager.getConnection(postgreURL,userName,password);
            System.out.println("connected to DB");
            Statement statement = connnection.createStatement();
            AtomicInteger rowsupdate = new AtomicInteger();
            Row[] result = df.collectAsList().toArray(new Row[0]);

            for (Row row : result) {
                String insertSql = "INSERT INTO GITHUB_HIS (name,email,commit_date,commit_message,commit_id)" +
                        " VALUES ("
                        + "'"+ row.get(0) + "',"+
                         "'"+ row.get(1) +"',"+
                         "'"+ row.get(2) +"',"+
                         "'"+ row.get(3)+"',"+
                         "'"+ row.get(4)+"')";
                System.out.println(insertSql);
                resultCount =  statement.executeUpdate(insertSql);
            }

            if(resultCount >0){
                System.out.println("rows insrted -"+resultCount);
            }
        } catch (Exception e){
            e.printStackTrace();
        }finally {
            //if (connection != null) try { connection.close(); } catch (SQLException logOrIgnore) {}
            connnection.close();
        }

    }


    public static void main(String[] args) throws IOException, SQLException {
        Dataset<Row> df = gitHubCheck();
        df.cache();
        dbConnection (df);


    }
}
