package nl.tudelft.st01.sqlfpcws;

import java.util.List;

public final class Application {

    protected Application() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    public static void main(String[] args) {
        String sqlQuery = "SELECT * FROM tableA WHERE tableA.id = 1";
        String dbSchema =   "<schema dbms=\"MySQL\">\n"
                        + "    <table name=\"tableA\">\n"
                        + "        <column name=\"id\" type=\"VARCHAR\" size=\"50\" key=\"true\" notnull=\"true\"/>\n"
                        + "        <column name=\"name\" type=\"VARCHAR\" size=\"255\" default=\"NULL\"/>\n"
                        + "    </table>\n"
                        + "</schema>";
        String options = "";

        List<String> result = SQLFpcWS.getCoverageTargets(sqlQuery, dbSchema, options);

        System.out.println("Coverage targets:");
        for (String s : result) {
            System.out.println("\"" + s + "\",");
        }
    }
}
