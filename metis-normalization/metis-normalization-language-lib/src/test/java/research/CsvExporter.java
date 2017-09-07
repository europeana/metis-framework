package research;

import eu.europeana.normalization.language.nal.EuropeanLanguagesNal;
import eu.europeana.normalization.language.nal.NalLanguage;
import eu.europeana.normalization.util.MapOfLists;
import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.GZIPInputStream;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

public class CsvExporter implements Closeable {

  File exportFolder;
  CSVPrinter codeMatchPrinter;
  CSVPrinter labelMatchPrinter;
  CSVPrinter labelWordMatchPrinter;
  CSVPrinter labelWordAllMatchPrinter;
  CSVPrinter noMatchPrinter;

  //	Set<String> labelMatchCases=new HashSet<>();
//	Set<String> labelWordAllMatchCases=new HashSet<>();
  MapOfLists<String, String> labelMatchCases = new MapOfLists<>();
  MapOfLists<String, String> labelWordMatchCases = new MapOfLists<>();
  MapOfLists<String, String> labelWordAllMatchCases = new MapOfLists<>();
  MapOfLists<String, String> labelMatchCasesIds = new MapOfLists<>();
  MapOfLists<String, String> labelWordMatchCasesIds = new MapOfLists<>();
  MapOfLists<String, String> labelWordAllMatchCasesIds = new MapOfLists<>();
  Set<String> noMatchCases = new HashSet<>();
  MapOfLists<String, String> noMatchCasesIds = new MapOfLists<>();

  EuropeanLanguagesNal europaEuLanguagesNal;

  public CsvExporter(File exportFolder, EuropeanLanguagesNal europaEuLanguagesNal) {
    super();
    try {
      this.exportFolder = exportFolder;
      FileWriter out = new FileWriter(new File(exportFolder, "LangCodeMatch.csv"));
      out.write('\ufeff');
      codeMatchPrinter = new CSVPrinter(out, CSVFormat.EXCEL);
      out = new FileWriter(new File(exportFolder, "LangLabelMatch.csv"));
      out.write('\ufeff');
      labelMatchPrinter = new CSVPrinter(out, CSVFormat.EXCEL);
      out = new FileWriter(new File(exportFolder, "LangLabelWordMatch.csv"));
      out.write('\ufeff');
      labelWordMatchPrinter = new CSVPrinter(out, CSVFormat.EXCEL);
      out = new FileWriter(new File(exportFolder, "LangLabelWordAllMatch.csv"));
      out.write('\ufeff');
      labelWordAllMatchPrinter = new CSVPrinter(out, CSVFormat.EXCEL);
      out = new FileWriter(new File(exportFolder, "LangNoMatch.csv"));
      out.write('\ufeff');
      noMatchPrinter = new CSVPrinter(out, CSVFormat.EXCEL);
      this.europaEuLanguagesNal = europaEuLanguagesNal;
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public void exportCodeMatch(String label, String match) {
    try {
      codeMatchPrinter.printRecord(label, getMatchDescription(match));
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private String getMatchDescription(String match) {
    return match + "(" + europaEuLanguagesNal.lookupNormalizedLanguageId(match).getPrefLabel("eng")
        + ")";
  }

  public void exportLabelMatch(String label, List<String> normalizeds) {
    try {
      labelMatchPrinter.printRecord(createCsvRecord(label, normalizeds));
      labelMatchCases.putAll(label, normalizeds);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  private Iterable<String> createCsvRecord(String label, List<String> normalizeds) {
    ArrayList<String> ret = new ArrayList<>(normalizeds.size() + 1);
    ret.add(label);
    for (String normVal : normalizeds) {
      ret.add(getMatchDescription(normVal));
    }
    return ret;
  }

  public void exportLabelWordMatch(String label, List<String> normalizeds) {
    try {
      labelWordMatchPrinter.printRecord(createCsvRecord(label, normalizeds));
      labelWordMatchCases.putAll(label, normalizeds);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public void exportLabelWordAllMatch(String label, List<String> normalizeds) {
    try {
      labelWordAllMatchPrinter.printRecord(createCsvRecord(label, normalizeds));
      labelWordAllMatchCases.putAll(label, normalizeds);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }

  public void exportNoMatch(String label) {
    try {
      noMatchPrinter.printRecord(label);
      noMatchCases.add(label);
    } catch (IOException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
  }


  public void gatherCases(File dcLanguageFieldExportCsvFile) throws IOException {
    InputStream in = new FileInputStream(dcLanguageFieldExportCsvFile);
    if (dcLanguageFieldExportCsvFile.getName().toLowerCase().endsWith(".gz")) {
      in = new GZIPInputStream(in);
    }
    InputStreamReader reader = new InputStreamReader(in, "UTF-8");
    BufferedReader br = new BufferedReader(reader);
    CSVParser csvParser = new CSVParser(br, CSVFormat.EXCEL);
    Iterator<CSVRecord> iterator = csvParser.iterator();
    int cnt = 0;
    while (iterator.hasNext()) {
      cnt++;
      if (cnt % 100000 == 0) {
        System.out.println("processed " + cnt + " cases");
      }
      CSVRecord rec = iterator.next();
      String value = rec.get(2);
      String uri = rec.get(0);
      if (this.labelMatchCases.containsKey(value)) {
        List<String> listUris = labelMatchCasesIds.get(value);
        if (listUris == null || listUris.size() < 10) {
          labelMatchCasesIds.put(value, uri);
        }
      } else if (this.labelWordMatchCases.containsKey(value)) {
        List<String> listUris = labelWordMatchCasesIds.get(value);
        if (listUris == null || listUris.size() < 10) {
          labelWordMatchCasesIds.put(value, uri);
        }
      } else if (this.labelWordAllMatchCases.containsKey(value)) {
        List<String> listUris = labelWordAllMatchCasesIds.get(value);
        if (listUris == null || listUris.size() < 10) {
          labelWordAllMatchCasesIds.put(value, uri);
        }
      } else if (this.noMatchCases.contains(value)) {
        List<String> listUris = noMatchCasesIds.get(value);
        if (listUris == null || listUris.size() < 10) {
          noMatchCasesIds.put(value, uri);
        }
      }
    }
    csvParser.close();
//      expor final  result to a csv
  }

  public void exportEvaluationCsv(File evaluationCsvFolder) throws IOException {
    System.out.println("Exporting evaluation CSV to:\n - " + evaluationCsvFolder.getAbsolutePath());
    exportEvaluationCsvOfMethod(evaluationCsvFolder, "evaluation_match-label-all-words.csv",
        labelWordAllMatchCases, labelWordAllMatchCasesIds, 5);
    exportEvaluationCsvOfMethod(evaluationCsvFolder, "evaluation_match-label.csv", labelMatchCases,
        labelMatchCasesIds, 5);
    exportEvaluationCsvOfMethod(evaluationCsvFolder, "evaluation_match-label-word.csv",
        labelWordMatchCases, labelWordMatchCasesIds, 1);

    FileWriter out = new FileWriter(new File(evaluationCsvFolder, "evaluation_no_match.csv"));
    out.write('\ufeff');
    CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.EXCEL);
    for (String value : noMatchCasesIds.keySet()) {
      List<String> uris = noMatchCasesIds.get(value);
      if (uris.size() == 1) {
        continue;
      }
      csvPrinter.print(value);
      for (int i = 0; i < 7; i++) {
        csvPrinter.print(null);
      }
      for (String uri : uris) {
        csvPrinter.print(uri);
      }
      csvPrinter.println();
    }
    csvPrinter.close();
    out.close();

    out = new FileWriter(new File(evaluationCsvFolder, "evaluation_target-vocabulary.csv"));
    out.write('\ufeff');
    csvPrinter = new CSVPrinter(out, CSVFormat.EXCEL);
    for (NalLanguage value : europaEuLanguagesNal.getLanguages()) {
      String normalizedLanguageId = value
          .getNormalizedLanguageId(europaEuLanguagesNal.getTargetVocabulary());
      if (normalizedLanguageId != null) {
        csvPrinter.print(normalizedLanguageId);
        csvPrinter.print(getMatchDescription(normalizedLanguageId));
        csvPrinter.println();
      }
    }
    csvPrinter.close();
    out.close();
  }

  private void exportEvaluationCsvOfMethod(File evaluationCsvFolder, String filename,
      MapOfLists<String, String> casesMap,
      MapOfLists<String, String> idsMap, int numberOfAnnotators) throws IOException {
    FileWriter out = new FileWriter(new File(evaluationCsvFolder, filename));
    out.write('\ufeff');
    CSVPrinter csvPrinter = new CSVPrinter(out, CSVFormat.EXCEL);
    ArrayList<String> keys = new ArrayList<>(idsMap.keySet());
    Collections.sort(keys);
    int idx = 0;
    for (String value : keys) {
      idx++;
      if (numberOfAnnotators > 1 && idx % (keys.size() / numberOfAnnotators) == 0) {
        csvPrinter.printRecord("-----");
      }
      List<String> langIdsNormalized = casesMap.get(value);
      if (langIdsNormalized.size() > 3) {
        continue;
      }
      csvPrinter.print(value);
      for (String lang : langIdsNormalized) {
        csvPrinter.print(getMatchDescription(lang));
      }
      for (int i = langIdsNormalized.size(); i < 7; i++) {
        csvPrinter.print(null);
      }
      for (String uri : idsMap.get(value)) {
        csvPrinter.print(uri);
      }
      csvPrinter.println();
    }
    csvPrinter.close();
    out.close();

  }

  public void close() throws IOException {
    codeMatchPrinter.close();
    labelMatchPrinter.close();
    labelWordMatchPrinter.close();
    labelWordAllMatchPrinter.close();
    noMatchPrinter.close();
  }

  public MapOfLists<String, String> getLabelMatchCases() {
    return labelMatchCases;
  }

  public MapOfLists<String, String> getLabelWordAllMatchCases() {
    return labelWordAllMatchCases;
  }


}
