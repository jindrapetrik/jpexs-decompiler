# Animated filter implementation

Animated Blur, Glow, Drop Shadow and inner Bevel are now reconstructed as native motion-object filter channels. Numeric channels include blur X/Y, strength, angle and distance; colors include RGB and alpha, plus both Bevel colors. The original filter order and constant quality/flags are preserved. Strength export now retains SWF fixed8 precision instead of rounding the XFL value to two decimal places.

The implementation uses property names captured from native CS6, including its `Bevel_HilightColor` spelling, strength in percent and packed `0xRRGGBBAA` colors. Each numeric/color channel must fit a supported curve independently before a motion object is emitted. Output uses per-frame property keys to preserve the actual SWF samples, including held frames; the observed easing is preserved in those values. This is not recovery of the original filter easing preset or sparsest native curve representation.

## Verify the decompiled exports

Seven filter examples are in the same motion_object folder:

- 18_blur, 19_glow, 20_drop_shadow, 21_bevel and 22_filter_stack use the captured native SWFs as references.
- 23_filter_parameters uses a synthetic reference that changes all numeric channels, RGB/alpha and arbitrary fixed8 strength. Values that cannot be represented in CS6 motion XML retain ordinary filter keyframes; safe subspans may still become motion objects.
- 24_filter_integer_strength exercises the same parameters with strength values that can be represented by native integer percentages. It exports one motion object.

Each folder contains its decompiled XFL, reference.swf, expected_filters.csv and English instructions. The first five also preserve native_motion.xml.

## Native CS6 results and strength precision

The run `native-filter-roundtrip-1791106778043` published all six initial examples. For 18 through 22, every filter field and stack position matches the reference exactly in all 61 frames. Frame rate, stage dimensions, instance presence, matrices, color transforms and flags also match, except for position residuals of at most one twip (0.05 px).

The initial 23 export had strength differences in 58 frames, reaching three fixed8 units. All other filter properties, including RGB/alpha, blur, angle, distance and order, matched exactly. CS6 truncates motion-object strength keys to whole percentages before publishing fixed8 strength. The detector now rejects a motion span if any strength sample has no integer-percent representation; representable values are written using the corresponding integer percent. The original arbitrary-precision reference for 23 is retained rather than weakened to hide the problem.

The follow-up run `native-filter-roundtrip-1791107327675` published all seven examples. Examples 18–22 and the new 24 motion object have exact filter fields and order in all 61 frames. In particular, 24 verifies simultaneous animated blur X/Y, strength, angle, distance, RGB and alpha for the ordered four-filter stack. Position residuals remain at most one twip; all other compared instance fields match.

Example 23 retains its original arbitrary fixed8 reference. Its corrected mixed fallback still has strength differences in 45 frames, reaching three fixed8 units (3/256). Every other filter field matches exactly. Native CS6 also truncates strength in ordinary filter keyframes to whole percent: preserving the raw value in XFL does not guarantee that the compiler retains it. The fallback prevents motion XML from claiming support for unrepresentable samples, but it does not overcome this native compiler precision limit. Example 23 is an explicitly documented limitation case, not an exact roundtrip success.

Run **Commands > Run Command > native_filter_roundtrip.jsfl** once in CS6. It publishes all seven fresh diagnostic copies automatically and retains their native FLAs and motion XML when the first frame is a motion object. No separate Publish step is needed. Existing source examples and publications are untouched. Results go to **build/motion-diagnostic/native-filter-roundtrip-TIMESTAMP**. Tell the coding agent when the command finishes so all filters and other instance properties can be compared against the references.

The command passes four simulated API scenarios, including motion-object loss, save failure and export failure. These are command safety tests, not native fidelity tests.

## Capture native authoring data

The first capture completed in native-filters-1791101355467. All five FLAs, XML files and SWFs exist. The Glow and Bevel status errors were diagnostic parser failures after successful saving/export, not missing outputs. That parser has been replaced by a simple tag scan. Sequential property setters retained base values for several channels; the capture command now applies a complete filter pose in one setFilters call. Rerunning authoring capture is not required for the seven current export checks.

In Flash CS6, choose **Commands > Run Command > native_filters.jsfl** from this folder. The command automatically processes five fresh copies of 01_position, applies filters with different values at frames 1, 31 and 61, saves native FLAs, reopens them, records motion XML and publishes SWFs. No separate Publish step is needed.

Outputs go to a fresh **ffdec_lib/build/motion-diagnostic/native-filters-TIMESTAMP** folder. The existing examples, published SWFs and other open documents are not modified. Only documents opened by the command are closed.

For each case, inspect:

- **initial_motion.xml**, **authored_motion.xml**, **persisted_motion.xml** for native filter property structure and keys.
- **initial_filters.txt**, **mid_filters.txt**, **end_filters.txt** for the native API values and units.
- The native **.fla** and **reference_native.swf** for later decompilation and comparison.
- **STATUS.txt** for failures or missing animated filter keys. Successful save/export is not proof that filters animate. The command explicitly reports when the native setters did not create nonzero-time filter keys.

Run the simulated API checks with Node.js:

    node libsrc/ffdec_lib/test/com/jpexs/decompiler/flash/xfl/NativeFilterCaptureTest.cjs

## Detection constraints to preserve

The detector preserves filter order, count and type. It splits runs at discrete flag, quality and topology changes. Unknown filter attributes/types, duplicate types, disabled filters and outer/full Bevel retain their original filter keyframes. Gradient Glow and Gradient Bevel now have animated channel support; see GRADIENT_FILTERS.md for native verification status. Adjust Color brightness/contrast/saturation/hue channels are implemented using the existing converter, with native channel names confirmed and six exports verified for parameter fidelity and measured float matrix residuals; see ADJUST_COLOR.md. Constant unsupported filters remain part of instance identity.

51 Java tests pass, including filter-only animation, independent curve/easing recognition, RGBA byte order, integer-percent strength, exact inverse mapping across the complete fixed8 domain, unrepresentable-strength fallback, ordered stacks, exact native Bevel naming, discrete boundaries, unsupported/duplicate filters, irregular samples and held colors. The existing motion and classic tests also pass. All 17 original fixtures still export. These checks do not replace native CS6 publishing verification.

## Adobe API references

The command uses documented [filter creation](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Document_object/documen3.md), [filter property setters](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Document_object/docum520.md), [filter properties and units](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Filter_object/filter_summary.md), and [frame selection](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Timeline_object/timeli46.md). Native XML and publishing results, rather than simulated API checks, establish compiler fidelity.
