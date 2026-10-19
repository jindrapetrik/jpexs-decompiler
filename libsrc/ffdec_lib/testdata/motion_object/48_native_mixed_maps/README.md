# 48_native_mixed_maps

Native Quadratic and DualQuadratic both use strength 50, with separate map indices.

Open `48_native_mixed_maps.xfl` in Flash CS6 and publish to `48_native_mixed_maps_compiled.swf` (already configured).

Reference: `reference.swf`, 30 fps, 640 x 360 pixels, 61 frames. Instance to compare: `Demo`. The export contains 1 motion object spans. Some channels use sampled property keys to preserve SWF-frame values.

`expected.csv` contains reference matrices and RGBA transforms for each frame. Indices start at zero; matrices use fixed16, multipliers use fixed8, and translations use twips. `preview.png` shows frame 31 of the reference SWF.

The XFL document and reference SWF require no external images, fonts or scripts.

Native CS6 publication verified on 2026-10-04: all 61 frames, 30 fps and the stage match. Quadratic 50 and DualQuadratic 50 remain separate after native FLA save/reopen; X and X scale share their Quadratic map. Positions differ by at most one twip (0.05 px), X scale by at most one fixed16 unit (1/65536); other matrix coefficients, RGBA transforms and compared instance fields match exactly. The compiled SWF and persisted native_motion.xml are included.
