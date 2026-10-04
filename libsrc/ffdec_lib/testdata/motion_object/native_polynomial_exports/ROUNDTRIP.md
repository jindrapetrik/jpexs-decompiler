# Native CS6 decompiler roundtrip

| Case | Frames | X changed frames | Max X difference (twips) | X-scale changed frames | Max X-scale difference (fixed16 units) | Other differences |
| --- | ---: | ---: | ---: | ---: | ---: | ---: |
| cubic_50 | 61 | 1 | 1 | 2 | 1 | 0 |
| dual_cubic_50 | 61 | 0 | 0 | 2 | 1 | 0 |
| dual_quartic_50 | 61 | 0 | 0 | 2 | 1 | 0 |
| dual_quintic_50 | 61 | 1 | 1 | 2 | 1 | 0 |
| quartic_50 | 61 | 0 | 0 | 2 | 1 | 0 |
| quintic_50 | 61 | 0 | 0 | 2 | 1 | 0 |

Passed: 6; failed: 0; missing: 0; compared positions: 366; max X difference: 1 twips.

Other differences include metadata, instance presence/name, all matrix fields except X translation and X scale, RGBA transforms, filters and instance flags.

The allowed X-scale residual is one fixed16 unit (1/65536) in the pre-existing Quadratic -40 channel, consistent with previous native verification. All other compared fields require exact equality.
