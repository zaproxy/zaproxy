package org.zaproxy.zap.control;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.zip.ZipEntry;

import org.junit.jupiter.api.Test;

class AddOnIntegrationTest extends AddOnTestUtils{

    @Test
    void shouldLoadFixtureZipAndValidateEverything() throws Exception{

        //creates a dependceny addon fixture that contains a declared lib 
        Path depPath = createAddOnWithLibs("support-lib.jar");
        AddOn dep = new AddOn(depPath);
        dep.setId("support");

        //simulates the installed libs so runtime checks pass deterministically 
        installAllLibs(dep);

        //creates main addon zip fixture 
        Path mainPath = createAddOnFile(
            "main.zap", "release", "1.2.3", m ->{

                //resets any exisitng manifest content before building 
                m.setLength(0);

                //builds a minimal valid addon manifest 
                m.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
                .append("<addon>")
                .append("<name>Main</name>")
                .append("<status>release</status>")
                .append("<version>1.2.3</version>")
                .append("<dependencies>")
                .append("<addons>")
                .append("<addon>")
                .append("<id>support</id>")
                .append("<version>1.*</version>")
                .append("</addon>")
                .append("</addons>")
                .append("</dependencies>")
                .append("<libs><lib>main-lib.jar</lib></libs>")
                .append("</addon>");
            },

            zos -> {

                //adds lib files that manifest declares 
                zos.putNextEntry(new ZipEntry("main-lib.jar"));
                zos.write("lib".getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        );

        //validates the zip structure, manifest, and declares lib entries 
        assertThat(AddOn.isValidAddOn(mainPath).getValidity(), is(AddOn.ValidationResult.Validity.VALID));

        //loads the addon from the generated fixture 
        AddOn main = new AddOn(mainPath);

        //simulates the lib installation so runtime checks can succeed
        installAllLibs(main);

        //evaluates the run requirements against the dependency addon 
        AddOn.AddOnRunRequirements reqs = main.calculateRunRequirements(List.of(dep));

        //assertions
        assertThat(main.getId(), is("main"));
        assertThat(main.getName(), is("Main"));
        assertThat(main.getVersion().toString(), is("1.2.3"));

        assertThat(reqs.isRunnable(), is(true));
        assertThat(reqs.hasDependencyIssue(), is(false));
        assertThat(reqs.getDependencies().contains(dep), is(true));
    }

    private static void installAllLibs(AddOn addOn) throws Exception{
        for (AddOn.Lib lib : addOn.getLibs()){
            Path installed = installLib(addOn, lib.getName());
            lib.setFileSystemUrl(installed.toUri().toURL());
        }
    }
}