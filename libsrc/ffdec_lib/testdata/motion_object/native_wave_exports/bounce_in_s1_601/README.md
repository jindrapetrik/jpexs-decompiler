# bounce_in_s1_601

Real decompilation of the retained native CS6 reference.swf. BounceIn 1 is equivalent to Quadratic -100, which is the native motion map used by this actual SWF export. Dense frame inputs may instead select a classic tween.

Run [publish_remaining_native_wave_exports.jsfl](../../publish_remaining_native_wave_exports.jsfl) to save/reopen and publish this case only. The original command incorrectly required a classic tween; its corrected validator accepts this native Quadratic -100 map. Verified in native CS6 in native-wave-roundtrip-1791322525559: all 601 positions differ by at most one twip; metadata and all other compared values match exactly. Quadratic -100 and its two base keys survive native FLA save/reopen. Native XML is retained as native_imported_motion.xml and native_motion.xml.
