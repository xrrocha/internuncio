package shuar.dsl;

import org.junit.jupiter.api.Test;
import shuar.ScottTigerIT;
import shuar.Shuar;
import shuar.Shuar.Mapper;
import shuar.reducer.file.FileReducer;
import shuar.util.Utils;

import java.io.File;
import java.sql.ResultSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FixedRecordBuilderIT extends ScottTigerIT
        implements SqlSourceBuilder, FixedRecordBuilder<ResultSet> {

    private final int recordLength = 40;
    private final Mapper<ResultSet, String> fixedRecordMapper;

    {
        length(recordLength);
        field("department", 0, 14, rsString("dname"));
        field("employee", 14, 10, rsString("ename"));
        dateField("hireDate", 24, 10, rsDate("hiredate"), "yyyy-MM-dd");
        numberField("salary", 34, 5, rsDouble("sal"), "######.##");
        field("trailer", 39, 1, rs -> "\n");

        fixedRecordMapper = buildMapper();
    }


    @Test
    public void fixedRecordExtractorWorks() {

        final var file = new File("target/example.dat");
        final var sink = new FileReducer(file, false);

        new Shuar<>(source.apply(connection), fixedRecordMapper, sink)
                .mapReduce();

        assertEquals(expectedRecordCount, (int) file.length() / recordLength);

        final var expectedRecords = List.of(
                "SALES         BLAKE     2011-05-0114250",
                "SALES         MARTIN    2011-09-2806250",
                "SALES         TURNER    2011-09-0806000",
                "SALES         WARD      2011-02-2206250"
        );
        final var actualRecords = Utils.readLines(file);
        assertEquals(expectedRecords, actualRecords);
    }
}
