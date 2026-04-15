package com.entro.calculator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;

import java.util.List;

/**
 * Адаптер для матрицы входных данных
 */
public class MatrixAdapter extends ArrayAdapter<String> {
    private final int size;
    private final List<List<EditText>> matrixEntries;

    public MatrixAdapter(Context context, int size, List<List<EditText>> matrixEntries) {
        super(context, 0);
        this.size = size;
        this.matrixEntries = matrixEntries;
    }

    public int getSize() {
        return size;
    }

    public List<List<EditText>> getMatrixEntries() {
        return matrixEntries;
    }
}
