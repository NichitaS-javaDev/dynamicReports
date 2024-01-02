package org.example;

import net.sf.dynamicreports.report.builder.chart.BarChartBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabColumnGroupBuilder;
import net.sf.dynamicreports.report.builder.crosstab.CrosstabRowGroupBuilder;
import net.sf.dynamicreports.report.builder.style.StyleBuilder;
import net.sf.dynamicreports.report.constant.Calculation;
import net.sf.dynamicreports.report.constant.HorizontalTextAlignment;
import net.sf.dynamicreports.report.constant.VerticalTextAlignment;
import net.sf.dynamicreports.report.exception.DRException;
import org.example.util.QueryUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Properties;

import static net.sf.dynamicreports.report.builder.DynamicReports.*;

public class Report {
    {
        initDBConnection();
    }

    private Connection connection;

    public void build() throws SQLException {
        try {
            String query = QueryUtil.getQueryFromXML("select-all-holidays.xml");

            StyleBuilder titleStyle = stl.style()
                    .setPadding(20)
                    .setTextAlignment(HorizontalTextAlignment.CENTER, VerticalTextAlignment.MIDDLE);
            StyleBuilder columnHeaderStyle = stl.style()
                    .setPadding(stl.padding().setBottom(10));
            StyleBuilder columnStyle = stl.style()
                    .setPadding(stl.padding().setBottom(5));
            StyleBuilder summaryStyle = stl.style()
                    .setPadding(stl.padding().setTop(25));

            report()
                    .setDataSource(query, connection)
                    .setColumnHeaderStyle(columnHeaderStyle)
                    .setColumnStyle(columnStyle)
                    .columns(
                            col.column("Country", "country", type.stringType()),
                            col.column("Date", "date", type.stringType()),
                            col.column("Name", "name", type.stringType()).setWidth(300)
                    )
                    .title(cmp.text("Holidays").setStyle(titleStyle))
                    .columnFooter(cmp.line())
                    .pageFooter(cmp.pageXofY())
                    .summary(generateCrossTab())
                    .setSummaryStyle(summaryStyle)
                    .summary(generateChart().setStyle(stl.style().setPadding(stl.padding().setTop(30))))
                    .show();
        } catch (DRException | IOException e) {
            e.printStackTrace();
        } finally {
            if (Objects.nonNull(connection)){
                connection.close();
            }
        }
    }

    private void initDBConnection() {
        try (var propertiesFile = Report.class.getClassLoader().getResourceAsStream("config.properties")) {
            Properties properties = new Properties();
            properties.load(propertiesFile);
            connection = DriverManager.getConnection(properties.getProperty("db.url"));
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    private CrosstabBuilder generateCrossTab() throws IOException {
        String crossTabQuery = QueryUtil.getQueryFromXML("select-holidays-crossTab.xml");

        StyleBuilder mainStyle = stl.style()
                .setHorizontalTextAlignment(HorizontalTextAlignment.CENTER);

        CrosstabRowGroupBuilder<String> rowGroup = ctab.rowGroup("country", String.class)
                .setHeaderStyle(mainStyle);
        CrosstabColumnGroupBuilder<Integer> columnGroup = ctab.columnGroup("month", Integer.class)
                .setHeaderStyle(mainStyle);

        return ctab.crosstab()
                .rowGroups(rowGroup)
                .columnGroups(columnGroup)
                .measures(
                        ctab.measure("month", Integer.class, Calculation.COUNT))
                .setDataSource(crossTabQuery, connection)
                .setCellWidth(45)
                .setCellStyle(mainStyle)
                .setGrandTotalStyle(stl.style(mainStyle).bold());
    }

    private BarChartBuilder generateChart() throws IOException {
        String chartQuery = QueryUtil.getQueryFromXML("select-holidays-for-chart.xml");

        return cht.barChart()
                .setCategory("month", String.class)
                .series(
                        cht.serie(col.column("Italia", "first_count", type.integerType())),
                        cht.serie(col.column("Moldavia", "second_count", type.integerType()))
                )
                .setDataSource(chartQuery, connection)
                .setStyle(stl.style().setPadding(stl.padding().setTop(10)));
    }

}
