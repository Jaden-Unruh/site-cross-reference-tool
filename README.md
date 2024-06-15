# Site Cross-Reference Tool
A tool for Akana to cross reference a location hierarchy with a list of FCA dates to see where they have to visit.
## Contents
- [Setup & Requirements](#setup-&-requirements)
- [GUI and How To Use](#gui-and-how-to-use)
  - [FCA Date Sheet](#fca-date-sheet)
  - [Location Hierarchy Sheet](#location-hierarchy-sheet)
- [Troubleshooting](#troubleshooting)
- [Details](#details)
- [Changing the code](#changing-the-code)
- [License](#license)

## Setup & Requirements
Java SE 17 is required to run this program. If you've used any of my previous tools, you'll already have it installed. If you don't have Java 17 or newer, you can download an installer for Temurin/OpenJDK 17 from [here](https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.8%2B7/OpenJDK17U-jdk_x64_windows_hotspot_17.0.8_7.msi). This is an open-source version of java. Once downloaded, you can run the installer by double-clicking, it will open a window guiding you through the installation. Leaving everything as the defaults and just clicking through the pages should work perfectly.

The program itself can be downloaded from the [GitHub](https://github.com/Jaden-Unruh/site-cross-reference-tool), in the releases section on the right. The latest release will be showing, click on it, then click on the first item in the 'assets' section, called something like `site-cross-reference-tool-xx.x.x.jar`. You can rename it as desired once it's downloaded.

Once Temurin/Java 17 and the program `.jar` are installed, double-click the `.jar` to run.

## GUI and How To Use
After double-clicking the `.jar`, a window titled `Site Cross-Reference Tool` will open. It will have a few elements:
1. `FCA Date Sheet; Drop or Select...`
    - Drag-and-drop or use the selection prompt to pick the FCA Date Sheet file. This should be a `*.xlsx` file as described below.
2. `Location Hierarchy Sheet; Drop or Select...`
    - Drag-and-drop or use the selection prompt to pick the Location Hierarchy Spreadsheet. This should be a `*.xlsx` file as described below.
3. `Close`
    - Closes the program
4. `Help`
    - Opens a window with some help and a link to this GitHub to view the README.
5. `Run`
    - Runs the main portion of the program.

### FCA Date Sheet
This should be a workbook with just one sheet in it, the title does not matter. The second column of this sheet should have all the Location IDs, and data should start on the second row.

### Location Hierarchy Sheet
This should be a workbook with just one sheet in it, the title does not matter. The fifth column of this sheet should have all the location IDs, and data should start on the seventh row.

## Troubleshooting
> Nothing's happening when I double-click the `.JAR` file

Ensure you've installed Java as specified under [Setup](#setup-&-requirements). If you believe you have, try checking your Java version:

1. Press Win+R, type `cmd` and press enter - this will open a command prompt window
2. Type `java -version` and press enter
3. If you've installed Java as specified, the first line under your typing should read `openjdk version "17.0.8" 2023-07-18`[^1]. If, instead, it says `'java' is not recognized as an internal...` then java is not installed.

[^1]: If you had a version of Java other than the one specified in Setup, this may show a different version but should be similar. However, you probably wouldn't be in this troubleshooting step.

---

> I only have spreadsheets of type `*.xlsb` or `*.csv` (or any other spreadsheet type) and the program won't open them

Open the spreadsheets in Microsoft Excel, select 'File -> Save As -> This PC', and choose 'Excel Workbook (.xlsx)' from the drop-down. A full list of filetypes that Excel supports (and thus can be converted to .xlsx) can be found [here](https://learn.microsoft.com/en-us/deployoffice/compat/office-file-format-reference#file-formats-that-are-supported-in-excel).

---

> `Run` isn't doing anything

Ensure that you've selected two `*.xlsx` files. Spreadsheets of a different type will not work.

---

> I'm getting an error message popping up when I run the file

If you're getting an error message and you can't figure out what it's saying or how to fix it, reach out to me. If you click `More Info` on the error popup and copy the big text box, that text (a full stack trace on the error) can help me figure out what's going on.

---

> Something else is going wrong

Don't hesitate to reach out to me if you have any other issues - always happy to help.

## Details
This program mostly just has one step in its running. First, we make a big HashSet and populate it with all the location IDs from the location hierarchy. Then, we go down the FCA Date sheet and remove all those location IDs from the HashSet, and we output what's left. Note that this means the output will not be in any guaranteeable order.

## Changing the Code
The `.jar` file is compiled and compressed, meaning all the code is not human-readable. Instead, all of the program files are included in this GitHub repository so that anyone other than me can download them and open them in an IDE.

## License
Site Cross-Reference Tool is available under the [GNU General Public License v3.0](https://www.gnu.org/licenses/gpl-3.0.en.html) or later. In summary, this code is available to use, copy, and modify, under the condition that all derivative works containing the code (not including sheets viewed or modified by the code, or outputs from the tool itself) are released under the same license. This project is provided without liability or warranty. See the `LICENSE` for more.
