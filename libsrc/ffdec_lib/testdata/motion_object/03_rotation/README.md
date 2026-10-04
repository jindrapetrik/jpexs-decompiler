# 03_rotation

Initial rotation of 30 degrees followed by a 540-degree turn.

Open `03_rotation.xfl` in Flash CS6 and publish to `03_rotation_compiled.swf` (already configured).

Reference: `reference.swf`, 30 fps, 640 x 360 pixels, 61 frames. Instance to compare: `Demo`. The export contains 1 motion object spans. Some channels use sampled property keys to preserve SWF-frame values.

`expected.csv` contains reference matrices and RGBA transforms for each frame. Indices start at zero; matrices use fixed16, multipliers use fixed8, and translations use twips. `preview.png` shows frame 31 of the reference SWF.

The XFL document and reference SWF require no external images, fonts or scripts.

This current XFL has been published in Flash CS6, but larger angular matrix errors remain. It is not yet a verified faithful motion-object representation. See [roundtrip results](../CS6_ROUNDTRIP.md). The matching compiled SWF is included in the parent folder.
