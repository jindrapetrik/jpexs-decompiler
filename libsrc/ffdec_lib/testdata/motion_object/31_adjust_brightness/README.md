# 31_adjust_brightness

Adjust Color: independently animated brightness.

Open `31_adjust_brightness.xfl` in Flash CS6. Publish uses the unique `31_adjust_brightness_compiled.swf` filename.

The synthetic reference has 61 frames, 30 fps and a 640 x 360 stage. `expected_filters.csv` records all 20 color-matrix elements and other filter fields.

The decompiled XFL contains 1 motion-object spans; Adjust Color scalar channels use the existing matrix conversion. Native channel spelling is confirmed by captured CS6 XML; native matrix comparison has completed; see ../ADJUST_COLOR.md for the measured float residuals. Run ../native_adjust_color_filters.jsfl to capture native Adjust Color property encoding automatically. See ../ADJUST_COLOR.md.

Native CS6 run: `native-adjust-color-1791112001348`. All four recovered parameters match in all 61 frames. The color matrices match exactly. Other filters and stack order match exactly. Position residuals are at most one twip (0.05 px). The matching native publication is retained as `31_adjust_brightness_compiled.swf`.
