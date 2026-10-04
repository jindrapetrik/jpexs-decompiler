# Native CS6 angular conversion probes

Both historical diagnostic commands have completed. They identified the integer-angle serialization issue; the subsequent fractional-angle detection fix is now verified in native CS6. The two affected examples use faithful ordinary-keyframe fallbacks. See [SKEW_FIX.md](SKEW_FIX.md) and [CS6_ROUNDTRIP.md](CS6_ROUNDTRIP.md) for current results. No further publishing is pending.

The historical scripts use unsafe_motion.xml when present so their experiments still operate on the original failing motion objects after the production fixtures have been corrected.

## Controlled encoding comparison (completed)

Run **Commands > Run Command > native_encoding.jsfl** once in Flash CS6. It automatically opens and publishes nine diagnostic variants for each of the three examples; no separate Publish step is needed. Original examples and open documents remain unchanged. Results are written to a fresh **build/motion-diagnostic/native-encoding-TIMESTAMP** folder.

The variants compare the unchanged control, axis-aligned/endpoint/quarter-turn base matrices, Rotation_Z with relative skew (with two base matrices), six-decimal angles, angles plus a full turn, and a tiny angle offset. These are hypotheses to test, not production fixes. A mathematical reconstruction check confirms that all 27 variants describe the reference transform within 2.1 fixed16 units; native publishing must establish which encodings actually work.

Each output folder retains its XFL, imported_motion.xml and published.swf. STATUS.txt reports publishing failures and successes; success does not assert SWF fidelity.

Run the local mathematical and simulated API checks with Node.js:

    node libsrc/ffdec_lib/test/com/jpexs/decompiler/flash/xfl/NativeMotionEncodingTest.cjs

## Results of the first native authoring run

The run in native-authoring-1791070927226 completed all three cases. Motion XML remained unchanged after matrix assignment. Stage matrix reads were constant throughout the motion span; assigning a matrix altered the shared base instance rather than each animated property key. Consequently, those stage mismatch counts cannot validate evaluated animation frames. The published SWFs remain usable evidence.

| Example | Maximum matrix difference before assignment | After assignment | Maximum position difference after assignment |
| --- | ---: | ---: | ---: |
| 03_rotation | 210 | 3 | 2 |
| 05_animated_skew | 201 | 201 | 1 |
| 16_rotation_skew_dual_quadratic | 644 | 644 | 1 |

Matrix differences are fixed16 units; positions are twips. All movies retain 61 frames, 30 fps, matching stage size, instance presence and filters. Rotation improved after changing the base matrix, but none of this establishes a general angular encoding fix. Full comparisons are saved beside the native SWFs as before_compare.csv/.txt and after_compare.csv/.txt.

## Run once in Flash CS6

1. Choose **Commands > Run Command**.
2. Select **native_authoring.jsfl** from this folder.
3. Let the command finish. It opens and edits fresh diagnostic copies of the three examples, saves native FLAs and exports SWFs automatically. No separate Publish step is required.
4. The final message shows the output directory and any per-example failures. Tell the coding agent when the command has finished.

Every run creates a new folder under **libsrc/ffdec_lib/build/motion-diagnostic/native-authoring-TIMESTAMP**. The original XFL examples, reference data, and current published SWFs remain unchanged. Existing open source documents are not edited or closed.

The command retains the motion-object representation while assigning reference matrices through the native authoring API. The completed native run showed that these assignments affect the shared base instance, not individual motion property keys. If an assignment destroys the motion object, that example stops with an error.

## Output for each example

- **before_motion.xml**, **before_stage.csv**, **before_native.fla**, **before_native.swf** record the imported motion object before the native matrix assignment.
- **after_motion.xml**, **after_stage.csv**, **after_native.fla** record CS6's own property representation after setting each frame's matrix.
- **persisted_motion.xml**, **persisted_stage.csv**, **after_native.swf** record the saved and reopened result.
- The top-level **STATUS.txt** records completed cases, stage mismatch counts and failures.

The CSV matrices use fixed16 coefficients and twip positions, just like expected.csv. Matrix values are compared with two fixed16 units of tolerance and positions with 0.051 pixel tolerance. A successful API assignment or save is not proof of published SWF fidelity; the SWFs must still be compared against reference.swf.

The command has been tested with five simulated Adobe API scenarios: successful execution, loss of the motion object during assignment, invalid reference CSV, a failed copy, and a matrix change after save/reopen. It has also been run by the user in native CS6, with the limitations reported above.

## API references

The command uses Adobe's documented [matrix assignment](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Element_object/elemen10.md), [motion XML retrieval](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Frame_object/frame7.md), [FLA saving](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/flash_object_%28fl%29/fl64.md), and [SWF export with current settings](https://github.com/AdobeDocs/developers-animatesdk-docs/blob/master/Document_object/docume67.md).

Run the simulated tests from the repository root with Node.js:

    node libsrc/ffdec_lib/test/com/jpexs/decompiler/flash/xfl/NativeMotionAuthoringTest.cjs
