# 47_native_quadratic

Native Quadratic maps: X and scale share strength -40, Y uses strength 65.

Open `47_native_quadratic.xfl` in Flash CS6 and publish to `47_native_quadratic_compiled.swf` (already configured).

Reference: `reference.swf`, 30 fps, 640 x 360 pixels, 61 frames. Instance to compare: `Demo`. The export contains 1 motion object spans. Some channels use sampled property keys to preserve SWF-frame values.

`expected.csv` contains reference matrices and RGBA transforms for each frame. Indices start at zero; matrices use fixed16, multipliers use fixed8, and translations use twips. `preview.png` shows frame 31 of the reference SWF.

The XFL document and reference SWF require no external images, fonts or scripts.

Native CS6 publication verified on 2026-10-04: all 61 frames, 30 fps and the stage match. Native map assignments survive save/reopen. Positions differ by at most one twip (0.05 px), X scale by at most one fixed16 unit (1/65536); other matrix coefficients, RGBA transforms and compared instance fields match exactly. The compiled SWF is included.
