package shuar.dsl;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URI;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static shuar.util.Utils.*;

public class ShuarScriptIT {
    @Test
    public void distillerScriptWorks() {
        class MyScript extends ShuarScript<ResultSet, String, URI>
                implements SqlDelimitedRecordBuilder, FileReducerBuilder {
            {
                withConnection(c -> {
                    final var script = readResource("sql/scott-tiger.sql");
                    Arrays.stream(script.split(";\n"))
                            .forEach(sql -> runUnchecked(() -> c.createStatement().execute(sql)));
                });
            }
        }

        final var file = new File("target/myscript.tsv");

        final var myScript = new MyScript() {{
            dbDriver("org.h2.Driver", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=false");
            dbCredentials("sa", "sa");

            delimiter("\t");
            outputFilename(file.getPath());

            field("department", rsString("dname"));
            field("employee", rsString("ename"));
            dateField("hireDate", "hiredate", "yyyy-MM-dd");
            numberField("salary", rsDouble("sal"), "######.##");

            query("""
                     SELECT      *
                     FROM        emp JOIN dept ON emp.deptno = dept.deptno
                     WHERE       dept.dname = :departmentName
                       AND       emp.sal >= :minimumSalary
                       AND       emp.hiredate >= :minimumDate
                     ORDER BY    dept.dname, emp.ename
                    """);

            parameters(Map.of(
                    "departmentName", "SALES",
                    "minimumSalary", 5000,
                    "minimumDate", LocalDate.of(2011, 2, 22)
            ));
        }};

        final var result = myScript.mapReduce();
        assertEquals(file.getAbsolutePath(), result.getRawPath());

        final var expectedRecords = List.of(
                "SALES\tBLAKE\t2011-05-01\t14250",
                "SALES\tMARTIN\t2011-09-28\t6250",
                "SALES\tTURNER\t2011-09-08\t6000",
                "SALES\tWARD\t2011-02-22\t6250"
        );
        final var actualRecords = readLines(file);
        assertEquals(expectedRecords, actualRecords);

    }
}
