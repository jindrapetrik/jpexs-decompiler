package com.jpexs.decompiler.flash.cli;

import com.jpexs.decompiler.flash.ApplicationInfo;
import picocli.CommandLine;

/**
 *
 * @author JPEXS
 */
public class VersionProvider implements CommandLine.IVersionProvider {
    @Override
    public String[] getVersion() throws Exception {
        return new String[]{ApplicationInfo.applicationVerName};
    }        
}