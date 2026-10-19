# bounce_s6_validation

Real decompilation of the retained native CS6 reference.swf. Motion_X uses a recovered native bounce TimeMap with two linear base keys. 

Open bounce_s6_validation.xfl in CS6 and Publish to bounce_s6_validation_compiled.swf, or run ../../publish_native_wave_exports.jsfl. Verified in native CS6: all frames match reference.swf within one twip in X; metadata, other matrix coefficients, colors, filters and instance flags match exactly. Native imported/persisted map assignments are retained in native_imported_motion.xml and native_motion.xml.

