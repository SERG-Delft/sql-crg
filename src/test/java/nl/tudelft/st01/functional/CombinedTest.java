package nl.tudelft.st01.functional;

import org.junit.jupiter.api.Test;

import static nl.tudelft.st01.functional.AssertUtils.verify;

/**
 * This class tests if the coverage targets for complex queries (with joins, conditionals,
 * aggregation functions, etc.) are generated correctly.
 *
 * Suppresses the checkstyle multipleStringLiterals violation, because some output sets have queries in common.
 */
@SuppressWarnings("checkstyle:multipleStringLiterals")
public class CombinedTest {

    /**
     * A test case with WHERE, JOIN and AGGREGATE parts.
     */
    @Test
    public void testIntegratedWhereJoinAggregate() {
        verify("SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id < 10 GROUP BY b.id",

                // WHERE RESULTS
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id = 9 GROUP BY b.id",
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id = 11 GROUP BY b.id",
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id = 10 GROUP BY b.id",
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id IS NULL GROUP BY b.id",

                // JOIN RESULTS
                "SELECT AVG(b.id) FROM a RIGHT JOIN b ON a.id = b.id "
                    + "WHERE (a.id IS NULL) AND (b.id IS NOT NULL) GROUP BY b.id",
                "SELECT AVG(b.id) FROM a RIGHT JOIN b ON a.id = b.id "
                    + "WHERE (a.id IS NULL) AND (b.id IS NULL) GROUP BY b.id",
                "SELECT AVG(b.id) FROM a LEFT JOIN b ON a.id = b.id "
                    + "WHERE ((b.id IS NULL) AND (a.id IS NOT NULL)) AND (a.id < 10) GROUP BY b.id",
                "SELECT AVG(b.id) FROM a LEFT JOIN b ON a.id = b.id "
                    + "WHERE (b.id IS NULL) AND (a.id IS NULL) GROUP BY b.id",
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id "
                    + "WHERE (a.id < 10) GROUP BY b.id",

                // AGGREGATE RESULTS
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id < 10 GROUP BY b.id "
                    + "HAVING COUNT(b.id) > COUNT(DISTINCT b.id) AND COUNT(DISTINCT b.id) > 1",
                "SELECT COUNT(*) FROM a INNER JOIN b ON a.id = b.id WHERE a.id < 10 "
                    + "HAVING COUNT(DISTINCT b.id) > 1",
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id < 10 GROUP BY b.id "
                    + "HAVING COUNT(*) > COUNT(b.id) AND COUNT(DISTINCT b.id) > 1",
                "SELECT AVG(b.id) FROM a INNER JOIN b ON a.id = b.id WHERE a.id < 10 GROUP BY b.id "
                    + "HAVING COUNT(*) > 1");
    }

    /**
     * Tests whether the columns related to the left table (in case of LEFT JOIN)
     * or the right table (in case of RIGHT JOIN) that are not the columns used in the on expression
     * are still present in the where expression.
     */
    @Test
    public void testJoinWithWhereNonIdsIncludedInWhereExpression() {
        verify("SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE a.length < 50 OR b.length > 70",

                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE NOT (a.length < 50) AND (b.length = 69)",
                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE NOT (a.length < 50) AND (b.length = 71)",
                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE NOT (a.length < 50) AND (b.length = 70)",

                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.length = 49) AND NOT (b.length > 70)",
                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.length = 50) AND NOT (b.length > 70)",
                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.length = 51) AND NOT (b.length > 70)",

                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE NOT (a.length < 50) AND (b.length IS NULL)",
                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.length IS NULL) AND NOT (b.length > 70)",

                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.length < 50 OR b.length > 70)",
                "SELECT * FROM a LEFT JOIN b ON a.id = b.id "
                    + "WHERE ((b.id IS NULL) AND (a.id IS NULL)) AND (a.length < 50)",
                "SELECT * FROM a LEFT JOIN b ON a.id = b.id "
                    + "WHERE ((b.id IS NULL) AND (a.id IS NOT NULL)) AND (a.length < 50)",
                "SELECT * FROM a RIGHT JOIN b ON a.id = b.id "
                    + "WHERE ((a.id IS NULL) AND (b.id IS NULL)) AND (b.length > 70)",
                "SELECT * FROM a RIGHT JOIN b ON a.id = b.id "
                    + "WHERE ((a.id IS NULL) AND (b.id IS NOT NULL)) AND (b.length > 70)");
    }

    /**
     * Tests whether the columns related to the left table (in case of LEFT JOIN)
     * or the right table (in case of RIGHT JOIN) that are not the columns used in the on expression
     * are still present in the where expression.
     */
    @Test
    public void testJoinWithWhereIdsExcludedInWhereExpressionWhenIsNull() {
        verify("SELECT * FROM a RIGHT JOIN b ON a.id = b.id WHERE a.id <= 50 AND b.id >= 70",

                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.id = 49) AND (b.id >= 70)",
                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.id = 50) AND (b.id >= 70)",
                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.id = 51) AND (b.id >= 70)",

                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.id <= 50) AND (b.id = 70)",
                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.id <= 50) AND (b.id = 71)",
                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.id <= 50) AND (b.id = 69)",

                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.id IS NULL) AND (b.id >= 70)",
                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.id <= 50) AND (b.id IS NULL)",

                "SELECT * FROM a INNER JOIN b ON a.id = b.id WHERE (a.id <= 50 AND b.id >= 70)",
                "SELECT * FROM a LEFT JOIN b ON a.id = b.id WHERE (b.id IS NULL) AND (a.id IS NULL)",
                "SELECT * FROM a LEFT JOIN b ON a.id = b.id "
                    + "WHERE ((b.id IS NULL) AND (a.id IS NOT NULL)) AND (a.id <= 50)",
                "SELECT * FROM a RIGHT JOIN b ON a.id = b.id WHERE (a.id IS NULL) AND (b.id IS NULL)",
                "SELECT * FROM a RIGHT JOIN b ON a.id = b.id "
                    + "WHERE ((a.id IS NULL) AND (b.id IS NOT NULL)) AND (b.id >= 70)");
    }


    /**
     * Tests the most basic query, for which case no mutations should be generated.
     *
     * Note that there are no queries in the 'expected' area, which indicates no queries should be generated.
     */
    @Test
    public void testQueryOnlySelectAndFrom() {
        verify("SELECT * FROM TableA");
    }
}
