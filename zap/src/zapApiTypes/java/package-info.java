@Java2TS(
        declare = {
            @Type(value = java.nio.file.Files.class, export = true),
            @Type(value = java.nio.file.Path.class),
            @Type(value = java.nio.file.Paths.class, export = true),
            @Type(value = java.util.Arrays.class, export = true),
            @Type(value = java.net.URI.class, export = true)
        })
package org.zaproxy.zap.api;

import org.bsc.processor.annotation.Java2TS;
import org.bsc.processor.annotation.Type;
