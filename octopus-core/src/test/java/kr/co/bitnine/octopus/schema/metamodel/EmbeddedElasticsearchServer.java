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

package kr.co.bitnine.octopus.schema.metamodel;

import org.apache.commons.io.FileUtils;
import org.elasticsearch.client.Client;
import org.elasticsearch.common.settings.ImmutableSettings;
import org.elasticsearch.node.Node;

import java.io.File;
import java.io.IOException;

import static org.elasticsearch.node.NodeBuilder.nodeBuilder;

public class EmbeddedElasticsearchServer {
    private static final String DEFAULT_DATA_DIRECTORY = "target/elasticsearch-data";
    private static final String DEFAULT_PORT = "19102";

    private final Node node;
    private final String dataDirectory;
    private final String dataPort;

    public EmbeddedElasticsearchServer() {
        this(DEFAULT_DATA_DIRECTORY, DEFAULT_PORT);
    }

    public EmbeddedElasticsearchServer(String dataDirectory, String dataPort) {
        this.dataDirectory = dataDirectory;
        this.dataPort = dataPort;

        ImmutableSettings.Builder elasticsearchSettings = ImmutableSettings.settingsBuilder()
                .put("http.enabled", "false")
                .put("path.data", dataDirectory)
                .put("transport.tcp.port", dataPort);

        node = nodeBuilder()
                .settings(elasticsearchSettings.build())
                .node();
    }

    public Client getClient() {
        return node.client();
    }

    public void shutdown() {
        node.close();
        deleteDataDirectory();
    }

    private void deleteDataDirectory() {
        try {
            FileUtils.deleteDirectory(new File(dataDirectory));
        } catch (IOException e) {
            throw new RuntimeException("Could not delete data directory of embedded elasticsearch server", e);
        }
    }
}
