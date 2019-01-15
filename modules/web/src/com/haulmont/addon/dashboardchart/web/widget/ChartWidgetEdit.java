package com.haulmont.addon.dashboardchart.web.widget;

import com.haulmont.addon.dashboard.web.annotation.WidgetParam;
import com.haulmont.cuba.core.global.DataManager;
import com.haulmont.cuba.core.global.LoadContext;
import com.haulmont.cuba.gui.WindowParam;
import com.haulmont.cuba.gui.components.AbstractFrame;
import com.haulmont.cuba.gui.components.CheckBox;
import com.haulmont.cuba.gui.components.LookupField;
import com.haulmont.reports.entity.Report;
import com.haulmont.reports.entity.ReportOutputType;
import com.haulmont.reports.entity.ReportTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ChartWidgetEdit extends AbstractFrame {

    @Inject
    protected DataManager dataManager;

    @Named("reportLookup")
    protected LookupField<Report> reportLookup;

    @Named("templateLookup")
    protected LookupField<ReportTemplate> templateLookup;

    @Inject
    private CheckBox refreshAutomaticallyCheckbox;

    @WidgetParam
    @WindowParam
    protected UUID reportId;

    @WidgetParam
    @WindowParam
    protected UUID templateId;

    @WidgetParam
    @WindowParam
    protected Boolean refreshAutomatically = false;

    @Override
    public void init(Map<String, Object> params) {
        super.init(params);

        reportLookup.addValueChangeListener(r -> {
            Report report = r.getValue();
            if (report != null) {
                List<ReportTemplate> chartTemplates = report.getTemplates().stream()
                        .filter(rt -> ReportOutputType.CHART == rt.getReportOutputType())
                        .collect(Collectors.toList());
                templateLookup.setOptionsList(chartTemplates);
                reportId = report.getId();
                if (ReportOutputType.CHART == report.getDefaultTemplate().getReportOutputType()) {
                    templateLookup.setValue(report.getDefaultTemplate());
                    templateId = report.getDefaultTemplate().getId();
                }
            }

        });

        templateLookup.addValueChangeListener(t -> {
            ReportTemplate reportTemplate = t.getValue();
            if (reportTemplate != null) {
                templateId = reportTemplate.getId();
            }
        });

        if (reportId != null) {
            LoadContext<Report> loadContext = LoadContext.create(Report.class)
                    .setId(reportId)
                    .setView("report.edit");

            Report report = dataManager.load(loadContext);
            if (report != null) {
                List<ReportTemplate> chartTemplates = report.getTemplates().stream()
                        .filter(rt -> ReportOutputType.CHART == rt.getReportOutputType())
                        .collect(Collectors.toList());
                ReportTemplate reportTemplate = null;
                if (templateId != null) {
                    reportTemplate = chartTemplates.stream()
                            .filter(t -> templateId.equals(t.getId()))
                            .findFirst()
                            .orElse(ReportOutputType.CHART == report.getDefaultTemplate().getReportOutputType() ? report.getDefaultTemplate() : null);
                }
                reportLookup.setValue(report);
                templateLookup.setOptionsList(chartTemplates);
                templateLookup.setValue(reportTemplate);
            }
        }

        refreshAutomaticallyCheckbox.setValue(refreshAutomatically);
        refreshAutomaticallyCheckbox.addValueChangeListener(e -> {
            refreshAutomatically = e.getValue();
        });
    }


}