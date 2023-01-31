# 1. Checkstyle Style Configuration

This directory contains the Checkstyle Google's Style configuration customized for VertiGIS source code formatting.

The configuration contains rules for checking and formatting Java source code according to Google Java Style
Guide.

Import file `google_checks-VertiGIS.xml` to your IDE's Checkstyle configuration to use it for:

* code analysis
* code formatting

> Actual style configuration exported from Checkstyle version: **10.6.0**.

> You can find also original Google's Style configuration in file `google_checks-original.xml` if you want to compare
> differences between original and customized version.

# 2. VertiGIS Google's Style Customization

`google_checks-VertiGIS.xml` customize these properties compared to the original Google's Style configuration:

| Property         | Value |
|------------------|-------|
| `LineLength.max` | `120` |

# 3. IDE Setup

## 3.1. IntelliJ IDEA

At first, install Checkstyle plugin and restart IDE.

### 3.1.1. Checkstyle Configuration

* Go to: **File - Settings - Tools - Checkstyle**
* **Checkstyle version**: 10.6.0
* **Scan Scope**: Only Java sources (including tests)
* **Configuration File** section
    * disable all selected items
    * click on **Add**
* Dialog window for adding a new configuration
    * **Description**: Google Checks VertiGIS
    * select: **Use a local Checkstyle file**
        * Click on browse and find `google_checks-VertiGIS.xml` file
    * check: **Store relative to project location**
    * Click on the **Next**, leave all next settings everything in default setting, click on the **Next** ... **Next
      ** ... buttons and then on the **Finish**
      button.
* **Configuration File** section
    * Make sure that the **Google Checks VertiGIS** is selected
* Click on the **OK** and close the settings

### 3.1.2. Code Style Configuration

* Go to: **File - Settings - Editor - Code Style**
* **Scheme**:
    * select: **Project**
    * **Import Scheme - Checkstyle configuration**
        * find `google_checks-VertiGIS.xml` file
* Click on the **OK** and close the settings

> Reformat code with: `Ctrl + Alt + L` \
> Press `Ctrl + Alt + L` twice to reformat that will remove your own line breaks.

> Rearrange imports with: `Ctrl + Alt + O`

## 3.2. MS Visual Studio Code

At first, install Checkstyle plugin and restart IDE.

*... TODO*

# 4. More Information

For more information about Google Java Style Guide see:

* https://google.github.io/styleguide/javaguide.html

For more information what is Checkstyle and Google's Style configuration see:

* https://checkstyle.org/index.html
* https://checkstyle.org/google_style.html
* https://github.com/checkstyle/checkstyle/blob/master/src/main/resources/google_checks.xml