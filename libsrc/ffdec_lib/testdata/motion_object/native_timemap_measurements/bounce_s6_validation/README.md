# bounce_s6_validation

Native CS6 Bounce strength 6. 361 frames at 24 fps, 1600 x 360 stage.

Motion_X has a linear base curve from 100 px to 1100 px; only its TimeMap changes. All other instance channels are constant. This is a native measurement input, not a decompiler fidelity test. Its SWF reference must be produced by CS6.

Keep these observations out of fitting; use them to validate the derived formula.

Run ../../measure_native_timemaps.jsfl (from the motion_object folder) for automatic publication. The command opens an unchanged copy of verified example 47, then applies requested_motion.xml using native CS6 APIs. The generated candidate XFL currently fails native import; do not use direct XFL Publish for these measurements.

Retained native SWFs, motion XML and samples: [native_capture](native_capture/). See [NATIVE_RESULTS.md](../NATIVE_RESULTS.md) for validation and formulas.
