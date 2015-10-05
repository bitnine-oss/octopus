/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.co.bitnine.octopus.maven.plugin.versioninfo;

import kr.co.bitnine.octopus.maven.plugin.util.Exec;
import kr.co.bitnine.octopus.maven.plugin.util.FileSetUtils;

import org.apache.maven.model.FileSet;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

/**
 * VersionInfoMojo calculates information about the current version of the
 * codebase and exports the information as properties for further use
 * in a Maven build.
 * The version information includes build time, SCM URI, SCM branch,
 * SCM commit, and an MD5 checksum of the contents of the files
 * in the codebase.
 */
@Mojo(name = "version-info")
public final class VersionInfoMojo extends AbstractMojo {
    @Parameter(defaultValue = "${project}")
    private MavenProject project;

    @Parameter(required = true)
    private FileSet source;

    @Parameter(defaultValue = "version-info.scm.uri")
    private String scmUriProperty;

    @Parameter(defaultValue = "version-info.scm.branch")
    private String scmBranchProperty;

    @Parameter(defaultValue = "version-info.scm.commit")
    private String scmCommitProperty;

    @Parameter(defaultValue = "version-info.build.time")
    private String buildTimeProperty;

    @Parameter(defaultValue = "version-info.source.md5")
    private String md5Property;

    @Parameter(defaultValue = "git")
    private String gitCommand;

    @Parameter(defaultValue = "svn")
    private String svnCommand;

    @Override
    public void execute() throws MojoExecutionException {
        try {
            AbstractSCM scm = determineSCM();
            project.getProperties().setProperty(scmUriProperty, scm.getUri());
            project.getProperties().setProperty(scmBranchProperty, scm.getBranch());
            project.getProperties().setProperty(scmCommitProperty, scm.getCommit());

            project.getProperties().setProperty(buildTimeProperty, getBuildTime());
            project.getProperties().setProperty(md5Property, computeMD5());
        } catch (Throwable e) {
            throw new MojoExecutionException(e.toString(), e);
        }
    }

    /**
     * Determines which SCM is in use (git, Subversion, or none) and returns
     * the SCM in use.
     *
     * @return SCM in use for this build
     * @throws Exception if any error occurs attempting to determine SCM
     */
    private AbstractSCM determineSCM() throws Exception {
        AbstractSCM scm = new GitSCM();
        if (scm.isInUse()) {
            VersionInfoMojo.this.getLog().info("SCM: git");
            return scm;
        }

        scm = new SvnSCM();
        if (scm.isInUse()) {
            VersionInfoMojo.this.getLog().info("SCM: svn");
            return scm;
        }

        VersionInfoMojo.this.getLog().info("SCM: none");
        return new UnknownSCM();
    }

    private abstract class AbstractSCM {
        private boolean inUse;

        void markInUse() {
            inUse = true;
        }

        boolean isInUse() {
            return inUse;
        }

        /**
         * Returns URI of SCM.
         *
         * @return String URI of SCM
         */
        String getUri() {
            return isInUse() ? getSCMUri() : "Unknown";
        }

        /**
         * Returns branch of SCM.
         *
         * @return String branch of SCM
         */
        String getBranch() {
            return isInUse() ? getSCMBranch() : "Unknown";
        }

        /**
         * Returns commit of SCM.
         *
         * @return String commit of SCM
         */
        String getCommit() {
            return isInUse() ? getSCMCommit() : "Unknown";
        }

        protected String getSCMUri() {
            return "Unknown";
        }

        protected String getSCMBranch() {
            return "Unknown";
        }

        protected String getSCMCommit() {
            return "Unknown";
        }
    }

    private class GitSCM extends AbstractSCM {
        private final List<String> output = new ArrayList<String>();

        GitSCM() {
            Exec exec = new Exec(VersionInfoMojo.this);
            int retCode = exec.run(Arrays.asList(gitCommand, "branch"), output);
            if (retCode == 0) {
                retCode = exec.run(Arrays.asList(gitCommand, "remote", "-v"), output);
                if (retCode != 0)
                    return;
                retCode = exec.run(Arrays.asList(gitCommand, "log", "-n", "1"), output);
                if (retCode != 0)
                    return;

                markInUse();
            }

            VersionInfoMojo.this.getLog().debug(output.toString());
        }

        @Override
        protected String getSCMUri() {
            String uri = "Unknown";

            for (String s : output) {
                if (s.startsWith("origin") && s.endsWith("(fetch)")) {
                    uri = s.substring("origin".length());
                    uri = uri.substring(0, uri.length() - "(fetch)".length());
                    break;
                }
            }

            return uri.trim();
        }

        @Override
        protected String getSCMBranch() {
            String branch = "Unknown";

            for (String s : output) {
                if (s.startsWith("*")) {
                    branch = s.substring("*".length());
                    break;
                }
            }

            return branch.trim();
        }

        @Override
        protected String getSCMCommit() {
            String commit = "Unknown";

            for (String s : output) {
                if (s.startsWith("commit")) {
                    commit = s.substring("commit".length());
                    break;
                }
            }

            return commit.trim();
        }
    }

    private class SvnSCM extends AbstractSCM {
        private final List<String> output = new ArrayList<String>();

        SvnSCM() {
            Exec exec = new Exec(VersionInfoMojo.this);
            int retCode = exec.run(Arrays.asList(svnCommand, "info"), output);
            if (retCode == 0)
                markInUse();

            VersionInfoMojo.this.getLog().debug(output.toString());
        }

        @Override
        protected String getSCMUri() {
            String uri = "Unknown";

            for (String s : output) {
                if (s.startsWith("URL:")) {
                    uri = s.substring(4).trim();
                    uri = getSvnUriInfo(uri)[0];
                    break;
                }
            }

            return uri.trim();
        }

        @Override
        protected String getSCMBranch() {
            String branch = "Unknown";

            for (String s : output) {
                if (s.startsWith("URL:")) {
                    branch = s.substring(4).trim();
                    branch = getSvnUriInfo(branch)[1];
                    break;
                }
            }

            return branch.trim();
        }

        /**
         * Return URI and branch of Subversion repository.
         *
         * @param path String Subversion info output containing URI and branch
         * @return String[] containing URI and branch
         */
        private String[] getSvnUriInfo(String path) {
            String[] res = new String[] {"Unknown", "Unknown"};

            try {
                int index = path.indexOf("trunk");
                if (index > -1) {
                    res[0] = path.substring(0, index - 1);
                    res[1] = "trunk";
                    return res;
                }

                index = path.indexOf("branches");
                if (index > -1) {
                    res[0] = path.substring(0, index - 1);

                    int branchIndex = index + "branches".length() + 1;
                    index = path.indexOf("/", branchIndex);
                    if (index > -1)
                        res[1] = path.substring(branchIndex, index);
                    else
                        res[1] = path.substring(branchIndex);
                }
            } catch (Exception e) {
                VersionInfoMojo.this.getLog().warn("Could not determine URI & branch from SVN URI: " + path);
            }

            return res;
        }

        @Override
        protected String getSCMCommit() {
            String commit = "Unknown";

            for (String s : output) {
                if (s.startsWith("Revision:")) {
                    commit = s.substring("Revision:".length());
                    break;
                }
            }

            return commit.trim();
        }
    }

    private class UnknownSCM extends AbstractSCM { }

    /**
     * Returns a string representing current build time.
     *
     * @return String representing current build time
     */
    public String getBuildTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm'Z'");
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        return dateFormat.format(new Date());
    }

    public String computeMD5() throws Exception {
        List<File> files = FileSetUtils.convertFileSetToFiles(source);

        /*
         * File order of MD5 calculation is significant.
         * Sorting is done on unix-format names, case-folded, in order to
         * get a platform-independent sort and
         * calculate the same MD5 on all platforms.
         */
        Collections.sort(files, new Comparator<File>() {
            @Override
            public int compare(File lhs, File rhs) {
                return normalizePath(lhs).compareTo(normalizePath(rhs));
            }

            private String normalizePath(File file) {
                return file.getPath().toUpperCase().replaceAll("\\\\", "/");
            }
        });

        byte[] md5 = computeMD5(files);
        String md5str = byteArrayToString(md5);
        getLog().info("Computed MD5: " + md5str);
        return md5str;
    }

    /**
     * Given a list of files, computes and returns an MD5 checksum of the full
     * contents of all files.
     *
     * @param files List<File> containing every file to input into the MD5 checksum
     * @return byte[] calculated MD5 checksum
     * @throws IOException              if there is an I/O error while reading a file
     * @throws NoSuchAlgorithmException if the MD5 algorithm is not supported
     */
    private byte[] computeMD5(List<File> files) throws IOException, NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        for (File file : files) {
            getLog().debug("Computing MD5 for: " + file);
            md5.update(readFile(file));
        }
        return md5.digest();
    }

    /**
     * Reads and returns the full contents of the specified file.
     *
     * @param file File to read
     * @return byte[] containing full contents of file
     * @throws IOException if there is an I/O error while reading the file
     */
    private byte[] readFile(File file) throws IOException {
        RandomAccessFile raf = new RandomAccessFile(file, "r");
        byte[] buffer = new byte[(int) raf.length()];
        raf.readFully(buffer);
        raf.close();
        return buffer;
    }

    /**
     * Converts bytes to a hexadecimal string representation and returns it.
     *
     * @param array byte[] to convert
     * @return String containing hexadecimal representation of bytes
     */
    private String byteArrayToString(byte[] array) {
        StringBuilder sb = new StringBuilder();
        for (byte b : array)
            sb.append(Integer.toHexString(0xff & b));

        return sb.toString();
    }
}
