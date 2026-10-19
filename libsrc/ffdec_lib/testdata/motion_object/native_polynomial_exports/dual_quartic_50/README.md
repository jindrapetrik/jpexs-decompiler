# dual_quartic_50

Real captured SWF -> XFL export. Motion_X uses native DualQuartic strength 50 with two linear base keys. Motion_Y retains Quadratic 65; X scale retains Quadratic -40. Reference: reference.swf, 61 frames, 30 fps, 640 x 360 stage, Demo instance.

Open dual_quartic_50.xfl and Publish to dual_quartic_50_compiled.swf, or run ../../publish_native_polynomial_exports.jsfl. All source X positions match the serialized model within one twip after rounding. Verified by native CS6 save/reopen and recompilation in native-polynomial-roundtrip-1791323831469. X differs by at most one twip; the existing Quadratic -40 X-scale channel differs in two frames by one fixed16 unit. Metadata, Y, other matrix fields, RGBA, filters and instance flags match exactly. Native map assignments and two base keys survive save/reopen. Native XML and roundtrip_differences.csv are retained in this folder.
