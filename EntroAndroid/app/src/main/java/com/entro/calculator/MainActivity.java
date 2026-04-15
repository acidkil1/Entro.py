package com.entro.calculator;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Главное приложение Entro для Android
 * Порт функционала из entro_gui.py
 */
public class MainActivity extends AppCompatActivity {

    private Spinner spinnerChannelType;
    private Spinner spinnerSize;
    private GridLayout gridMatrix;
    private LinearLayout linearVector;
    private TextView textResults;
    
    private List<List<EditText>> matrixEntries = new ArrayList<>();
    private List<EditText> vectorEntries = new ArrayList<>();
    
    private int currentSize = 3;
    private String channelType = "b|a";
    
    private static final String[] CHANNEL_TYPES = {"b|a", "a|b", "a,b"};
    private static final String[] SIZES = {"3x3", "4x4", "5x5"};

    private final ActivityResultLauncher<String> importLauncher = registerForActivityResult(
        new ActivityResultContracts.GetContent(),
        uri -> {
            if (uri != null) {
                importFromExcel(uri);
            }
        }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerChannelType = findViewById(R.id.spinner_channel_type);
        spinnerSize = findViewById(R.id.spinner_size);
        gridMatrix = findViewById(R.id.grid_matrix);
        linearVector = findViewById(R.id.linear_vector);
        textResults = findViewById(R.id.text_results);

        // Настройка адаптеров для спиннеров
        ArrayAdapter<String> channelAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, CHANNEL_TYPES);
        channelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerChannelType.setAdapter(channelAdapter);

        ArrayAdapter<String> sizeAdapter = new ArrayAdapter<>(this,
            android.R.layout.simple_spinner_item, SIZES);
        sizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSize.setAdapter(sizeAdapter);

        // Обработчики изменений
        spinnerChannelType.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                channelType = CHANNEL_TYPES[position];
                createInputs(currentSize, channelType);
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        spinnerSize.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                String sizeStr = SIZES[position].split("x")[0];
                int newSize = Integer.parseInt(sizeStr);
                if (newSize != currentSize) {
                    currentSize = newSize;
                    createInputs(newSize, channelType);
                }
            }
            @Override
            public void onNothingSelected(android.widget.AdapterView<?> parent) {}
        });

        // Кнопки действий
        Button btnImport = findViewById(R.id.btn_import);
        Button btnRandom = findViewById(R.id.btn_random);
        Button btnClear = findViewById(R.id.btn_clear);
        Button btnCalc = findViewById(R.id.btn_calc);

        btnImport.setOnClickListener(v -> importLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        btnRandom.setOnClickListener(v -> fillRandom());
        btnClear.setOnClickListener(v -> clearAll());
        btnCalc.setOnClickListener(v -> calculate());

        // Инициализация полей ввода
        createInputs(currentSize, channelType);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_import) {
            importLauncher.launch("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Создание полей ввода для матрицы и вектора
     */
    private void createInputs(int size, String channelType) {
        gridMatrix.removeAllViews();
        linearVector.removeAllViews();
        matrixEntries.clear();
        vectorEntries.clear();

        gridMatrix.setColumnCount(size);
        gridMatrix.setRowCount(size);

        Random random = new Random();

        // Создание матрицы
        for (int i = 0; i < size; i++) {
            List<EditText> rowEntries = new ArrayList<>();
            for (int j = 0; j < size; j++) {
                EditText entry = new EditText(this);
                entry.setWidth(120);
                entry.setHint("0.00");
                double val = 0.1 + random.nextDouble() * 0.9;
                entry.setText(String.format("%.2f", val));
                entry.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                                   android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                
                gridMatrix.addView(entry);
                rowEntries.add(entry);
            }
            matrixEntries.add(rowEntries);
        }

        // Создание вектора (если нужен)
        if (!channelType.equals("a,b")) {
            TextView labelVector = new TextView(this);
            String vectorLabelText = channelType.equals("b|a") ? 
                "Вероятности P(Ai)" : "Вероятности P(Bj)";
            labelVector.setText(vectorLabelText);
            labelVector.setTextSize(16);
            labelVector.setPadding(0, 20, 0, 10);
            linearVector.addView(labelVector);

            LinearLayout vectorLayout = new LinearLayout(this);
            vectorLayout.setOrientation(LinearLayout.HORIZONTAL);
            
            for (int i = 0; i < size; i++) {
                EditText entry = new EditText(this);
                entry.setWidth(120);
                entry.setHint("0.00");
                double val = 0.1 + random.nextDouble() * 0.9;
                entry.setText(String.format("%.2f", val));
                entry.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | 
                                   android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
                
                vectorLayout.addView(entry);
                vectorEntries.add(entry);
            }
            linearVector.addView(vectorLayout);
        }
    }

    /**
     * Импорт данных из Excel файла
     */
    private void importFromExcel(Uri uri) {
        try (InputStream inputStream = getContentResolver().openInputStream(uri);
             Workbook workbook = new XSSFWorkbook(inputStream)) {
            
            Sheet matrixSheet = workbook.getSheet("Matrix");
            if (matrixSheet == null) {
                matrixSheet = workbook.getSheetAt(0);
            }
            
            List<double[]> dataMatrix = new ArrayList<>();
            for (Row row : matrixSheet) {
                List<Double> rowData = new ArrayList<>();
                for (Cell cell : row) {
                    if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                        rowData.add(cell.getNumericCellValue());
                    }
                }
                if (!rowData.isEmpty()) {
                    dataMatrix.add(rowData.stream().mapToDouble(Double::doubleValue).toArray());
                }
            }
            
            if (dataMatrix.isEmpty()) {
                Toast.makeText(this, "Матрица пуста!", Toast.LENGTH_SHORT).show();
                return;
            }
            
            int n = dataMatrix.size();
            if (n != dataMatrix.get(0).length) {
                Toast.makeText(this, "Матрица должна быть квадратной!", Toast.LENGTH_SHORT).show();
                return;
            }
            if (n < 3 || n > 5) {
                Toast.makeText(this, "Поддерживаются размеры 3x3, 4x4, 5x5", Toast.LENGTH_SHORT).show();
                return;
            }
            
            // Обновление размера
            currentSize = n;
            spinnerSize.setSelection(n - 3);
            createInputs(n, channelType);
            
            // Заполнение матрицы
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    matrixEntries.get(i).get(j).setText(String.valueOf(dataMatrix.get(i)[j]));
                }
            }
            
            // Импорт вектора если есть
            if (!channelType.equals("a,b") && workbook.getNumberOfSheets() > 1) {
                Sheet vectorSheet = workbook.getSheet("Vector");
                if (vectorSheet == null && workbook.getNumberOfSheets() > 1) {
                    vectorSheet = workbook.getSheetAt(1);
                }
                
                if (vectorSheet != null) {
                    List<Double> vectorData = new ArrayList<>();
                    for (Row row : vectorSheet) {
                        for (Cell cell : row) {
                            if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
                                vectorData.add(cell.getNumericCellValue());
                            }
                        }
                    }
                    
                    if (vectorData.size() >= n) {
                        for (int i = 0; i < n; i++) {
                            vectorEntries.get(i).setText(String.valueOf(vectorData.get(i)));
                        }
                    }
                }
            }
            
            Toast.makeText(this, "Данные импортированы! Размер: " + n + "x" + n, Toast.LENGTH_SHORT).show();
            
        } catch (IOException e) {
            Toast.makeText(this, "Ошибка импорта: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Заполнение случайными значениями
     */
    private void fillRandom() {
        Random random = new Random();
        
        for (List<EditText> row : matrixEntries) {
            for (EditText entry : row) {
                double val = 0.1 + random.nextDouble() * 0.9;
                entry.setText(String.format("%.2f", val));
            }
        }
        
        if (!channelType.equals("a,b")) {
            for (EditText entry : vectorEntries) {
                double val = 0.1 + random.nextDouble() * 0.9;
                entry.setText(String.format("%.2f", val));
            }
        }
    }

    /**
     * Очистка всех полей
     */
    private void clearAll() {
        for (List<EditText> row : matrixEntries) {
            for (EditText entry : row) {
                entry.setText("");
            }
        }
        if (!vectorEntries.isEmpty()) {
            for (EditText entry : vectorEntries) {
                entry.setText("");
            }
        }
        textResults.setText("");
    }

    /**
     * Получение значений из полей ввода
     */
    private double[][] getMatrixValues() {
        double[][] matrix = new double[currentSize][currentSize];
        try {
            for (int i = 0; i < currentSize; i++) {
                for (int j = 0; j < currentSize; j++) {
                    String val = matrixEntries.get(i).get(j).getText().toString();
                    matrix[i][j] = val.isEmpty() ? 0 : Double.parseDouble(val);
                }
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректные числа!", Toast.LENGTH_SHORT).show();
            return null;
        }
        return matrix;
    }

    private double[] getVectorValues() {
        double[] vector = new double[currentSize];
        try {
            for (int i = 0; i < currentSize; i++) {
                String val = vectorEntries.get(i).getText().toString();
                vector[i] = val.isEmpty() ? 0 : Double.parseDouble(val);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Введите корректные числа!", Toast.LENGTH_SHORT).show();
            return null;
        }
        return vector;
    }

    /**
     * Основной метод расчета энтропии
     */
    private void calculate() {
        double[][] rawMatrix = getMatrixValues();
        if (rawMatrix == null) return;

        double[][] matrix;
        double[] pa = null, pb = null;
        double[][] jointMatrix;

        if (channelType.equals("b|a")) {
            double[] rawVector = getVectorValues();
            if (rawVector == null) return;
            pa = EntropyCalculator.normalizeVector(rawVector);
            matrix = EntropyCalculator.normalizeMatrixRows(rawMatrix);
            
            // Построение совместной матрицы P(A,B) = P(A) * P(B|A)
            jointMatrix = new double[currentSize][currentSize];
            for (int i = 0; i < currentSize; i++) {
                for (int j = 0; j < currentSize; j++) {
                    jointMatrix[i][j] = pa[i] * matrix[i][j];
                }
            }
        } else if (channelType.equals("a|b")) {
            double[] rawVector = getVectorValues();
            if (rawVector == null) return;
            pb = EntropyCalculator.normalizeVector(rawVector);
            matrix = EntropyCalculator.normalizeMatrixCols(rawMatrix);
            
            // Построение совместной матрицы P(A,B) = P(A|B) * P(B)
            jointMatrix = new double[currentSize][currentSize];
            for (int i = 0; i < currentSize; i++) {
                for (int j = 0; j < currentSize; j++) {
                    jointMatrix[i][j] = matrix[i][j] * pb[j];
                }
            }
        } else { // a,b
            // Нормализация совместной матрицы
            double totalSum = 0;
            for (double[] row : rawMatrix) {
                for (double val : row) {
                    totalSum += val;
                }
            }
            if (totalSum == 0) {
                Toast.makeText(this, "Сумма всех элементов матрицы не может быть 0", Toast.LENGTH_SHORT).show();
                return;
            }
            jointMatrix = new double[currentSize][currentSize];
            for (int i = 0; i < currentSize; i++) {
                for (int j = 0; j < currentSize; j++) {
                    jointMatrix[i][j] = rawMatrix[i][j] / totalSum;
                }
            }
            matrix = rawMatrix;
        }

        EntropyResult result = EntropyCalculator.calculateFromJoint(jointMatrix);
        if (result == null) return;

        double hConditionalUniform = EntropyCalculator.calculateUniformConditionalEntropy(matrix, channelType);
        double hMax = Math.log(currentSize) / Math.log(2);

        StringBuilder sb = new StringBuilder();
        
        if (channelType.equals("b|a")) {
            sb.append(String.format("H(A)max: %.3f\n", hMax));
            sb.append(String.format("H(A): %.3f\n", result.hA));
            sb.append(String.format("H(B): %.3f\n", result.hB));
            for (int i = 0; i < result.hAiList.length; i++) {
                sb.append(String.format("H(a%d): %.3f\n", i + 1, result.hAiList[i]));
            }
            sb.append(String.format("H(B/A) равн.: %.3f\n", hConditionalUniform));
            sb.append(String.format("H(B/A) нерав.: %.3f", result.hBGivenA));
        } else if (channelType.equals("a|b")) {
            sb.append(String.format("H(B)max: %.3f\n", hMax));
            sb.append(String.format("H(B): %.3f\n", result.hB));
            sb.append(String.format("H(A): %.3f\n", result.hA));
            for (int i = 0; i < result.hBjList.length; i++) {
                sb.append(String.format("H(b%d): %.3f\n", i + 1, result.hBjList[i]));
            }
            sb.append(String.format("H(A/B) равн.: %.3f\n", hConditionalUniform));
            sb.append(String.format("H(A/B) нерав.: %.3f", result.hAGivenB));
        } else { // a,b
            sb.append(String.format("H(B)max: %.3f\n", hMax));
            sb.append(String.format("H(B): %.3f\n", result.hB));
            sb.append(String.format("H(A): %.3f\n", result.hA));
            sb.append(String.format("H(A,B): %.3f\n", result.hAB));
            for (int i = 0; i < result.hAiList.length; i++) {
                sb.append(String.format("H(a%d): %.3f\n", i + 1, result.hAiList[i]));
            }
            for (int i = 0; i < result.hBjList.length; i++) {
                sb.append(String.format("H(b%d): %.3f\n", i + 1, result.hBjList[i]));
            }
            sb.append(String.format("H(B/A) нерав.: %.3f\n", result.hBGivenA));
            sb.append(String.format("H(A/B) нерав.: %.3f", result.hAGivenB));
        }

        textResults.setText(sb.toString());
    }
}
