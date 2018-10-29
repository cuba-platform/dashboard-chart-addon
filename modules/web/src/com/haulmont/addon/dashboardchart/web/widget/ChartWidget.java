package com.haulmont.addon.dashboardchart.web.widget;

import com.haulmont.addon.dashboard.gui.components.DashboardFrame;
import com.haulmont.addon.dashboard.model.Dashboard;
import com.haulmont.addon.dashboard.model.Parameter;
import com.haulmont.addon.dashboard.model.Widget;
import com.haulmont.addon.dashboard.web.annotation.DashboardWidget;
import com.haulmont.addon.dashboard.web.annotation.WidgetParam;
import com.haulmont.addon.dashboard.web.events.DashboardEvent;
import com.haulmont.addon.dashboard.web.parametertransformer.ParameterTransformer;
import com.haulmont.addon.dashboard.web.widget.RefreshableWidget;
import com.haulmont.charts.gui.amcharts.model.charts.AbstractChart;
import com.haulmont.charts.gui.components.charts.CustomChart;
import com.haulmont.charts.gui.components.charts.PieChart;
import com.haulmont.charts.gui.data.MapDataItem;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractFrame;
import com.haulmont.cuba.gui.components.BoxLayout;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.haulmont.addon.dashboardchart.web.widget.ChartWidget.CAPTION;


@DashboardWidget(name = CAPTION, editFrameId = "dashboardchart$ChartWidget.edit")
public class ChartWidget extends AbstractFrame implements RefreshableWidget {

    public static final String CAPTION = "Chart";

    private final String CHART_JSON_PARAMETER = "chartJson";
    private final String JSON_CHART_SCREEN_ID = "chart$jsonChart";

    @Inject
    protected DataManager dataManager;

    @Inject
    protected ReportGuiManager reportGuiManager;

    @Inject
    private ParameterTransformer parameterTransformer;

    @WindowParam
    protected Widget widget;

    @WindowParam
    protected Dashboard dashboard;

    @WindowParam
    protected DashboardFrame dashboardFrame;

    @WidgetParam
    @WindowParam
    protected UUID reportId;

    @WidgetParam
    @WindowParam
    protected Boolean refreshAutomatically = false;

    @WidgetParam
    @WindowParam
    protected UUID templateId;

    @Named("errorLabel")
    protected Label errorLabel;

    @Inject
    protected CustomChart reportJsonChart;
    private Report report;
    private ReportTemplate reportTemplate;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        LoadContext<Report> loadContext = LoadContext.create(Report.class)
                .setId(reportId)
                .setView("report.edit");

        report = dataManager.load(loadContext);
        reportTemplate = null;
        if (report != null) {
            List<ReportTemplate> chartTemplates = report.getTemplates().stream()
                    .filter(rt -> ReportOutputType.CHART == rt.getReportOutputType())
                    .collect(Collectors.toList());
            if (templateId != null) {
                reportTemplate = chartTemplates.stream()
                        .filter(t -> templateId.equals(t.getId()))
                        .findFirst()
                        .orElse(ReportOutputType.CHART == report.getDefaultTemplate().getReportOutputType() ? report.getDefaultTemplate() : null);
            }

        }
        updateChart();
    }

    private void updateChart() {
        if (report == null || reportTemplate == null) {
            errorLabel.setVisible(true);
            reportJsonChart.setVisible(false);
            return;
        }

        Map<String, Object> widgetParams = new HashMap<>();
        for (Parameter p : widget.getParameters()) {
            Object rawValue = parameterTransformer.transform(p.getParameterValue());
            widgetParams.put(p.getName(), rawValue);
        }
        ReportOutputDocument document = reportGuiManager.getReportResult(report, widgetParams, reportTemplate.getCode());

        if (document.getContent() != null) {
            reportJsonChart.setVisible(true);
            reportJsonChart.setSizeFull();
            reportJsonChart.setConfiguration(new BasicChart());
            reportJsonChart.setNativeJson(new String(document.getContent(), StandardCharsets.UTF_8));
        } else {
            errorLabel.setVisible(true);
            reportJsonChart.setVisible(false);
        }
    }

    @Override
    public void refresh(DashboardEvent dashboardEvent) {
        if (refreshAutomatically) {
            updateChart();
        }
    }

    /**
     * Used for default initialization in
     * WebChart.CubaAmchartsSceneExt#setupDefaults(AbstractChart)
     */
    protected static class BasicChart extends AbstractChart<BasicChart> {
    }


}