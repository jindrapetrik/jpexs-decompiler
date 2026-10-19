# Controlled native encoding comparisons

All 27 variants were published by Flash CS6. Values below are maximum absolute errors against reference.swf; matrix values are fixed16 units and positions are twips. All variants retain frame rate, frame count, stage size, instance presence and filters.

| Example | Variant | Matrix units | Position twips |
| --- | --- | ---: | ---: |
| 03_rotation | angles_epsilon | 3 | 2 |
| 03_rotation | angles_plus_360 | 3 | 2 |
| 03_rotation | angles_six_decimals | 3 | 2 |
| 03_rotation | base_axes | 210 | 1 |
| 03_rotation | base_endpoint | 210 | 1 |
| 03_rotation | base_quarter_turn | 210 | 1 |
| 03_rotation | control | 210 | 1 |
| 03_rotation | rotation_z | 212 | 2 |
| 03_rotation | rotation_z_axes | 212 | 2 |
| 05_animated_skew | angles_epsilon | 201 | 1 |
| 05_animated_skew | angles_plus_360 | 201 | 1 |
| 05_animated_skew | angles_six_decimals | 201 | 1 |
| 05_animated_skew | base_axes | 201 | 1 |
| 05_animated_skew | base_endpoint | 201 | 1 |
| 05_animated_skew | base_quarter_turn | 201 | 1 |
| 05_animated_skew | control | 201 | 1 |
| 05_animated_skew | rotation_z | 190 | 2 |
| 05_animated_skew | rotation_z_axes | 190 | 2 |
| 16_rotation_skew_dual_quadratic | angles_epsilon | 644 | 1 |
| 16_rotation_skew_dual_quadratic | angles_plus_360 | 644 | 1 |
| 16_rotation_skew_dual_quadratic | angles_six_decimals | 644 | 1 |
| 16_rotation_skew_dual_quadratic | base_axes | 644 | 1 |
| 16_rotation_skew_dual_quadratic | base_endpoint | 644 | 1 |
| 16_rotation_skew_dual_quadratic | base_quarter_turn | 644 | 1 |
| 16_rotation_skew_dual_quadratic | control | 644 | 1 |
| 16_rotation_skew_dual_quadratic | rotation_z | 855 | 1 |
| 16_rotation_skew_dual_quadratic | rotation_z_axes | 855 | 1 |

Rounding angle XML to six decimal places resolves the large pure-rotation error. Comparing the generated production fix with this native-tested variant confirms that Rotation_Z, Skew_X, Skew_Y and both scale properties are identical. Rotation still has small residual errors of 3 matrix units and 2 twips. None of the nine encodings resolves the animated-skew or combined rotation/skew errors.
