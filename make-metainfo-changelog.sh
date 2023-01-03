#!/usr/bin/env bash

# SPDX-License-Identifier: GPL-3.0-or-later OR ISC OR MIT
# Copyright Stefan "Newbyte" Hansson

# Script to automatically update the Appstream metainfo release section based on CHANGELOG.md.
# Dependencies: Bash, GNU Awk 4.1.0 or later, and seq.

set -refu

CHANGELOG_ENTRY_PATTERN='^-\ *'
CHANGELOG_TYPE_PATTERN='^###\ *'
CHANGELOG_FILENAME='CHANGELOG.md'
METAINFO_FILENAME='resources/com.jpexs.decompiler.flash.metainfo.xml'
RELEASE_DATE_PATTERN='[[:digit:]]+-[[:digit:]]+-[[:digit:]]+'
RELEASE_VERSION_PATTERN='[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+'
RELEASE_PATTERN_CHANGELOG="^##\ \[$RELEASE_VERSION_PATTERN\]"
RELEASE_PATTERN_METAINFO="<release version=\"$RELEASE_VERSION_PATTERN\" date=\"$RELEASE_DATE_PATTERN\">"

find_metainfo_entry_line() {
    local line

    while read -r line; do
        echo "$line"
    done < "$METAINFO_FILENAME"
}

get_date_from_line() {
    echo "$1" | grep -Eo "$RELEASE_DATE_PATTERN"
}

get_release_from_line() {    
    echo "$1" | grep -Eo "$RELEASE_VERSION_PATTERN"
}

get_latest_metainfo_release() {
    local line_number=0
    local line
    
    while read -r line; do
        let 'line_number++'
        if [[ $line =~ $RELEASE_PATTERN_METAINFO ]]; then
            echo $(get_release_from_line "$line") "$line_number"
            break
        fi
    done < "$METAINFO_FILENAME"
}

process_line() {
    # Remove [] from the line to convert [#1234] to #1234
    echo "$1" | sed 's/[][]//g'
}

get_changelog_until_version() {
    print_with_indent() {
        local indents="$1"
        local message="$2"
        
        printf '\t%.0s' $(seq "$indents")
        echo "$message"
    }
    
    local version_limit="$1"
    local previous_type='undefined'
    local start_parsing='false'
    local changelog_text
    local current_type
    local line
    local release_date
    local release_version
    
    while read -r line; do
        if [[ $line =~ $RELEASE_PATTERN_CHANGELOG ]]; then
            release_version=$(get_release_from_line "$line")
            release_date=$(get_date_from_line "$line")
            
            [ "$release_version" == "$version_limit" ] && break
            
            start_parsing='true'
        fi
        
        # We don't want to parse the changelog's preamble
        if [ "$start_parsing" == 'true' ]; then
            if [[ $line =~ $RELEASE_PATTERN_CHANGELOG ]]; then
                current_type='release'
            elif [[ $line =~ $CHANGELOG_TYPE_PATTERN ]]; then
                current_type='type'
            elif [[ $line =~ $CHANGELOG_ENTRY_PATTERN ]]; then
                current_type='entry'
            else
                current_type='none'
            fi
            
            line=$(process_line "$line")
            
            if [ "$current_type" == 'entry' ] && [ "$previous_type" != 'entry' ]; then
                print_with_indent 4 '<ul>'
            fi
            
            if [ "$current_type" != 'entry' ] && [ "$previous_type" == 'entry' ]; then
                print_with_indent 4 '</ul>'
            fi
            
            if [ "$current_type" == 'release' ] && [ "$previous_type" == 'none' ]; then
                print_with_indent 3 '</description>'
                print_with_indent 2 '</release>'
            fi
            
            case "$current_type" in
            release)
                print_with_indent 2 "<release version=\"$release_version\" date=\"$release_date\">"
                print_with_indent 3 '<description>'
                ;;
            type)
                print_with_indent 4 "<p>${line:4}</p>"
                ;;
            entry)
                print_with_indent 5 "<li>${line:2}</li>"
                ;;
            esac
            
            previous_type="$current_type"
        fi
    done < "$CHANGELOG_FILENAME"
    
    if [ "$start_parsing" == 'true' ]; then
        if [ "$previous_type" == 'entry' ]; then
            print_with_indent 4 '</ul>'
        fi

        print_with_indent 3 '</description>'
        print_with_indent 2 '</release>'
    fi
}

read -ra args <<< $(get_latest_metainfo_release)

latest_release="${args[0]}"
insert_at_line="${args[1]}"
changelog=$(get_changelog_until_version "$latest_release")

[ -z "$changelog" ] && exit

awk -i inplace -v changelog="$changelog" -v insert_at_line="$insert_at_line" 'NR==insert_at_line {print changelog} 1' resources/com.jpexs.decompiler.flash.metainfo.xml
