package guru.qa;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import guru.qa.domain.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;

public class FileParseTest {

    ClassLoader classLoader = FileParseTest.class.getClassLoader();
    private InputStream inputStream;
    private ZipInputStream zipInputStream;
    private ZipFile zipFile;
    private ZipEntry entry;

    @BeforeEach
    void init() throws IOException {
        inputStream = classLoader.getResourceAsStream("archive.zip");
        zipInputStream = new ZipInputStream(inputStream);
        zipFile = new ZipFile(new File("src/test/resources/archive.zip"));
    }

    @Test
    void csvTest() throws IOException, CsvException {
        var fileName = "addresses.csv";
        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (entry.getName().equals(fileName)) {
                try (InputStream inputStream = zipFile.getInputStream(entry);
                     CSVReader csvReader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                    List<String[]> csv = csvReader.readAll();
                    assertThat(csv).contains(
                            new String[]{"John", "Doe", "Riverside", "NJ", "8075"},
                            new String[]{"Jack", "McGinnis", "Phila", "PA", "9119"}
                    );
                }
            }

        }
    }

    @Test
    void pdfTest() throws IOException {
        var fileName = "sample.pdf";
        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (entry.getName().equals(fileName)) {
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    PDF pdf = new PDF(inputStream);
                    assertThat(pdf.text).contains("A Simple PDF File");
                    assertThat(pdf.text).contains("Simple PDF File 2");
                }
            }
        }
    }

    @Test
    void xlsxTest() throws IOException {
        var fileName = "file_example_XLSX_10.xlsx";
        while ((entry = zipInputStream.getNextEntry()) != null) {
            if (entry.getName().equals(fileName)) {
                try (InputStream inputStream = zipFile.getInputStream(entry)) {
                    XLS xls = new XLS(inputStream);
                    assertThat(xls.excel
                            .getSheetAt(0).getRow(2).getCell(2)
                            .getStringCellValue()).contains("Hashimoto");
                }
            }
        }
    }

    @Test
    void jsonTest() throws IOException {
        var fileName = "pokemon.json";
        try (InputStream inputStream = classLoader.getResourceAsStream(fileName)) {
            ObjectMapper objectMapper = new ObjectMapper();
            Pokemon pokemon = objectMapper.readValue(inputStream, Pokemon.class);
            assertThat(pokemon.getName()).isEqualTo("ditto");
            assertThat(pokemon.getOrder()).isEqualTo(214);
            assertThat(pokemon.getHeight()).isEqualTo(3);
            assertThat(pokemon.getWeight()).isEqualTo(40);
            assertThat(pokemon.getId()).isEqualTo(132);
            assertThat(pokemon.getBaseExperience()).isEqualTo(101);
            assertThat(pokemon.getAbility().getName()).isEqualTo("limber");
            assertThat(pokemon.getAbility().getSlot()).isEqualTo(1);
            assertThat(pokemon.getAbility().isHidden()).isEqualTo(false);
        }
    }
}
