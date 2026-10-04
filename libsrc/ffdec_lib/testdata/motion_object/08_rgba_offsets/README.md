# 08_rgba_offsets

Independent positive and negative RGBA offsets.

Open `08_rgba_offsets.xfl` in Flash CS6 and publish to `08_rgba_offsets_compiled.swf` (already configured).

Reference: `reference.swf`, 30 fps, 640 x 360 pixels, 61 frames. Instance to compare: `Demo`. The export contains 1 motion object spans. Unsupported spans retain sampled frames or classic tweens.

`expected.csv` contains reference matrices and RGBA transforms for each frame. Indices start at zero; matrices use fixed16, multipliers use fixed8, and translations use twips. `preview.png` shows frame 31 of the reference SWF.

The XFL document and reference SWF require no external images, fonts or scripts.

This current XFL has been published and compared in Flash CS6; remaining differences are within one quantization unit or zero. See [roundtrip results](../CS6_ROUNDTRIP.md). The matching compiled SWF is included in the parent folder.
