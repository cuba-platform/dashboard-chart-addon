/*
 * Copyright (c) 2008-2019 Haulmont.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.haulmont.addon.dashboardchart.web.widget;

import com.haulmont.addon.dashboard.gui.components.DashboardFrame;
import com.haulmont.addon.dashboard.model.Dashboard;
import com.haulmont.addon.dashboard.model.Widget;
import com.haulmont.addon.dashboard.web.annotation.DashboardWidget;
import com.haulmont.addon.dashboard.web.annotation.WidgetParam;
import com.haulmont.addon.dashboard.web.events.DashboardEvent;
import com.haulmont.addon.dashboard.web.parametertransformer.ParameterTransformer;
import com.haulmont.addon.dashboard.web.repository.WidgetRepository;
import com.haulmont.addon.dashboard.web.widget.RefreshableWidget;
import com.haulmont.charts.gui.amcharts.model.charts.AbstractChart;
import com.haulmont.charts.gui.components.charts.CustomChart;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractFrame;
import com.haulmont.cuba.gui.components.Label;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.ReportTemplate;
import com.haulmont.reports.gui.ReportGuiManager;
import com.haulmont.yarg.reporting.ReportOutputDocument;

import javax.inject.Inject;
import javax.inject.Named;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.haulmont.addon.dashboardchart.web.widget.ChartWidget.CAPTION;


@DashboardWidget(name = CAPTION, editFrameId = "dashboardchart$ChartWidget.edit")
public class ChartWidget extends AbstractFrame implements RefreshableWidget {

    public static final String CAPTION = "Chart";

    private final static String CHART_JSON_PARAMETER = "chartJson";
    private final static String JSON_CHART_SCREEN_ID = "chart$jsonChart";

    @Inject
    protected DataManager dataManager;

    @Inject
    protected ReportGuiManager reportGuiManager;

    @Inject
    private ParameterTransformer parameterTransformer;

    @Inject
    protected WidgetRepository widgetRepository;

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
    protected Label<String> errorLabel;

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

        Map<String, Object> widgetParams = widgetRepository.getWidgetParams(widget);
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