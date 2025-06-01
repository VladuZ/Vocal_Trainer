package com.example.testing;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ViewGrid extends View {

    private Paint borderPaint;
    private Paint cellPaint;
    private Paint selectedCellPaint;
    private List<Cell> selectedCells;
    private float scrollX = 0;
    private boolean isScrolling = false;
    private float initialTouchX = 0;
    private int numRows = 60;
    private int numColumns = 16;
    private int numColumnsOnScreen = 16;
    private static final int EXTRA_COLUMNS = 10; // Додаткові колонки

    public ViewGrid(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(1);

        cellPaint = new Paint();
        cellPaint.setColor(Color.LTGRAY);
        cellPaint.setStyle(Paint.Style.FILL);

        selectedCellPaint = new Paint();
        selectedCellPaint.setColor(Color.YELLOW); // Колір для підсвічування
        selectedCellPaint.setStyle(Paint.Style.FILL);

        selectedCells = new ArrayList<>();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGrid(canvas);
    }

    private void drawGrid(Canvas canvas) {
        float cellWidth = getWidth() / (float) numColumnsOnScreen;
        float cellHeight = getHeight() / (float) numRows;

        for (int row = 0; row < numRows; row++) {
            for (int col = 0; col < numColumns; col++) {
                float left = col * cellWidth - scrollX;
                float top = row * cellHeight;
                float right = left + cellWidth;
                float bottom = top + cellHeight;

                if (isCellSelected(row, col)) {
                    canvas.drawRect(left, top, right, bottom, selectedCellPaint);
                } else {
                    canvas.drawRect(left, top, right, bottom, cellPaint);
                }

                canvas.drawRect(left, top, right, bottom, borderPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                initialTouchX = event.getX();
                isScrolling = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - initialTouchX) > 20) { // Порогове значення для скролінгу
                    isScrolling = true;
                }
                if (isScrolling) {
                    float newScrollX = scrollX - (event.getX() - initialTouchX);
                    if (newScrollX < 0) {
                        newScrollX = 0;
                    } else if (newScrollX > (numColumns - numColumnsOnScreen) * getWidth() / (float) numColumnsOnScreen) {
                        newScrollX = (numColumns - numColumnsOnScreen) * getWidth() / (float) numColumnsOnScreen;
                    }
                    scrollX = newScrollX;
                    initialTouchX = event.getX();
                    invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isScrolling) {
                    selectCell(event.getX(), event.getY());
                    ensureExtraColumns();
                }
                isScrolling = false;
                break;
        }
        return true;
    }

    private void selectCell(float x, float y) {
        boolean flag = true;
        float cellWidth = getWidth() / (float) numColumnsOnScreen;
        float cellHeight = getHeight() / (float) numRows;

        int col = (int) ((x + scrollX) / cellWidth);
        int row = (int) (y / cellHeight);

        Cell cell = new Cell(row, col);
        if (selectedCells.contains(cell)) {
            selectedCells.remove(cell); // Якщо нота вже обрана, видаляємо її
        } else {
            for (Cell c: selectedCells) {
                if (c.col == cell.col) {
                    flag = false;
                    break;
                }
            }
            if (flag){
                selectedCells.add(cell); // Додаємо нову ноту
            }
        }

        invalidate(); // Перемалювати для оновлення вигляду
    }

    private boolean isCellSelected(int row, int col) {
        return selectedCells.contains(new Cell(row, col));
    }

    public void ensureExtraColumns() {
        if (!selectedCells.isEmpty()) {
            int maxSelectedColumn = 0;
            for (Cell cell : selectedCells) {
                if (cell.col > maxSelectedColumn) {
                    maxSelectedColumn = cell.col;
                }
            }
            if (maxSelectedColumn > numColumns - EXTRA_COLUMNS) {
                numColumns = maxSelectedColumn + EXTRA_COLUMNS;
                invalidate();
            }
        }
    }

    public static class Cell implements Serializable {
        int row;
        int col;

        Cell(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Cell cell = (Cell) o;

            if (row != cell.row) return false;
            return col == cell.col;
        }

        @Override
        public int hashCode() {
            int result = row;
            result = 31 * result + col;
            return result;
        }
    }

    public List<Cell> getSelectedCells() {
        return selectedCells;
    }

    public void setSelectedCells(List<Cell> cells) {
        selectedCells.clear();
        selectedCells.addAll(cells);
        invalidate(); // Refresh the view to reflect the changes
    }

}
