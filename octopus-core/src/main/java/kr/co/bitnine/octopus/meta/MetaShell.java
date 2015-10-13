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

package kr.co.bitnine.octopus.meta;

import kr.co.bitnine.octopus.conf.OctopusConfiguration;
import kr.co.bitnine.octopus.meta.model.MetaUser;
import kr.co.bitnine.octopus.meta.privilege.SystemPrivilege;

import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

public final class MetaShell {
    private final MetaStore metaStore;

    private MetaShell() throws MetaException, ReflectiveOperationException {
        OctopusConfiguration conf = new OctopusConfiguration();

        metaStore = MetaStores.newInstance(conf.get("metastore.class"));

        Properties props = new Properties();
        for (Map.Entry e : conf)
            props.put(e.getKey(), e.getValue());
        metaStore.start(props);
    }

    private void process(String[] args) {
        if (args.length < 1)
            throw new IllegalArgumentException("invalid number(" + args.length + ") of arguments");

        Command cmd;

        String opt = args[0];
        switch (opt) {
        case "-superuser":
            if (args.length < 3)
                throw new IllegalArgumentException('"' + opt + "\" requires <username>, <password> arguments");
            String username = args[1];
            String password = args[2];
            cmd = new CommandCreateSuperUser(username, password);
            break;
        default:
            throw new IllegalArgumentException("invalid option \"" + opt + '"');
        }

        cmd.execute();
    }

    private void stop() {
        metaStore.stop();
    }

    private interface Command {
        void execute();
    }

    private class CommandCreateSuperUser implements Command {
        private final String username;
        private final String password;

        CommandCreateSuperUser(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public void execute() {
            try {
                MetaContext mc = metaStore.getMetaContext();

                if (mc.userExists(username)) {
                    System.out.println("User \"" + username + "\" already exists!");
                    return;
                }

                MetaUser user = mc.createUser(username, password);
                mc.addSystemPrivileges(Arrays.asList(SystemPrivilege.values()), Arrays.asList(user.getName()));

                mc.close();
            } catch (MetaException e) {
                System.err.println(e.getMessage());
            }
        }
    }

    public static void main(String[] args) throws ReflectiveOperationException {
        try {
            MetaShell metaShell = new MetaShell();
            metaShell.process(args);
            metaShell.stop();
        } catch (ReflectiveOperationException | MetaException | IllegalArgumentException e) {
            System.err.println(e.getMessage());
        }
    }
}
