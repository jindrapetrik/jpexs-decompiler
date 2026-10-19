# Native wave decompiler exports

21 actual SWF -> XFL exports from native CS6 references. Spring, Bounce and BounceIn use recovered native maps at measured strengths 1, 2, 3, 4, 5, 6 and 8. BounceIn 1 is equivalent to Quadratic -100; this actual SWF export uses that native motion map. Dense frame inputs may instead select an equivalent classic tween.

Run [publish_native_wave_exports.jsfl](../publish_native_wave_exports.jsfl) in CS6 to save/reopen and publish every export. Each output belongs to its own example folder.

Runs native-wave-roundtrip-1791322188603 and native-wave-roundtrip-1791322525559 successfully compiled all 21 cases. [ROUNDTRIP.md](ROUNDTRIP.md) compares 11,901 source frame positions: X differs by at most one twip; metadata, instance presence/name, all other matrix fields, RGBA transforms, filters and instance flags match exactly. Native imported/persisted XML is retained per case. Map types, strengths and two base keys survived save/reopen.

The command incorrectly required BounceIn 1 to be classic and stopped before publishing it. The exporter produced the equivalent native Quadratic -100 map correctly. Validation now accepts either representation. Run [publish_remaining_native_wave_exports.jsfl](../publish_remaining_native_wave_exports.jsfl) to publish only this final case. The retry succeeded; all 601 frames match within one twip, other compared values match exactly, and native Quadratic -100 survives FLA save/reopen. [NATIVE_STATUS_RETRY.txt](NATIVE_STATUS_RETRY.txt) records the successful retry. [NATIVE_STATUS.txt](NATIVE_STATUS.txt) preserves the first run's status, including that validator error.
