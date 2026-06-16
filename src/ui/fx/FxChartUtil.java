package ui.fx;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.util.Map;

public class FxChartUtil {

    private FxChartUtil() {
    }

    public static BarChart<String, Number> barChart(String title, Map<String, ? extends Number> values) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        BarChart<String, Number> chart = new BarChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.getStyleClass().add("business-chart");
        chart.setMinHeight(260);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<String, ? extends Number> entry : values.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().clear();
        chart.getData().add(series);
        return chart;
    }

    public static LineChart<String, Number> connectedPlot(String title, Map<String, ? extends Number> values) {
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();

        LineChart<String, Number> chart = new LineChart<>(xAxis, yAxis);
        chart.setTitle(title);
        chart.setLegendVisible(false);
        chart.setAnimated(false);
        chart.setCreateSymbols(true);
        chart.getStyleClass().add("business-chart");
        chart.getStyleClass().add("connected-plot");
        chart.setMinHeight(260);

        XYChart.Series<String, Number> series = new XYChart.Series<>();

        for (Map.Entry<String, ? extends Number> entry : values.entrySet()) {
            series.getData().add(new XYChart.Data<>(entry.getKey(), entry.getValue()));
        }

        chart.getData().clear();
        chart.getData().add(series);
        return chart;
    }
}
