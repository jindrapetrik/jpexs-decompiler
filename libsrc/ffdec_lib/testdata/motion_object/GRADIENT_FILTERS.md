# Gradient filter motion-object implementation

Gradient Glow and Gradient Bevel motion channels are implemented for numeric parameters, RGB/alpha and byte positions of gradient stops. Inner, outer and full types, quality, knockout, stack order and stop-count boundaries are preserved. Unknown gradient attributes, invalid or unsorted stops and duplicate filter types retain ordinary frames. Strength uses the same native integer-percent precision guard as other animated filters.

Native capture `native-gradient-filters-1791108388461` succeeded for all six cases after correcting static import. The recorded schema uses GradientGlow_Filter and GradientBevel_Filter containers, scalar BlurX/BlurY/Strength/Angle/Distance channels, constant Quality/Knockout/Type fields, and a single Gradient channel. A gradient Keyframe has `colors="0xRRGGBBAA,..."` and `indexes="0,...,255"`; type values are inner=0, outer=1 and full=2. Native pose XML is retained as native_motion.xml in each example.

Each gradient component must fit a supported independent curve before a motion span is emitted. Output keys contain the full gradient vector at each sampled frame, including held frames. Original easing is preserved through its sampled values; recovering the original minimal gradient key layout or easing preset is not implemented.

## Verify the decompiled exports

All six references export motion objects: one span for examples 25–28 and 30, two spans separated at frame index 30 for the topology-change example 29.

Native CS6 run `native-gradient-roundtrip-1791108933199` successfully published all six decompiled exports. Every filter field matches its reference exactly in all 61 frames, including numeric parameters, fixed8 strength, every stop's RGB/alpha and byte position, type, quality, flags, stop count and stack order. The transition from three to four stops in example 29 remains at frame index 30, with the second motion span preserved on import.

All publications retain 30 fps, 61 frames and the 640 x 360 stage. Instance presence, matrix coefficients, color transforms and other compared instance flags match; position residuals are at most one twip (0.05 px). These are exact filter roundtrips, not binary-identical SWF files or exact-position roundtrips. The matching publications are retained as `<example>_compiled.swf` inside their example folders. Per-frame comparison CSVs and full reports remain in the diagnostic run folder.

Run **Commands > Run Command > native_gradient_roundtrip.jsfl** once in CS6. It automatically opens, saves and publishes six fresh diagnostic copies. It checks that motion objects survive import and that example 29 retains the second span at frame 30. No separate Publish is needed. Results, imported XML and native FLAs go to **build/motion-diagnostic/native-gradient-roundtrip-TIMESTAMP**. Tell the coding agent when it finishes so published SWF values can be compared with reference.swf. This export check is distinct from the already completed native schema capture below.

## Prepared references

Six synthetic examples share the existing motion_object folder. Each has 61 frames, 30 fps, a 640 x 360 stage, a unique publication filename, reference.swf and expected_filters.csv. The CSV includes every gradient stop's RGB, alpha and byte ratio, as well as the numeric and discrete filter fields.

- 25_gradient_glow: inner Gradient Glow, changing blur X/Y, strength, angle and distance.
- 26_gradient_bevel: inner Gradient Bevel, changing numeric parameters.
- 27_gradient_colors: Gradient Glow, three independently changing RGB/alpha stops.
- 28_gradient_ratios: Gradient Bevel, changing middle-stop position.
- 29_gradient_topology: three stops become four at frame index 30. The count change must remain a span boundary.
- 30_gradient_stack: ordered outer Gradient Glow and full Gradient Bevel with numeric animation.

The references use integer-percent-representable strength values. Static gradient XFL export now retains the SWF fixed8 strength value instead of rounding it to two decimal places. Native CS6 can still lose strength precision, as documented in FILTERS.md.

## Capture the native schema

Run **Commands > Run Command > native_gradient_filters.jsfl** once in Flash CS6. No separate Publish is required. The command uses fresh diagnostic copies and preserves the examples and other open documents.

For each reference it imports the filter poses from source frame indices 0, 30 and 60 into copies of 01_position. It removes the existing motion object from the copied XFL, imports each pose as a static filtered symbol, and calls native createMotionObject(0, 61). CS6 then produces the actual gradient property identifiers, values and key structure for each pose. The command saves three native pose FLAs and motion XML files. It then authors filter keys at those three times by copying the native key structure, without guessing gradient property names or undocumented gradient API arrays. The topology-change example is captured as separate poses; it is deliberately not merged across the stop-count change.

The first capture, native-gradient-filters-1791108159854, failed in all six cases with "Native gradient count differs" before saving native XML. That command inserted static filters into an existing motion object with an empty Filters container, and CS6 did not retain them. The corrected command imports static filtered instances first, creates the motion objects natively, and preserves diagnostic motion XML and filter dumps before validating the resulting filter count.

Outputs are under **ffdec_lib/build/motion-diagnostic/native-gradient-filters-TIMESTAMP**:

- Each pose_N folder contains source_filters.xml, native_motion.xml, native_filters.txt and native_pose.fla.
- requested_motion.xml and authored_motion.xml describe the requested and accepted animation, when keys can be constructed from the native poses.
- persisted_motion.xml, native_gradient.fla and reference_native.swf preserve the reopened native result.
- STATUS.txt records each success or failure. Captured pose data remains available if native key construction fails.

If CS6 emits a property representation without pose keyframes, the command records a failure rather than inventing its key syntax. The native pose encodings will still let the exporter implementation continue. Successful publication does not itself prove fidelity; compare the persisted keys and sampled SWF fields.

## Validation and remaining work

The generator compiles for Java 8 and creates all six references. All 44 Java detector tests pass. Six gradient-specific tests verify the recorded native schema, exact color/position vectors, three type values for both filters, topology and mode boundaries, held samples, malformed/irregular inputs and mixed stack order. Seven simulated capture scenarios and four simulated publishing scenarios check command failures and source preservation. These command simulations are not native CS6 verification.

Detection/export uses the measured native encoding and the six decompiled exports now pass native filter comparisons. The arbitrary fixed8 strength limitation documented in FILTERS.md still applies; these examples deliberately use strengths representable as native integer percentages. Unsupported/malformed inputs retain ordinary frames, and the detector does not recover the original sparse curve layout or named filter easing preset.

The command uses Adobe's documented [getFilters/setFilters model](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Document_object/docum530.md), [gradient filter names](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Filter_object/filter13.md) and [setMotionObjectXML](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Frame_object/frame26.md). Native pose XML supplies the gradient-specific schema.
