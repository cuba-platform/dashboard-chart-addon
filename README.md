# Dashboard charts addon

## Overview

Dashboard charts addon provides additional charts widget for [dashboard component](https://github.com/cuba-platform/rich-search-addon)

## Installation
To add the component to your project, the following steps should be taken:

1. Open your application in CUBA Studio. 

2. Edit Project properties.

3. Click the plus button in the *App components* section of the *Main* tab.

4. Install [dashboard component](https://github.com/cuba-platform/rich-search-addon)

5. Specify the coordinates of the component in the corresponding field as follows: group:name:version.
   Click *OK* to confirm the operation.
    
    * Artifact group: *com.haulmont.addon.dashboardchart*
    * Artifact name: *dashboardchart-global*
    * Version: *add-on version*
    
        When specifying the component version, you should select the one, which is compatible with the platform version used
    in your project.
    
    | Platform Version | Component Version |
    |------------------|-------------------|
    | 6.10.X            | 1.0.0             |
 

## Usage

Component provides charts widget for dashboards. Chart can be configured as report in Reports browser.

![chart-widget.png](img/chart-widget.png)

###Available widget settings:

![widget-settings.png](img/widget-settings.png)

- **Report** - report which contains chart template
- **Template** - chart template
- **Refresh automatically** - if checked then chart will be updated automatically on dashboard update 