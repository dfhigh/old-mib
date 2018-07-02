package person.dufei.utils.main;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.page.PageReadStore;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.convert.GroupRecordConverter;
import org.apache.parquet.format.converter.ParquetMetadataConverter;
import org.apache.parquet.hadoop.ParquetFileReader;
import org.apache.parquet.hadoop.metadata.ParquetMetadata;
import org.apache.parquet.io.ColumnIOFactory;
import org.apache.parquet.io.MessageColumnIO;
import org.apache.parquet.io.RecordReader;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.Type;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com._4paradigm.prophet.rest.utils.Validator.validateStringNotBlank;

@Slf4j
public class ParquetParseMain {

    public static void main(String[] args) throws Exception {
        helpIntercept();
        String filePath = System.getProperty("filePath");
        validateStringNotBlank(filePath, "file path");
        String delimiter = Optional.ofNullable(System.getProperty("delimiter")).orElse("\t");
        String output = Optional.ofNullable(System.getProperty("outputPath")).orElse("/tmp/ptext");

        File file = new File(filePath);
        if (!file.exists()) throw new IllegalArgumentException(filePath + " does not exist");
        List<Path> files = Lists.newArrayList();
        if (file.isDirectory()) {
            File[] pFiles = file.listFiles(f -> f.getName().endsWith(".parquet"));
            if (pFiles != null) Arrays.stream(pFiles).map(f -> new Path(f.getAbsolutePath())).forEach(files::add);
        } else if (file.isFile()) {
            files.add(new Path(filePath));
        }

        if (files.isEmpty()) throw new IllegalArgumentException("no parquet files found");

        Configuration conf = new Configuration();
        ColumnIOFactory factory = new ColumnIOFactory();
        long rowCount = 0;
        try (BufferedWriter bw = new BufferedWriter(new FileWriter(output))) {
            for (Path path : files) {
                log.info("parsing file {}...", path);
                ParquetMetadata footer = ParquetFileReader.readFooter(conf, path, ParquetMetadataConverter.NO_FILTER);
                MessageType schema = footer.getFileMetaData().getSchema();
                try (ParquetFileReader pfr = new ParquetFileReader(conf, path, footer)) {
                    PageReadStore prs;
                    while ((prs = pfr.readNextRowGroup()) != null) {
                        long rows = prs.getRowCount();
                        rowCount += rows;
                        MessageColumnIO columnIO = factory.getColumnIO(schema);
                        RecordReader rr = columnIO.getRecordReader(prs, new GroupRecordConverter(schema));
                        for (long i = 0; i < rows; i++) {
                            Group group = (Group) rr.read();
                            int fieldCount = group.getType().getFieldCount();
                            for (int j = 0; j < fieldCount; j++) {
                                int valueCount = group.getFieldRepetitionCount(j);
                                Type fieldType = group.getType().getType(j);
                                for (int k = 0; k < valueCount; k++) {
                                    if (fieldType.isPrimitive()) {
                                        bw.write(group.getValueToString(j, k));
                                        if (k < valueCount-1) bw.write(",");
                                    }
                                }
                                if (j < fieldCount-1) bw.write(delimiter);
                            }
                            bw.newLine();
                        }
                    }
                }
            }
        }
        log.info("finished parsing of {} lines", rowCount);
    }

    private static void helpIntercept() {
        boolean isHelp = Boolean.parseBoolean(System.getProperty("help", "false"));
        if (!isHelp) return;
        log.info("this function is used to convert parquet files to text, we support below configurations");
        log.info("");
        log.info("\t-h, print this message and exit");
        log.info("\t-f {file}, mandatory and must use absolute path of the parquet file or directory that contains parquet files (file name must end with \".parquet\" and exist directly under the dir)");
        log.info("\t-d {delimiter}, optional, column delimiter of the output text file, default value is '\\t'");
        log.info("\t-o {output}, optional, absolute output file path of parsed content, default value is /tmp/ptext");

        System.exit(0);
    }
}
