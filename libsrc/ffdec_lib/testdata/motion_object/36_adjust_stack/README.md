# 36_adjust_stack

Ordered Blur, Adjust Color and Glow stack with animated Adjust Color parameters.

Open `36_adjust_stack.xfl` in Flash CS6. Publish uses the unique `36_adjust_stack_compiled.swf` filename.

The synthetic reference has 61 frames, 30 fps and a 640 x 360 stage. `expected_filters.csv` records all 20 color-matrix elements and other filter fields.

The decompiled XFL contains 1 motion-object spans; Adjust Color scalar channels use the existing matrix conversion. Native channel spelling is confirmed by captured CS6 XML; native matrix comparison has completed; see ../ADJUST_COLOR.md for the measured float residuals. Run ../native_adjust_color_filters.jsfl to capture native Adjust Color property encoding automatically. See ../ADJUST_COLOR.md.

Native CS6 run: `native-adjust-color-1791112001348`. All four recovered parameters match in all 61 frames. Matrix coefficients differ by at most 4.77e-7 and offsets by at most 7.63e-6, consistent with native float evaluation differences; the matrices are not bit-identical. Other filters and stack order match exactly. Position residuals are at most one twip (0.05 px). The matching native publication is retained as `36_adjust_stack_compiled.swf`.
