# 11_color_dual_quadratic

Different DualQuadratic easing strengths for colors, alpha and an offset.

Open `11_color_dual_quadratic.xfl` in Flash CS6 and publish to `11_color_dual_quadratic_compiled.swf` (already configured).

Reference: `reference.swf`, 30 fps, 640 x 360 pixels, 61 frames. Instance to compare: `Demo`. The export contains 1 motion object spans. Some channels use sampled property keys to preserve SWF-frame values.

`expected.csv` contains reference matrices and RGBA transforms for each frame. Indices start at zero; matrices use fixed16, multipliers use fixed8, and translations use twips. `preview.png` shows frame 31 of the reference SWF.

The XFL document and reference SWF require no external images, fonts or scripts.

This current XFL has been published and compared in Flash CS6; remaining differences are within one quantization unit or zero. See [roundtrip results](../CS6_ROUNDTRIP.md). The matching compiled SWF is included in the parent folder.
