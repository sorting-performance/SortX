package com.example.sortx;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;

import java.io.*;
import java.util.*;

public class HelloController {

    @FXML
    private ComboBox<String> columnComboBox;

    @FXML
    private HBox previewHBox; // horizontal preview

    @FXML
    private TextArea sortResultArea;

    private List<List<String>> csvRows = new ArrayList<>();
    private String[] headers;

    @FXML
    private BarChart<String, Number> chart;

    @FXML
    private Label bestAlgoLabel;


    @FXML
    public void buttonClick() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select CSV File");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File selectedFile = fileChooser.showOpenDialog(null);

        if (selectedFile != null) {
            try {
                loadCSV(selectedFile);
                List<String> numericColumns = getNumericColumns();
                columnComboBox.getItems().clear();
                columnComboBox.getItems().addAll(numericColumns);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadCSV(File csvFile) throws IOException {
        csvRows.clear();
        BufferedReader reader = new BufferedReader(new FileReader(csvFile));

        String headerLine = reader.readLine();
        if (headerLine == null) return;

        headers = headerLine.split(",");

        String line;
        while ((line = reader.readLine()) != null) {
            csvRows.add(Arrays.asList(line.split(",")));
        }

        reader.close();
    }

    private List<String> getNumericColumns() {
        List<String> numericCols = new ArrayList<>();
        for (int i = 0; i < headers.length; i++) {
            boolean isNumeric = true;
            for (List<String> row : csvRows) {
                if (i >= row.size() || !row.get(i).matches("-?\\d+(\\.\\d+)?")) {
                    isNumeric = false;
                    break;
                }
            }
            if (isNumeric) numericCols.add(headers[i]);
        }
        return numericCols;
    }


    @FXML
    public void handleSortColumn() {
        String columnName = columnComboBox.getValue();
        if (columnName == null) return;

        int colIndex = Arrays.asList(headers).indexOf(columnName);
        if (colIndex < 0) return;

        double[] data = csvRows.stream()
                .mapToDouble(row -> Double.parseDouble(row.get(colIndex)))
                .toArray();

        // Map to store algorithm -> time
        Map<String, Double> algoTimes = new LinkedHashMap<>();
        Map<String, double[]> algoResults = new LinkedHashMap<>();

        // --- Run each sorting algorithm and measure time ---
        algoResults.put("Insertion", Arrays.copyOf(data, data.length));
        long start = System.nanoTime();
        insertionSort(algoResults.get("Insertion"));
        long end = System.nanoTime();
        algoTimes.put("Insertion", (end - start)/1_000_000.0);

        algoResults.put("Shell", Arrays.copyOf(data, data.length));
        start = System.nanoTime();
        shellSort(algoResults.get("Shell"));
        end = System.nanoTime();
        algoTimes.put("Shell", (end - start)/1_000_000.0);

        algoResults.put("Merge", Arrays.copyOf(data, data.length));
        start = System.nanoTime();
        mergeSort(algoResults.get("Merge"), 0, data.length-1);
        end = System.nanoTime();
        algoTimes.put("Merge", (end - start)/1_000_000.0);

        algoResults.put("Quick", Arrays.copyOf(data, data.length));
        start = System.nanoTime();
        quickSort(algoResults.get("Quick"), 0, data.length-1);
        end = System.nanoTime();
        algoTimes.put("Quick", (end - start)/1_000_000.0);

        algoResults.put("Heap", Arrays.copyOf(data, data.length));
        start = System.nanoTime();
        heapSort(algoResults.get("Heap"));
        end = System.nanoTime();
        algoTimes.put("Heap", (end - start)/1_000_000.0);

        // --- Display results in TextArea ---
        StringBuilder sb = new StringBuilder();
        for (String algo : algoTimes.keySet()) {
            sb.append(algo).append(" Sort:\n")
                    .append(Arrays.toString(algoResults.get(algo)))
                    .append("\nTime: ").append(algoTimes.get(algo)).append(" ms\n\n");
        }
        sortResultArea.setText(sb.toString());

        // --- Determine best algorithm ---
        String bestAlgo = Collections.min(algoTimes.entrySet(), Map.Entry.comparingByValue()).getKey();
        bestAlgoLabel.setText("Best Algorithm: " + bestAlgo + " Sort (" + algoTimes.get(bestAlgo) + " ms)");

        // --- Visualize with BarChart ---
        chart.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (String algo : algoTimes.keySet()) {
            series.getData().add(new XYChart.Data<>(algo, algoTimes.get(algo)));
        }
        chart.getData().add(series);
    }


    // -------- Sorting Algorithms --------

    private void insertionSort(double[] arr) {
        for (int i = 1; i < arr.length; i++) {
            double key = arr[i];
            int j = i - 1;
            while (j >= 0 && arr[j] > key) {
                arr[j+1] = arr[j];
                j--;
            }
            arr[j+1] = key;
        }
    }

    private void shellSort(double[] arr) {
        int n = arr.length;
        for (int gap = n/2; gap > 0; gap /= 2) {
            for (int i = gap; i < n; i++) {
                double temp = arr[i];
                int j;
                for (j = i; j >= gap && arr[j - gap] > temp; j -= gap) {
                    arr[j] = arr[j - gap];
                }
                arr[j] = temp;
            }
        }
    }

    private void mergeSort(double[] arr, int left, int right) {
        if (left < right) {
            int mid = (left + right)/2;
            mergeSort(arr, left, mid);
            mergeSort(arr, mid+1, right);
            merge(arr, left, mid, right);
        }
    }

    private void merge(double[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;
        double[] L = new double[n1];
        double[] R = new double[n2];
        for (int i=0; i<n1; i++) L[i] = arr[left+i];
        for (int j=0; j<n2; j++) R[j] = arr[mid+1+j];

        int i=0, j=0, k=left;
        while(i<n1 && j<n2) {
            if (L[i] <= R[j]) arr[k++] = L[i++];
            else arr[k++] = R[j++];
        }
        while(i<n1) arr[k++] = L[i++];
        while(j<n2) arr[k++] = R[j++];
    }

    private void quickSort(double[] arr, int low, int high) {
        if (low < high) {
            int pi = partition(arr, low, high);
            quickSort(arr, low, pi-1);
            quickSort(arr, pi+1, high);
        }
    }

    private int partition(double[] arr, int low, int high) {
        double pivot = arr[high];
        int i = low -1;
        for (int j = low; j < high; j++) {
            if (arr[j] <= pivot) {
                i++;
                double temp = arr[i];
                arr[i] = arr[j];
                arr[j] = temp;
            }
        }
        double temp = arr[i+1];
        arr[i+1] = arr[high];
        arr[high] = temp;
        return i+1;
    }

    private void heapSort(double[] arr) {
        int n = arr.length;
        for (int i=n/2-1; i>=0; i--) heapify(arr, n, i);
        for (int i=n-1; i>0; i--) {
            double temp = arr[0];
            arr[0] = arr[i];
            arr[i] = temp;
            heapify(arr, i, 0);
        }
    }

    private void heapify(double[] arr, int n, int i) {
        int largest = i;
        int l = 2*i+1;
        int r = 2*i+2;

        if (l<n && arr[l]>arr[largest]) largest = l;
        if (r<n && arr[r]>arr[largest]) largest = r;

        if (largest != i) {
            double swap = arr[i];
            arr[i] = arr[largest];
            arr[largest] = swap;
            heapify(arr, n, largest);
        }
    }
}
