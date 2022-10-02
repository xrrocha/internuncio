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

public class DelimitedRecordBuilderIT extends ScottTigerIT
        implements SqlSourceBuilder, DelimitedRecordBuilder<ResultSet> {

    private final Mapper<ResultSet, String> delimitedRecordMapper;

    {
        delimiter("\t");
        delimiterEscape(s -> s.replace("\t", "\\t"));
        field("department", rsString("dname"));
        field("employee", rsString("ename"));
        dateField("hireDate", rsDate("hiredate"), "yyyy-MM-dd");
        numberField("salary", rsDouble("sal"), "######.##");

        delimitedRecordMapper = buildMapper();
    }

    @Test
    public void delimitedRecordExtractorWorks() {

        final var file = new File("target/example.tsv");
        final var sink = new FileReducer(file);

        new Shuar<>(source.apply(connection), delimitedRecordMapper, sink)
                .mapReduce();

        final var expectedRecords = List.of(
                "SALES\tBLAKE\t2011-05-01\t14250",
                "SALES\tMARTIN\t2011-09-28\t6250",
                "SALES\tTURNER\t2011-09-08\t6000",
                "SALES\tWARD\t2011-02-22\t6250"
        );
        final var actualRecords = Utils.readLines(file);
        assertEquals(expectedRecords, actualRecords);
    }
}
