#!/usr/bin/env bash
set -euo pipefail

dist_dir=$1
jre_dir=$2
appimagetool=$3
metainfo=$4
version=$5
output=$6
id=com.jpexs.decompiler.flash

if [[ ! -r "$dist_dir/ffdec.jar" || ! -d "$dist_dir/lib" ]]; then
    echo "Build FFDec before packaging it as an AppImage: $dist_dir" >&2
    exit 1
fi
if [[ ! -x "$jre_dir/bin/java" ]]; then
    echo "Expected an extracted Linux x86-64 JRE at $jre_dir" >&2
    exit 1
fi

output_dir=$(dirname -- "$output")
mkdir -p "$output_dir"
work_dir=$(mktemp -d "$output_dir/.appimage.XXXXXX")
trap 'rm -rf -- "$work_dir"' EXIT
appdir=$work_dir/FFDec.AppDir

mkdir -p "$appdir/usr/"{bin,lib/ffdec,lib/jre,share/applications,share/metainfo,share/icons/hicolor/512x512/apps}
cp -a "$dist_dir/." "$appdir/usr/lib/ffdec/"
cp -a "$jre_dir/." "$appdir/usr/lib/jre/"
cp "$dist_dir/icon.png" "$appdir/usr/share/icons/hicolor/512x512/apps/$id.png"
cp "$metainfo" "$appdir/usr/share/metainfo/$id.appdata.xml"

cat > "$appdir/usr/share/applications/$id.desktop" <<EOF
[Desktop Entry]
Type=Application
Name=JPEXS Free Flash Decompiler
Exec=ffdec %f
Icon=$id
Categories=Development;
MimeType=application/x-shockwave-flash;
EOF
ln -s "usr/share/applications/$id.desktop" "$appdir/$id.desktop"
ln -s "usr/share/icons/hicolor/512x512/apps/$id.png" "$appdir/$id.png"
ln -s "$id.png" "$appdir/.DirIcon"

cat > "$appdir/AppRun" <<'EOF'
#!/usr/bin/env bash
appdir=${APPDIR:-$(dirname -- "$(readlink -f -- "$0")")}
export VLC_VERBOSE=-1

java_args=(-Djava.net.preferIPv4Stack=true -Djna.nosys=true -Djava.util.Arrays.useLegacyMergeSort=true)
memory=${FFDEC_MEMORY-1024m}
if [[ -n "$memory" ]]; then
    java_args+=("-Xmx$memory")
fi
if [[ -n "${FFDEC_STACK_SIZE:-}" ]]; then
    java_args+=("-Xss$FFDEC_STACK_SIZE")
fi

exec "$appdir/usr/lib/jre/bin/java" "${java_args[@]}" -jar "$appdir/usr/lib/ffdec/ffdec.jar" "$@"
EOF
chmod +x "$appdir/AppRun"
ln -s ../../AppRun "$appdir/usr/bin/ffdec"

ARCH=x86_64 VERSION="$version" APPIMAGE_EXTRACT_AND_RUN=1 \
    "$appimagetool" "$appdir" "$work_dir/$(basename -- "$output")"
mv -f -- "$work_dir/$(basename -- "$output")" "$output"
