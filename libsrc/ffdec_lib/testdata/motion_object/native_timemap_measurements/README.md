# Native TimeMap measurement inputs

28 controlled XFL documents for Bounce, BounceIn, Spring and RandomSquareWave. Native type names were captured from CS6. Native parameter checks and measured formula validation are documented in [NATIVE_RESULTS.md](NATIVE_RESULTS.md).

Discovery uses strengths 1, 2, 3, 4, 5 and 8 with 601 frames at 30 fps. Validation uses strength 6 with 361 frames at 24 fps. Do not use validation observations to fit a formula; check the completed candidate against them.

The base displacement is 1000 pixels. Normalize SWF positions as (xTwips - 2000) / 20000, and time as frameIndex / (frameCount - 1). Endpoints are measured; do not assume that a native wave or bounce ends at normalized progress 1.

Run [measure_native_timemaps.jsfl](../measure_native_timemaps.jsfl) in CS6. It opens fresh unchanged copies of verified example 47 and applies requested_motion.xml through native APIs. Direct import of the generated candidate XFL has failed in CS6; use the command instead. Deterministic cases are published before and after native FLA save/reopen; Random is published twice per session, both before and after reopen. Every output has a unique filename. No source document or existing reference is overwritten. Expect 70 SWF exports if all cases succeed. See [NATIVE_RESULTS.md](NATIVE_RESULTS.md) for retained native publications and formula verification; regenerated inputs alone do not establish native validation.

The command reports its build/motion-diagnostic/native-timemap-measurement-* output directory. STEP files are immutable; STATUS.txt is written once at the end. Metadata and motion XML must be validated on import and reopen. Failed cases are reported and the next case continues.

NativeTimeMapMeasurementAnalyzer reads that run directory, writes per-frame CSV files, checks metadata/static channels and compares repeated publications. Its random comparison uses sampled positions, not binary SWF equality. Comparing different strengths does not establish Random reproducibility; only repeated exports of the same case do.

| XFL | Type | Strength | Frames | FPS | Role |
| --- | --- | ---: | ---: | ---: | --- |
| [bounce_s1_601](bounce_s1_601/bounce_s1_601.xfl) | Bounce | 1 | 601 | 30 | discovery |
| [bounce_s2_601](bounce_s2_601/bounce_s2_601.xfl) | Bounce | 2 | 601 | 30 | discovery |
| [bounce_s3_601](bounce_s3_601/bounce_s3_601.xfl) | Bounce | 3 | 601 | 30 | discovery |
| [bounce_s4_601](bounce_s4_601/bounce_s4_601.xfl) | Bounce | 4 | 601 | 30 | discovery |
| [bounce_s5_601](bounce_s5_601/bounce_s5_601.xfl) | Bounce | 5 | 601 | 30 | discovery |
| [bounce_s8_601](bounce_s8_601/bounce_s8_601.xfl) | Bounce | 8 | 601 | 30 | discovery |
| [bounce_s6_validation](bounce_s6_validation/bounce_s6_validation.xfl) | Bounce | 6 | 361 | 24 | validation |
| [bounce_in_s1_601](bounce_in_s1_601/bounce_in_s1_601.xfl) | BounceIn | 1 | 601 | 30 | discovery |
| [bounce_in_s2_601](bounce_in_s2_601/bounce_in_s2_601.xfl) | BounceIn | 2 | 601 | 30 | discovery |
| [bounce_in_s3_601](bounce_in_s3_601/bounce_in_s3_601.xfl) | BounceIn | 3 | 601 | 30 | discovery |
| [bounce_in_s4_601](bounce_in_s4_601/bounce_in_s4_601.xfl) | BounceIn | 4 | 601 | 30 | discovery |
| [bounce_in_s5_601](bounce_in_s5_601/bounce_in_s5_601.xfl) | BounceIn | 5 | 601 | 30 | discovery |
| [bounce_in_s8_601](bounce_in_s8_601/bounce_in_s8_601.xfl) | BounceIn | 8 | 601 | 30 | discovery |
| [bounce_in_s6_validation](bounce_in_s6_validation/bounce_in_s6_validation.xfl) | BounceIn | 6 | 361 | 24 | validation |
| [spring_s1_601](spring_s1_601/spring_s1_601.xfl) | Spring | 1 | 601 | 30 | discovery |
| [spring_s2_601](spring_s2_601/spring_s2_601.xfl) | Spring | 2 | 601 | 30 | discovery |
| [spring_s3_601](spring_s3_601/spring_s3_601.xfl) | Spring | 3 | 601 | 30 | discovery |
| [spring_s4_601](spring_s4_601/spring_s4_601.xfl) | Spring | 4 | 601 | 30 | discovery |
| [spring_s5_601](spring_s5_601/spring_s5_601.xfl) | Spring | 5 | 601 | 30 | discovery |
| [spring_s8_601](spring_s8_601/spring_s8_601.xfl) | Spring | 8 | 601 | 30 | discovery |
| [spring_s6_validation](spring_s6_validation/spring_s6_validation.xfl) | Spring | 6 | 361 | 24 | validation |
| [random_s1_601](random_s1_601/random_s1_601.xfl) | RandomSquareWave | 1 | 601 | 30 | discovery |
| [random_s2_601](random_s2_601/random_s2_601.xfl) | RandomSquareWave | 2 | 601 | 30 | discovery |
| [random_s3_601](random_s3_601/random_s3_601.xfl) | RandomSquareWave | 3 | 601 | 30 | discovery |
| [random_s4_601](random_s4_601/random_s4_601.xfl) | RandomSquareWave | 4 | 601 | 30 | discovery |
| [random_s5_601](random_s5_601/random_s5_601.xfl) | RandomSquareWave | 5 | 601 | 30 | discovery |
| [random_s8_601](random_s8_601/random_s8_601.xfl) | RandomSquareWave | 8 | 601 | 30 | discovery |
| [random_s6_validation](random_s6_validation/random_s6_validation.xfl) | RandomSquareWave | 6 | 361 | 24 | validation |

Generator: test/com/jpexs/decompiler/flash/xfl/NativeTimeMapMeasurementGenerator.java.
