package shuar;

import shuar.Shuar.Source;
import shuar.source.sql.SqlSource;

import java.sql.Connection;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ScottTigerIT extends BaseSqlIT {
    protected final static int expectedRecordCount = 4;

    final static String query = """
             SELECT      *
             FROM        emp JOIN dept ON emp.deptno = dept.deptno
             WHERE       dept.dname = :departmentName
               AND       emp.sal >= :minimumSalary
               AND       emp.hiredate >= :minimumDate
             ORDER BY    dept.dname, emp.ename
            """;
    final static Map<String, Object> parameters = Map.of(
            "departmentName", "SALES",
            "minimumSalary", 5000,
            "minimumDate", LocalDate.of(2011, 2, 22)
    );

    protected final static Function<Connection, Source<ResultSet>> source =
            (connection) -> new SqlSource(connection, query, parameters);

    @Override
    protected List<String> resourceNames() {
        return List.of("sql/scott-tiger.sql");
    }
}
