# Native TimeMap captures

These native XML/SWF references establish spelling, assignment and observed progress. They do not establish full parameter ranges or decompiler export support. Earlier captures: [Simple (Medium), Cubic 50](cubic_50/README.md) and [Simple (Fast), Quartic 50](quartic_50/README.md).

| User-supplied UI name | XML type | Strength | Reference |
| --- | --- | ---: | --- |
| Not supplied in this batch | Quintic | 50 | [quintic_50](quintic_50/README.md) |
| Not supplied in this batch | DualQuadratic | 50 | [dual_quadratic_50](dual_quadratic_50/README.md) |
| Not supplied in this batch | DualCubic | 50 | [dual_cubic_50](dual_cubic_50/README.md) |
| Not supplied in this batch | DualQuartic | 50 | [dual_quartic_50](dual_quartic_50/README.md) |
| Stop and Start (Fastest) | DualQuintic | 50 | [dual_quintic_50](dual_quintic_50/README.md) |
| Bounce | Bounce | 4 | [bounce_4](bounce_4/README.md) |
| Bounce In | BounceIn | 4 | [bounce_in_4](bounce_in_4/README.md) |
| Spring | Spring | 5 | [spring_5](spring_5/README.md) |
| Sine Wave | SineWave | 3 | [sine_wave_3](sine_wave_3/README.md) |
| Sawtooth Wave | SawtoothWave | 3 | [sawtooth_wave_3](sawtooth_wave_3/README.md) |
| Square Wave | SquareWave | 3 | [square_wave_3](square_wave_3/README.md) |
| Random | RandomSquareWave | 3 | [random_square_wave_3](random_square_wave_3/README.md) |
| Damped Wave | DampedWave | 3 | [damped_wave_3](damped_wave_3/README.md) |

Current polynomial support: Cubic, Quartic, Quintic and their Dual variants at strength 50 are now recognized as native maps in the exporter. The [actual SWF -> XFL examples](../native_polynomial_exports/README.md) pass local assignment/position checks; native CS6 recompilation of all six exports is now verified, including map persistence after save/reopen. X residuals are at most one twip and the existing X-scale residual at most one fixed16 unit; other compared values match exactly. The individual capture notes describe the original measurement stage; see [current implementation status](../NATIVE_TIMEMAPS.md) for subsequent work.
