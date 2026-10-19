# 37_ease_quad

quad easing: X uses In, Y uses Out and scale uses InOut.

Open `37_ease_quad.xfl` in Flash CS6 and publish to `37_ease_quad_compiled.swf` (already configured).

Reference: `reference.swf`, 30 fps, 640 x 360 pixels, 61 frames. Instance to compare: `Demo`. The export contains 1 motion object spans. Some channels use sampled property keys to preserve SWF-frame values.

`expected.csv` contains reference matrices and RGBA transforms for each frame. Indices start at zero; matrices use fixed16, multipliers use fixed8, and translations use twips. `preview.png` shows frame 31 of the reference SWF.

The XFL document and reference SWF require no external images, fonts or scripts.

Native CS6 roundtrip verified on 2026-10-04: all 61 frames, 30 fps, stage and instance identity match. Maximum translation error is one twip (0.05 px). X scale differs by at most one fixed16 unit (1/65536). RGBA transforms, visibility, cache, blend mode, clipping, ratio and filters match exactly. The compiled SWF is included.
